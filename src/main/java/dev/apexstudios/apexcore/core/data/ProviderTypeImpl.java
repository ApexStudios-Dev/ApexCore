package dev.apexstudios.apexcore.core.data;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public sealed abstract class ProviderTypeImpl<TProvider> implements ProviderType<TProvider> {
    private static final Map<ResourceLocation, ProviderType<?>> REGISTRY = Maps.newHashMap();

    private final ResourceLocation registryName;

    protected ProviderTypeImpl(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    @Override
    public ResourceLocation registryName() {
        return registryName;
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
        return register(new Main<>(registryName, factory));
    }

    public static <TProvider> ProviderType<TProvider> registerForDist(ResourceLocation registryName, Dist dist, Supplier<Function<ProviderListenerContext, TProvider>> factory) {
        return register(new ForDist<>(registryName, dist, factory));
    }

    public static <TProvider> ProviderType<TProvider> register(ProviderType<TProvider> providerType) {
        if(REGISTRY.putIfAbsent(providerType.registryName(), providerType) != null)
            throw new IllegalStateException("Duplicate ProviderType: " + providerType.registryName());

        return providerType;
    }

    private static final class ForDist<TProvider> extends ProviderTypeImpl<TProvider> {
        private final Dist dist;
        private final Supplier<Function<ProviderListenerContext, TProvider>> factory;

        private ForDist(ResourceLocation registryName, Dist dist, Supplier<Function<ProviderListenerContext, TProvider>> factory) {
            super(registryName);

            this.dist = dist;
            this.factory = factory;
        }

        @Override
        public TProvider create(ProviderListenerContext context) {
            return FMLEnvironment.getDist() == dist ? factory.get().apply(context) : null;
        }
    }

    private static final class Main<TProvider> extends ProviderTypeImpl<TProvider> {
        private final Function<ProviderListenerContext, TProvider> factory;

        private Main(ResourceLocation registryName, Function<ProviderListenerContext, TProvider> factory) {
            super(registryName);

            this.factory = factory;
        }

        @Override
        public TProvider create(ProviderListenerContext context) {
            return factory.apply(context);
        }
    }
}
