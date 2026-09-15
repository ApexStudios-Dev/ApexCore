package dev.apexstudios.apexcore.api.data.provider.context;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;

public interface ProviderListenerContext extends ProviderContext {
    HolderLookup.Provider worldRegistries();

    HolderLookup.Provider reloadableRegistries();

    static ProviderListenerContext of(ProviderContext context, HolderLookup.Provider worldRegistries, HolderLookup.Provider reloadableRegistries) {
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
            public HolderLookup.Provider worldRegistries() {
                return worldRegistries;
            }

            @Override
            public HolderLookup.Provider reloadableRegistries() {
                return reloadableRegistries;
            }
        };
    }
}
