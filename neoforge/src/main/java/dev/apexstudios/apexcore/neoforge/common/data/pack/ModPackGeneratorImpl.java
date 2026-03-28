package dev.apexstudios.apexcore.neoforge.common.data.pack;

import dev.apexstudios.apexcore.neoforge.api.data.pack.ModPackGenerator;
import java.nio.file.Path;
import net.minecraft.data.PackOutput;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public final class ModPackGeneratorImpl extends PackGeneratorImpl<ModPackGenerator> implements ModPackGenerator {
    private boolean isDummy = false;

    @Override
    public boolean isDummy() {
        return isDummy;
    }

    @Override
    public ModPackGenerator markDummy() {
        isDummy = true;
        return this;
    }

    @Override
    protected FeatureFlagSet enabledFeatures() {
        return FeatureFlags.VANILLA_SET;
    }

    @Override
    protected PackOutput createPackOutput(Path outputDir) {
        return new PackOutput(outputDir);
    }
}
