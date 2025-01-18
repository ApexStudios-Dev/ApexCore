package dev.apexstudios.apexcore.lib.data.provider.tag;

import dev.apexstudios.apexcore.core.data.provider.tag.SimpleTagProviderImpl;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface SimpleTagProvider<TRegistry> extends TagProvider<TRegistry, SimpleTagBuilder<TRegistry>> {
    static <TRegistry> ProviderType<SimpleTagProvider<TRegistry>> register(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType) {
        return SimpleTagProviderImpl.register(namespace, registryType);
    }
}
