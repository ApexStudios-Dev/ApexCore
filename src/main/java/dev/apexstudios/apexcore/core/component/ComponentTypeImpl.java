package dev.apexstudios.apexcore.core.component;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.lib.component.Component;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public final class ComponentTypeImpl<
        TBase extends Component<TBase>,
        TComponent extends TBase,
        TBuilder extends ComponentBuilder
> implements ComponentType<TBase, TComponent, TBuilder> {
    private static final Map<Class<? extends Component<?>>, ? super Map<ResourceLocation, ? extends ComponentType<?, ?, ?>>> ROOT_REGISTRY = Maps.newConcurrentMap();

    private final ResourceLocation registryName;
    private final Supplier<TBuilder> builderFactory;
    private final BiFunction<ComponentHolder<TBase>, TBuilder, TComponent> componentFactory;

    private ComponentTypeImpl(ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<ComponentHolder<TBase>, TBuilder, TComponent> componentFactory) {
        this.registryName = registryName;
        this.builderFactory = builderFactory;
        this.componentFactory = componentFactory;
    }

    @Override
    public ResourceLocation registryName() {
        return registryName;
    }

    @Override
    public TComponent newInstance(ComponentHolder<TBase> holder, Consumer<TBuilder> modifier) {
        var builder = builderFactory.get();
        modifier.accept(builder);
        return componentFactory.apply(holder, builder);
    }

    public static <TBase extends Component<TBase>, TComponent extends TBase, TBuilder extends ComponentBuilder> ComponentType<TBase, TComponent, TBuilder> register(Class<TBase> baseType, ResourceLocation registryName, Supplier<TBuilder> builderFactory, BiFunction<ComponentHolder<TBase>, TBuilder, TComponent> componentFactory) {
        var componentType = new ComponentTypeImpl<>(registryName, builderFactory, componentFactory);

        if(registry(baseType).putIfAbsent(registryName, componentType) != null)
            throw new IllegalStateException("Duplicate " + baseType.getName() + " registration: " + registryName);

        return componentType;
    }

    private static <TBase extends Component<TBase>, THolder extends ComponentHolder<TBase>> Map<ResourceLocation, ComponentType<TBase, ?, ?>> registry(Class<TBase> baseType) {
        return (Map<ResourceLocation, ComponentType<TBase, ?, ?>>) ROOT_REGISTRY.computeIfAbsent(baseType, $ -> Maps.newConcurrentMap());
    }
}
