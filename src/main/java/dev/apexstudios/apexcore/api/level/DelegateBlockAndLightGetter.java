package dev.apexstudios.apexcore.api.level;

import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface DelegateBlockAndLightGetter extends DelegateBlockGetter, BlockAndLightGetter {
    @Override
    BlockAndLightGetter delegate();

    @Override
    default LevelLightEngine getLightEngine() {
        return delegate().getLightEngine();
    }

    static DelegateBlockAndLightGetter wrap(BlockAndLightGetter level) {
        return level instanceof DelegateBlockAndLightGetter delegate ? delegate : () -> level;
    }

    static BlockAndLightGetter unwrap(BlockAndLightGetter level) {
        if(level instanceof DelegateBlockAndLightGetter delegate) {
            return unwrap(delegate.delegate());
        }

        return level;
    }
}
