package dev.apexstudios.apexcore.common;

import dev.apexstudios.apexcore.api.util.ApexTags;
import dev.apexstudios.apexcore.common.seat.SeatSetup;
import dev.apexstudios.registree.neoforge.NeoForgeRegistree;
import dev.apexstudios.registree.xplat.Registree;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(ApexCore.ID)
public final class ApexCore {
    public static final String ID = "apexcore";
    public static final Registree REGISTREE = Registree.create(ID);

    public ApexCore(IEventBus modBus) {
        ApexTags.register();
        SeatSetup.register(modBus);

        modBus.addListener(AddPackFindersEvent.class, event ->  event.addPackFinders(
                identifier("packs/visual_vanilla"),
                PackType.SERVER_DATA,
                Component.literal("Visual Vanilla"),
                PackSource.create(PackSource.BUILT_IN::decorate, false),
                false,
                Pack.Position.TOP
        ));

        NeoForgeRegistree.register(REGISTREE, modBus);
    }

    public static Identifier identifier(String identifier) {
        return Identifier.fromNamespaceAndPath(ID, identifier);
    }

    public static String id(String identifier) {
        return ID + Identifier.NAMESPACE_SEPARATOR + identifier;
    }
}
