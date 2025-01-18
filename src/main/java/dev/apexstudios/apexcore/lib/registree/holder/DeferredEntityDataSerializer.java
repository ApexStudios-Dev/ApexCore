package dev.apexstudios.apexcore.lib.registree.holder;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;

public final class DeferredEntityDataSerializer<TData> extends ApexDeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<TData>> {
    public DeferredEntityDataSerializer(ResourceKey<EntityDataSerializer<?>> registryKey) {
        super(registryKey);
    }
}
