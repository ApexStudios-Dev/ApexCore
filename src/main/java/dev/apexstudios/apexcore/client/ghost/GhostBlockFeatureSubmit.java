package dev.apexstudios.apexcore.client.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.api.ghost.GhostLevel;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

public record GhostBlockFeatureSubmit(
        PoseStack.Pose pose,
        GhostLevel level
) implements SubmitNode {
    public GhostBlockFeatureSubmit {
        pose = pose.copy();
        level = level.immutable();
    }

    @Override
    public FeatureRendererType<GhostBlockFeatureSubmit> featureType() {
        return level.alwaysOnTop() ? GhostBlockFeatureRenderer.ALWAYS_ON_TOP_TYPE : GhostBlockFeatureRenderer.AFTER_TERRAIN_TYPE;
    }
}
