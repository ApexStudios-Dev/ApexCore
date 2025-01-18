package dev.apexstudios.apexcore.core.data.pack;

import dev.apexstudios.apexcore.lib.data.pack.FeaturePackGenerator;
import java.nio.file.Path;
import net.minecraft.data.PackOutput;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.jetbrains.annotations.Nullable;

public final class FeaturePackGeneratorImpl extends PackGeneratorImpl<FeaturePackGenerator> implements FeaturePackGenerator {
    private FeatureFlagSet enabledFeatures = FeatureFlagSet.of();
    @Nullable private String path = null;
    private final String packId;

    public FeaturePackGeneratorImpl(String packId) {
        this.packId = packId;
    }

    @Override
    public FeaturePackGenerator path(String path) {
        this.path = path;
        return this;
    }

    @Override
    public FeaturePackGenerator enabling(FeatureFlagSet enabledFeatures) {
        this.enabledFeatures = this.enabledFeatures.join(enabledFeatures);
        return this;
    }

    @Override
    protected FeatureFlagSet enabledFeatures() {
        return enabledFeatures.join(FeatureFlags.VANILLA_SET);
    }

    @Override
    protected PackOutput createPackOutput(Path outputDir) {
        if(path != null)
            return new PackOutput(outputDir.resolve(path));

        return new PackOutput(outputDir.resolve("packs").resolve(packId));
    }
}
