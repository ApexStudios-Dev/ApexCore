package dev.apexstudios.apexcore.core.component;

import dev.apexstudios.apexcore.lib.component.Component;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public class ComponentTypeImpl<
        TBase extends Component<TBase, TObj, THolder, TType>,
        TComponent extends TBase,
        TObj,
        THolder extends ComponentHolder<TBase, TObj, THolder, TType>,
        TType extends ComponentType<TBase, ? extends TBase, TObj, THolder, TType, ?>,
        TBuilder
> implements ComponentType<TBase, TComponent, TObj, THolder, TType, TBuilder> {
    private final ResourceLocation registryName;
    private final Supplier<TBuilder> builderFactory;
    private final BiFunction<THolder, TBuilder, TComponent> componentFactory;

    protected ComponentTypeImpl(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<THolder, TBuilder, TComponent> componentFactory) {
        this.registryName = registryName;
        this.builderFactory = builderFactory;
        this.componentFactory = (holder, object) -> componentFactory.apply(holder, (TBuilder) object);
    }

    @Override
    public ResourceLocation registryName() {
        return registryName;
    }

    @Override
    public TComponent newInstance(THolder holder, Consumer<TBuilder> modifier) {
        var builder = builderFactory.get();
        modifier.accept(builder);
        return componentFactory.apply(holder, builder);
    }
}
