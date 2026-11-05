package dev.apexstudios.apexcore.core;

import dev.apexstudios.apexcore.api.ApexCore;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ApexCore.ID)
public final class ApexCoreEntryPoint {
    public ApexCoreEntryPoint(IEventBus modBus) {
        ApexCore.REGISTREE.register(modBus);
    }
}
