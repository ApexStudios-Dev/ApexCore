package dev.apexstudios.apexcore.common.data.provider.tag;

import dev.apexstudios.apexcore.api.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.api.data.provider.tag.IntrusiveTagBuilder;
import dev.apexstudios.apexcore.api.data.provider.tag.IntrusiveTagProvider;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public sealed class IntrusiveTagProviderImpl<TRegistry, TBuilder extends IntrusiveTagBuilder<TRegistry, TBuilder>> extends TagProviderImpl<TRegistry, TBuilder> implements IntrusiveTagProvider<TRegistry, TBuilder> permits BlockTagProviderImpl, ItemTagProviderImpl, SimpleIntrusiveTagProviderImpl {
    protected IntrusiveTagProviderImpl(ProviderListenerContext context, ResourceKey<? extends Registry<TRegistry>> registryType, Function<String, TBuilder> builderFactory) {
        super(context, registryType, builderFactory);
    }
}
