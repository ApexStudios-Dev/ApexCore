package dev.apexstudios.apexcore.lib.block;

import com.mojang.serialization.MapCodec;
import dev.apexstudios.apexcore.lib.block.entity.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public abstract class BaseEntityBlock extends net.minecraft.world.level.block.BaseEntityBlock {
    protected BaseEntityBlock(Properties properties) {
        super(properties);
    }

    protected InteractionResult openMenu(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        var menuProvider = blockState.getMenuProvider(level, pos);

        if(menuProvider != null) {
            player.openMenu(menuProvider);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack stack) {
        if(blockEntity instanceof BaseBlockEntity base)
            base.playerDestroy(level, player, blockState, stack);

        super.playerDestroy(level, player, pos, blockState, blockEntity, stack);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            blockEntity.setPlacedBy(level, blockState, placer, stack);

        super.setPlacedBy(level, pos, blockState, placer, stack);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
        var result = blockState;

        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            result = blockEntity.playerWillDestroy(level, result, player);

        return super.playerWillDestroy(level, pos, result, player);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction facing, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        var result = blockState;

        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            result = blockEntity.updateShape(result, level, tickAccess, facing, neighborPos, neighborBlockState, random);

        return super.updateShape(result, level, tickAccess, pos, facing, neighborPos, neighborBlockState, random);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            blockEntity.neighborChanged(blockState, level, neighborBlock, orientation, movedByPiston);

        super.neighborChanged(blockState, level, pos, neighborBlock, orientation, movedByPiston);
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {
        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            blockEntity.onPlace(blockState, level, oldBlockState, movedByPiston);

        super.onPlace(blockState, level, pos, oldBlockState, movedByPiston);
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean movedByPiston) {
        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            blockEntity.onRemove(blockState, level, newBlockState, movedByPiston);

        super.onRemove(blockState, level, pos, newBlockState, movedByPiston);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity) {
            var interactionResult = blockEntity.useItemOn(stack, blockState, level, player, hand, result);

            if(interactionResult.consumesAction())
                return interactionResult;
        }

        return super.useItemOn(stack, blockState, level, pos, player, hand, result);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity) {
            var interactionResult = blockEntity.useWithoutItem(blockState, level, player, result);

            if(interactionResult.consumesAction())
                return interactionResult;
        }

        var interactionResult = openMenu(blockState, level, pos, player, result);

        if(interactionResult.consumesAction())
            return interactionResult;

        return super.useWithoutItem(blockState, level, pos, player, result);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        var blockEntitySignal = level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity ? blockEntity.getAnalogOutputSignal(blockState, level) : 0;
        return blockEntitySignal + super.getAnalogOutputSignal(blockState, level, pos);
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity) {
        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            blockEntity.entityInside(blockState, level, entity);

        super.entityInside(blockState, level, pos, entity);
    }

    @Override
    public void handlePrecipitation(BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            blockEntity.handlePrecipitation(blockState, level, precipitation);

        super.handlePrecipitation(blockState, level, pos, precipitation);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            blockEntity.stepOn(level, blockState, entity);

        super.stepOn(level, pos, blockState, entity);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        if(level.getBlockEntity(entity.getOnPos()) instanceof BaseBlockEntity blockEntity && blockEntity.updateEntityMovementAfterFallOn(level, entity))
            return;

        super.updateEntityMovementAfterFallOn(level, entity);
    }

    @MustBeInvokedByOverriders
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState blockState, boolean includeData) {
        var stack = super.getCloneItemStack(level, pos, blockState, includeData);

        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            blockEntity.modifyCloneItemStack(stack, level, includeData);

        return stack;
    }

    @MustBeInvokedByOverriders
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState blockState, boolean includeData, Player player) {
        var stack = super.getCloneItemStack(level, pos, blockState, includeData, player);

        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            blockEntity.modifyCloneItemStack(stack, level, includeData, player);

        return stack;
    }
}
