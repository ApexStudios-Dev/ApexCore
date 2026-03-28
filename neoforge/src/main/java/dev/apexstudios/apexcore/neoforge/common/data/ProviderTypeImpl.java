package dev.apexstudios.apexcore.neoforge.common.data;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.neoforge.api.data.ProviderType;
import dev.apexstudios.apexcore.neoforge.api.data.provider.context.ProviderListenerContext;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public sealed abstract class ProviderTypeImpl<TProvider> implements ProviderType<TProvider> {
    private static final Map<Identifier, ProviderType<?>> REGISTRY = Maps.newHashMap();

    private final Identifier registryName;

    protected ProviderTypeImpl(Identifier registryName) {
        this.registryName = registryName;
    }

    @Override
    public Identifier registryName() {
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

    public static <TProvider> ProviderType<TProvider> register(Identifier registryName, Function<ProviderListenerContext, TProvider> factory) {
        return register(new Main<>(registryName, factory));
    }

    public static <TProvider> ProviderType<TProvider> registerForDist(Identifier registryName, Dist dist, Supplier<Function<ProviderListenerContext, TProvider>> factory) {
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

        private ForDist(Identifier registryName, Dist dist, Supplier<Function<ProviderListenerContext, TProvider>> factory) {
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

        private Main(Identifier registryName, Function<ProviderListenerContext, TProvider> factory) {
            super(registryName);

            this.factory = factory;
        }

        @Override
        public TProvider create(ProviderListenerContext context) {
            return factory.apply(context);
        }
    }
}
