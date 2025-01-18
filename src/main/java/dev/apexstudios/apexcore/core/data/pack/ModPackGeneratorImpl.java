package dev.apexstudios.apexcore.core.data.pack;

import dev.apexstudios.apexcore.lib.data.pack.ModPackGenerator;
import java.nio.file.Path;
import net.minecraft.data.PackOutput;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public final class ModPackGeneratorImpl extends PackGeneratorImpl<ModPackGenerator> implements ModPackGenerator {
    @Override
    protected FeatureFlagSet enabledFeatures() {
        return FeatureFlags.VANILLA_SET;
    }

    @Override
    protected PackOutput createPackOutput(Path outputDir) {
        return new PackOutput(outputDir);
    }
}
