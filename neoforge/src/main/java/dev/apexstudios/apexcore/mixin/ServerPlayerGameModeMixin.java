package dev.apexstudios.apexcore.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.apexstudios.apexcore.api.multiblock.MultiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    private BlockPos destroyPos;

    @ModifyExpressionValue(
            method = "handleBlockBreakAction",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z"
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/core/BlockPos;equals(Ljava/lang/Object;)Z"
                    )
            }
    )
    private boolean ApexCore$handleBlockBreakAction(boolean original, @Local(name = "pos") BlockPos pos) {
        if(MultiBlock.isSameMultiBlock(level, destroyPos, pos)) {
            return true;
        }

        return original;
    }
}
