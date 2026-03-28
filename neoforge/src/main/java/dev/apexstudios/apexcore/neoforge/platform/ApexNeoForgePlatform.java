package dev.apexstudios.apexcore.neoforge.platform;

import dev.apexstudios.apexcore.xplat.platform.ApexPlatform;
import dev.apexstudios.apexcore.xplat.platform.ModLoader;

public final class ApexNeoForgePlatform implements ApexPlatform {
    private final ModLoader modLoader = new NeoForgeModLoader();

    @Override
    public ModLoader modLoader() {
        return modLoader;
    }
}
