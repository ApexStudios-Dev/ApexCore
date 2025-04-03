package dev.apexstudios.apexcore.lib.data.pack;

import net.minecraft.server.packs.PackType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;

public interface FeaturePackGenerator extends PackGenerator<FeaturePackGenerator> {
    FeaturePackGenerator packType(PackType packType);

    FeaturePackGenerator path(String path);

    FeaturePackGenerator enabling(FeatureFlagSet enabledFeatures);

    default FeaturePackGenerator enabling(FeatureFlag featureFlag) {
        return enabling(FeatureFlagSet.of(featureFlag));
    }

    default FeaturePackGenerator enabling(FeatureFlag featureFlag, FeatureFlag... featureFlags) {
        return enabling(FeatureFlagSet.of(featureFlag, featureFlags));
    }
}
