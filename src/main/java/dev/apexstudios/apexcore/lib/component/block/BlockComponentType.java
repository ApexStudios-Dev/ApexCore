package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.core.component.BlockComponentTypeImpl;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BlockComponentType<TComponent extends BlockComponent, TBuilder> extends ComponentType<
        BlockComponent,
        TComponent,
        Block,
        BlockComponentHolder,
        BlockComponentType<? extends BlockComponent, ?>,
        TBuilder
> {
    static <TComponent extends BlockComponent, TBuilder> BlockComponentType<TComponent, TBuilder> register(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<BlockComponentHolder, TBuilder, TComponent> componentFactory) {
        return BlockComponentTypeImpl.register(registryName, builderFactory, componentFactory);
    }

    static <TComponent extends BlockComponent> BlockComponentType<TComponent, Object> register(ResourceLocation registryName, Function<BlockComponentHolder, TComponent> componentFactory) {
        return register(registryName, DUMMY_BUILDER, (holder, builder) -> componentFactory.apply(holder));
    }
}
