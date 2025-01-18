package dev.apexstudios.apexcore.lib.data.pack;

import dev.apexstudios.apexcore.lib.data.ExtendedRegistryBootstrap;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

public interface PackGenerator<TSelf extends PackGenerator<TSelf>> {
    TSelf description(Component description);

    default TSelf description(String description) {
        return description(Component.literal(description));
    }

    <TProvider> TSelf providing(ProviderType<TProvider> providerType, BiConsumer<ProviderListenerContext, TProvider> listener);

    <TRegistry> TSelf registering(ResourceKey<? extends Registry<TRegistry>> registryType, Consumer<ExtendedRegistryBootstrap<TRegistry>> bootstrap);
}
