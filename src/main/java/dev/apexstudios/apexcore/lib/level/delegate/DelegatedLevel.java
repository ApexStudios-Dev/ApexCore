package dev.apexstudios.apexcore.lib.level.delegate;

import dev.apexstudios.apexcore.mixin.LevelAccessor;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.client.model.data.ModelDataManager;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

public class DelegatedLevel extends Level implements DelegatedLevelAccessor, DelegatedAttachmentHolder {
    private final Level delegate;

    protected DelegatedLevel(Level delegate) {
        super((WritableLevelData) delegate.getLevelData(), delegate.dimension(), delegate.registryAccess(), delegate.dimensionTypeRegistration(), delegate.isClientSide, delegate.isDebug(), 0L, 0);

        this.delegate = delegate;
    }

    @Override
    public Level delegate() {
        return delegate;
    }

    @Override
    public boolean isClientSide() {
        return delegate().isClientSide();
    }

    @Override
    public @Nullable MinecraftServer getServer() {
        return delegate().getServer();
    }

    @Override
    public boolean isInWorldBounds(BlockPos pos) {
        return delegate().isInWorldBounds(pos);
    }

    @Override
    public LevelChunk getChunkAt(BlockPos pos) {
        return delegate().getChunkAt(pos);
    }

    @Override
    public LevelChunk getChunk(int chunkX, int chunkZ) {
        return delegate().getChunk(chunkX, chunkZ);
    }

    @Override
    public @Nullable ChunkAccess getChunk(int p_46502_, int p_46503_, ChunkStatus p_331611_, boolean p_46505_) {
        return delegate().getChunk(p_46502_, p_46503_, p_331611_, p_46505_);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
        return delegate().setBlock(pos, newState, flags);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        return delegate().setBlock(pos, state, flags, recursionLeft);
    }

    @Override
    public void markAndNotifyBlock(BlockPos p_46605_, @Nullable LevelChunk levelchunk, BlockState blockstate, BlockState p_46606_, int p_46607_, int p_46608_) {
        delegate().markAndNotifyBlock(p_46605_, levelchunk, blockstate, p_46606_, p_46607_, p_46608_);
    }

    @Override
    public void onBlockStateChange(BlockPos pos, BlockState blockState, BlockState newState) {
        delegate().onBlockStateChange(pos, blockState, newState);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        return delegate().removeBlock(pos, isMoving);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        return delegate().destroyBlock(pos, dropBlock, entity, recursionLeft);
    }

    @Override
    public void addDestroyBlockEffect(BlockPos pos, BlockState state) {
        delegate().addDestroyBlockEffect(pos, state);
    }

    @Override
    public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
        return delegate().setBlockAndUpdate(pos, state);
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        delegate().sendBlockUpdated(pos, oldState, newState, flags);
    }

    @Override
    public void setBlocksDirty(BlockPos blockPos, BlockState oldState, BlockState newState) {
        delegate().setBlocksDirty(blockPos, oldState, newState);
    }

    @Override
    public void updateNeighborsAt(BlockPos pos, Block block) {
        delegate().updateNeighborsAt(pos, block);
    }

    @Override
    public void updateNeighborsAt(BlockPos pos, Block block, @Nullable Orientation orientation) {
        delegate().updateNeighborsAt(pos, block, orientation);
    }

    @Override
    public void updateNeighborsAtExceptFromFacing(BlockPos pos, Block block, Direction facing, @Nullable Orientation orientation) {
        delegate().updateNeighborsAtExceptFromFacing(pos, block, facing, orientation);
    }

    @Override
    public void neighborChanged(BlockPos pos, Block block, @Nullable Orientation orientation) {
        delegate().neighborChanged(pos, block, orientation);
    }

    @Override
    public void neighborChanged(BlockState state, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        delegate().neighborChanged(state, pos, block, orientation, movedByPiston);
    }

    @Override
    public void neighborShapeChanged(Direction p_220385_, BlockPos p_220387_, BlockPos p_220388_, BlockState p_220386_, int p_220389_, int p_220390_) {
        delegate().neighborShapeChanged(p_220385_, p_220387_, p_220388_, p_220386_, p_220389_, p_220390_);
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        return delegate().getHeight(heightmapType, x, z);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return delegate().getLightEngine();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return delegate().getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return delegate().getFluidState(pos);
    }

    @Override
    public boolean isDay() {
        return delegate().isDay();
    }

    @Override
    public boolean isNight() {
        return delegate().isNight();
    }

    @Override
    public void playSound(@Nullable Entity entity, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        delegate().playSound(entity, pos, sound, category, volume, pitch);
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        delegate().playSound(player, pos, sound, category, volume, pitch);
    }

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {
        delegate().playSeededSound(player, x, y, z, sound, category, volume, pitch, seed);
    }

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, long seed) {
        delegate().playSeededSound(player, x, y, z, sound, category, volume, pitch, seed);
    }

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {
        delegate().playSeededSound(player, entity, sound, category, volume, pitch, seed);
    }

    @Override
    public void playSound(@Nullable Player player, double x, double y, double z, SoundEvent sound, SoundSource category) {
        delegate().playSound(player, x, y, z, sound, category);
    }

    @Override
    public void playSound(@Nullable Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
        delegate().playSound(player, x, y, z, sound, category, volume, pitch);
    }

    @Override
    public void playSound(@Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch) {
        delegate().playSound(player, x, y, z, sound, category, volume, pitch);
    }

    @Override
    public void playSound(@Nullable Player player, Entity entity, SoundEvent event, SoundSource category, float volume, float pitch) {
        delegate().playSound(player, entity, event, category, volume, pitch);
    }

    @Override
    public void playLocalSound(BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay) {
        delegate().playLocalSound(pos, sound, category, volume, pitch, distanceDelay);
    }

    @Override
    public void playLocalSound(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
        delegate().playLocalSound(entity, sound, category, volume, pitch);
    }

    @Override
    public void playLocalSound(double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay) {
        delegate().playLocalSound(x, y, z, sound, category, volume, pitch, distanceDelay);
    }

    @Override
    public void addParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        delegate().addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void addParticle(ParticleOptions particle, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        delegate().addParticle(particle, overrideLimiter, alwaysShow, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        delegate().addAlwaysVisibleParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions particle, boolean ignoreRange, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        delegate().addAlwaysVisibleParticle(particle, ignoreRange, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public float getSunAngle(float partialTick) {
        return delegate().getSunAngle(partialTick);
    }

    @Override
    public void addBlockEntityTicker(TickingBlockEntity ticker) {
        delegate().addBlockEntityTicker(ticker);
    }

    @Override
    public void addFreshBlockEntities(Collection<BlockEntity> beList) {
        delegate().addFreshBlockEntities(beList);
    }

    @Override
    protected void tickBlockEntities() {
        ((LevelAccessor) delegate()).ApexCore$tickBlockEntities();
    }

    @Override
    public <T extends Entity> void guardEntityTick(Consumer<T> consumerEntity, T entity) {
        delegate().guardEntityTick(consumerEntity, entity);
    }

    @Override
    public boolean shouldTickDeath(Entity entity) {
        return delegate().shouldTickDeath(entity);
    }

    @Override
    public boolean shouldTickBlocksAt(long chunkPos) {
        return delegate().shouldTickBlocksAt(chunkPos);
    }

    @Override
    public boolean shouldTickBlocksAt(BlockPos pos) {
        return delegate().shouldTickBlocksAt(pos);
    }

    @Override
    public void explode(@Nullable Entity source, double x, double y, double z, float radius, ExplosionInteraction explosionInteraction) {
        delegate().explode(source, x, y, z, radius, explosionInteraction);
    }

    @Override
    public void explode(@Nullable Entity source, double x, double y, double z, float radius, boolean fire, ExplosionInteraction explosionInteraction) {
        delegate().explode(source, x, y, z, radius, fire, explosionInteraction);
    }

    @Override
    public void explode(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, Vec3 pos, float radius, boolean fire, ExplosionInteraction explosionInteraction) {
        delegate().explode(source, damageSource, damageCalculator, pos, radius, fire, explosionInteraction);
    }

    @Override
    public void explode(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, ExplosionInteraction explosionInteraction) {
        delegate().explode(source, damageSource, damageCalculator, x, y, z, radius, fire, explosionInteraction);
    }

    @Override
    public void explode(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, ExplosionInteraction explosionInteraction, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, Holder<SoundEvent> explosionSound) {
        delegate().explode(source, x, y, z, radius, explosionInteraction);
    }

    @Override
    public String gatherChunkSourceStats() {
        return delegate().gatherChunkSourceStats();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return delegate().getBlockEntity(pos);
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        delegate().setBlockEntity(blockEntity);
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
        delegate().removeBlockEntity(pos);
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return delegate().isLoaded(pos);
    }

    @Override
    public boolean loadedAndEntityCanStandOnFace(BlockPos pos, Entity entity, Direction direction) {
        return delegate().loadedAndEntityCanStandOnFace(pos, entity, direction);
    }

    @Override
    public boolean loadedAndEntityCanStandOn(BlockPos pos, Entity entity) {
        return delegate().loadedAndEntityCanStandOn(pos, entity);
    }

    @Override
    public void updateSkyBrightness() {
        delegate().updateSkyBrightness();
    }

    @Override
    public void setSpawnSettings(boolean spawnSettings) {
        delegate().setSpawnSettings(spawnSettings);
    }

    @Override
    public BlockPos getSharedSpawnPos() {
        return delegate().getSharedSpawnPos();
    }

    @Override
    public float getSharedSpawnAngle() {
        return delegate().getSharedSpawnAngle();
    }

    @Override
    protected void prepareWeather() {
        ((LevelAccessor) delegate()).ApexCore$prepareWeather();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public @Nullable BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return delegate().getChunkForCollisions(chunkX, chunkZ);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB boundingBox, Predicate<? super Entity> predicate) {
        return delegate().getEntities(entity, boundingBox, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> p_151528_, AABB p_151529_, Predicate<? super T> p_151530_) {
        return delegate().getEntities(p_151528_, p_151529_, p_151530_);
    }

    @Override
    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate, List<? super T> output) {
        delegate().getEntities(entityTypeTest, bounds, predicate, output);
    }

    @Override
    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate, List<? super T> output, int maxResults) {
        delegate().getEntities(entityTypeTest, bounds, predicate, output, maxResults);
    }

    @Override
    public @Nullable Entity getEntity(int id) {
        return delegate().getEntity(id);
    }

    @Override
    public Collection<PartEntity<?>> dragonParts() {
        return delegate().dragonParts();
    }

    @Override
    public void blockEntityChanged(BlockPos pos) {
        delegate().blockEntityChanged(pos);
    }

    @Override
    public void disconnect() {
        delegate().disconnect();
    }

    @Override
    public long getGameTime() {
        return delegate().getGameTime();
    }

    @Override
    public long getDayTime() {
        return delegate().getDayTime();
    }

    @Override
    public boolean mayInteract(Player player, BlockPos pos) {
        return delegate().mayInteract(player, pos);
    }

    @Override
    public void broadcastEntityEvent(Entity entity, byte state) {
        delegate().broadcastEntityEvent(entity, state);
    }

    @Override
    public void broadcastDamageEvent(Entity entity, DamageSource damageSource) {
        delegate().broadcastDamageEvent(entity, damageSource);
    }

    @Override
    public void blockEvent(BlockPos pos, Block block, int eventID, int eventParam) {
        delegate().blockEvent(pos, block, eventID, eventParam);
    }

    @Override
    public LevelData getLevelData() {
        return delegate().getLevelData();
    }

    @Override
    public TickRateManager tickRateManager() {
        return delegate().tickRateManager();
    }

    @Override
    public float getThunderLevel(float partialTick) {
        return delegate().getThunderLevel(partialTick);
    }

    @Override
    public void setThunderLevel(float strength) {
        delegate().setThunderLevel(strength);
    }

    @Override
    public float getRainLevel(float partialTick) {
        return delegate().getRainLevel(partialTick);
    }

    @Override
    public void setRainLevel(float strength) {
        delegate().setRainLevel(strength);
    }

    @Override
    public boolean isThundering() {
        return delegate().isThundering();
    }

    @Override
    public boolean isRaining() {
        return delegate().isRaining();
    }

    @Override
    public boolean isRainingAt(BlockPos pos) {
        return delegate().isRainingAt(pos);
    }

    @Override
    public @Nullable MapItemSavedData getMapData(MapId mapId) {
        return delegate().getMapData(mapId);
    }

    @Override
    public void setMapData(MapId mapId, MapItemSavedData mapData) {
        delegate().setMapData(mapId, mapData);
    }

    @Override
    public MapId getFreeMapId() {
        return delegate().getFreeMapId();
    }

    @Override
    public void globalLevelEvent(int id, BlockPos pos, int data) {
        delegate().globalLevelEvent(id, pos, data);
    }

    @Override
    public CrashReportCategory fillReportDetails(CrashReport report) {
        return delegate().fillReportDetails(report);
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
        delegate().destroyBlockProgress(breakerId, pos, progress);
    }

    @Override
    public void createFireworks(double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, List<FireworkExplosion> explosions) {
        delegate().createFireworks(x, y, z, xSpeed, ySpeed, zSpeed, explosions);
    }

    @Override
    public Scoreboard getScoreboard() {
        return delegate().getScoreboard();
    }

    @Override
    public void updateNeighbourForOutputSignal(BlockPos pos, Block block) {
        delegate().updateNeighbourForOutputSignal(pos, block);
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        return delegate().getCurrentDifficultyAt(pos);
    }

    @Override
    public int getSkyDarken() {
        return delegate().getSkyDarken();
    }

    @Override
    public void setSkyFlashTime(int timeFlash) {
        delegate().setSkyFlashTime(timeFlash);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return delegate().getWorldBorder();
    }

    @Override
    public void sendPacketToServer(Packet<?> packet) {
        delegate().sendPacketToServer(packet);
    }

    @Override
    public DimensionType dimensionType() {
        return delegate().dimensionType();
    }

    @Override
    public Holder<DimensionType> dimensionTypeRegistration() {
        return delegate().dimensionTypeRegistration();
    }

    @Override
    public ResourceKey<Level> dimension() {
        return delegate().dimension();
    }

    @Override
    public RandomSource getRandom() {
        return delegate().getRandom();
    }

    @Override
    public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> state) {
        return delegate().isStateAtPosition(pos, state);
    }

    @Override
    public boolean isFluidAtPosition(BlockPos p_151541_, Predicate<FluidState> p_151542_) {
        return delegate().isFluidAtPosition(p_151541_, p_151542_);
    }

    @Override
    public RecipeAccess recipeAccess() {
        return delegate().recipeAccess();
    }

    @Override
    public BlockPos getBlockRandomPos(int x, int y, int z, int yMask) {
        return delegate().getBlockRandomPos(x, y, z, yMask);
    }

    @Override
    public boolean noSave() {
        return delegate().noSave();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return delegate().getBiomeManager();
    }

    @Override
    public double getMaxEntityRadius() {
        return delegate().getMaxEntityRadius();
    }

    @Override
    public double increaseMaxEntityRadius(double value) {
        return delegate().increaseMaxEntityRadius(value);
    }

    @Override
    public @Nullable ModelDataManager getModelDataManager() {
        return delegate().getModelDataManager();
    }

    @Override
    public <T, C> @Nullable T getCapability(BlockCapability<T, C> cap, BlockPos pos, C context) {
        return delegate().getCapability(cap, pos, context);
    }

    @Override
    public <T, C> @Nullable T getCapability(BlockCapability<T, C> cap, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return delegate().getCapability(cap, pos, state, blockEntity, context);
    }

    @Override
    public <T> @Nullable T getCapability(BlockCapability<T, @Nullable Void> cap, BlockPos pos) {
        return delegate().getCapability(cap, pos);
    }

    @Override
    public <T> @Nullable T getCapability(BlockCapability<T, @Nullable Void> cap, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        return delegate().getCapability(cap, pos, state, blockEntity);
    }

    @Override
    public void invalidateCapabilities(BlockPos pos) {
        delegate().invalidateCapabilities(pos);
    }

    @Override
    public void invalidateCapabilities(ChunkPos pos) {
        delegate().invalidateCapabilities(pos);
    }

    @Override
    public String getDescriptionKey() {
        return delegate().getDescriptionKey();
    }

    @Override
    public Component getDescription() {
        return delegate().getDescription();
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return ((LevelAccessor) delegate()).ApexCore$getEntities();
    }

    @Override
    public long nextSubTickCount() {
        return delegate().nextSubTickCount();
    }

    @Override
    public RegistryAccess registryAccess() {
        return delegate().registryAccess();
    }

    @Override
    public DamageSources damageSources() {
        return delegate().damageSources();
    }

    @Override
    public PotionBrewing potionBrewing() {
        return delegate().potionBrewing();
    }

    @Override
    public FuelValues fuelValues() {
        return delegate().fuelValues();
    }

    @Override
    public void setDayTimeFraction(float dayTimeFraction) {
        delegate().setDayTimeFraction(dayTimeFraction);
    }

    @Override
    public float getDayTimeFraction() {
        return delegate().getDayTimeFraction();
    }

    @Override
    public float getDayTimePerTick() {
        return delegate().getDayTimePerTick();
    }

    @Override
    public void setDayTimePerTick(float dayTimePerTick) {
        delegate().setDayTimePerTick(dayTimePerTick);
    }

    @Override
    protected long advanceDaytime() {
        return ((LevelAccessor) delegate()).ApexCore$advanceDaytime();
    }

    @Override
    public <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return delegate().getExistingData(type);
    }

    @Override
    public <T> @Nullable T setData(AttachmentType<T> type, T data) {
        return delegate().setData(type, data);
    }

    @Override
    public <T> @Nullable T removeData(AttachmentType<T> type) {
        return delegate().removeData(type);
    }
}
