package dev.apexstudios.apexcore.core;

import dev.apexstudios.apexcore.core.seat.SeatSetup;
import dev.apexstudios.apexcore.lib.registree.Registree;
import dev.apexstudios.apexcore.lib.util.ApexPackSources;
import dev.apexstudios.apexcore.lib.util.ApexTags;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(ApexCore.ID)
public final class ApexCore {
    public static final String ID = "apexcore";
    public static final Registree REGISTREE = new Registree(ID);

    public ApexCore(IEventBus modBus) {
        REGISTREE.registerEvents(modBus);

        ApexTags.register();
        SeatSetup.register(modBus);

        modBus.addListener(AddPackFindersEvent.class, event ->  event.addPackFinders(
                identifier("packs/visual_vanilla"),
                PackType.SERVER_DATA,
                Component.literal("Visual Vanilla"),
                ApexPackSources.BUILT_IN_NOT_AUTO,
                false,
                Pack.Position.TOP
        ));
    }

    public static ResourceLocation identifier(String identifier) {
        return ResourceLocation.fromNamespaceAndPath(ID, identifier);
    }

    public static String id(String identifier) {
        return ID + ResourceLocation.NAMESPACE_SEPARATOR + identifier;
    }
}
