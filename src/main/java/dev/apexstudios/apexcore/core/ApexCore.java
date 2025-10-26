package dev.apexstudios.apexcore.core;

import dev.apexstudios.apexcore.core.seat.SeatSetup;
import dev.apexstudios.apexcore.lib.util.ApexTags;
import dev.apexstudios.registree.api.Registree;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ApexCore.ID)
public final class ApexCore {
    public static final String ID = "apexcore";
    public static final Registree REGISTREE = Registree.create(ID);

    public ApexCore(IEventBus modBus) {
        REGISTREE.registerEvents(modBus);

        ApexTags.register();
        SeatSetup.register(modBus);
    }

    public static ResourceLocation identifier(String identifier) {
        return ResourceLocation.fromNamespaceAndPath(ID, identifier);
    }

    public static String id(String identifier) {
        return ID + ResourceLocation.NAMESPACE_SEPARATOR + identifier;
    }
}
