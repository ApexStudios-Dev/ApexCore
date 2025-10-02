package dev.apexstudios.apexcore.lib.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.core.placement.GhostVertexConsumer;
import dev.apexstudios.apexcore.lib.util.ApexRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface SubmitBlockPlacementState<T> {
    void submit(T state, LevelRenderState levelState, BlockPlacementState placementState, PoseStack pose, SubmitNodeCollector collector);

    static void submitGhostBlock(PoseStack pose, BlockAndTintGetter level, BlockPos pos, BlockState blockState, boolean canBePlaced, SubmitNodeCollector collector) {
        if(blockState.getRenderShape() == RenderShape.INVISIBLE)
            return;

        var client = Minecraft.getInstance();
        var overlay = canBePlaced ? OverlayTexture.NO_OVERLAY : OverlayTexture.pack(OverlayTexture.RED_OVERLAY_V, OverlayTexture.NO_WHITE_U);
        var lightColor = LevelRenderer.getLightColor(level, pos);
        var blockColor = client.getBlockColors().getColor(blockState, level, pos, 0);
        var model = client.getBlockRenderer().getBlockModel(blockState);

        collector.submitCustomGeometry(pose, ApexRenderTypes.entityTranslucentNoDepth(TextureAtlas.LOCATION_BLOCKS), (pose1, consumer) -> {
            var r = ARGB.redFloat(blockColor);
            var g = ARGB.greenFloat(blockColor);
            var b = ARGB.blueFloat(blockColor);

            ModelBlockRenderer.renderModel(pose1, new GhostVertexConsumer(consumer, 170), model, r, g, b, lightColor, overlay);
        });

        client.getModelManager().specialBlockModelRenderer().get().renderByBlock(blockState.getBlock(), ItemDisplayContext.NONE, pose, collector, lightColor, overlay, 0);
    }
}
