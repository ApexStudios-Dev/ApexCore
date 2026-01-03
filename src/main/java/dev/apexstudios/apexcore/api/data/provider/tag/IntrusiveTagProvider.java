package dev.apexstudios.apexcore.api.data.provider.tag;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.common.data.provider.tag.IntrusiveTagProviderImpl;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface IntrusiveTagProvider<TRegistry> extends TagProvider<TRegistry, IntrusiveTagBuilder<TRegistry>> {
    static <TRegistry> ProviderType<IntrusiveTagProvider<TRegistry>> register(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, ResourceKey<TRegistry>> keyLookup) {
        return IntrusiveTagProviderImpl.register(namespace, registryType, keyLookup);
    }

    static <TRegistry> ProviderType<IntrusiveTagProvider<TRegistry>> registerForHolder(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, Holder.Reference<TRegistry>> holderLookup) {
        return register(namespace, registryType, value -> holderLookup.apply(value).key());
    }
}
