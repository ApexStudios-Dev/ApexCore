package dev.apexstudios.apexcore.fabric.platform;

import dev.apexstudios.apexcore.xplat.platform.ApexPlatform;
import dev.apexstudios.apexcore.xplat.platform.ModLoader;

public final class ApexFabricPlatform implements ApexPlatform {
    private final ModLoader modLoader = new FabricModLoader();

    @Override
    public ModLoader modLoader() {
        return modLoader;
    }
}
