package dev.apexstudios.apexcore.core;

import dev.apexstudios.apexcore.core.placement.PlacementRendererRegistry;
import dev.apexstudios.apexcore.lib.util.ApexRenderTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = ApexCore.ID, dist = Dist.CLIENT)
public final class ApexCoreClientEntryPoint {
    public ApexCoreClientEntryPoint(IEventBus modBus) {
        ApexRenderTypes.register();
        PlacementRendererRegistry.register();
    }
}
