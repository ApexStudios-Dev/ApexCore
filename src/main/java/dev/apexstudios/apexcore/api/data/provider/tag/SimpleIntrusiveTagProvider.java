package dev.apexstudios.apexcore.api.data.provider.tag;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.common.data.provider.tag.SimpleIntrusiveTagProviderImpl;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface SimpleIntrusiveTagProvider<TRegistry> extends IntrusiveTagProvider<TRegistry, SimpleIntrusiveTagBuilder<TRegistry>> {
    static <TRegistry> ProviderType<SimpleIntrusiveTagProvider<TRegistry>> register(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, ResourceKey<TRegistry>> keyLookup) {
        return SimpleIntrusiveTagProviderImpl.register(namespace, registryType, keyLookup);
    }

    static <TRegistry> ProviderType<SimpleIntrusiveTagProvider<TRegistry>> registerForHolder(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType, Function<TRegistry, Holder.Reference<TRegistry>> holderLookup) {
        return register(namespace, registryType, value -> holderLookup.apply(value).key());
    }
}
