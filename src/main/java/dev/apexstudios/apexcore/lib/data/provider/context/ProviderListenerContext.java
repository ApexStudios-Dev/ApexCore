package dev.apexstudios.apexcore.lib.data.provider.context;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;

public interface ProviderListenerContext extends ProviderContext {
    HolderLookup.Provider registries();

    static ProviderListenerContext of(ProviderContext context, HolderLookup.Provider registries) {
        return new ProviderListenerContext() {
            @Override
            public String modId() {
                return context.modId();
            }

            @Override
            public ResourceManager getResourceManager(PackType packType) {
                return context.getResourceManager(packType);
            }

            @Override
            public FeatureFlagSet enabledFeatures() {
                return context.enabledFeatures();
            }

            @Override
            public HolderLookup.Provider registries() {
                return registries;
            }
        };
    }
}
