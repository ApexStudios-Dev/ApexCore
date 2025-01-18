package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface DelegatedBlockAndTintGetter extends DelegatedBlockGetter, BlockAndTintGetter {
    @Override
    BlockAndTintGetter delegate();

    @Override
    default float getShade(Direction direction, boolean shade) {
        return delegate().getShade(direction, shade);
    }

    @Override
    default LevelLightEngine getLightEngine() {
        return delegate().getLightEngine();
    }

    @Override
    default int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return delegate().getBlockTint(blockPos, colorResolver);
    }

    @Override
    default int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return delegate().getBrightness(lightType, blockPos);
    }

    @Override
    default int getRawBrightness(BlockPos blockPos, int amount) {
        return delegate().getRawBrightness(blockPos, amount);
    }

    @Override
    default boolean canSeeSky(BlockPos blockPos) {
        return delegate().canSeeSky(blockPos);
    }

    @Override
    default float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return delegate().getShade(normalX, normalY, normalZ, shade);
    }
}
