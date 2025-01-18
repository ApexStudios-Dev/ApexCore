package dev.apexstudios.apexcore.lib.component;

import dev.apexstudios.apexcore.core.component.ComponentTypeImpl;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponent;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ComponentType<TBase extends Component<TBase>, TComponent extends TBase, TBuilder extends ComponentBuilder> {
    ResourceLocation registryName();

    @ApiStatus.Internal
    TComponent newInstance(ComponentHolder<TBase> holder, Consumer<TBuilder> builder);

    static <TBase extends Component<TBase>, TComponent extends TBase, TBuilder extends ComponentBuilder> ComponentType<TBase, TComponent, TBuilder> register(Class<TBase> baseType, ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<ComponentHolder<TBase>, TBuilder, TComponent> componentFactory) {
        return ComponentTypeImpl.register(baseType, registryName, builderFactory, componentFactory);
    }

    static <TComponent extends BlockComponent, TBuilder extends ComponentBuilder> ComponentType<BlockComponent, TComponent, TBuilder> registerBlock(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<ComponentHolder<BlockComponent>, TBuilder, TComponent> componentFactory) {
        return register(BlockComponent.class, registryName, builderFactory, componentFactory);
    }

    static <TComponent extends BlockComponent> ComponentType<BlockComponent, TComponent, ComponentBuilder> registerBlock(ResourceLocation registryName, Function<ComponentHolder<BlockComponent>, TComponent> componentFactory) {
        return registerBlock(registryName, ComponentBuilder.NOOP, (holder, builder) -> componentFactory.apply(holder));
    }

    static <TComponent extends BlockEntityComponent, TBuilder extends ComponentBuilder> ComponentType<BlockEntityComponent, TComponent, TBuilder> registerBlockEntity(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<ComponentHolder<BlockEntityComponent>, TBuilder, TComponent> componentFactory) {
        return register(BlockEntityComponent.class, registryName, builderFactory, componentFactory);
    }

    static <TComponent extends BlockEntityComponent> ComponentType<BlockEntityComponent, TComponent, ComponentBuilder> registerBlockEntity(ResourceLocation registryName, Function<ComponentHolder<BlockEntityComponent>, TComponent> componentFactory) {
        return registerBlockEntity(registryName, ComponentBuilder.NOOP, (holder, builder) -> componentFactory.apply(holder));
    }
}
