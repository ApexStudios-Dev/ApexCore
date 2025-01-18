package dev.apexstudios.apexcore.core.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.lib.placement.BlockPlacementRenderer;
import dev.apexstudios.apexcore.lib.placement.PlacementRenderEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public interface PlacementRendererRegistry {
    static void register() {
        var renderers = PlacementRenderEvent.registerRenderers(registrar -> {
            registrar.accept(new BlockItemPlacementRenderer());
            registrar.accept(new BucketItemPlacementRenderer());
        });

        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, event -> {
            if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
                return;

            var camera = event.getCamera();
            var pose = event.getPoseStack();

            var client = Minecraft.getInstance();
            var level = client.level;
            var player = client.player;
            assert player != null && level != null;
            var alwaysRender = false;

            if(!(client.hitResult instanceof BlockHitResult hitResult) || (hitResult.getType() != HitResult.Type.BLOCK && !alwaysRender))
                return;

            var buffers = client.renderBuffers().bufferSource();

            if(PlacementRendererRegistry.renderForHand(renderers, level, player, InteractionHand.MAIN_HAND, hitResult, camera, pose, buffers))
                return;

            PlacementRendererRegistry.renderForHand(renderers, level, player, InteractionHand.OFF_HAND, hitResult, camera, pose, buffers);
        });
    }

    private static boolean renderForHand(Iterable<BlockPlacementRenderer> renderers, Level level, Player player, InteractionHand hand, BlockHitResult hitResult, Camera camera, PoseStack pose, MultiBufferSource.BufferSource buffers) {
        for(var renderer : renderers) {
            if(renderer.renderForHand(level, player, hand, hitResult, camera, pose, buffers))
                return true;
        }

        return false;
    }
}
