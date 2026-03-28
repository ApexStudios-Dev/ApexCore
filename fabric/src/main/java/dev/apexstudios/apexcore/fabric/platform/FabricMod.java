package dev.apexstudios.apexcore.fabric.platform;

import dev.apexstudios.apexcore.xplat.platform.Mod;
import net.fabricmc.loader.api.ModContainer;

record FabricMod(ModContainer modContainer) implements Mod {
    @Override
    public String modId() {
        return modContainer.getMetadata().getId();
    }

    @Override
    public String namespace() {
        return modId();
    }

    @Override
    public String displayName() {
        return modContainer.getMetadata().getName();
    }

    @Override
    public String version() {
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }
}
