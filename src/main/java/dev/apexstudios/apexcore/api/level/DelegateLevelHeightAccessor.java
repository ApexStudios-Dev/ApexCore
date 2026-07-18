package dev.apexstudios.apexcore.api.level;

import net.minecraft.world.level.LevelHeightAccessor;

public interface DelegateLevelHeightAccessor extends LevelHeightAccessor {
    LevelHeightAccessor delegate();

    @Override
    default int getHeight() {
        return delegate().getHeight();
    }

    @Override
    default int getMinY() {
        return delegate().getMinY();
    }

    static DelegateLevelHeightAccessor wrap(LevelHeightAccessor level) {
        return level instanceof DelegateLevelHeightAccessor delegate ? delegate : () -> level;
    }

    static LevelHeightAccessor unwrap(LevelHeightAccessor level) {
        if(level instanceof DelegateLevelHeightAccessor delegate) {
            return unwrap(delegate.delegate());
        }

        return level;
    }
}
