package dev.apexstudios.apexcore.common.data.provider.tag;

import dev.apexstudios.apexcore.api.data.provider.tag.BlockTagBuilder;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

final class BlockTagBuilderImpl extends IntrusiveTagBuilderImpl<Block, BlockTagBuilder> implements BlockTagBuilder {
    BlockTagBuilderImpl(String namespace) {
        super(namespace);
    }

    @Override
    protected ResourceKey<Block> getKey(Block value) {
        return value.builtInRegistryHolder().key();
    }
}
