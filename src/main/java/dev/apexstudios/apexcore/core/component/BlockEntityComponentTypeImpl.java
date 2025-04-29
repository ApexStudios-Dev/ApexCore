package dev.apexstudios.apexcore.core.component;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponentHolder;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponentType;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BlockEntityComponentTypeImpl<
        TComponent extends BlockEntityComponent,
        TBuilder
> extends ComponentTypeImpl<
        BlockEntityComponent,
        TComponent,
        BlockEntity,
        BlockEntityComponentHolder,
        BlockEntityComponentType<? extends BlockEntityComponent, ?>,
        TBuilder
> implements BlockEntityComponentType<TComponent, TBuilder> {
    private static final Map<ResourceLocation, BlockEntityComponentType<? extends BlockEntityComponent, ?>> REGISTRY = Maps.newConcurrentMap();

    private BlockEntityComponentTypeImpl(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<BlockEntityComponentHolder, TBuilder, TComponent> componentFactory) {
        super(registryName, builderFactory, componentFactory);
    }

    public static <TComponent extends BlockEntityComponent, TBuilder> BlockEntityComponentType<TComponent, TBuilder> register(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<BlockEntityComponentHolder, TBuilder, TComponent> componentFactory) {
        var componentType = new BlockEntityComponentTypeImpl<>(registryName, builderFactory, componentFactory);

        if(REGISTRY.putIfAbsent(registryName, componentType) != null)
            throw new IllegalStateException("Duplicate BlockEntityComponent registration: " + registryName);

        return componentType;
    }
}
