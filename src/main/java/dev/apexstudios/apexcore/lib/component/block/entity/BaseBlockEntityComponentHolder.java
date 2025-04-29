package dev.apexstudios.apexcore.lib.component.block.entity;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.block.entity.BaseBlockEntity;
import dev.apexstudios.apexcore.lib.component.ComponentHelper;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LockBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LootTableBlockEntityComponent;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

public class BaseBlockEntityComponentHolder extends BaseBlockEntity implements BlockEntityComponentHolder, Nameable, MenuProvider {
    private final Map<BlockEntityComponentType<? extends BlockEntityComponent, ?>, BlockEntityComponent> components = ComponentHelper.registerComponents(this, BlockEntityComponentRegistrar::new, this::registerComponents);

    protected BaseBlockEntityComponentHolder(BlockEntityType<? extends BaseBlockEntityComponentHolder> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Nullable
    protected AbstractContainerMenu createMenu(int windowId, Player player) {
        return null;
    }

    // region: ComponentHolder
    @ForOverride
    protected void registerComponents(BlockEntityComponentRegistrar registrar) {

    }

    @Override
    public final <TComponent extends BlockEntityComponent> @Nullable TComponent getComponent(BlockEntityComponentType<TComponent, ?> componentType) {
        return (TComponent) components.get(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> Optional<TComponent> findComponent(BlockEntityComponentType<TComponent, ?> componentType) {
        return BlockEntityComponentHolder.super.findComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> TComponent getComponentOrThrow(BlockEntityComponentType<TComponent, ?> componentType) {
        return BlockEntityComponentHolder.super.getComponentOrThrow(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> void runForComponent(BlockEntityComponentType<TComponent, ?> componentType, Consumer<TComponent> action) {
        BlockEntityComponentHolder.super.runForComponent(componentType, action);
    }

    @Nullable
    @Override
    public final <TComponent extends BlockEntityComponent> TComponent getComponent(ComponentType<BlockEntityComponent, TComponent, BlockEntity, BlockEntityComponentHolder, BlockEntityComponentType<? extends BlockEntityComponent, ?>, ?> componentType) {
        return BlockEntityComponentHolder.super.getComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> Optional<TComponent> findComponent(ComponentType<BlockEntityComponent, TComponent, BlockEntity, BlockEntityComponentHolder, BlockEntityComponentType<? extends BlockEntityComponent, ?>, ?> componentType) {
        return BlockEntityComponentHolder.super.findComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> TComponent getComponentOrThrow(ComponentType<BlockEntityComponent, TComponent, BlockEntity, BlockEntityComponentHolder, BlockEntityComponentType<? extends BlockEntityComponent, ?>, ?> componentType) {
        return BlockEntityComponentHolder.super.getComponentOrThrow(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> void runForComponent(ComponentType<BlockEntityComponent, TComponent, BlockEntity, BlockEntityComponentHolder, BlockEntityComponentType<? extends BlockEntityComponent, ?>, ?> componentType, Consumer<TComponent> action) {
        BlockEntityComponentHolder.super.runForComponent(componentType, action);
    }

    @Override
    public final boolean hasComponent(BlockEntityComponentType<? extends BlockEntityComponent, ?> componentType) {
        return components.containsKey(componentType);
    }

    @Override
    public final Set<BlockEntityComponentType<? extends BlockEntityComponent, ?>> getComponentTypes() {
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
    public BlockState playerWillDestroy(Level level, BlockState blockState, Player player) {
        var result = BlockEntityComponentHelper.playerWillDestroy(this, level, worldPosition, blockState, player);
        return super.playerWillDestroy(level, result, player);
    }

    @MustBeInvokedByOverriders
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        BlockEntityComponentHelper.preRemoveSideEffects(this, pos, blockState);
        super.preRemoveSideEffects(pos, blockState);
    }

    @MustBeInvokedByOverriders
    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level) {
        var componentSignal = BlockEntityComponentHelper.getAnalogOutputSignal(this, blockState, level, worldPosition);
        return Math.max(super.getAnalogOutputSignal(blockState, level), componentSignal);
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
