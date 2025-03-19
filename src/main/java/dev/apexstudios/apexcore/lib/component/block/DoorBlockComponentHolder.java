package dev.apexstudios.apexcore.lib.component.block;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.types.FacingBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.MultiBlockComponent;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public class DoorBlockComponentHolder extends DoorBlock implements ComponentHolder<BlockComponent>, BucketPickup, LiquidBlockContainer {
    private final Map<ComponentType<BlockComponent, ?, ?>, BlockComponent> components = BlockComponentHelper.registerComponents(this, (holder, registrar) -> {
        holder.registerRequiredComponents(registrar);
        holder.registerComponents(registrar);
    }, DoorBlock.FACING);

    public DoorBlockComponentHolder(BlockSetType type, Properties properties) {
        super(type, properties);

        registerDefaultState(defaultBlockState()
                .setValue(OPEN, false)
                .setValue(HINGE, DoorHingeSide.LEFT)
                .setValue(POWERED, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
        );
    }

    public DoorBlockComponentHolder(Properties properties) {
        super(BlockSetType.OAK, properties);
    }

    protected boolean openMenu(Level level, BlockPos pos, BlockState blockState, Player player) {
        var menuProvider = blockState.getMenuProvider(level, pos);

        if(menuProvider == null)
            return false;

        player.openMenu(menuProvider);
        return true;
    }

    // region: ComponentHolder
    private void registerRequiredComponents(ComponentRegistrar<BlockComponent> registrar) {
        FacingBlockComponent.registerHorizontal(registrar, builder -> builder.facingForPlacement(UseOnContext::getHorizontalDirection));
    }

    @ForOverride
    protected void registerComponents(ComponentRegistrar<BlockComponent> registrar) {

    }

    @Nullable
    @Override
    public final <TComponent extends BlockComponent> TComponent getComponent(ComponentType<BlockComponent, TComponent, ?> componentType) {
        return (TComponent) components.get(componentType);
    }

    @Override
    public final <TComponent extends BlockComponent> Optional<TComponent> findComponent(ComponentType<BlockComponent, TComponent, ?> componentType) {
        return ComponentHolder.super.findComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockComponent> TComponent getComponentOrThrow(ComponentType<BlockComponent, TComponent, ?> componentType) {
        return ComponentHolder.super.getComponentOrThrow(componentType);
    }

    @Override
    public final <TComponent extends BlockComponent> void runForComponent(ComponentType<BlockComponent, TComponent, ?> componentType, Consumer<TComponent> action) {
        ComponentHolder.super.runForComponent(componentType, action);
    }

    @Override
    public final boolean hasComponent(ComponentType<BlockComponent, ?, ?> componentType) {
        return ComponentHolder.super.hasComponent(componentType);
    }

    @Override
    public final Set<ComponentType<BlockComponent, ?, ?>> getComponentTypes() {
        return components.keySet();
    }

    @Override
    public final Collection<BlockComponent> getComponents() {
        return components.values();
    }
    // endregion

    // region: Overrides
    @MustBeInvokedByOverriders
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var blockState = BlockComponentHelper.getStateForPlacement(this, context, defaultBlockState());
        return blockState == null ? null : getDoorBlockStateForPlacement(context, blockState);
    }

    @MustBeInvokedByOverriders
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack stack) {
        BlockComponentHelper.playerDestroy(this, level, player, pos, blockState, stack);
        super.playerDestroy(level, player, pos, blockState, blockEntity, stack);
    }

    @MustBeInvokedByOverriders
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        BlockComponentHelper.setPlacedBy(this, level, pos, blockState, placer, stack);
        // super.setPlacedBy(level, pos, blockState, placer, stack);
    }

    @MustBeInvokedByOverriders
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
        var result = BlockComponentHelper.playerWillDestroy(this, level, pos, blockState, player);
        return super.playerWillDestroy(level, pos, result, player);
    }

    @MustBeInvokedByOverriders
    @Override
    public BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction facing, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        return BlockComponentHelper.updateShape(this, blockState, level, tickAccess, pos, facing, neighborPos, neighborBlockState, random);
    }

    @MustBeInvokedByOverriders
    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        BlockComponentHelper.neighborChanged(this, blockState, level, pos, neighborBlock, orientation, movedByPiston);
        // super.neighborChanged(blockState, level, pos, neighborBlock, orientation, movedByPiston);
        doorNeighborChanged(blockState, level, pos, neighborBlock, orientation, movedByPiston);
    }

    @MustBeInvokedByOverriders
    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {
        BlockComponentHelper.onPlace(this, blockState, level, pos, oldBlockState, movedByPiston);
        super.onPlace(blockState, level, pos, oldBlockState, movedByPiston);
    }

    @MustBeInvokedByOverriders
    @Override
    public void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockComponentHelper.affectNeighborsAfterRemoval(this, blockState, level, pos, movedByPiston);
        super.affectNeighborsAfterRemoval(blockState, level, pos, movedByPiston);
    }

    @MustBeInvokedByOverriders
    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        var interactionResult = BlockComponentHelper.useItemOn(this, stack, blockState, level, pos, player, hand, result);

        if(interactionResult.consumesAction())
            return interactionResult;

        return super.useItemOn(stack, blockState, level, pos, player, hand, result);
    }

    @MustBeInvokedByOverriders
    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        var interactionResult = BlockComponentHelper.useWithoutItem(this, blockState, level, pos, player, result);

        if(interactionResult.consumesAction())
            return interactionResult;

        if(openMenu(level, pos, blockState, player))
            return InteractionResult.SUCCESS;

        // return super.useWithoutItem(blockState, level, pos, player, result);
        return useDoor(blockState, level, pos, player, result);
    }

    @MustBeInvokedByOverriders
    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        var componentSignal = BlockComponentHelper.getAnalogOutputSignal(this, blockState, level, pos);
        return componentSignal + super.getAnalogOutputSignal(blockState, level, pos);
    }

    @MustBeInvokedByOverriders
    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        var hasSignal = BlockComponentHelper.hasAnalogOutputSignal(this, blockState);
        return hasSignal || super.hasAnalogOutputSignal(blockState);
    }

    @MustBeInvokedByOverriders
    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathType) {
        var isPathfindable = BlockComponentHelper.isPathfindable(this, blockState, pathType);
        return super.hasAnalogOutputSignal(blockState) && isPathfindable;
    }

    @MustBeInvokedByOverriders
    @Override
    protected void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockComponentHelper.tick(this, blockState, level, pos, random);
        super.tick(blockState, level, pos, random);
    }

    @MustBeInvokedByOverriders
    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier) {
        BlockComponentHelper.entityInside(this, blockState, level, pos, entity, applier);
        super.entityInside(blockState, level, pos, entity, applier);
    }

    @MustBeInvokedByOverriders
    @Override
    public void handlePrecipitation(BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        BlockComponentHelper.handlePrecipitation(this, blockState, level, pos, precipitation);
        super.handlePrecipitation(blockState, level, pos, precipitation);
    }

    @MustBeInvokedByOverriders
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        BlockComponentHelper.stepOn(this, level, pos, blockState, entity);
        super.stepOn(level, pos, blockState, entity);
    }

    @MustBeInvokedByOverriders
    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        var result = BlockComponentHelper.rotate(this, blockState, rotation);
        return super.rotate(result, rotation);
    }

    @MustBeInvokedByOverriders
    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        var result = BlockComponentHelper.mirror(this, blockState, mirror);
        return super.mirror(result, mirror);
    }

    @MustBeInvokedByOverriders
    @Override
    protected FluidState getFluidState(BlockState blockState) {
        var defaultFluidState = super.getFluidState(blockState);
        return BlockComponentHelper.getFluidState(this, blockState, defaultFluidState);
    }

    @MustBeInvokedByOverriders
    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity player, LevelAccessor level, BlockPos pos, BlockState blockState) {
        var component = getComponent(BlockComponentTypes.FLUID_LOGGED);
        return component == null ? ItemStack.EMPTY : component.pickupBlock(player, level, pos, blockState);
    }

    @MustBeInvokedByOverriders
    @Override
    public Optional<SoundEvent> getPickupSound() {
        var component = getComponent(BlockComponentTypes.FLUID_LOGGED);
        return component == null ? Optional.empty() : component.getPickupSound();
    }

    @MustBeInvokedByOverriders
    @Override
    public Optional<SoundEvent> getPickupSound(BlockState blockState) {
        var component = getComponent(BlockComponentTypes.FLUID_LOGGED);
        return component == null ? Optional.empty() : component.getPickupSound(blockState);
    }

    @MustBeInvokedByOverriders
    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity player, BlockGetter level, BlockPos pos, BlockState blockState, Fluid fluid) {
        var component = getComponent(BlockComponentTypes.FLUID_LOGGED);
        return component != null && component.canPlaceLiquid(player, level, pos, blockState, fluid);
    }

    @MustBeInvokedByOverriders
    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState blockState, FluidState fluidState) {
        var component = getComponent(BlockComponentTypes.FLUID_LOGGED);
        return component != null && component.placeLiquid(level, pos, blockState, fluidState);
    }

    @MustBeInvokedByOverriders
    @Override
    protected void onExplosionHit(BlockState blockState, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
        BlockComponentHelper.onExplosionHit(this, blockState, level, pos, explosion, dropConsumer);
        super.onExplosionHit(blockState, level, pos, explosion, dropConsumer);
        onExplosionHitDoor(blockState, level, pos, explosion);
    }

    @MustBeInvokedByOverriders
    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        if(!BlockComponentHelper.updateEntityMovementAfterFallOn(this, level, entity))
            super.updateEntityMovementAfterFallOn(level, entity);
    }

    @MustBeInvokedByOverriders
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState blockState, boolean includeData) {
        var stack = super.getCloneItemStack(level, pos, blockState, includeData);
        BlockComponentHelper.modifyCloneItemStack(this, stack, level, pos, blockState, includeData);
        return stack;
    }
    // endregion

    // region: NeoForgeExtensions
    @Override
    public final boolean isBed(BlockState blockState, BlockGetter level, BlockPos pos, LivingEntity sleeper) {
        return hasComponent(BlockComponentTypes.BED);
    }

    @Override
    public final void setBedOccupied(BlockState blockState, Level level, BlockPos pos, LivingEntity sleeper, boolean occupied) {
        runForComponent(BlockComponentTypes.BED, component -> component.setOccupied(level, pos, blockState, occupied));
    }

    @Override
    public final Direction getBedDirection(BlockState blockState, LevelReader level, BlockPos pos) {
        var facing = getComponent(BlockComponentTypes.FACING);
        return facing == null ? Direction.NORTH : facing.get(blockState).getOpposite();
    }

    @MustBeInvokedByOverriders
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState blockState, boolean includeData, Player player) {
        var stack = super.getCloneItemStack(level, pos, blockState, includeData);
        BlockComponentHelper.modifyCloneItemStack(this, stack, level, pos, blockState, includeData, player);
        return stack;
    }
    // endregion

    // region: DoorBlock
    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        var facing = getComponentOrThrow(BlockComponentTypes.FACING).get(blockState);
        var open = !blockState.getValue(OPEN);
        var rightSided = blockState.getValue(HINGE) == DoorHingeSide.RIGHT;
        var dir = open ? (rightSided ? facing.getCounterClockWise() : facing.getClockWise()) : facing;
        return SHAPES.get(dir);
    }

    private void onExplosionHitDoor(BlockState blockState, ServerLevel level, BlockPos pos, Explosion explosion) {
        var isOrigin = true;
        var multiBlock = getComponent(BlockComponentTypes.MULTI_BLOCK);

        if(multiBlock != null)
            isOrigin = multiBlock.indexOf(blockState) == MultiBlockComponent.ORIGIN_INDEX;

        if (explosion.canTriggerBlocks() && isOrigin && type().canOpenByWindCharge() && !blockState.getValue(POWERED))
            setOpen(null, level, blockState, pos, !isOpen(blockState));
    }

    private BlockState getDoorBlockStateForPlacement(BlockPlaceContext context, BlockState blockState) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var hasPower = hasPower(level, pos, blockState);
        var facing = getComponentOrThrow(BlockComponentTypes.FACING).get(blockState);
        var half = getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK).indexOf(blockState) == MultiBlockComponent.ORIGIN_INDEX ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;

        return blockState
                .setValue(HINGE, getHinge(context, facing))
                .setValue(POWERED, hasPower)
                .setValue(OPEN, hasPower)
                .setValue(HALF, half);
    }

    private InteractionResult useDoor(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        if(!type().canOpenByHand())
            return InteractionResult.PASS;

        setOpen(player, level, blockState, pos, !isOpen(blockState));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setOpen(@Nullable Entity entity, Level level, BlockState blockState, BlockPos pos, boolean open) {
        super.setOpen(entity, level, blockState, pos, open);

        runForComponent(BlockComponentTypes.MULTI_BLOCK, multiBlock -> {
            var index = multiBlock.indexOf(blockState);
            var origin = multiBlock.getOrigin(pos, blockState);

            for(var i = 0; i < multiBlock.size(); i++) {
                if(i == index)
                    continue;

                var otherBlockState = multiBlock.withIndex(blockState, i);
                var otherPos = multiBlock.getPos(origin, otherBlockState);

                if(isOpen(otherBlockState) != open) {
                    level.setBlock(otherPos, otherBlockState.setValue(OPEN, open), 10);
                    // playSound(entity, level, otherPos, open);
                    // level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, otherPos);
                }
            }
        });
    }

    private void doorNeighborChanged(BlockState blockState, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        var hasPower = hasPower(level, pos, blockState);

        if(hasPower != blockState.getValue(POWERED)) {
            if(hasPower != isOpen(blockState)) {
                playSound(null, level, pos, hasPower);
                level.gameEvent(null, hasPower ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }

            level.setBlock(pos, blockState.setValue(POWERED, hasPower).setValue(OPEN, hasPower), Block.UPDATE_CLIENTS);

            runForComponent(BlockComponentTypes.MULTI_BLOCK, multiBlock -> {
                var index = multiBlock.indexOf(blockState);
                var origin = multiBlock.getOrigin(pos, blockState);

                for(var i = 0; i < multiBlock.size(); i++) {
                    if(i == index)
                        continue;

                    var otherBlockState = multiBlock.withIndex(blockState, i);
                    var otherPos = multiBlock.getPos(origin, otherBlockState);

                    if(isOpen(otherBlockState) != hasPower)
                        level.setBlock(otherPos, otherBlockState.setValue(POWERED, hasPower).setValue(OPEN, hasPower), 2);
                }
            });
        }
    }

    private boolean hasPower(SignalGetter level, BlockPos pos, BlockState blockState) {
        var multiBlock = getComponent(BlockComponentTypes.MULTI_BLOCK);

        if(multiBlock != null) {
            var index = multiBlock.indexOf(blockState);
            var origin = multiBlock.getOrigin(pos, blockState);

            for(var i = 0; i < multiBlock.size(); i++) {
                if(i == index)
                    continue;

                var otherBlockState = multiBlock.withIndex(blockState, i);
                var otherPos = multiBlock.getPos(origin, otherBlockState);

                if(level.hasNeighborSignal(otherPos))
                    return true;
            }
        }

        return level.hasNeighborSignal(pos);
    }

    private void playSound(@Nullable Entity source, Level level, BlockPos pos, boolean isOpening) {
        level.playSound(source, pos, isOpening ? type().doorOpen() : type().doorClose(), SoundSource.BLOCKS, 1F, level.random.nextFloat() * .1F + .9F);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        return Mth.getSeed(pos);
    }

    public static DoorHingeSide getHinge(BlockPlaceContext context, Direction facing) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var facingCCW = facing.getCounterClockWise();
        var posCCW = pos.relative(facingCCW);
        var blockStateCCW = level.getBlockState(posCCW);
        var facingCW = facing.getClockWise();
        var posCW = pos.relative(facingCW);
        var blockStateCW = level.getBlockState(posCW);
        var i = (blockStateCCW.isCollisionShapeFullBlock(level, posCCW) ? -1 : 0) + (blockStateCW.isCollisionShapeFullBlock(level, posCW) ? 1 : 0);
        var isDoorCCW = isDoorOrigin(blockStateCCW);
        var isDoorCW = isDoorOrigin(blockStateCW);

        if((!isDoorCCW || isDoorCW) && i <= 0) {
            if((!isDoorCW || isDoorCCW) && i >= 0) {
                var stepX = facing.getStepX();
                var stepZ = facing.getStepZ();
                var clickLocation = context.getClickLocation();
                var clickX = clickLocation.x - (double) pos.getX();
                var clickZ = clickLocation.z - (double) pos.getZ();

                return (stepX >= 0D || clickZ >= .5D) &&
                        (stepX <= 0D || clickZ <= .5D) &&
                        (stepZ >= 0D || clickX <= .5D) &&
                        (stepZ <= 0D || clickX >= .5D) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            }

            return DoorHingeSide.LEFT;
        }

        return DoorHingeSide.RIGHT;
    }

    private static boolean isDoorOrigin(BlockState blockState) {
        var multiBlock = BlockComponentHelper.getComponent(blockState, BlockComponentTypes.MULTI_BLOCK);

        if(multiBlock == null)
            return true;

        return multiBlock.indexOf(blockState) == MultiBlockComponent.ORIGIN_INDEX;
    }
    // endregion
}
