package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

public interface DelegatedLevelAccessor extends DelegatedCommonLevelAccessor, DelegatedLevelTimeAccess, DelegatedScheduledTickAccess, LevelAccessor {
    @Override
    LevelAccessor delegate();

    @Override
    default <T> ScheduledTick<T> createTick(BlockPos pos, T type, int delay) {
        return delegate().createTick(pos, type, delay);
    }

    @Override
    default LevelData getLevelData() {
        return delegate().getLevelData();
    }

    @Override
    default DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        return delegate().getCurrentDifficultyAt(pos);
    }

    @Override
    @Nullable
    default MinecraftServer getServer() {
        return delegate().getServer();
    }

    @Override
    default Difficulty getDifficulty() {
        return delegate().getDifficulty();
    }

    @Override
    default ChunkSource getChunkSource() {
        return delegate().getChunkSource();
    }

    @Override
    default <T> ScheduledTick<T> createTick(BlockPos pos, T type, int delay, TickPriority priority) {
        return delegate().createTick(pos, type, delay, priority);
    }

    @Override
    default long dayTime() {
        return delegate().dayTime();
    }

    @Override
    default long nextSubTickCount() {
        return delegate().nextSubTickCount();
    }

    @Override
    default boolean hasChunk(int chunkX, int chunkZ) {
        return delegate().hasChunk(chunkX, chunkZ);
    }

    @Override
    default RandomSource getRandom() {
        return delegate().getRandom();
    }

    @Override
    default void blockUpdated(BlockPos pos, Block block) {
        delegate().blockUpdated(pos, block);
    }

    @Override
    default void neighborShapeChanged(Direction direction, BlockPos pos, BlockPos neighborPos, BlockState neighborState, int flags, int recursionLeft) {
        delegate().neighborShapeChanged(direction, pos, neighborPos, neighborState, flags, recursionLeft);
    }

    @Override
    default void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource source) {
        delegate().playSound(player, pos, sound, source);
    }

    @Override
    default void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        delegate().playSound(player, pos, sound, source, volume, pitch);
    }

    @Override
    default void addParticle(ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        delegate().addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    default void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {
        delegate().levelEvent(player, type, pos, data);
    }

    @Override
    default void levelEvent(int type, BlockPos pos, int data) {
        delegate().levelEvent(type, pos, data);
    }

    @Override
    default void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {
        delegate().gameEvent(gameEvent, pos, context);
    }

    @Override
    default void gameEvent(@Nullable Entity entity, Holder<GameEvent> gameEvent, Vec3 pos) {
        delegate().gameEvent(entity, gameEvent, pos);
    }

    @Override
    default void gameEvent(@Nullable Entity entity, Holder<GameEvent> gameEvent, BlockPos pos) {
        delegate().gameEvent(entity, gameEvent, pos);
    }

    @Override
    default void gameEvent(Holder<GameEvent> gameEvent, BlockPos pos, GameEvent.Context context) {
        delegate().gameEvent(gameEvent, pos, context);
    }

    @Override
    default void gameEvent(ResourceKey<GameEvent> gameEvent, BlockPos pos, GameEvent.Context context) {
        delegate().gameEvent(gameEvent, pos, context);
    }
}
