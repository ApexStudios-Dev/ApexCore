package dev.apexstudios.apexcore.api.level;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;

public interface DelegateBlockAndTintGetter extends DelegateBlockAndLightGetter, BlockAndTintGetter {
    @Override
    BlockAndTintGetter delegate();

    @Override
    default CardinalLighting cardinalLighting() {
        return delegate().cardinalLighting();
    }

    @Override
    default int getBlockTint(BlockPos pos, ColorResolver color) {
        return delegate().getBlockTint(pos, color);
    }

    static DelegateLevelHeightAccessor wrap(BlockAndTintGetter level) {
        return level instanceof DelegateBlockAndTintGetter delegate ? delegate : () -> level;
    }

    static BlockAndTintGetter unwrap(BlockAndTintGetter level) {
        if(level instanceof DelegateBlockAndTintGetter delegate) {
            return unwrap(delegate.delegate());
        }

        return level;
    }
}
