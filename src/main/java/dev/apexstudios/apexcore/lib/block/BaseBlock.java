package dev.apexstudios.apexcore.lib.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class BaseBlock extends BaseEntityBlock {
    public BaseBlock(Properties properties) {
        super(properties);

        var defaultBlockState = defaultBlockState();

        if(this instanceof FacingBlock facing)
            defaultBlockState = defaultBlockState.setValue(facing.facingProperty(), facing.defaultFacing());
        if(this instanceof FluidLoggedBlock fluidLogged)
            defaultBlockState = defaultBlockState.setValue(fluidLogged.fluidLoggedProperty(), false);
        if(this instanceof MultiBlock multiBlock)
            defaultBlockState = defaultBlockState.setValue(multiBlock.multiBlockPattern().property(), 0);
        if(this instanceof SeatBlock seat)
            defaultBlockState = defaultBlockState.setValue(seat.seatOccupiedProperty(), false);

        registerDefaultState(defaultBlockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        if(this instanceof FacingBlock facing)
            builder.add(facing.facingProperty());
        if(this instanceof FluidLoggedBlock fluidLogged)
            builder.add(fluidLogged.fluidLoggedProperty());
        if(this instanceof MultiBlock multiBlock)
            builder.add(multiBlock.multiBlockPattern().property());
        if(this instanceof SeatBlock seat)
            builder.add(seat.seatOccupiedProperty());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var placementBlockState = defaultBlockState();

        if(this instanceof FacingBlock facing)
            placementBlockState = placementBlockState.setValue(facing.facingProperty(), facing.facingForPlacement(context));
        if(this instanceof FluidLoggedBlock fluidLogged)
            placementBlockState = placementBlockState.setValue(fluidLogged.fluidLoggedProperty(), fluidLogged.isFluidLoggedForPlacement(context));
        if(this instanceof MultiBlock multiBlock && !multiBlock.isPlacementValidForMultiBlock(context, placementBlockState))
            return null;

        return placementBlockState;
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        var result = blockState;

        if(this instanceof FacingBlock facing)
            result = result.setValue(facing.facingProperty(), facing.rotate(rotation, blockState.getValue(facing.facingProperty())));

        return result;
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        var result = blockState;

        if(this instanceof FacingBlock facing)
            result = result.setValue(facing.facingProperty(), facing.mirror(mirror, blockState.getValue(facing.facingProperty())));

        return result;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction neighborDirection, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        if(this instanceof FluidLoggedBlock fluidLogged && blockState.getValue(fluidLogged.fluidLoggedProperty()))
            ticks.scheduleTick(pos, fluidLogged.fluidLoggedFluid(), fluidLogged.fluidLoggedFluid().getTickDelay(level));

        return super.updateShape(blockState, level, ticks, pos, neighborDirection, neighborPos, neighborBlockState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        return this instanceof FluidLoggedBlock fluidLogged && blockState.getValue(fluidLogged.fluidLoggedProperty()) ? fluidLogged.getFluidLoggedState() : super.getFluidState(blockState);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return this instanceof InventoryBlock;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        var inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        return inventory == null ? 0 : ItemHandlerHelper.calcRedstoneFromInventory(inventory);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathType) {
        return !(this instanceof InventoryBlock);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(this instanceof SeatBlock seat) {
            var result = seat.trySit(level, pos, player);

            if(!result.consumesAction())
                result = seat.tryUnsit(level, pos);
            if(result.consumesAction())
                return result;
        }

        var menuProvider = blockState.getMenuProvider(level, pos);

        if(menuProvider != null) {
            if(!level.isClientSide)
                player.openMenu(menuProvider);

            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(blockState, level, pos, player, hitResult);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(blockState, level, pos, movedByPiston);

        if(blockState.hasAnalogOutputSignal())
            Containers.updateNeighboursAfterDestroy(blockState, level, pos);

        MultiBlock.destroyBlocks(level, pos, blockState);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, blockState, placer, stack);
        MultiBlock.setBlocks(level, pos, blockState);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        super.stepOn(level, pos, blockState, entity);

        if(this instanceof SeatBlock seat && !(entity instanceof Player) && entity instanceof LivingEntity living)
            seat.trySit(level, pos, living);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos pos) {
        if(this instanceof MultiBlock multiBlock && !multiBlock.isMultiBlockOrigin(blockState)) {
            var origin = multiBlock.getMultiBlockOrigin(pos, blockState);
            var originBlockState = multiBlock.setMultiBlockIndex(blockState, 0);
            return originBlockState.getMenuProvider(level, origin);
        }

        return super.getMenuProvider(blockState, level, pos);
    }
}
