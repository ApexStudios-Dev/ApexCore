package dev.apexstudios.apexcore.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.apexstudios.apexcore.extension.BlockStateExtension;
import dev.apexstudios.apexcore.extension.EntityExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin implements EntityExtension {
    @WrapOperation(
            method = "restituteMovementAfterCollisions(Lnet/minecraft/world/level/block/state/BlockState;ZZLnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "getBlockBounciness(Lnet/minecraft/world/level/block/Block;)D"
            )
    )
    private double ApexCore$getBlockBounciness(Entity entity, Block block, Operation<Double> original, @Local(name = "effectState") BlockState effectState) {
        return ((BlockStateExtension) effectState).getBounceRestitution();
    }
}
