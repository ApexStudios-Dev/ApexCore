package dev.apexstudios.apexcore.core.seat;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.block.types.SeatBlockComponent;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredEntity;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public interface SeatSetup {
    TagKey<EntityType<?>> BLACKLIST = ApexCore.REGISTREE.tag(Registries.ENTITY_TYPE, "seat_blacklist");
    TagKey<Block> ORIGIN_ONLY = ApexCore.REGISTREE.tag(Registries.BLOCK, "seat_origin_only");
    DeferredEntity<SeatEntity> ENTITY = ApexCore.REGISTREE.registerEntity("seat", SeatEntity::new, MobCategory.MISC, builder -> builder.sized(1F, 1F).noLootTable());

    static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> event.registerEntityRenderer(ENTITY.value(), NoopRenderer::new));

        modBus.addListener(RegisterCapabilitiesEvent.class, event -> {
            SeatBlockComponent.registerCapabilities(event, EntityType.CAMEL, (camel, pos, blockState) -> camel.sitDown(), (camel, pos, blockState) -> camel.standUp());
            SeatBlockComponent.registerCapabilities(event, EntityType.FOX, (fox, pos, blockState) -> fox.setSitting(true), (fox, pos, blockState) -> fox.setSitting(false));

            SeatBlockComponent.registerCapabilities(
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

            event.registerEntity(SeatBlockComponent.MAY_SIT_CAPABILITY, EntityType.VILLAGER, (villager, context) -> () -> {
                if(villager.level().isNight())
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
