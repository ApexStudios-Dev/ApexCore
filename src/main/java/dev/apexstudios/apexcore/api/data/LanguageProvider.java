package dev.apexstudios.apexcore.api.data;

import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockItemTagId;

public abstract class LanguageProvider extends net.neoforged.neoforge.common.data.LanguageProvider {
    public static final String EN_US = "en_us";
    public static final String EN_GB = "en_gb";

    public LanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    public LanguageProvider(PackOutput output, String modid) {
        super(output, modid, EN_US);
    }

    // TODO: Merge upstream into NeoForge
    public void add(BlockItemTagId tag, String blockDescription, String itemDescription) {
        add(tag.block(), blockDescription);
        add(tag.item(), itemDescription);
    }

    public void add(BlockItemTagId tag, String description) {
        add(tag, description, description);
    }
}
