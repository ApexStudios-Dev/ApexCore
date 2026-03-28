package dev.apexstudios.apexcore.neoforge.common.data.provider.tag;

import dev.apexstudios.apexcore.neoforge.api.data.ProviderType;
import dev.apexstudios.apexcore.neoforge.api.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.neoforge.api.data.provider.tag.IntrusiveTagBuilder;
import dev.apexstudios.apexcore.neoforge.api.data.provider.tag.IntrusiveTagProvider;
import dev.apexstudios.apexcore.neoforge.api.data.provider.tag.TagProvider;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class IntrusiveTagProviderImpl<TRegistry> extends TagProviderImpl<TRegistry, IntrusiveTagBuilder<TRegistry>> implements IntrusiveTagProvider<TRegistry> {
    private IntrusiveTagProviderImpl(ProviderListenerContext context, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, ResourceKey<TRegistry>> keyLookup) {
        super(context, registryType, namespace -> new IntrusiveTagBuilderImpl<>(namespace, keyLookup));
    }

    public static <TRegistry> ProviderType<IntrusiveTagProvider<TRegistry>> register(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, ResourceKey<TRegistry>> keyLookup) {
        return TagProvider.register(namespace, registryType, context -> new IntrusiveTagProviderImpl<>(context, registryType, keyLookup));
    }
}
