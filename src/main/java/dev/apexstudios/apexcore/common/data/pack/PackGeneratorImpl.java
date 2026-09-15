package dev.apexstudios.apexcore.common.data.pack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.apexstudios.apexcore.api.data.ExtendedRegistryBootstrap;
import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.api.data.pack.PackGenerator;
import dev.apexstudios.apexcore.api.data.provider.context.ProviderContext;
import dev.apexstudios.apexcore.api.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.api.data.provider.context.ProviderOutputContext;
import dev.apexstudios.apexcore.common.data.provider.BaseProvider;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.jspecify.annotations.Nullable;

public sealed abstract class PackGeneratorImpl<TSelf extends PackGenerator<TSelf>> implements PackGenerator<TSelf> permits FeaturePackGeneratorImpl, ModPackGeneratorImpl {
    @Nullable protected Component description = null;
    private final Multimap<ProviderType<?>, BiConsumer<ProviderListenerContext, ?>> providerListeners = HashMultimap.create();
    private final Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> worldListeners = HashMultimap.create();
    private final Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> reloadableListeners = HashMultimap.create();

    protected abstract FeatureFlagSet enabledFeatures();

    protected abstract PackOutput createPackOutput(Path outputDir);

    protected PackType packType() {
        return PackType.SERVER_DATA;
    }

    protected boolean isDummy() {
        return false;
    }

    public void defaultDescription(Supplier<Component> description) {
        if(this.description == null)
            this.description = description.get();
    }

    @Override
    public TSelf description(Component description) {
        this.description = description;
        return (TSelf) this;
    }

    @Override
    public <TProvider> TSelf providing(ProviderType<TProvider> providerType, BiConsumer<ProviderListenerContext, TProvider> listener) {
        providerListeners.put(providerType, listener);
        return (TSelf) this;
    }

    @Override
    public <TRegistry> TSelf registering(ResourceKey<? extends Registry<TRegistry>> registryType, Consumer<ExtendedRegistryBootstrap<TRegistry>> bootstrap) {
        worldListeners.put(registryType, bootstrap);
        return (TSelf) this;
    }

    @Override
    public <TRegistry> TSelf reloading(ResourceKey<? extends Registry<TRegistry>> registryType, Consumer<ExtendedRegistryBootstrap<TRegistry>> bootstrap) {
        reloadableListeners.put(registryType, bootstrap);
        return (TSelf) this;
    }

    public ModdedRegistries generate(String modId, Function<PackType, ResourceManager> resourceManagerGetter, CompletableFuture<HolderLookup.Provider> vanillaWorldRegistries, CompletableFuture<HolderLookup.Provider> vanillaReloadableRegistries, Path outputDir, Consumer<DataProvider> providerConsumer) {
        var context = ProviderContext.of(modId, resourceManagerGetter, enabledFeatures());
        var output = createPackOutput(outputDir);
        var moddedRegistries = ModdedRegistries.create(modId, vanillaWorldRegistries, vanillaReloadableRegistries, worldListeners, reloadableListeners);
        registerProviders(context, output, moddedRegistries, providerConsumer);
        return moddedRegistries;
    }

    private void registerProviders(ProviderContext context, PackOutput output, ModdedRegistries registries, Consumer<DataProvider> providerConsumer) {
        if(!isDummy()) {
            if(description == null)
                description = Component.empty();

            var generatedFeatures = context.enabledFeatures().subtract(FeatureFlags.VANILLA_SET);
            var packType = packType();
            var metadataGenerator = new PackMetadataGenerator(output).add(PackMetadataSection.forPackType(packType), new PackMetadataSection(description, DetectedVersion.BUILT_IN.packVersion(packType).minorRange()));

            if(!generatedFeatures.isEmpty()) {
                if(packType != PackType.SERVER_DATA)
                    throw new IllegalStateException("FeatureFlags are only supported by packs of type: SERVER_DATA");

                metadataGenerator = metadataGenerator.add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(generatedFeatures));
            }

            providerConsumer.accept(metadataGenerator);
        }

        providerListeners.keySet().forEach(providerType -> registerProvider(providerType, context, output, registries, providerConsumer));
    }

    private <TProvider> void registerProvider(ProviderType<TProvider> providerType, ProviderContext context, PackOutput output, ModdedRegistries registries, Consumer<DataProvider> providerConsumer) {
        providerConsumer.accept(new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return registries.world().thenCombine(registries.reloadable(), (world, reloadable) -> {
                    var listenerContext = ProviderListenerContext.of(context, world, reloadable);
                    var provider = providerType.create(listenerContext);

                    if(provider == null)
                        return CompletableFuture.completedFuture(null);

                    providerListeners.get(providerType).forEach(listener -> ((BiConsumer<ProviderListenerContext, TProvider>) listener).accept(listenerContext, provider));
                    return ((BaseProvider) provider).generate(cache, ProviderOutputContext.of(listenerContext, output));
                });
            }

            @Override
            public String getName() {
                return providerType.registryName().toDebugFileName();
            }
        });
    }
}
