package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface BlockComponentHolder extends ComponentHolder<
        BlockComponent,
        Block,
        BlockComponentHolder,
        BlockComponentType<? extends BlockComponent, ?>
> {
    @ApiStatus.NonExtendable
    @Nullable
    <TComponent extends BlockComponent> TComponent getComponent(BlockComponentType<TComponent, ?> componentType);

    @ApiStatus.NonExtendable
    default <TComponent extends BlockComponent> Optional<TComponent> findComponent(BlockComponentType<TComponent, ?> componentType) {
        return Optional.ofNullable(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends BlockComponent> TComponent getComponentOrThrow(BlockComponentType<TComponent, ?> componentType) {
        return Objects.requireNonNull(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends BlockComponent> void runForComponent(BlockComponentType<TComponent, ?> componentType, Consumer<TComponent> action) {
        var component = getComponent(componentType);

        if(component != null)
            action.accept(component);
    }

    // region: Legacy
    @ApiStatus.Obsolete
    @Override
    default <TComponent extends BlockComponent> @Nullable TComponent getComponent(ComponentType<BlockComponent, TComponent, Block, BlockComponentHolder, BlockComponentType<? extends BlockComponent, ?>, ?> componentType) {
        return componentType instanceof BlockComponentType<TComponent, ?> blockComponentType ? getComponent(blockComponentType) : null;
    }

    @ApiStatus.Obsolete
    @Override
    default <TComponent extends BlockComponent> Optional<TComponent> findComponent(ComponentType<BlockComponent, TComponent, Block, BlockComponentHolder, BlockComponentType<? extends BlockComponent, ?>, ?> componentType) {
        return ComponentHolder.super.findComponent(componentType);
    }

    @ApiStatus.Obsolete
    @Override
    default <TComponent extends BlockComponent> TComponent getComponentOrThrow(ComponentType<BlockComponent, TComponent, Block, BlockComponentHolder, BlockComponentType<? extends BlockComponent, ?>, ?> componentType) {
        return ComponentHolder.super.getComponentOrThrow(componentType);
    }

    @ApiStatus.Obsolete
    @Override
    default <TComponent extends BlockComponent> void runForComponent(ComponentType<BlockComponent, TComponent, Block, BlockComponentHolder, BlockComponentType<? extends BlockComponent, ?>, ?> componentType, Consumer<TComponent> action) {
        ComponentHolder.super.runForComponent(componentType, action);
    }
    // endregion
}
