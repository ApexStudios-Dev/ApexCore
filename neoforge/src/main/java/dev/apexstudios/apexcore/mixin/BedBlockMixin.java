package dev.apexstudios.apexcore.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.apexstudios.apexcore.api.multiblock.MultiBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
public class BedBlockMixin {
    @ModifyExpressionValue(
            method = "playerWillDestroy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;preventsBlockDrops()Z"
            )
    )
    private boolean ApexCore$playerWillDestroy(boolean original, @Local(argsOnly = true) BlockState blockState) {
        return !MultiBlock.isMultiBlock(blockState) && original;
    }
}
