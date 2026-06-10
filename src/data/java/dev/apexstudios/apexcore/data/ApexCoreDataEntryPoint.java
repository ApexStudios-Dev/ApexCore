package dev.apexstudios.apexcore.data;

import dev.apexstudios.apexcore.api.data.ProviderTypes;
import dev.apexstudios.apexcore.api.data.ResourceGenerator;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.apexcore.common.seat.SeatSetup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.Tags;

@Mod(value = ApexCore.ID, dist = Dist.CLIENT)
public final class ApexCoreDataEntryPoint {
    public ApexCoreDataEntryPoint(IEventBus modBus) {
        ResourceGenerator.of(modBus, generator -> {
            generator.pack()
                    .providing(ProviderTypes.LANGUAGE, (context, provider) -> provider.addEntityType(SeatSetup.ENTITY, "Seat"))
                    .providing(ProviderTypes.ENTITY_TYPE_TAGS, (context, provider) -> {
                        provider.tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).withElement(SeatSetup.ENTITY);
                        provider.tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED).withElement(SeatSetup.ENTITY);
                    });
        });
    }
}
