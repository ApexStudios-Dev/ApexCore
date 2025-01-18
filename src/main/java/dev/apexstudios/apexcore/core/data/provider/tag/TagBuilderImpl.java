package dev.apexstudios.apexcore.core.data.provider.tag;

import com.google.common.collect.Lists;
import dev.apexstudios.apexcore.lib.data.provider.tag.TagBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

sealed class TagBuilderImpl<TRegistry, TSelf extends TagBuilder<TRegistry, TSelf>> implements TagBuilder<TRegistry, TSelf> permits SimpleTagBuilderImpl, IntrusiveTagBuilderImpl {
    private final List<TagEntry> elements = Lists.newArrayList();
    private final List<TagEntry> removals = Lists.newArrayList();
    private final String namespace;
    private boolean replace = false;

    protected TagBuilderImpl(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public TSelf replace() {
        replace = true;
        return (TSelf) this;
    }

    // region: Add
    // region: Element
    // region: Required
    @Override
    public TSelf withElement(ResourceLocation registryName) {
        elements.add(TagEntry.element(registryName));
        return (TSelf) this;
    }

    @Override
    public TSelf withElement(String identifier) {
        return withElement(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
    }
    // endregion

    // region: Optional
    @Override
    public TSelf withOptionalElement(ResourceLocation registryName) {
        elements.add(TagEntry.optionalElement(registryName));
        return (TSelf) this;
    }

    @Override
    public TSelf withOptionalElement(String identifier) {
        return withOptionalElement(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
    }
    // endregion
    // endregion

    // region: Tag
    // region: Required
    @Override
    public TSelf withTag(ResourceLocation tagName) {
        elements.add(TagEntry.tag(tagName));
        return (TSelf) this;
    }
    // endregion

    // region: Optional
    @Override
    public TSelf withOptionalTag(ResourceLocation tagName) {
        elements.add(TagEntry.optionalTag(tagName));
        return (TSelf) this;
    }
    // endregion
    // endregion
    // endregion

    // region: Remove
    // region: Element
    // region: Required
    @Override
    public TSelf removeElement(ResourceLocation registryName) {
        removals.add(TagEntry.element(registryName));
        return (TSelf) this;
    }

    @Override
    public TSelf removeElement(String identifier) {
        return removeElement(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
    }
    // endregion

    // region: Optional
    @Override
    public TSelf removeOptionalElement(ResourceLocation registryName) {
        removals.add(TagEntry.optionalElement(registryName));
        return (TSelf) this;
    }

    @Override
    public TSelf removeOptionalElement(String identifier) {
        return removeOptionalElement(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
    }
    // endregion
    // endregion

    // region: Tag
    // region: Required
    @Override
    public TSelf removeTag(ResourceLocation tagName) {
        removals.add(TagEntry.tag(tagName));
        return (TSelf) this;
    }
    // endregion

    // region: Optional
    @Override
    public TSelf removeOptionalTag(ResourceLocation tagName) {
        removals.add(TagEntry.optionalTag(tagName));
        return (TSelf) this;
    }
    // endregion
    // endregion
    // endregion

    TagFile compile() {
        return new TagFile(List.copyOf(elements), replace, List.copyOf(removals));
    }
}
