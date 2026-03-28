package dev.apexstudios.apexcore.fabric.common;

import dev.apexstudios.apexcore.xplat.common.ApexCoreXplat;
import net.fabricmc.api.ModInitializer;

public final class ApexCoreFabric implements ModInitializer, ApexCoreXplat {
    @Override
    public void onInitialize() {
        init();
    }
}
