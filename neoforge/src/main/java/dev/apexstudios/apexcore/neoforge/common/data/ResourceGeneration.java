package dev.apexstudios.apexcore.neoforge.common.data;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.neoforge.api.data.ResourceGenerator;
import dev.apexstudios.apexcore.neoforge.api.data.pack.FeaturePackGenerator;
import dev.apexstudios.apexcore.neoforge.api.data.pack.ModPackGenerator;
import dev.apexstudios.apexcore.neoforge.api.util.StringHelper;
import dev.apexstudios.apexcore.neoforge.common.data.pack.FeaturePackGeneratorImpl;
import dev.apexstudios.apexcore.neoforge.common.data.pack.ModPackGeneratorImpl;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModContainer;
import org.jspecify.annotations.Nullable;

public final class ResourceGeneration implements ResourceGenerator {
    private final ModPackGeneratorImpl pack = new ModPackGeneratorImpl();
    private final Map<String, FeaturePackGeneratorImpl> featurePacks = Maps.newHashMap();

    @Override
    public ModPackGenerator pack() {
        return pack;
    }

    @Override
    public FeaturePackGenerator pack(String packId) {
        return featurePacks.computeIfAbsent(packId, FeaturePackGeneratorImpl::new);
    }

    public void generate(ModContainer mod, Function<PackType, ResourceManager> resourceManagerGetter, CompletableFuture<HolderLookup.Provider> vanillaRegistries, DataGenerator generator) {
        var outputDir = generator.getPackOutput().getOutputFolder();
        var modId = mod.getModId();

        pack.defaultDescription(() -> Component.literal(mod.getModInfo().getDisplayName()));
        var moddedRegistries = pack.generate(modId, resourceManagerGetter, vanillaRegistries, outputDir, addProvider(null, generator));

        featurePacks.forEach((packId, pack) -> {
            pack.defaultDescription(() -> Component.literal(StringHelper.toEnglishName(packId)));
            pack.generate(modId, resourceManagerGetter, moddedRegistries, outputDir, addProvider(packId, generator));
        });
    }

    private Consumer<DataProvider> addProvider(@Nullable String providerPrefix, DataGenerator generator) {
        return original -> generator.addProvider(true, new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return original.run(cache);
            }

            @Override
            public String getName() {
                return providerPrefix == null ? original.getName() : providerPrefix + '/' + original.getName();
            }
        });
    }
}
