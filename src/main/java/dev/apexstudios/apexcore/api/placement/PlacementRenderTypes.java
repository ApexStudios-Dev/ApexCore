package dev.apexstudios.apexcore.api.placement;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.apexstudios.apexcore.common.ApexCore;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public interface PlacementRenderTypes {
    Function<Identifier, RenderType> TRANSLUCENT_NO_DEPTH = Util.memoize(texture -> RenderType.create(ApexCore.id("translucent_no_depth"), RenderSetup.builder(Pipelines.TRANSLUCENT_NO_DEPTH)
            .withTexture("Sampler0", texture)
            .setOutputTarget(OutputTarget.OUTLINE_TARGET)
            .useLightmap()
            .useOverlay()
            .affectsCrumbling()
            .sortOnUpload()
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .createRenderSetup()));

    static RenderType translucentNoDepth(Identifier texture) {
        return TRANSLUCENT_NO_DEPTH.apply(texture);
    }

    interface Pipelines {
        RenderPipeline TRANSLUCENT_NO_DEPTH = RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE.toBuilder()
                .withLocation(ApexCore.identifier("pipeline/translucent_no_depth"))
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .build();
    }
}
