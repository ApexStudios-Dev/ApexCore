package dev.apexstudios.apexcore.lib.data;

import dev.apexstudios.apexcore.core.data.ProviderTypeImpl;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;

public interface ProviderType<TProvider> {
    Identifier registryName();

    TProvider create(ProviderListenerContext context);

    static <TProvider> ProviderType<TProvider> register(Identifier registryName, Function<ProviderListenerContext, TProvider> factory) {
        return ProviderTypeImpl.register(registryName, factory);
    }

    static <TProvider> ProviderType<TProvider> register(Identifier registryName, Supplier<TProvider> factory) {
        return register(registryName, context -> factory.get());
    }

    static <TProvider> ProviderType<TProvider> registerForDist(Identifier registryName, Dist dist, Supplier<Function<ProviderListenerContext, TProvider>> factory) {
        return ProviderTypeImpl.registerForDist(registryName, dist, factory);
    }
}
