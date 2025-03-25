package dev.apexstudios.apexcore.core;

import dev.apexstudios.apexcore.core.client.DyeColorItemTintSource;
import dev.apexstudios.apexcore.core.placement.PlacementRendererRegistry;
import dev.apexstudios.apexcore.lib.util.ApexRenderTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@Mod(value = ApexCore.ID, dist = Dist.CLIENT)
public final class ApexCoreClientEntryPoint {
    public ApexCoreClientEntryPoint(IEventBus modBus) {
        ApexRenderTypes.register(modBus);
        PlacementRendererRegistry.register();

        modBus.addListener(RegisterColorHandlersEvent.ItemTintSources.class, event -> event.register(ApexCore.identifier("dye_color"), DyeColorItemTintSource.MAP_CODEC));
    }
}
