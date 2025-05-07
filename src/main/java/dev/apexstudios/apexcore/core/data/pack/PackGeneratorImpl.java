package dev.apexstudios.apexcore.core.data.pack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import dev.apexstudios.apexcore.core.data.ExtendedRegistryBootstrapImpl;
import dev.apexstudios.apexcore.core.data.provider.BaseProvider;
import dev.apexstudios.apexcore.lib.data.ExtendedRegistryBootstrap;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import dev.apexstudios.apexcore.lib.data.pack.PackGenerator;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderContext;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderOutputContext;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

public sealed abstract class PackGeneratorImpl<TSelf extends PackGenerator<TSelf>> implements PackGenerator<TSelf> permits FeaturePackGeneratorImpl, ModPackGeneratorImpl {
    @Nullable protected Component description = null;
    private final Multimap<ProviderType<?>, BiConsumer<ProviderListenerContext, ?>> providerListeners = HashMultimap.create();
    private final Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> bootstrapListeners = HashMultimap.create();

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
        bootstrapListeners.put(registryType, bootstrap);
        return (TSelf) this;
    }

    public CompletableFuture<HolderLookup.Provider> generate(String modId, Function<PackType, ResourceManager> resourceManagerGetter, CompletableFuture<HolderLookup.Provider> vanillaRegistries, Path outputDir, Consumer<DataProvider> providerConsumer) {
        var context = ProviderContext.of(modId, resourceManagerGetter, enabledFeatures());
        var output = createPackOutput(outputDir);
        var moddedRegistries = registerDatapackEntries(context, output, vanillaRegistries, providerConsumer);
        registerProviders(context, output, moddedRegistries, providerConsumer);
        return moddedRegistries;
    }

    private CompletableFuture<HolderLookup.Provider> registerDatapackEntries(ProviderContext context, PackOutput output, CompletableFuture<HolderLookup.Provider> vanillaRegistries, Consumer<DataProvider> providerConsumer) {
        if(bootstrapListeners.isEmpty())
            return vanillaRegistries;

        var registrySetBuilder = new RegistrySetBuilder();
        var patchedRegistries = RegistryPatchGenerator.createLookup(vanillaRegistries, registrySetBuilder);
        var conditionsMap = Maps.<ResourceKey<?>, List<ICondition>>newHashMap();

        bootstrapListeners.keySet().forEach(registryType -> register(
                (ResourceKey) registryType,
                context.modId(),
                registrySetBuilder::add,
                (registryKey, conditions) -> {
                    var list = conditionsMap.computeIfAbsent(registryKey, $ -> Lists.newArrayList());
                    Collections.addAll(list, conditions);
                }
        ));

        var moddedRegistries = patchedRegistries.thenApply(RegistrySetBuilder.PatchedRegistries::patches);
        providerConsumer.accept(new RegistriesDatapackGenerator(output, moddedRegistries, Collections.singleton(context.modId()), conditionsMap));
        return moddedRegistries;
    }

    private <TRegistry> void register(ResourceKey<? extends Registry<TRegistry>> registryType, String modId, BiConsumer<ResourceKey<? extends Registry<TRegistry>>, RegistrySetBuilder.RegistryBootstrap<TRegistry>> consumer, BiConsumer<ResourceKey<TRegistry>, ICondition[]> conditionConsumer) {
        consumer.accept(registryType, context -> {
            var extended = new ExtendedRegistryBootstrapImpl<>(context, registryType, modId, conditionConsumer);
            bootstrapListeners.get(registryType).forEach(listener -> ((Consumer<ExtendedRegistryBootstrap<TRegistry>>) listener).accept(extended));
        });
    }

    private void registerProviders(ProviderContext context, PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Consumer<DataProvider> providerConsumer) {
        if(!isDummy()) {
            if(description == null)
                description = Component.empty();

            var generatedFeatures = context.enabledFeatures().subtract(FeatureFlags.VANILLA_SET);
            var packType = packType();
            var metadataGenerator = new PackMetadataGenerator(output).add(PackMetadataSection.TYPE, new PackMetadataSection(description, DetectedVersion.BUILT_IN.getPackVersion(packType)));

            if(!generatedFeatures.isEmpty()) {
                if(packType != PackType.SERVER_DATA)
                    throw new IllegalStateException("FeatureFlags are only supported by packs of type: SERVER_DATA");

                metadataGenerator = metadataGenerator.add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(generatedFeatures));
            }

            providerConsumer.accept(metadataGenerator);
        }

        providerListeners.keySet().forEach(providerType -> registerProvider(providerType, context, output, registries, providerConsumer));
    }

    private <TProvider> void registerProvider(ProviderType<TProvider> providerType, ProviderContext context, PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Consumer<DataProvider> providerConsumer) {
        providerConsumer.accept(new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return registries.thenCompose(registries -> {
                    var listenerContext = ProviderListenerContext.of(context, registries);
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
