package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public interface DelegatedLevelReader extends DelegatedBlockAndTintGetter, DelegatedCollisionGetter, DelegatedSignalGetter, DelegatedNoiseBiomeSource, LevelReader {
    @Override
    LevelReader delegate();

    @Override
    default @Nullable BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return delegate().getChunkForCollisions(chunkX, chunkZ);
    }

    @Override
    default int getHeight() {
        return delegate().getHeight();
    }

    @Override
    default int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return delegate().getBlockTint(blockPos, colorResolver);
    }

    @Override
    default Holder<Biome> getNoiseBiome(int x, int y, int z) {
        return delegate().getNoiseBiome(x, y, z);
    }

    @Override
    default int getMinY() {
        return delegate().getMinY();
    }

    @Override
    @Nullable
    default ChunkAccess getChunk(int x, int z, ChunkStatus chunkStatus, boolean requireChunk) {
        return delegate().getChunk(x, z, chunkStatus, requireChunk);
    }

    @Override
    default boolean hasChunk(int chunkX, int chunkZ) {
        return delegate().hasChunk(chunkX, chunkZ);
    }

    @Override
    default int getHeight(Heightmap.Types heightmapType, int x, int z) {
        return delegate().getHeight(heightmapType, x, z);
    }

    @Override
    default int getSkyDarken() {
        return delegate().getSkyDarken();
    }

    @Override
    default BiomeManager getBiomeManager() {
        return delegate().getBiomeManager();
    }

    @Override
    default Holder<Biome> getBiome(BlockPos pos) {
        return delegate().getBiome(pos);
    }

    @Override
    default Stream<BlockState> getBlockStatesIfLoaded(AABB aabb) {
        return delegate().getBlockStatesIfLoaded(aabb);
    }

    @Override
    default Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return delegate().getUncachedNoiseBiome(x, y, z);
    }

    @Override
    default boolean isClientSide() {
        return delegate().isClientSide();
    }

    @Override
    default int getSeaLevel() {
        return delegate().getSeaLevel();
    }

    @Override
    default DimensionType dimensionType() {
        return delegate().dimensionType();
    }

    @Override
    default BlockPos getHeightmapPos(Heightmap.Types heightmapType, BlockPos pos) {
        return delegate().getHeightmapPos(heightmapType, pos);
    }

    @Override
    default boolean isEmptyBlock(BlockPos pos) {
        return delegate().isEmptyBlock(pos);
    }

    @Override
    default boolean canSeeSkyFromBelowWater(BlockPos pos) {
        return delegate().canSeeSkyFromBelowWater(pos);
    }

    @Override
    default float getPathfindingCostFromLightLevels(BlockPos pos) {
        return delegate().getPathfindingCostFromLightLevels(pos);
    }

    @Override
    default float getLightLevelDependentMagicValue(BlockPos pos) {
        return delegate().getLightLevelDependentMagicValue(pos);
    }

    @Override
    default ChunkAccess getChunk(BlockPos pos) {
        return delegate().getChunk(pos);
    }

    @Override
    default ChunkAccess getChunk(int chunkX, int chunkZ) {
        return delegate().getChunk(chunkX, chunkZ);
    }

    @Override
    default ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus chunkStatus) {
        return delegate().getChunk(chunkX, chunkZ, chunkStatus);
    }

    @Override
    default boolean isWaterAt(BlockPos pos) {
        return delegate().isWaterAt(pos);
    }

    @Override
    default boolean containsAnyLiquid(AABB bb) {
        return delegate().containsAnyLiquid(bb);
    }

    @Override
    default int getMaxLocalRawBrightness(BlockPos pos) {
        return delegate().getMaxLocalRawBrightness(pos);
    }

    @Override
    default int getMaxLocalRawBrightness(BlockPos pos, int amount) {
        return delegate().getMaxLocalRawBrightness(pos, amount);
    }

    @Override
    default boolean hasChunkAt(int x, int z) {
        return delegate().hasChunkAt(x, z);
    }

    @Override
    default boolean hasChunkAt(BlockPos pos) {
        return delegate().hasChunkAt(pos);
    }

    @Override
    default boolean hasChunksAt(BlockPos from, BlockPos to) {
        return delegate().hasChunksAt(from, to);
    }

    @Override
    default boolean hasChunksAt(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return delegate().hasChunksAt(fromX, fromY, fromZ, toX, toY, toZ);
    }

    @Override
    default boolean hasChunksAt(int fromX, int fromZ, int toX, int toZ) {
        return delegate().hasChunksAt(fromX, fromZ, toX, toZ);
    }

    @Override
    default RegistryAccess registryAccess() {
        return delegate().registryAccess();
    }

    @Override
    default FeatureFlagSet enabledFeatures() {
        return delegate().enabledFeatures();
    }

    @Override
    default <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
        return delegate().holderLookup(registryKey);
    }

    @Override
    default boolean isAreaLoaded(BlockPos center, int range) {
        return delegate().isAreaLoaded(center, range);
    }

    @Override
    default <T> Holder<T> holderOrThrow(ResourceKey<T> key) {
        return delegate().holderOrThrow(key);
    }

    @Override
    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        return delegate().holder(key);
    }
}
