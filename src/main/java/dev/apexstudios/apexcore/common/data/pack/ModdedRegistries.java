package dev.apexstudios.apexcore.common.data.pack;

import com.google.common.collect.Multimap;
import dev.apexstudios.apexcore.api.data.ExtendedRegistryBootstrap;
import dev.apexstudios.apexcore.common.data.ExtendedRegistryBootstrapImpl;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

@SuppressWarnings({"unchecked", "rawtypes"})
public record ModdedRegistries(
        CompletableFuture<HolderLookup.Provider> world,
        CompletableFuture<HolderLookup.Provider> reloadable
) {
    CompletableFuture<?> apply(BiFunction<HolderLookup.Provider, HolderLookup.Provider, CompletableFuture<?>> function) {
        return world.thenCombine(reloadable, function);
    }

    static ModdedRegistries create(
            String modId,
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> vanillaWorld,
            CompletableFuture<HolderLookup.Provider> vanillaReloadable,
            Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> worldListeners,
            Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> reloadableListeners,
            Consumer<DataProvider> providerConsumer
    ) {
        var moddedWorld = register(
                modId,
                output,
                DatapackBuiltinEntriesProvider::forWorldLayer,
                vanillaWorld,
                worldListeners,
                ModdedRegistries::register,
                RegistryPatchGenerator::createWorldLookup,
                providerConsumer
        );

        return new ModdedRegistries(
                moddedWorld,
                register(
                        modId,
                        output,
                        DatapackBuiltinEntriesProvider::forReloadableLayer,
                        vanillaReloadable,
                        reloadableListeners,
                        ModdedRegistries::register,
                        (vanilla, modded) -> RegistryPatchGenerator.createReloadableLookup(moddedWorld, vanilla, modded),
                        providerConsumer
                )
        );
    }

    private static CompletableFuture<HolderLookup.Provider> register(
            String modId,
            PackOutput output,
            BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, DataProvider> providerFactory,
            CompletableFuture<HolderLookup.Provider> vanilla,
            Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> listeners,
            DynamicRegistrar registrar,
            RegistryPatcher patcher,
            Consumer<DataProvider> providerConsumer
    ) {
        if(listeners.isEmpty()) {
            return vanilla;
        }

        var registrySetBuilder = new RegistrySetBuilder();

        listeners.keySet().forEach(registryType -> registrar.accept(
                (ResourceKey) registryType,
                modId,
                registrySetBuilder::add,
                listeners.get(registryType)
        ));

        var patched = patcher.patch(vanilla, registrySetBuilder);
        var modded = patched.thenApply(RegistrySetBuilder.PatchedRegistries::patches);
        providerConsumer.accept(providerFactory.apply(output, modded));
        return modded;
    }

    private static <TRegistry> void register(ResourceKey<? extends Registry<TRegistry>> registryType, String modId, BootstrapRegistrar<TRegistry> consumer, Iterable<Consumer<? extends ExtendedRegistryBootstrap<?>>> listeners) {
        consumer.accept(registryType, context -> {
            var extended = new ExtendedRegistryBootstrapImpl<>(context, registryType, modId);
            listeners.forEach(listener -> ((Consumer<ExtendedRegistryBootstrap<TRegistry>>) listener).accept(extended));
        });
    }
}
