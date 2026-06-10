package dev.apexstudios.apexcore.client.placement;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;
import net.neoforged.neoforge.common.NeoForge;

public class PlacementRenderer {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlacementRenderer::extract);
        NeoForge.EVENT_BUS.addListener(PlacementRenderer::submit);
    }

    private static void extract(ExtractLevelRenderStateEvent event) {
        var levelRenderState = event.getRenderState();
        var level = event.getLevel();

        var client = Minecraft.getInstance();
        var player = client.player;
        var blockModelResolver = client.getBlockModelResolver();

        if(player == null) {
            return;
        }

        if(!(client.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }

        var placementRenderState = PlacementRenderState.create(level, player, InteractionHand.MAIN_HAND, hitResult, blockModelResolver);

        if(placementRenderState == null) {
            placementRenderState = PlacementRenderState.create(level, player, InteractionHand.OFF_HAND, hitResult, blockModelResolver);
        }

        if(placementRenderState != null) {
            levelRenderState.setRenderData(PlacementRenderState.KEY, placementRenderState);
        }
    }

    private static void submit(SubmitCustomGeometryEvent event) {
        var levelRenderState = event.getLevelRenderState();
        var placementRenderState = levelRenderState.getRenderData(PlacementRenderState.KEY);

        if(placementRenderState == null) {
            return;
        }

        var cameraPos = levelRenderState.cameraRenderState.pos;
        var placementPos = placementRenderState.pos;

        var pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(
                placementPos.getX() - cameraPos.x(),
                placementPos.getY() - cameraPos.y(),
                placementPos.getZ() - cameraPos.z()
        );

        var nodeCollector = event.getSubmitNodeCollector();
        nodeCollector.submitSpecial(RenderPhaseKeys.ALWAYS_ON_TOP, placementRenderState.asExtendedModelSubmit(pose));

        pose.popPose();
    }
}
