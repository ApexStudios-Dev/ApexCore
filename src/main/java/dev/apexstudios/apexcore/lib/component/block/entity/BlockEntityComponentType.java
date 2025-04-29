package dev.apexstudios.apexcore.lib.component.block.entity;

import dev.apexstudios.apexcore.core.component.BlockEntityComponentTypeImpl;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BlockEntityComponentType<
        TComponent extends BlockEntityComponent,
        TBuilder
> extends ComponentType<
        BlockEntityComponent,
        TComponent,
        BlockEntity,
        BlockEntityComponentHolder,
        BlockEntityComponentType<? extends BlockEntityComponent, ?>,
        TBuilder
> {
    static <TComponent extends BlockEntityComponent, TBuilder> BlockEntityComponentType<TComponent, TBuilder> register(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<BlockEntityComponentHolder, TBuilder, TComponent> componentFactory) {
        return BlockEntityComponentTypeImpl.register(registryName, builderFactory, componentFactory);
    }

    static <TComponent extends BlockEntityComponent> BlockEntityComponentType<TComponent, Object> register(ResourceLocation registryName, Function<BlockEntityComponentHolder, TComponent> componentFactory) {
        return register(registryName, DUMMY_BUILDER, (holder, builder) -> componentFactory.apply(holder));
    }
}
