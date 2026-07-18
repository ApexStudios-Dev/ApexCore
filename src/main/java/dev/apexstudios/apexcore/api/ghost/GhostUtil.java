package dev.apexstudios.apexcore.api.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.client.ghost.GhostBlockFeatureSubmit;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;

public interface GhostUtil {
    static void submit(
            SubmitNodeCollector nodeCollector,
            PoseStack poseStack,
            Vec3 cameraPosition,
            GhostLevel level
    ) {
        poseStack.pushPose();
        poseStack.translate(cameraPosition.scale(-1D));
        submit(nodeCollector, poseStack, level);
        poseStack.popPose();
    }

    static void submit(
            SubmitNodeCollector nodeCollector,
            PoseStack poseStack,
            GhostLevel level
    ) {
        nodeCollector.submitSpecial(
                level.alwaysOnTop() ? RenderPhaseKeys.ALWAYS_ON_TOP : RenderPhaseKeys.AFTER_TERRAIN,
                new GhostBlockFeatureSubmit(poseStack.last(), level)
        );
    }
}
