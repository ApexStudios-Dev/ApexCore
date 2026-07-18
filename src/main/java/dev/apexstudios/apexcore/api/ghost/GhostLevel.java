package dev.apexstudios.apexcore.api.ghost;

import dev.apexstudios.apexcore.api.level.DelegateBlockAndTintGetter;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongObjectBiConsumer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.model.data.ModelData;

public sealed class GhostLevel implements DelegateBlockAndTintGetter {
    private final BlockAndTintGetter delegate;
    private final boolean alwaysOnTop;
    protected final Long2ObjectMap<GhostBlockState> entries;

    private GhostLevel(BlockAndTintGetter level, boolean alwaysOnTop, Long2ObjectMap<GhostBlockState> entries) {
        this.delegate = level;
        this.alwaysOnTop = alwaysOnTop;
        this.entries = entries;
    }

    @Override
    public BlockAndTintGetter delegate() {
        return delegate;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return entries.get(pos.asLong()).blockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color) {
        if(entries.containsKey(pos.asLong())) {
            return DelegateBlockAndTintGetter.super.getBlockTint(pos, color);
        }

        return CommonColors.WHITE;
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return LightEngine.MAX_LEVEL;
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        return entries.get(pos.asLong()).modelData();
    }

    public boolean alwaysOnTop() {
        return alwaysOnTop;
    }

    public void forEachGhost(LongObjectBiConsumer<GhostBlockState> action) {
        entries.forEach(action);
    }

    public GhostLevel immutable() {
        return this;
    }

    public static Mutable create(BlockAndTintGetter level, boolean alwaysOnTop) {
        return new Mutable(level, alwaysOnTop);
    }

    public static final class Mutable extends GhostLevel {
        private Mutable(BlockAndTintGetter level, boolean alwaysOnTop) {
            super(DelegateBlockAndTintGetter.unwrap(level), alwaysOnTop, new Long2ObjectOpenHashMap<>());

            entries.defaultReturnValue(GhostBlockState.EMPTY);
        }

        public void setBlock(BlockPos pos, GhostBlockState blockState) {
            entries.put(pos.asLong(), blockState);
        }

        @Override
        public GhostLevel immutable() {
            return new GhostLevel(
                    this,
                    alwaysOnTop(),
                    Long2ObjectMaps.unmodifiable(entries)
            );
        }
    }
}
