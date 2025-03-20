package dev.apexstudios.apexcore.lib.level;

import dev.apexstudios.apexcore.mixin.LevelAccessor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

public final class FakeLevel extends Level {
    private final Level delegate;
    private final Long2ObjectMap<BlockState> blockStates = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<BlockEntity> blockEntities = new Long2ObjectOpenHashMap<>();

    public FakeLevel(Level delegate) {
        super((WritableLevelData) delegate.getLevelData(), delegate.dimension(), delegate.registryAccess(), delegate.dimensionTypeRegistration(), delegate.isClientSide, delegate.isDebug(), 0L, 0);

        this.delegate = delegate;
    }

    public Stream<BlockPos> positions() {
        return blockStates.keySet().longStream().mapToObj(BlockPos::of);
    }

    private void isClientSide(boolean isClientSide) {
        ((LevelAccessor) (Level) this).ApexCore$setIsClientSide(isClientSide);
    }

    public void runAsServerSide(Runnable runnable) {
        var isClientSide = isClientSide();
        isClientSide(false);
        runnable.run();
        isClientSide(isClientSide);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        var key = pos.asLong();
        var old = blockStates.getOrDefault(key, Blocks.AIR.defaultBlockState());

        if(old == state)
            return false;

        if(state.isAir())
            blockStates.remove(key);
        else {
            blockStates.put(key, state);

            if((flags & Block.UPDATE_NONE) != 0) {
                //markAndNotifyBlock(pos, getChunkAt(pos), state, old, flags, recursionLeft);
                runAsServerSide(() -> {
                    updateNeighbourForOutputSignal(pos, state.getBlock());
                    old.updateIndirectNeighbourShapes(this, pos, flags & -34, recursionLeft - 1);
                    // state.updateNeighbourShapes(this, pos, flags & -34, recursionLeft - 1);
                    // state.updateIndirectNeighbourShapes(this, pos, flags & -34, recursionLeft - 1);
                    // onBlockStateChange(pos, state, old);
                    // state.onBlockStateChange(this, pos, old);
                    state.onPlace(this, pos, old, (flags & Block.UPDATE_MOVE_BY_PISTON) != 0);
                });
            }
        }

        var blockEntity = getBlockEntity(pos);

        if(blockEntity != null && !blockEntity.getBlockState().is(state.getBlock()))
            removeBlockEntity(pos);
        else if(state.hasBlockEntity()) {
            var entity = ((EntityBlock) state.getBlock()).newBlockEntity(pos, state);

            if(entity != null)
                setBlockEntity(entity);
        }

        return true;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        var key = pos.asLong();
        return blockStates.containsKey(key) ? blockStates.get(key) : super.getBlockState(pos);
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return ((LevelAccessor) delegate).ApexCore$getEntities();
    }

    @Override
    public List<? extends Player> players() {
        return delegate.players();
    }

    @Override
    public @Nullable Entity getEntity(int id) {
        return delegate.getEntity(id);
    }

    @Override
    public Collection<PartEntity<?>> dragonParts() {
        return delegate.dragonParts();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        removeBlockEntity(pos);
        blockEntity.setLevel(this);
        blockEntity.clearRemoved();
        blockEntities.put(pos.asLong(), blockEntity);
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
        var key = pos.asLong();
        var blockEntity = blockEntities.get(key);

        if(blockEntity != null) {
            blockEntity.setRemoved();
            blockEntities.remove(key);
        }
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return blockEntities != null ? blockEntities.get(pos.asLong()) : null;
    }

    @Override
    public void neighborShapeChanged(Direction p_220385_, BlockPos p_220387_, BlockPos p_220388_, BlockState p_220386_, int p_220389_, int p_220390_) {

    }

    // region: Delegation
    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

    }

    @Override
    public void playSeededSound(@Nullable Entity player, double x, double y, double z, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSeededSound(@Nullable Entity player, Entity entity, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {

    }

    @Override
    public void explode(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, ExplosionInteraction explosionInteraction, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, Holder<SoundEvent> explosionSound) {

    }

    @Override
    public String gatherChunkSourceStats() {
        return delegate.gatherChunkSourceStats();
    }

    @Override
    public TickRateManager tickRateManager() {
        return delegate.tickRateManager();
    }

    @Override
    public @Nullable MapItemSavedData getMapData(MapId mapId) {
        return null;
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return delegate.getScoreboard();
    }

    @Override
    public RecipeAccess recipeAccess() {
        return delegate.recipeAccess();
    }

    @Override
    public PotionBrewing potionBrewing() {
        return delegate.potionBrewing();
    }

    @Override
    public FuelValues fuelValues() {
        return delegate.fuelValues();
    }

    @Override
    public void setDayTimeFraction(float dayTimeFraction) {

    }

    @Override
    public float getDayTimeFraction() {
        return delegate.getDayTimeFraction();
    }

    @Override
    public float getDayTimePerTick() {
        return delegate.getDayTimePerTick();
    }

    @Override
    public void setDayTimePerTick(float dayTimePerTick) {

    }

    @Override
    public ChunkSource getChunkSource() {
        return delegate.getChunkSource();
    }

    @Override
    public void levelEvent(@Nullable Entity player, int type, BlockPos pos, int data) {

    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {

    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return delegate.getShade(direction, shade);
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return delegate.getUncachedNoiseBiome(x, y, z);
    }

    @Override
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return delegate.enabledFeatures();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }
    // endregion
}
