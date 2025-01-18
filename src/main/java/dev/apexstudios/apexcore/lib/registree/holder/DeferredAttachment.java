package dev.apexstudios.apexcore.lib.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;

public final class DeferredAttachment<TData> extends ApexDeferredHolder<AttachmentType<?>, AttachmentType<TData>> {
    public DeferredAttachment(ResourceKey<AttachmentType<?>> registryKey) {
        super(registryKey);
    }
}
