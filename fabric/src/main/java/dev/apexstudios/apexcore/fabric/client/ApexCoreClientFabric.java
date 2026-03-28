package dev.apexstudios.apexcore.fabric.client;

import dev.apexstudios.apexcore.xplat.client.ApexCoreClientXplat;
import net.fabricmc.api.ClientModInitializer;

public final class ApexCoreClientFabric implements ClientModInitializer, ApexCoreClientXplat {
    @Override
    public void onInitializeClient() {
        init();
    }
}
