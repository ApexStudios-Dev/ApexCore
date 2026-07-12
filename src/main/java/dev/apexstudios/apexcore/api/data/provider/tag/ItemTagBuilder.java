package dev.apexstudios.apexcore.api.data.provider.tag;

import net.minecraft.references.BlockItemId;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.world.item.Item;

public interface ItemTagBuilder extends IntrusiveTagBuilder<Item, ItemTagBuilder> {
    // region: Add
    // region: Element
    // region: Required
    default ItemTagBuilder withElement(BlockItemId blockItemId) {
        return withElement(blockItemId.item());
    }
    // endregion

    // region: Optional
    default ItemTagBuilder withOptionalElement(BlockItemId blockItemId) {
        return withOptionalElement(blockItemId.item());
    }
    // endregion
    // endregion

    // region: Tag
    // region: Required
    default ItemTagBuilder withTag(BlockItemTagId tag) {
        return withTag(tag.item());
    }
    // endregion

    // region: Optional
    default ItemTagBuilder withOptionalTag(BlockItemTagId tag) {
        return withOptionalTag(tag.item());
    }
    // endregion
    // endregion
    // endregion

    // region: Remove
    // region: Element
    // region: Required
    default ItemTagBuilder removeElement(BlockItemId blockItemId) {
        return removeElement(blockItemId.item());
    }
    // endregion

    // region: Optional
    default ItemTagBuilder removeOptionalElement(BlockItemId blockItemId) {
        return removeOptionalElement(blockItemId.item());
    }
    // endregion
    // endregion

    // region: Tag
    // region: Required
    default ItemTagBuilder removeTag(BlockItemTagId tag) {
        return removeTag(tag.item());
    }
    // endregion

    // region: Optional
    default ItemTagBuilder removeOptionalTag(BlockItemTagId tag) {
        return removeOptionalTag(tag.item());
    }
    // endregion
    // endregion
    // endregion
}
