package dev.apexstudios.apexcore.neoforge.api.data.provider.tag;

import dev.apexstudios.apexcore.neoforge.api.data.ProviderType;
import dev.apexstudios.apexcore.neoforge.common.data.provider.tag.SimpleTagProviderImpl;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface SimpleTagProvider<TRegistry> extends TagProvider<TRegistry, SimpleTagBuilder<TRegistry>> {
    static <TRegistry> ProviderType<SimpleTagProvider<TRegistry>> register(String namespace, ResourceKey<? extends Registry<TRegistry>> registryType) {
        return SimpleTagProviderImpl.register(namespace, registryType);
    }
}
