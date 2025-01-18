package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.world.level.LevelTimeAccess;

public interface DelegatedLevelTimeAccess extends DelegatedLevelReader, LevelTimeAccess {
    @Override
    LevelTimeAccess delegate();

    @Override
    default long dayTime() {
        return delegate().dayTime();
    }

    @Override
    default float getMoonBrightness() {
        return delegate().getMoonBrightness();
    }

    @Override
    default float getTimeOfDay(float partialTick) {
        return delegate().getTimeOfDay(partialTick);
    }

    @Override
    default int getMoonPhase() {
        return delegate().getMoonPhase();
    }
}
