package dev.apexstudios.apexcore.data;

import dev.apexstudios.apexcore.api.ApexCore;
import dev.apexstudios.apexcore.api.data.DataGenUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(value = ApexCore.ID, dist = Dist.CLIENT)
public final class ApexCoreDataEntryPoint {
    static final Component PACK_DESC = DataGenUtil.packDescription(ApexCore.ID);

    public ApexCoreDataEntryPoint(IEventBus modBus) {
        modBus.addListener(GatherDataEvent.Client.class, event -> {
            event.createProvider(output -> DataGenUtil.metadata(output, PACK_DESC, PackType.SERVER_DATA));
            event.createProvider(LangProvider::new);
        });
    }
}
