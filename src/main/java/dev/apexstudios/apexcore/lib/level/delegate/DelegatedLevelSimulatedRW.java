package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.world.level.LevelSimulatedRW;

public interface DelegatedLevelSimulatedRW extends DelegatedLevelSimulatedReader, DelegatedLevelWriter {
    @Override
    LevelSimulatedRW delegate();
}
