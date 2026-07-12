package dev.apexstudios.apexcore.common.data.provider.tag;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.api.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.api.data.provider.tag.SimpleIntrusiveTagBuilder;
import dev.apexstudios.apexcore.api.data.provider.tag.SimpleIntrusiveTagProvider;
import dev.apexstudios.apexcore.api.data.provider.tag.TagProvider;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class SimpleIntrusiveTagProviderImpl<TRegistry> extends IntrusiveTagProviderImpl<TRegistry, SimpleIntrusiveTagBuilder<TRegistry>> implements SimpleIntrusiveTagProvider<TRegistry> {
    private SimpleIntrusiveTagProviderImpl(ProviderListenerContext context, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, ResourceKey<TRegistry>> keyLookup) {
        super(context, registryType, namespace -> new SimpleIntrusiveTagBuilderImpl<>(namespace, keyLookup));
    }

    public static <TRegistry> ProviderType<SimpleIntrusiveTagProvider<TRegistry>> register(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, ResourceKey<TRegistry>> keyLookup) {
        return TagProvider.register(namespace, registryType, context -> new SimpleIntrusiveTagProviderImpl<>(context, registryType, keyLookup));
    }
}
