package dev.apexstudios.apexcore.api.util;

import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

public interface ApexUtil {
    static PackMetadataGenerator createMetadataProvider(PackOutput output, Component description, PackType packType) {
        return new PackMetadataGenerator(output)
                .add(PackMetadataSection.forPackType(packType), new PackMetadataSection(description, DetectedVersion.BUILT_IN.packVersion(packType).minorRange()));
    }
}
