package dev.apexstudios.apexcore.mixin;

import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DoorBlock.class)
public class DoorBlockMixin extends Block {
    protected DoorBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(
            method = "onExplosionHit",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ApexCore$onExplosionHit(BlockState blockState, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer, CallbackInfo ci) {
        if(BlockComponentHelper.hasComponent(blockState, BlockComponentTypes.MULTI_BLOCK)) {
            super.onExplosionHit(blockState, level, pos, explosion, dropConsumer);
            ci.cancel();
        }
    }
}
