package dev.apexstudios.apexcore.lib.data.provider.tag;

import dev.apexstudios.apexcore.core.data.provider.tag.IntrusiveTagProviderImpl;
import dev.apexstudios.apexcore.lib.data.ProviderType;
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
