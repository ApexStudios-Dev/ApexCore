package dev.apexstudios.apexcore.neoforge.data;

import dev.apexstudios.apexcore.neoforge.api.data.ProviderTypes;
import dev.apexstudios.apexcore.neoforge.api.data.ResourceGenerator;
import dev.apexstudios.apexcore.neoforge.api.placement.BlockItemPlacementEvent;
import dev.apexstudios.apexcore.neoforge.common.seat.SeatSetup;
import dev.apexstudios.apexcore.xplat.common.ApexCoreXplat;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.Tags;

@Mod(value = ApexCoreXplat.ID, dist = Dist.CLIENT)
public final class ApexCoreDataEntryPoint {
    public ApexCoreDataEntryPoint(IEventBus modBus) {
        ResourceGenerator.of(modBus, generator -> {
            generator.pack()
                    .providing(ProviderTypes.LANGUAGE, (context, provider) -> provider.addEntityType(SeatSetup.ENTITY, "Seat"))
                    .providing(ProviderTypes.ENTITY_TYPE_TAGS, (context, provider) -> {
                        provider.tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).withElement(SeatSetup.ENTITY);
                        provider.tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED).withElement(SeatSetup.ENTITY);
                    });

            // 'vanilla vanilla' data pack enables the visualizer for all vanilla blocks
            generator.pack("visual_vanilla")
                    .description("Visual Vanilla")
                    .providing(ProviderTypes.BLOCK_TAGS, (context, provider) -> {
                        context.registries()
                                .lookupOrThrow(Registries.BLOCK)
                                .listElements()
                                .map(Holder::value)
                                .filter(block -> block.asItem() instanceof BlockItem)
                                .forEach(block -> provider.tag(BlockItemPlacementEvent.RENDERABLES).withElement(block));
                    });
        });
    }
}
