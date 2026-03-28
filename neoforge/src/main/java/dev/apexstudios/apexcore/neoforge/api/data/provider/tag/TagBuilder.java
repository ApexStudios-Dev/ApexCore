package dev.apexstudios.apexcore.neoforge.api.data.provider.tag;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface TagBuilder<TRegistry, TSelf extends TagBuilder<TRegistry, TSelf>> {
    TSelf replace();

    // region: Add
    // region: Element
    // region: Required
    default TSelf withElement(ResourceKey<TRegistry> registryKey) {
        return withElement(registryKey.identifier());
    }

    default TSelf withElement(Holder<TRegistry> holder) {
        return withElement(holder.unwrapKey().orElseThrow());
    }

    TSelf withElement(Identifier registryName);

    TSelf withElement(String identifier);
    // endregion

    // region: Optional
    default TSelf withOptionalElement(ResourceKey<TRegistry> registryKey) {
        return withOptionalElement(registryKey.identifier());
    }

    default TSelf withOptionalElement(Holder<TRegistry> holder) {
        return withOptionalElement(holder.unwrapKey().orElseThrow());
    }

    TSelf withOptionalElement(Identifier registryName);

    TSelf withOptionalElement(String identifier);
    // endregion
    // endregion

    // region: Tag
    // region: Required
    default TSelf withTag(TagKey<TRegistry> tag) {
        return withTag(tag.location());
    }

    TSelf withTag(Identifier tagName);
    // endregion

    // region: Optional
    default TSelf withOptionalTag(TagKey<TRegistry> tag) {
        return withOptionalTag(tag.location());
    }

    TSelf withOptionalTag(Identifier tagName);
    // endregion
    // endregion
    // endregion

    // region: Remove
    // region: Element
    // region: Required
    default TSelf removeElement(ResourceKey<TRegistry> registryKey) {
        return removeElement(registryKey.identifier());
    }

    default TSelf removeElement(Holder.Reference<TRegistry> holder) {
        return removeElement(holder.key());
    }

    default TSelf removeElement(DeferredHolder<TRegistry, ? extends TRegistry> holder) {
        return removeElement(holder.getId());
    }

    TSelf removeElement(Identifier registryName);

    TSelf removeElement(String identifier);
    // endregion

    // region: Optional
    default TSelf removeOptionalElement(ResourceKey<TRegistry> registryKey) {
        return removeOptionalElement(registryKey.identifier());
    }

    default TSelf removeOptionalElement(Holder.Reference<TRegistry> holder) {
        return removeOptionalElement(holder.key());
    }

    default TSelf removeOptionalElement(DeferredHolder<TRegistry, ? extends TRegistry> holder) {
        return removeOptionalElement(holder.getId());
    }

    TSelf removeOptionalElement(Identifier registryName);

    TSelf removeOptionalElement(String identifier);
    // endregion
    // endregion

    // region: Tag
    // region: Required
    default TSelf removeTag(TagKey<TRegistry> tag) {
        return removeTag(tag.location());
    }

    TSelf removeTag(Identifier tagName);
    // endregion

    // region: Optional
    default TSelf removeOptionalTag(TagKey<TRegistry> tag) {
        return removeOptionalTag(tag.location());
    }

    TSelf removeOptionalTag(Identifier tagName);
    // endregion
    // endregion
    // endregion
}
