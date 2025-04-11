package dev.apexstudios.apexcore.lib.component.block;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.block.BaseEntityBlock;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public abstract class BaseEntityBlockComponentHolder extends BaseEntityBlock implements ComponentHolder<BlockComponent>, BucketPickup, LiquidBlockContainer {
    private final Map<ComponentType<BlockComponent, ?, ?>, BlockComponent> components = BlockComponentHelper.registerComponents(this, BaseEntityBlockComponentHolder::registerComponents);

    protected BaseEntityBlockComponentHolder(Properties properties) {
        super(properties);
    }

    protected boolean openMenu(Level level, BlockPos pos, BlockState blockState, Player player) {
        var menuProvider = blockState.getMenuProvider(level, pos);

        if(menuProvider == null)
            return false;

        player.openMenu(menuProvider);
        return true;
    }

    // region: ComponentHolder
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
        return BlockComponentHelper.getStateForPlacement(this, context, defaultBlockState());
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
        super.setPlacedBy(level, pos, blockState, placer, stack);
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
        var result = BlockComponentHelper.updateShape(this, blockState, level, tickAccess, pos, facing, neighborPos, neighborBlockState, random);
        return super.updateShape(result, level, tickAccess, pos, facing, neighborPos, neighborBlockState, random);
    }

    @MustBeInvokedByOverriders
    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        BlockComponentHelper.neighborChanged(this, blockState, level, pos, neighborBlock, orientation, movedByPiston);
        super.neighborChanged(blockState, level, pos, neighborBlock, orientation, movedByPiston);
    }

    @MustBeInvokedByOverriders
    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {
        BlockComponentHelper.onPlace(this, blockState, level, pos, oldBlockState, movedByPiston);
        super.onPlace(blockState, level, pos, oldBlockState, movedByPiston);
    }

    @MustBeInvokedByOverriders
    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean movedByPiston) {
        BlockComponentHelper.onRemove(this, blockState, level, pos, newBlockState, movedByPiston);
        super.onRemove(blockState, level, pos, newBlockState, movedByPiston);
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

        return super.useWithoutItem(blockState, level, pos, player, result);
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
    public void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity) {
        BlockComponentHelper.entityInside(this, blockState, level, pos, entity);
        super.entityInside(blockState, level, pos, entity);
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
    public ItemStack pickupBlock(@Nullable Player player, LevelAccessor level, BlockPos pos, BlockState blockState) {
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
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState blockState, Fluid fluid) {
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

    @MustBeInvokedByOverriders
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState blockState, boolean includeData, Player player) {
        var stack = super.getCloneItemStack(level, pos, blockState, includeData);
        BlockComponentHelper.modifyCloneItemStack(this, stack, level, pos, blockState, includeData, player);
        return stack;
    }

    @MustBeInvokedByOverriders
    @Override
    public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState blockState, EntityType<?> entityType, LevelReader level, BlockPos pos, float orientation) {
        return BlockComponentHelper.getRespawnPosition(this, blockState, entityType, level, pos, orientation).or(() -> super.getRespawnPosition(blockState, entityType, level, pos, orientation));
    }
    // endregion
}
