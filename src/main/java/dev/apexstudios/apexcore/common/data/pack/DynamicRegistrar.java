package dev.apexstudios.apexcore.common.data.pack;

import dev.apexstudios.apexcore.api.data.ExtendedRegistryBootstrap;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@FunctionalInterface
public interface DynamicRegistrar {
    <TRegistry> void accept(ResourceKey<? extends Registry<TRegistry>> registryType, String modId, BootstrapRegistrar<TRegistry> consumer, Iterable<Consumer<? extends ExtendedRegistryBootstrap<?>>> listeners);
}
