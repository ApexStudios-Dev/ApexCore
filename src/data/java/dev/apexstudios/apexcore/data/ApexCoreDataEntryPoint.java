package dev.apexstudios.apexcore.data;

import dev.apexstudios.apexcore.ApexCore;
import dev.apexstudios.apexcore.util.DataGenUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(value = ApexCore.ID, dist = Dist.CLIENT)
public final class ApexCoreDataEntryPoint {
    public ApexCoreDataEntryPoint(IEventBus modBus) {
        modBus.addListener(GatherDataEvent.Client.class, event -> {
            event.createProvider(output -> DataGenUtil.metadata(output, Component.literal("ApexCore resources"), PackType.SERVER_DATA));
        });
    }
}
