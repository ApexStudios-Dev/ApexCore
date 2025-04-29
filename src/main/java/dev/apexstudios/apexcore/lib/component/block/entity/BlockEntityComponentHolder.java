package dev.apexstudios.apexcore.lib.component.block.entity;

import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface BlockEntityComponentHolder extends ComponentHolder<
        BlockEntityComponent,
        BlockEntity,
        BlockEntityComponentHolder,
        BlockEntityComponentType<? extends BlockEntityComponent, ?>
> {
    @ApiStatus.NonExtendable
    @Nullable
    <TComponent extends BlockEntityComponent> TComponent getComponent(BlockEntityComponentType<TComponent, ?> componentType);

    @ApiStatus.NonExtendable
    default <TComponent extends BlockEntityComponent> Optional<TComponent> findComponent(BlockEntityComponentType<TComponent, ?> componentType) {
        return Optional.ofNullable(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends BlockEntityComponent> TComponent getComponentOrThrow(BlockEntityComponentType<TComponent, ?> componentType) {
        return Objects.requireNonNull(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends BlockEntityComponent> void runForComponent(BlockEntityComponentType<TComponent, ?> componentType, Consumer<TComponent> action) {
        var component = getComponent(componentType);

        if(component != null)
            action.accept(component);
    }

    // region: Legacy
    @ApiStatus.Obsolete
    @Override
    default <TComponent extends BlockEntityComponent> @Nullable TComponent getComponent(ComponentType<BlockEntityComponent, TComponent, BlockEntity, BlockEntityComponentHolder, BlockEntityComponentType<? extends BlockEntityComponent, ?>, ?> componentType) {
        return componentType instanceof BlockEntityComponentType<TComponent, ?> blockEntityComponentType ? getComponent(blockEntityComponentType) : null;
    }

    @ApiStatus.Obsolete
    @Override
    default <TComponent extends BlockEntityComponent> Optional<TComponent> findComponent(ComponentType<BlockEntityComponent, TComponent, BlockEntity, BlockEntityComponentHolder, BlockEntityComponentType<? extends BlockEntityComponent, ?>, ?> componentType) {
        return ComponentHolder.super.findComponent(componentType);
    }

    @ApiStatus.Obsolete
    @Override
    default <TComponent extends BlockEntityComponent> TComponent getComponentOrThrow(ComponentType<BlockEntityComponent, TComponent, BlockEntity, BlockEntityComponentHolder, BlockEntityComponentType<? extends BlockEntityComponent, ?>, ?> componentType) {
        return ComponentHolder.super.getComponentOrThrow(componentType);
    }

    @ApiStatus.Obsolete
    @Override
    default <TComponent extends BlockEntityComponent> void runForComponent(ComponentType<BlockEntityComponent, TComponent, BlockEntity, BlockEntityComponentHolder, BlockEntityComponentType<? extends BlockEntityComponent, ?>, ?> componentType, Consumer<TComponent> action) {
        ComponentHolder.super.runForComponent(componentType, action);
    }
    // endregion
}
