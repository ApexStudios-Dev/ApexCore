package dev.apexstudios.apexcore.core.data.provider.tag;

import dev.apexstudios.apexcore.lib.data.ProviderType;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.lib.data.provider.tag.SimpleTagBuilder;
import dev.apexstudios.apexcore.lib.data.provider.tag.SimpleTagProvider;
import dev.apexstudios.apexcore.lib.data.provider.tag.TagProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class SimpleTagProviderImpl<TRegistry> extends TagProviderImpl<TRegistry, SimpleTagBuilder<TRegistry>> implements SimpleTagProvider<TRegistry> {
    private SimpleTagProviderImpl(ProviderListenerContext context, ResourceKey<? extends Registry<TRegistry>> registryType) {
        super(context, registryType, SimpleTagBuilderImpl::new);
    }

    public static <TRegistry> ProviderType<SimpleTagProvider<TRegistry>> register(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType) {
        return TagProvider.register(namespace, registryType, context -> new SimpleTagProviderImpl<>(context, registryType));
    }
}
