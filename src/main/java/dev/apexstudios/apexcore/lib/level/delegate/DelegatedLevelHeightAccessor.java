package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;

public interface DelegatedLevelHeightAccessor extends LevelHeightAccessor {
    LevelHeightAccessor delegate();

    @Override
    default int getHeight() {
        return delegate().getHeight();
    }

    @Override
    default int getMinY() {
        return delegate().getMinY();
    }

    @Override
    default int getMaxY() {
        return delegate().getMaxY();
    }

    @Override
    default int getSectionsCount() {
        return delegate().getSectionsCount();
    }

    @Override
    default int getMinSectionY() {
        return delegate().getMinSectionY();
    }

    @Override
    default int getMaxSectionY() {
        return delegate().getMaxSectionY();
    }

    @Override
    default boolean isInsideBuildHeight(int y) {
        return delegate().isInsideBuildHeight(y);
    }

    @Override
    default boolean isOutsideBuildHeight(BlockPos pos) {
        return delegate().isOutsideBuildHeight(pos);
    }

    @Override
    default boolean isOutsideBuildHeight(int y) {
        return delegate().isOutsideBuildHeight(y);
    }

    @Override
    default int getSectionIndex(int y) {
        return delegate().getSectionIndex(y);
    }

    @Override
    default int getSectionIndexFromSectionY(int sectionIndex) {
        return delegate().getSectionIndexFromSectionY(sectionIndex);
    }

    @Override
    default int getSectionYFromSectionIndex(int sectionIndex) {
        return delegate().getSectionYFromSectionIndex(sectionIndex);
    }
}
