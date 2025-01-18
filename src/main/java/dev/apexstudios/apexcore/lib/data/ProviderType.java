package dev.apexstudios.apexcore.lib.data;

import dev.apexstudios.apexcore.core.data.ProviderTypeImpl;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public interface ProviderType<TProvider> {
    ResourceLocation registryName();

    TProvider create(ProviderListenerContext context);

    static <TProvider> ProviderType<TProvider> register(ResourceLocation registryName, Function<ProviderListenerContext, TProvider> factory) {
        return ProviderTypeImpl.register(registryName, factory);
    }

    static <TProvider> ProviderType<TProvider> register(ResourceLocation registryName, Supplier<TProvider> factory) {
        return register(registryName, context -> factory.get());
    }
}
