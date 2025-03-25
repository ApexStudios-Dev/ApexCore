package dev.apexstudios.apexcore.lib.component.block.entity;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.block.entity.BaseBlockEntity;
import dev.apexstudios.apexcore.lib.component.ComponentHelper;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LockBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LootTableBlockEntityComponent;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

public class BaseBlockEntityComponentHolder extends BaseBlockEntity implements ComponentHolder<BlockEntityComponent, BlockEntity>, Nameable, MenuProvider {
    private final Map<ComponentType<BlockEntityComponent, ?, BlockEntity, ?>, BlockEntityComponent> components = ComponentHelper.registerComponents(this, BaseBlockEntityComponentHolder::registerComponents);

    protected BaseBlockEntityComponentHolder(BlockEntityType<? extends BaseBlockEntityComponentHolder> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Nullable
    protected AbstractContainerMenu createMenu(int windowId, Player player) {
        return null;
    }

    // region: ComponentHolder
    @ForOverride
    protected void registerComponents(ComponentRegistrar<BlockEntityComponent, BlockEntity> registrar) {

    }

    @Nullable
    @Override
    public final <TComponent extends BlockEntityComponent> TComponent getComponent(ComponentType<BlockEntityComponent, TComponent, BlockEntity, ?> componentType) {
        return (TComponent) components.get(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> Optional<TComponent> findComponent(ComponentType<BlockEntityComponent, TComponent, BlockEntity, ?> componentType) {
        return ComponentHolder.super.findComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> TComponent getComponentOrThrow(ComponentType<BlockEntityComponent, TComponent, BlockEntity, ?> componentType) {
        return ComponentHolder.super.getComponentOrThrow(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> void runForComponent(ComponentType<BlockEntityComponent, TComponent, BlockEntity, ?> componentType, Consumer<TComponent> action) {
        ComponentHolder.super.runForComponent(componentType, action);
    }

    @Override
    public final boolean hasComponent(ComponentType<BlockEntityComponent, ?, BlockEntity, ?> componentType) {
        return ComponentHolder.super.hasComponent(componentType);
    }

    @Override
    public final Set<ComponentType<BlockEntityComponent, ?, BlockEntity, ?>> getComponentTypes() {
        return components.keySet();
    }

    @Override
    public final Collection<BlockEntityComponent> getComponents() {
        return components.values();
    }

    @Override
    public final BlockEntity unwrap() {
        return this;
    }
    // endregion

    // region: Internal
    @MustBeInvokedByOverriders
    @Override
    public void playerDestroy(Level level, Player player, BlockState blockState, ItemStack stack) {
        BlockEntityComponentHelper.playerDestroy(this, level, player, worldPosition, blockState, stack);
        super.playerDestroy(level, player, blockState, stack);
    }

    @MustBeInvokedByOverriders
    @Override
    public void setPlacedBy(Level level, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntityComponentHelper.setPlacedBy(this, level, worldPosition, blockState, placer, stack);
        super.setPlacedBy(level, blockState, placer, stack);
    }

    @MustBeInvokedByOverriders
    @Override
    public BlockState playerWillDestroy(Level level, BlockState blockState, Player player) {
        var result = BlockEntityComponentHelper.playerWillDestroy(this, level, worldPosition, blockState, player);
        return super.playerWillDestroy(level, result, player);
    }

    @MustBeInvokedByOverriders
    @Override
    public BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, Direction facing, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        var result = BlockEntityComponentHelper.updateShape(this, blockState, level, tickAccess, worldPosition, facing, neighborPos, neighborBlockState, random);
        return super.updateShape(result, level, tickAccess, facing, neighborPos, neighborBlockState, random);
    }

    @MustBeInvokedByOverriders
    @Override
    public void neighborChanged(BlockState blockState, Level level, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        BlockEntityComponentHelper.neighborChanged(this, blockState, level, worldPosition, neighborBlock, orientation, movedByPiston);
        super.neighborChanged(blockState, level, neighborBlock, orientation, movedByPiston);
    }

    @MustBeInvokedByOverriders
    @Override
    public void onPlace(BlockState blockState, Level level, BlockState oldBlockState, boolean movedByPiston) {
        BlockEntityComponentHelper.onPlace(this, blockState, level, worldPosition, oldBlockState, movedByPiston);
        super.onPlace(blockState, level, oldBlockState, movedByPiston);
    }

    @MustBeInvokedByOverriders
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        BlockEntityComponentHelper.preRemoveSideEffects(this, pos, blockState);
        super.preRemoveSideEffects(pos, blockState);
    }

    @MustBeInvokedByOverriders
    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, Player player, InteractionHand hand, BlockHitResult result) {
        var interactionResult = BlockEntityComponentHelper.useItemOn(this, stack, blockState, level, worldPosition, player, hand, result);

        if(interactionResult.consumesAction())
            return interactionResult;

        return super.useItemOn(stack, blockState, level, player, hand, result);
    }

    @MustBeInvokedByOverriders
    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, Player player, BlockHitResult result) {
        var interactionResult = BlockEntityComponentHelper.useWithoutItem(this, blockState, level, worldPosition, player, result);

        if(interactionResult.consumesAction())
            return interactionResult;

        return super.useWithoutItem(blockState, level, player, result);
    }

    @MustBeInvokedByOverriders
    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level) {
        var componentSignal = BlockEntityComponentHelper.getAnalogOutputSignal(this, blockState, level, worldPosition);
        return Math.max(super.getAnalogOutputSignal(blockState, level), componentSignal);
    }

    @MustBeInvokedByOverriders
    @Override
    public void stepOn(Level level, BlockState blockState, Entity entity) {
        BlockEntityComponentHelper.stepOn(this, level, worldPosition, blockState, entity);
        super.stepOn(level, blockState, entity);
    }

    @MustBeInvokedByOverriders
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        BlockEntityComponentHelper.saveAdditional(this, tag, registries);
        super.saveAdditional(tag, registries);
    }

    @MustBeInvokedByOverriders
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        BlockEntityComponentHelper.loadAdditional(this, tag, registries);
        super.loadAdditional(tag, registries);
    }

    @MustBeInvokedByOverriders
    @Override
    public boolean triggerEvent(int id, int event) {
        if(BlockEntityComponentHelper.triggerEvent(this, id, event))
            return true;

        return super.triggerEvent(id, event);
    }

    @MustBeInvokedByOverriders
    @Override
    protected void applyImplicitComponents(DataComponentGetter getter) {
        BlockEntityComponentHelper.applyImplicitComponents(this, getter);
        super.applyImplicitComponents(getter);
    }

    @MustBeInvokedByOverriders
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        BlockEntityComponentHelper.collectImplicitComponents(this, components);
        super.collectImplicitComponents(components);
    }

    @MustBeInvokedByOverriders
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        BlockEntityComponentHelper.removeComponentsFromTag(this, tag);
        super.removeComponentsFromTag(tag);
    }

    @MustBeInvokedByOverriders
    @Override
    public void entityInside(BlockState blockState, Level level, Entity entity, InsideBlockEffectApplier applier) {
        BlockEntityComponentHelper.entityInside(this, blockState, level, worldPosition, entity, applier);
    }

    @MustBeInvokedByOverriders
    @Override
    public void handlePrecipitation(BlockState blockState, Level level, Biome.Precipitation precipitation) {
        BlockEntityComponentHelper.handlePrecipitation(this, blockState, level, worldPosition, precipitation);
    }

    @MustBeInvokedByOverriders
    @Override
    public boolean updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        return BlockEntityComponentHelper.updateEntityMovementAfterFallOn(this, level, entity);
    }

    @MustBeInvokedByOverriders
    @Override
    public void modifyCloneItemStack(ItemStack stack, LevelReader level, boolean includeData) {
        super.modifyCloneItemStack(stack, level, includeData);
        BlockEntityComponentHelper.modifyCloneItemStack(this, stack, level, worldPosition, getBlockState(), includeData);
    }

    @MustBeInvokedByOverriders
    @Override
    public void modifyCloneItemStack(ItemStack stack, LevelReader level, boolean includeData, Player player) {
        super.modifyCloneItemStack(stack, level, includeData, player);
        BlockEntityComponentHelper.modifyCloneItemStack(this, stack, level, worldPosition, getBlockState(), includeData, player);
    }
    // endregion

    // region: Vanilla Wrappers
    @ApiStatus.Obsolete
    @Nullable
    @Override
    public final AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        if(LockBlockEntityComponent.isLocked(this, player))
            return null;

        var menu = createMenu(windowId, player);

        if(menu == null)
            return null;

        LootTableBlockEntityComponent.unpack(this, player);
        return menu;
    }

    @ApiStatus.Obsolete
    @Override
    public final Component getName() {
        var component = getComponent(BlockEntityComponentTypes.NAMEABLE);
        return component == null ? getBlockState().getBlock().getName() : component.getName();
    }

    @ApiStatus.Obsolete
    @Override
    public final boolean hasCustomName() {
        var component = getComponent(BlockEntityComponentTypes.NAMEABLE);
        return component != null && component.hasCustomName();
    }

    @ApiStatus.Obsolete
    @Override
    public final Component getDisplayName() {
        var component = getComponent(BlockEntityComponentTypes.NAMEABLE);
        return component == null ? getBlockState().getBlock().getName() : component.getDisplayName();
    }

    @ApiStatus.Obsolete
    @Nullable
    @Override
    public final Component getCustomName() {
        var component = getComponent(BlockEntityComponentTypes.NAMEABLE);
        return component == null ? null : component.getCustomName();
    }
    // endregion
}
