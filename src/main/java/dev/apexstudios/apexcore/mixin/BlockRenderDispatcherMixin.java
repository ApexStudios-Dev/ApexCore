package dev.apexstudios.apexcore.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.apexstudios.apexcore.lib.multiblock.MultiBlock;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {
    @Shadow @Final private BlockModelShaper blockModelShaper;
    @Shadow @Final private RandomSource singleThreadRandom;
    @Shadow @Final private List<BlockModelPart> singleThreadPartList;
    @Shadow @Final private ModelBlockRenderer modelRenderer;

    @Inject(
            method = "renderBreakingTexture",
            at = @At("TAIL")
    )
    private void ApexCore$renderBreakingTexture(BlockState blockState, BlockPos pos, BlockAndTintGetter level, PoseStack pose, VertexConsumer consumer, CallbackInfo ci) {
        if(!MultiBlock.isMultiBlock(blockState))
            return;

        var index = MultiBlock.getIndex(blockState);
        // TODO: is the a better way to grab this?
        // its passed in as a method param by callers
        // but everything seemingly routes back to `GameRenderer#mainCamera`
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        var camPos = camera.getPosition();

        var camX = camPos.x();
        var camY = camPos.y();
        var camZ = camPos.z();

        var camOffsetX = pos.getX() - camX;
        var camOffsetY = pos.getY() - camY;
        var camOffsetZ = pos.getZ() - camZ;

        // render the crack progress for every block in the multi block
        MultiBlock.forEachPos(pos, blockState, (otherPos, otherBlockState) -> {
            // current index is handled by vanilla
            if(MultiBlock.getIndex(otherBlockState) == index)
                return;

            pose.pushPose();
            // we are currently rendering at the `pos` pov
            // undo this translation and move to `otherPos`
            pose.translate(-camOffsetX, -camOffsetY, -camOffsetZ);
            pose.translate(otherPos.getX() - camX, otherPos.getY() - camY, otherPos.getZ() - camZ);

            var model = blockModelShaper.getBlockModel(otherBlockState);

            singleThreadRandom.setSeed(otherBlockState.getSeed(otherPos));
            singleThreadPartList.clear();
            model.collectParts(level, otherPos, otherBlockState, singleThreadRandom, singleThreadPartList);
            modelRenderer.tesselateBlock(level, singleThreadPartList, otherBlockState, otherPos, pose, $ -> consumer, true, OverlayTexture.NO_OVERLAY);

            pose.popPose();
        });
    }
}
