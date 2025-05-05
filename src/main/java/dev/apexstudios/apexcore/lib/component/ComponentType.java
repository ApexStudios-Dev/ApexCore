package dev.apexstudios.apexcore.lib.component;

import dev.apexstudios.apexcore.core.component.ComponentTypeImpl;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval
@ApiStatus.NonExtendable
public interface ComponentType<TBase extends Component<TBase, TObj>, TComponent extends TBase, TObj, TBuilder extends ComponentBuilder> {
    ResourceLocation registryName();

    @ApiStatus.Internal
    TComponent newInstance(ComponentHolder<TBase, TObj> holder, Consumer<TBuilder> builder);

    static <TBase extends Component<TBase, TObj>, TComponent extends TBase, TObj, TBuilder extends ComponentBuilder> ComponentType<TBase, TComponent, TObj, TBuilder> register(Class<TBase> baseType, ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<ComponentHolder<TBase, TObj>, TBuilder, TComponent> componentFactory) {
        return ComponentTypeImpl.register(baseType, registryName, builderFactory, componentFactory);
    }

    static <TComponent extends BlockComponent, TBuilder extends ComponentBuilder> ComponentType<BlockComponent, TComponent, Block, TBuilder> registerBlock(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<ComponentHolder<BlockComponent, Block>, TBuilder, TComponent> componentFactory) {
        return register(BlockComponent.class, registryName, builderFactory, componentFactory);
    }

    static <TComponent extends BlockComponent> ComponentType<BlockComponent, TComponent, Block, ComponentBuilder> registerBlock(ResourceLocation registryName, Function<ComponentHolder<BlockComponent, Block>, TComponent> componentFactory) {
        return registerBlock(registryName, ComponentBuilder.NOOP, (holder, builder) -> componentFactory.apply(holder));
    }
}
