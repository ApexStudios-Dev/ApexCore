package dev.apexstudios.apexcore.core.data.provider.datamap;

import com.google.common.collect.Maps;
import com.mojang.serialization.JsonOps;
import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.core.data.provider.BaseProvider;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderOutputContext;
import dev.apexstudios.apexcore.lib.data.provider.datamap.AdvancedDataMapBuilder;
import dev.apexstudios.apexcore.lib.data.provider.datamap.DataMapProvider;
import dev.apexstudios.apexcore.lib.data.provider.datamap.SimpleDataMapBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.registries.DataMapLoader;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

public final class DataMapProviderImpl implements BaseProvider, DataMapProvider {
    public static final ProviderType<DataMapProvider> PROVIDER_TYPE = ProviderType.register(ApexCore.identifier("data_map"), DataMapProviderImpl::new);

    private final Map<DataMapType<?, ?>, DataMapBuilderImpl<?, ?, ?>> builders = Maps.newHashMap();

    private DataMapProviderImpl() {

    }

    @Override
    public CompletableFuture<?> generate(CachedOutput cache, ProviderOutputContext context) {
        var ops = context.registries().createSerializationContext(JsonOps.INSTANCE);
        var paths = context.pathProvider(PackOutput.Target.DATA_PACK, DataMapLoader.PATH);

        return DataProvider.saveAll(
                cache,
                builder -> builder.save(ops),
                dataMapType -> paths.json(dataMapType.id().withPrefix(DataMapLoader.getFolderLocation(dataMapType.registryKey().identifier()) + '/')),
                builders
        );
    }

    @Override
    public <TRegistry, TValue> SimpleDataMapBuilder<TRegistry, TValue> builder(DataMapType<TRegistry, TValue> dataMapType) {
        return (SimpleDataMapBuilderImpl<TRegistry, TValue>) builders.computeIfAbsent(dataMapType, $ -> new SimpleDataMapBuilderImpl<>(dataMapType));
    }

    @Override
    public <TRegistry, TValue, TRemover extends DataMapValueRemover<TRegistry, TValue>> AdvancedDataMapBuilder<TRegistry, TValue, TRemover> builder(AdvancedDataMapType<TRegistry, TValue, TRemover> dataMapType) {
        return (AdvancedDataMapBuilderImpl<TRegistry, TValue, TRemover>) builders.computeIfAbsent(dataMapType, $ -> new AdvancedDataMapBuilderImpl<>(dataMapType));
    }
}
