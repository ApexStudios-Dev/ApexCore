package dev.apexstudios.apexcore.common.data.pack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import dev.apexstudios.apexcore.api.data.ExtendedRegistryBootstrap;
import dev.apexstudios.apexcore.common.data.ExtendedRegistryBootstrapImpl;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ICondition;

@SuppressWarnings({"unchecked", "rawtypes"})
public record ModdedRegistries(
        CompletableFuture<HolderLookup.Provider> world,
        CompletableFuture<HolderLookup.Provider> reloadable
) {
    static ModdedRegistries create(
            String modId,
            CompletableFuture<HolderLookup.Provider> vanillaWorld,
            CompletableFuture<HolderLookup.Provider> vanillaReloadable,
            Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> worldListeners,
            Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> reloadableListeners
    ) {
        var moddedWorld = register(
                modId,
                vanillaWorld,
                worldListeners,
                ModdedRegistries::register,
                RegistryPatchGenerator::createWorldLookup
        );

        return new ModdedRegistries(
                moddedWorld,
                register(
                        modId,
                        vanillaReloadable,
                        reloadableListeners,
                        ModdedRegistries::register,
                        (vanilla, modded) -> RegistryPatchGenerator.createReloadableLookup(moddedWorld, vanilla, modded)
                )
        );
    }

    private static CompletableFuture<HolderLookup.Provider> register(
            String modId,
            CompletableFuture<HolderLookup.Provider> vanilla,
            Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> listeners,
            DynamicRegistrar registrar,
            RegistryPatcher patcher
    ) {
        if(listeners.isEmpty()) {
            return vanilla;
        }

        var registrySetBuilder = new RegistrySetBuilder();
        var conditionsMap = Maps.<ResourceKey<?>, List<ICondition>>newHashMap();

        listeners.keySet().forEach(registryType -> registrar.accept(
                (ResourceKey) registryType,
                modId,
                registrySetBuilder::add,
                (registryKey, conditions) -> {
                    var list = conditionsMap.computeIfAbsent(registryKey, $ -> Lists.newArrayList());
                    Collections.addAll(list, conditions);
                },
                listeners.get(registryType)
        ));

        return patcher.patch(vanilla, registrySetBuilder).thenApply(RegistrySetBuilder.PatchedRegistries::patches);
    }

    private static <TRegistry> void register(ResourceKey<? extends Registry<TRegistry>> registryType, String modId, BootstrapRegistrar<TRegistry> consumer, ConditionalRegistrar<TRegistry> conditionConsumer, Iterable<Consumer<? extends ExtendedRegistryBootstrap<?>>> listeners) {
        consumer.accept(registryType, context -> {
            var extended = new ExtendedRegistryBootstrapImpl<>(context, registryType, modId, conditionConsumer);
            listeners.forEach(listener -> ((Consumer<ExtendedRegistryBootstrap<TRegistry>>) listener).accept(extended));
        });
    }
}
