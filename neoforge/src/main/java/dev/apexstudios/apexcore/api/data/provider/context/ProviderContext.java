package dev.apexstudios.apexcore.api.data.provider.context;

import java.util.function.Function;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;

public interface ProviderContext {
    String modId();

    ResourceManager getResourceManager(PackType packType);

    FeatureFlagSet enabledFeatures();

    static ProviderContext of(String modId, Function<PackType, ResourceManager> resourceManagerGetter, FeatureFlagSet enabledFeatures) {
        return new ProviderContext() {
            @Override
            public String modId() {
                return modId;
            }

            @Override
            public ResourceManager getResourceManager(PackType packType) {
                return resourceManagerGetter.apply(packType);
            }

            @Override
            public FeatureFlagSet enabledFeatures() {
                return enabledFeatures;
            }
        };
    }
}
