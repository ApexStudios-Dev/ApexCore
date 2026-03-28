package dev.apexstudios.apexcore.neoforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.apexstudios.apexcore.neoforge.api.util.ApexTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @WrapOperation(
            method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"
            )
    )
    private float ApexCore$getDestroySpeed(ItemStack stack, BlockState blockState, Operation<Float> original) {
        if(stack.is(Tags.Items.TOOLS_SHEAR) && blockState.is(ApexTags.Blocks.SHEARS_EFFICIENT)) {
            return 5F;
        }

        return original.call(stack, blockState);
    }
}
