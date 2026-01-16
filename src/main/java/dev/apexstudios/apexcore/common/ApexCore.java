package dev.apexstudios.apexcore.common;

import com.mojang.serialization.Codec;
import dev.apexstudios.apexcore.api.util.ApexTags;
import dev.apexstudios.apexcore.common.network.ServerboundSetModifierKeyPacket;
import dev.apexstudios.apexcore.common.seat.SeatSetup;
import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holder.DeferredAttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(ApexCore.ID)
public final class ApexCore {
    public static final String ID = "apexcore";
    public static final Registree REGISTREE = Registree.create(ID);

    public static final DeferredAttachmentType<Boolean> PLAYER_MODIFIER = REGISTREE.registerAttachmentType("modifier", () -> false, builder -> builder
            .serialize(Codec.BOOL.fieldOf("apex_modifier"))
            .copyHandler((attachment, holder, provider) -> attachment)
            .copyOnDeath()
    );

    public ApexCore(IEventBus modBus) {
        REGISTREE.registerEvents(modBus);

        ApexTags.register();
        SeatSetup.register(modBus);

        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            var registrar = event.registrar("1");
            registrar.playToServer(ServerboundSetModifierKeyPacket.TYPE, ServerboundSetModifierKeyPacket.STREAM_CODEC, ServerboundSetModifierKeyPacket::handle);
        });

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
