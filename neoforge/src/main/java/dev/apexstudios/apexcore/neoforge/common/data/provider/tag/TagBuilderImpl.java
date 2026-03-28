package dev.apexstudios.apexcore.neoforge.common.data.provider.tag;

import com.google.common.collect.Lists;
import dev.apexstudios.apexcore.neoforge.api.data.provider.tag.TagBuilder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.Identifier;
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
    public TSelf withElement(Identifier registryName) {
        elements.add(TagEntry.element(registryName));
        return (TSelf) this;
    }

    @Override
    public TSelf withElement(String identifier) {
        return withElement(Identifier.fromNamespaceAndPath(namespace, identifier));
    }
    // endregion

    // region: Optional
    @Override
    public TSelf withOptionalElement(Identifier registryName) {
        elements.add(TagEntry.optionalElement(registryName));
        return (TSelf) this;
    }

    @Override
    public TSelf withOptionalElement(String identifier) {
        return withOptionalElement(Identifier.fromNamespaceAndPath(namespace, identifier));
    }
    // endregion
    // endregion

    // region: Tag
    // region: Required
    @Override
    public TSelf withTag(Identifier tagName) {
        elements.add(TagEntry.tag(tagName));
        return (TSelf) this;
    }
    // endregion

    // region: Optional
    @Override
    public TSelf withOptionalTag(Identifier tagName) {
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
    public TSelf removeElement(Identifier registryName) {
        removals.add(TagEntry.element(registryName));
        return (TSelf) this;
    }

    @Override
    public TSelf removeElement(String identifier) {
        return removeElement(Identifier.fromNamespaceAndPath(namespace, identifier));
    }
    // endregion

    // region: Optional
    @Override
    public TSelf removeOptionalElement(Identifier registryName) {
        removals.add(TagEntry.optionalElement(registryName));
        return (TSelf) this;
    }

    @Override
    public TSelf removeOptionalElement(String identifier) {
        return removeOptionalElement(Identifier.fromNamespaceAndPath(namespace, identifier));
    }
    // endregion
    // endregion

    // region: Tag
    // region: Required
    @Override
    public TSelf removeTag(Identifier tagName) {
        removals.add(TagEntry.tag(tagName));
        return (TSelf) this;
    }
    // endregion

    // region: Optional
    @Override
    public TSelf removeOptionalTag(Identifier tagName) {
        removals.add(TagEntry.optionalTag(tagName));
        return (TSelf) this;
    }
    // endregion
    // endregion
    // endregion

    TagFile compile() {
        return new TagFile(copyAndSort(elements), replace, copyAndSort(removals));
    }

    private static List<TagEntry> copyAndSort(List<TagEntry> list) {
        var copy = Lists.newArrayList(list);
        copy.sort(Comparator.comparing(TagEntry::isRequired).thenComparing(TagEntry::getId).thenComparing(TagEntry::isTag));
        return Collections.unmodifiableList(copy);
    }
}
