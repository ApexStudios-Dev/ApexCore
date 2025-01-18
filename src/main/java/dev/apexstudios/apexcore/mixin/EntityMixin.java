package dev.apexstudios.apexcore.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.apexstudios.apexcore.lib.event.DefineEntitySyncedDataEvent;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"
            )
    )
    private void ApexCore$defineSyncedData(Entity self, SynchedEntityData.Builder builder, Operation<Void> original) {
        original.call(self, builder);
        NeoForge.EVENT_BUS.post(new DefineEntitySyncedDataEvent(self, builder));
    }
}
