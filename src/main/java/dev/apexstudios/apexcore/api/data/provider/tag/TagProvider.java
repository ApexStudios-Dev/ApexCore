package dev.apexstudios.apexcore.api.data.provider.tag;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.api.data.provider.context.ProviderListenerContext;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface TagProvider<TRegistry, TBuilder extends TagBuilder<TRegistry, TBuilder>> {
    TBuilder tag(TagKey<TRegistry> tag);

    TBuilder tag(ResourceKey<TRegistry> registryKey);

    TBuilder tag(Identifier registryName);

    TBuilder tag(String tagPath);

    static <TRegistry, TProvider extends TagProvider<TRegistry, TBuilder>, TBuilder extends TagBuilder<TRegistry, TBuilder>> ProviderType<TProvider> register(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType, Function<ProviderListenerContext, TProvider> factory) {
        return ProviderType.register(Identifier.fromNamespaceAndPath(namespace, "tags/" + registryType.identifier().getPath()), factory);
    }

    static <TRegistry> ProviderType<SimpleTagProvider<TRegistry>> registerSimple(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType) {
        return SimpleTagProvider.register(namespace, registryType);
    }

    static <TRegistry> ProviderType<IntrusiveTagProvider<TRegistry>> registerIntrusive(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, ResourceKey<TRegistry>> keyLookup) {
        return IntrusiveTagProvider.register(namespace, registryType, keyLookup);
    }

    static <TRegistry> ProviderType<IntrusiveTagProvider<TRegistry>> registerIntrusiveForHolder(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, Holder.Reference<TRegistry>> holderLookup) {
        return IntrusiveTagProvider.registerForHolder(namespace, registryType, holderLookup);
    }
}
