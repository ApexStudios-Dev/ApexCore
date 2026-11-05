package dev.apexstudios.apexcore.client;

import dev.apexstudios.apexcore.api.ApexCore;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = ApexCore.ID, dist = Dist.CLIENT)
public final class ApexCoreClientEntryPoint {
    public ApexCoreClientEntryPoint(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> event.registerEntityRenderer(ApexCore.FAKE_ITEM.value(), FakeItemEntityRenderer::new));
    }
}
