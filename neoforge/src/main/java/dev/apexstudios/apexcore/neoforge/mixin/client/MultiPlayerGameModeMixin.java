package dev.apexstudios.apexcore.neoforge.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.apexstudios.apexcore.neoforge.api.multiblock.MultiBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow
    private BlockPos destroyBlockPos;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private ItemStack destroyingItem;

    @ModifyReturnValue(
            method = "sameDestroyTarget",
            at = @At("RETURN")
    )
    private boolean ApexCore$sameDestroyTarget(boolean original, BlockPos pos, @Local(name = "selected") ItemStack selected) {
        if(MultiBlock.isSameMultiBlock(minecraft.level, destroyBlockPos, pos) && !destroyingItem.shouldCauseBlockBreakReset(selected)) {
            return true;
        }

        return original;
    }
}
