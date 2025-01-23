package dev.apexstudios.apexcore.mixin;

import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DoublePlantBlock.class)
public class DoublePlantBlockMixin {
    @Inject(
            method = "preventDropFromBottomPart",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void ApexCore$preventDropFromBottomPart(Level level, BlockPos pos, BlockState blockState, Player player, CallbackInfo ci) {
        if(BlockComponentHelper.hasComponent(blockState, BlockComponentTypes.MULTI_BLOCK))
            ci.cancel();
    }
}
