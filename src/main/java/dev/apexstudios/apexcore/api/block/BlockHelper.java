package dev.apexstudios.apexcore.api.block;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockHelper {
    static boolean shouldBounceOnBlock(Entity entity, BlockState blockState) {
        if(entity.isSuppressingBounce()) {
            return false;
        }

        if(blockState.is(BlockTags.SUPPRESSES_BOUNCE)) {
            return false;
        }

        return entity.getBlockBounciness(blockState) > 0D;
    }
}
