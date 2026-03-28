package dev.apexstudios.apexcore.neoforge.common;

import dev.apexstudios.apexcore.neoforge.api.util.ApexTags;
import dev.apexstudios.apexcore.neoforge.common.seat.SeatSetup;
import dev.apexstudios.apexcore.xplat.common.ApexCoreXplat;
import dev.apexstudios.registree.neoforge.NeoForgeRegistree;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(ApexCoreXplat.ID)
public final class ApexCore implements ApexCoreXplat {
    public ApexCore(IEventBus modBus) {
        init();

        ApexTags.register();
        SeatSetup.register(modBus);

        modBus.addListener(AddPackFindersEvent.class, event ->  event.addPackFinders(
                ApexCoreXplat.identifier("packs/visual_vanilla"),
                PackType.SERVER_DATA,
                Component.literal("Visual Vanilla"),
                PackSource.create(PackSource.BUILT_IN::decorate, false),
                false,
                Pack.Position.TOP
        ));

        NeoForgeRegistree.register(REGISTREE, modBus);
    }
}
