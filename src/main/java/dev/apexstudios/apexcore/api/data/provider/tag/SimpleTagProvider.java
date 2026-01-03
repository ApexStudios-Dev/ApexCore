package dev.apexstudios.apexcore.api.data.provider.tag;

import dev.apexstudios.apexcore.common.data.provider.tag.SimpleTagProviderImpl;
import dev.apexstudios.apexcore.api.data.ProviderType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface SimpleTagProvider<TRegistry> extends TagProvider<TRegistry, SimpleTagBuilder<TRegistry>> {
    static <TRegistry> ProviderType<SimpleTagProvider<TRegistry>> register(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType) {
        return SimpleTagProviderImpl.register(namespace, registryType);
    }
}
