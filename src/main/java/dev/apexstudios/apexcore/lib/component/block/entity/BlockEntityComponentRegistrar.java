package dev.apexstudios.apexcore.lib.component.block.entity;

import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import java.util.function.UnaryOperator;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BlockEntityComponentRegistrar extends ComponentRegistrar<
        BlockEntityComponent,
        BlockEntity,
        BlockEntityComponentHolder,
        BlockEntityComponentType<? extends BlockEntityComponent, ?>,
        BlockEntityComponentRegistrar
> {
    public <TComponent extends BlockEntityComponent, TBuilder> BlockEntityComponentRegistrar register(BlockEntityComponentType<TComponent, TBuilder> componentType, UnaryOperator<TBuilder> builder) {
        return super.register(componentType, builder);
    }
}
