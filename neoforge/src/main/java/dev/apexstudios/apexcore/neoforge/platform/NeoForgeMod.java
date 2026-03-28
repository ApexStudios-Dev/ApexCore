package dev.apexstudios.apexcore.neoforge.platform;

import dev.apexstudios.apexcore.xplat.platform.Mod;
import net.neoforged.neoforgespi.language.IModInfo;

record NeoForgeMod(IModInfo modInfo) implements Mod {
    @Override
    public String modId() {
        return modInfo.getModId();
    }

    @Override
    public String namespace() {
        return modInfo.getNamespace();
    }

    @Override
    public String displayName() {
        return modInfo.getDisplayName();
    }

    @Override
    public String version() {
        return modInfo.getVersion().toString();
    }
}
