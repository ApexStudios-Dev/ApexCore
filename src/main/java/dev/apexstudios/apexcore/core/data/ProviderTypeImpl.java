package dev.apexstudios.apexcore.core.data;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public final class ProviderTypeImpl<TProvider> implements ProviderType<TProvider> {
    private static final Map<ResourceLocation, ProviderType<?>> REGISTRY = Maps.newHashMap();

    private final ResourceLocation registryName;
    private final Function<ProviderListenerContext, TProvider> factory;

    private ProviderTypeImpl(ResourceLocation registryName, Function<ProviderListenerContext, TProvider> factory) {
        this.registryName = registryName;
        this.factory = factory;
    }

    @Override
    public ResourceLocation registryName() {
        return registryName;
    }

    @Override
    public TProvider create(ProviderListenerContext context) {
        return factory.apply(context);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj instanceof ProviderType<?> other)
            return registryName.equals(other.registryName());
        return false;
    }

    @Override
    public int hashCode() {
        return registryName.hashCode();
    }

    @Override
    public String toString() {
        return "ProviderType{" + registryName + '}';
    }

    public static <TProvider> ProviderType<TProvider> register(ResourceLocation registryName, Function<ProviderListenerContext, TProvider> factory) {
        var providerType = new ProviderTypeImpl<>(registryName, factory);

        if(REGISTRY.putIfAbsent(registryName, providerType) != null)
            throw new IllegalStateException("Duplicate ProviderType: " + registryName);

        return providerType;
    }
}
