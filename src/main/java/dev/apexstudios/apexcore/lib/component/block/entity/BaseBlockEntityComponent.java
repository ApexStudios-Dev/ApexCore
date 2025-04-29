package dev.apexstudios.apexcore.lib.component.block.entity;

import dev.apexstudios.apexcore.lib.component.BaseComponent;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class BaseBlockEntityComponent extends BaseComponent<
        BlockEntityComponent,
        BlockEntity,
        BlockEntityComponentHolder,
        BlockEntityComponentType<? extends BlockEntityComponent, ?>
> implements BlockEntityComponent {
    protected BaseBlockEntityComponent(BlockEntityComponentHolder holder) {
        super(holder);
    }

    @Nullable
    @Override
    public final <TComponent extends BlockEntityComponent> TComponent getComponent(BlockEntityComponentType<TComponent, ?> componentType) {
        return holder.getComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> Optional<TComponent> findComponent(BlockEntityComponentType<TComponent, ?> componentType) {
        return holder.findComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> TComponent getComponentOrThrow(BlockEntityComponentType<TComponent, ?> componentType) {
        return holder.getComponentOrThrow(componentType);
    }

    @Override
    public final <TComponent extends BlockEntityComponent> void runForComponent(BlockEntityComponentType<TComponent, ?> componentType, Consumer<TComponent> action) {
        holder.runForComponent(componentType, action);
    }
}
