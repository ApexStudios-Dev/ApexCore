package dev.apexstudios.apexcore.core.seat;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredEntity;
import dev.apexstudios.apexcore.lib.seat.Seat;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public interface SeatSetup {
    TagKey<EntityType<?>> BLACKLIST = ApexCore.REGISTREE.tag(Registries.ENTITY_TYPE, "seat_blacklist");
    DeferredEntity<SeatEntity> ENTITY = ApexCore.REGISTREE.registerEntity("seat", SeatEntity::new, MobCategory.MISC, builder -> builder.sized(1F, 1F).noLootTable());

    static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> event.registerEntityRenderer(ENTITY.value(), NoopRenderer::new));

        modBus.addListener(RegisterCapabilitiesEvent.class, event -> {
            Seat.registerCapabilities(event, EntityType.CAMEL, (camel, pos, blockState) -> camel.sitDown(), (camel, pos, blockState) -> camel.standUp());
            Seat.registerCapabilities(event, EntityType.FOX, (fox, pos, blockState) -> fox.setSitting(true), (fox, pos, blockState) -> fox.setSitting(false));

            Seat.registerCapabilities(
                    event,
                    (tameable, pos, blockState) -> {
                        tameable.setOrderedToSit(true);
                        tameable.setInSittingPose(true);
                    },
                    (tameable, pos, blockState) -> {
                        tameable.setOrderedToSit(false);
                        tameable.setInSittingPose(false);
                    },
                    EntityType.CAT, EntityType.PARROT, EntityType.WOLF
            );
        });

        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, event -> {
            var entity = event.getTarget();

            if(!(entity instanceof Player) && entity.getVehicle() instanceof SeatEntity) {
                entity.stopRiding();
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        });
    }
}
