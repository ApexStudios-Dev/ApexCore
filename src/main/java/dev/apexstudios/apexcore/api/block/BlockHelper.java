package dev.apexstudios.apexcore.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockHelper {
    static boolean shouldBounceOnBlock(BlockPos pos, BlockState blockState, Entity entity) {
        if(entity.isSuppressingBounce()) {
            return false;
        }

        if(blockState.is(BlockTags.SUPPRESSES_BOUNCE)) {
            return false;
        }

        return entity.getBlockBounciness(pos, blockState) > 0D;
    }
}
