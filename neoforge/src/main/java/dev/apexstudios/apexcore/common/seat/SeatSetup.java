package dev.apexstudios.apexcore.common.seat;

import dev.apexstudios.apexcore.api.block.Seat;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.registree.xplat.holder.DeferredEntity;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public interface SeatSetup {
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

            event.registerEntity(Seat.MAY_SIT_CAPABILITY, EntityType.VILLAGER, (villager, context) -> () -> {
                if(villager.level().isDarkOutside())
                    return !villager.getBrain().hasMemoryValue(MemoryModuleType.HOME);
                return true;
            });
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
