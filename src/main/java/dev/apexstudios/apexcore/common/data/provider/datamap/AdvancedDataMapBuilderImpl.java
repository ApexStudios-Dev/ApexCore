package dev.apexstudios.apexcore.common.data.provider.datamap;

import com.mojang.datafixers.util.Either;
import dev.apexstudios.apexcore.api.data.provider.datamap.AdvancedDataMapBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

final class AdvancedDataMapBuilderImpl<TRegistry, TValue, TRemover extends DataMapValueRemover<TRegistry, TValue>> extends DataMapBuilderImpl<TRegistry, TValue, AdvancedDataMapBuilder<TRegistry, TValue, TRemover>> implements AdvancedDataMapBuilder<TRegistry, TValue, TRemover> {
    AdvancedDataMapBuilderImpl(AdvancedDataMapType<TRegistry, TValue, TRemover> dataMapType) {
        super(dataMapType);
    }

    @Override
    public AdvancedDataMapBuilder<TRegistry, TValue, TRemover> remove(ResourceKey<TRegistry> registryKey, TRemover remover) {
        return remove(Either.right(registryKey), remover);
    }

    @Override
    public AdvancedDataMapBuilder<TRegistry, TValue, TRemover> remove(Identifier registryName, TRemover remover) {
        return remove(registryKey(registryName), remover);
    }

    @Override
    public AdvancedDataMapBuilder<TRegistry, TValue, TRemover> remove(TagKey<TRegistry> tag, TRemover remover) {
        return remove(Either.left(tag), remover);
    }
}
