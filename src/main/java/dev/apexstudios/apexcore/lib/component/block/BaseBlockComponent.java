package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.lib.component.BaseComponent;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class BaseBlockComponent extends BaseComponent<BlockComponent, Block, BlockComponentHolder, BlockComponentType<? extends BlockComponent, ?>> implements BlockComponent {
    protected BaseBlockComponent(BlockComponentHolder holder) {
        super(holder);
    }

    @Nullable
    @Override
    public final <TComponent extends BlockComponent> TComponent getComponent(BlockComponentType<TComponent, ?> componentType) {
        return holder.getComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockComponent> Optional<TComponent> findComponent(BlockComponentType<TComponent, ?> componentType) {
        return holder.findComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockComponent> TComponent getComponentOrThrow(BlockComponentType<TComponent, ?> componentType) {
        return holder.getComponentOrThrow(componentType);
    }

    @Override
    public final <TComponent extends BlockComponent> void runForComponent(BlockComponentType<TComponent, ?> componentType, Consumer<TComponent> action) {
        holder.runForComponent(componentType, action);
    }
}
