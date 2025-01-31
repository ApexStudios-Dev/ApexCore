package dev.apexstudios.apexcore.lib.block.entity;

import com.google.errorprone.annotations.ForOverride;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BaseBlockEntity extends BlockEntity {
    protected BaseBlockEntity(BlockEntityType<? extends BaseBlockEntity> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    // region: Overrides
    @ForOverride
    public void playerDestroy(Level level, Player player, BlockState blockState, ItemStack stack) {

    }

    @ForOverride
    public void setPlacedBy(Level level, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {

    }

    @ForOverride
    public BlockState playerWillDestroy(Level level, BlockState blockState, Player player) {
        return blockState;
    }

    @ForOverride
    public BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, Direction facing, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        return blockState;
    }

    @ForOverride
    public void neighborChanged(BlockState blockState, Level level, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {

    }

    @ForOverride
    public void onPlace(BlockState blockState, Level level, BlockState oldBlockState, boolean movedByPiston) {

    }

    @ForOverride
    public void onRemove(BlockState blockState, Level level, BlockState newBlockState, boolean movedByPiston) {

    }

    @ForOverride
    public InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, Player player, InteractionHand hand, BlockHitResult result) {
        return InteractionResult.PASS;
    }

    @ForOverride
    public InteractionResult useWithoutItem(BlockState blockState, Level level, Player player, BlockHitResult result) {
        return InteractionResult.PASS;
    }

    @ForOverride
    public int getAnalogOutputSignal(BlockState blockState, Level level) {
        return -1;
    }

    @ForOverride
    public void entityInside(BlockState blockState, Level level, Entity entity) {

    }

    @ForOverride
    public void handlePrecipitation(BlockState blockState, Level level, Biome.Precipitation precipitation) {

    }

    @ForOverride
    public void stepOn(Level level, BlockState blockState, Entity entity) {

    }

    @ForOverride
    public boolean updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        return false;
    }
    // endregion

    // region: Internal
    @Override
    public final Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    // endregion
}
