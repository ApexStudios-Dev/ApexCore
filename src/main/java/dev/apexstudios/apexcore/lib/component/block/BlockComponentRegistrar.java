package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import java.util.function.UnaryOperator;
import net.minecraft.world.level.block.Block;

public final class BlockComponentRegistrar extends ComponentRegistrar<
        BlockComponent,
        Block,
        BlockComponentHolder,
        BlockComponentType<? extends BlockComponent, ?>,
        BlockComponentRegistrar
> {
    public <TComponent extends BlockComponent, TBuilder> BlockComponentRegistrar register(BlockComponentType<TComponent, TBuilder> componentType, UnaryOperator<TBuilder> builder) {
        return super.register(componentType, builder);
    }
}
