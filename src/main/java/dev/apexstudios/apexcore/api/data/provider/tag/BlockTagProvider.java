package dev.apexstudios.apexcore.api.data.provider.tag;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.common.data.provider.tag.BlockTagProviderImpl;
import net.minecraft.references.BlockItemId;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.world.level.block.Block;

public interface BlockTagProvider extends IntrusiveTagProvider<Block, BlockTagBuilder> {
    ProviderType<BlockTagProvider> PROVIDER_TYPE = BlockTagProviderImpl.PROVIDER_TYPE;

    default BlockTagBuilder tag(BlockItemTagId tag) {
        return tag(tag.block());
    }

    default BlockTagBuilder tag(BlockItemId blockItemId) {
        return tag(blockItemId.block());
    }
}
