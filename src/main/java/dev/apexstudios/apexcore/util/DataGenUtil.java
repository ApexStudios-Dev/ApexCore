package dev.apexstudios.apexcore.util;

import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;

public interface DataGenUtil {
    static PackMetadataGenerator metadata(PackOutput output, Component description, PackType type) {
        return new PackMetadataGenerator(output)
                .add(PackMetadataSection.forPackType(type), new PackMetadataSection(
                        description,
                        DetectedVersion.BUILT_IN.packVersion(type).minorRange()
                ));
    }

    static PackMetadataGenerator metadata(PackOutput output, Component description, PackType type, FeatureFlagSet enabledFeatures) {
        return metadata(output, description, type)
                .add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(enabledFeatures));
    }
}
