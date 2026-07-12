package dev.apexstudios.apexcore.api.data.provider.tag;

import net.minecraft.references.BlockItemId;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.world.level.block.Block;

public interface BlockTagBuilder extends IntrusiveTagBuilder<Block, BlockTagBuilder> {
    // region: Add
    // region: Element
    // region: Required
    default BlockTagBuilder withElement(BlockItemId blockItemId) {
        return withElement(blockItemId.block());
    }
    // endregion

    // region: Optional
    default BlockTagBuilder withOptionalElement(BlockItemId blockItemId) {
        return withOptionalElement(blockItemId.block());
    }
    // endregion
    // endregion

    // region: Tag
    // region: Required
    default BlockTagBuilder withTag(BlockItemTagId tag) {
        return withTag(tag.block());
    }
    // endregion

    // region: Optional
    default BlockTagBuilder withOptionalTag(BlockItemTagId tag) {
        return withOptionalTag(tag.block());
    }
    // endregion
    // endregion
    // endregion

    // region: Remove
    // region: Element
    // region: Required
    default BlockTagBuilder removeElement(BlockItemId blockItemId) {
        return removeElement(blockItemId.block());
    }
    // endregion

    // region: Optional
    default BlockTagBuilder removeOptionalElement(BlockItemId blockItemId) {
        return removeOptionalElement(blockItemId.block());
    }
    // endregion
    // endregion

    // region: Tag
    // region: Required
    default BlockTagBuilder removeTag(BlockItemTagId tag) {
        return removeTag(tag.block());
    }
    // endregion

    // region: Optional
    default BlockTagBuilder removeOptionalTag(BlockItemTagId tag) {
        return removeOptionalTag(tag.block());
    }
    // endregion
    // endregion
    // endregion
}
