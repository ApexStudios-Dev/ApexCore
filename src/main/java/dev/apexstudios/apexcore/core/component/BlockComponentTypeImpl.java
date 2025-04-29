package dev.apexstudios.apexcore.core.component;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHolder;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentType;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public final class BlockComponentTypeImpl<
        TComponent extends BlockComponent,
        TBuilder
> extends ComponentTypeImpl<
        BlockComponent,
        TComponent,
        Block,
        BlockComponentHolder,
        BlockComponentType<? extends BlockComponent, ?>,
        TBuilder
> implements BlockComponentType<TComponent, TBuilder> {
    private static final Map<ResourceLocation, BlockComponentType<? extends BlockComponent, ?>> REGISTRY = Maps.newConcurrentMap();

    private BlockComponentTypeImpl(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<BlockComponentHolder, TBuilder, TComponent> componentFactory) {
        super(registryName, builderFactory, componentFactory);
    }

    public static <TComponent extends BlockComponent, TBuilder> BlockComponentType<TComponent, TBuilder> register(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<BlockComponentHolder, TBuilder, TComponent> componentFactory) {
        var componentType = new BlockComponentTypeImpl<>(registryName, builderFactory, componentFactory);

        if(REGISTRY.putIfAbsent(registryName, componentType) != null)
            throw new IllegalStateException("Duplicate BlockComponent registration: " + registryName);

        return componentType;
    }
}
