package dev.apexstudios.apexcore.common;

import dev.apexstudios.apexcore.api.util.ApexTags;
import dev.apexstudios.apexcore.common.seat.SeatSetup;
import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.registrar.BlockRegistrar;
import dev.apexstudios.registree.registrar.EntityTypeRegistrar;
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
    public static final BlockRegistrar BLOCKS = REGISTREE.blocks();
    public static final EntityTypeRegistrar ENTITY_TYPES = REGISTREE.entityTypes();

    public ApexCore(IEventBus modBus) {
        REGISTREE.registerEvents(modBus);

        ApexTags.register();
        SeatSetup.register();

        modBus.addListener(AddPackFindersEvent.class, event ->  event.addPackFinders(
                identifier("packs/visual_vanilla"),
                PackType.SERVER_DATA,
                Component.literal("Visual Vanilla"),
                PackSource.create(PackSource.BUILT_IN::decorate, false),
                false,
                Pack.Position.TOP
        ));
    }

    public static Identifier identifier(String identifier) {
        return Identifier.fromNamespaceAndPath(ID, identifier);
    }

    public static String id(String identifier) {
        return ID + Identifier.NAMESPACE_SEPARATOR + identifier;
    }
}
