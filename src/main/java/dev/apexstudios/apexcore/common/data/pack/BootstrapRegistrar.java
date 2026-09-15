package dev.apexstudios.apexcore.common.data.pack;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.resources.ResourceKey;

@FunctionalInterface
public interface BootstrapRegistrar<TRegistry> {
    void accept(ResourceKey<? extends Registry<TRegistry>> registryType, SingleRegistryBootstrap<TRegistry> bootstrap);
}
