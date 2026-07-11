package dev.apexstudios.apexcore.common.seat;

import dev.apexstudios.apexcore.api.block.Seat;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.registree.holder.DeferredEntity;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public interface SeatSetup {
    DeferredEntity<SeatEntity> ENTITY = ApexCore.REGISTREE.entity("seat", SeatEntity::new, MobCategory.MISC)
            .properties(properties -> properties
                    .sized(1F, 1F)
                    .noLootTable()
            )
            .renderer(() -> () -> NoopRenderer::new)
            .register();

    static void register() {
        ApexCore.REGISTREE.event(RegisterCapabilitiesEvent.class, event -> {
            Seat.registerCapabilities(event, EntityTypes.CAMEL, (camel, pos, blockState) -> camel.sitDown(), (camel, pos, blockState) -> camel.standUp());
            Seat.registerCapabilities(event, EntityTypes.FOX, (fox, pos, blockState) -> fox.setSitting(true), (fox, pos, blockState) -> fox.setSitting(false));

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
                    EntityTypes.CAT, EntityTypes.PARROT, EntityTypes.WOLF
            );

            event.registerEntity(Seat.MAY_SIT_CAPABILITY, EntityTypes.VILLAGER, (villager, context) -> () -> {
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
