package dev.apexstudios.apexcore.api.placement;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.apexstudios.apexcore.common.ApexCore;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public interface PlacementRenderTypes {
    // Render type for block ghosting effect
    BiFunction<Identifier, Boolean, RenderType> TRANSLUCENT_NO_DEPTH = Util.memoize((texture, outline) -> RenderType.create("translucent_no_depth", RenderSetup
            .builder(Pipelines.TRANSLUCENT_NO_DEPTH)
            //.useLightmap()
            .useOverlay()
            .withTexture("Sampler0", texture, RenderTypes.MOVING_BLOCK_SAMPLER)
            .sortOnUpload()
            .bufferSize(RenderType.SMALL_BUFFER_SIZE)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    ));

    static RenderType translucentNoDepth(Identifier texture, boolean outline) {
        return TRANSLUCENT_NO_DEPTH.apply(texture, outline);
    }

    static RenderType translucentNoDepth(Identifier texture) {
        return translucentNoDepth(texture, true);
    }

    static RenderType translucentNoDepth(boolean outline) {
        return translucentNoDepth(TextureAtlas.LOCATION_BLOCKS, outline);
    }

    static RenderType translucentNoDepth() {
        return translucentNoDepth(true);
    }

    interface Pipelines {
        RenderPipeline TRANSLUCENT_NO_DEPTH = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                //.withLocation("pipeline/translucent_moving_block")
                //.withVertexShader("core/rendertype_translucent_moving_block")
                //.withFragmentShader("core/rendertype_translucent_moving_block")
                .withSampler("Sampler0")
                .withBlend(BlendFunction.TRANSLUCENT)
                .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)

                .withLocation(ApexCore.identifier("pipeline/translucent_no_depth"))
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)

                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withSampler("Sampler1")
                .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
                .withShaderDefine("EMISSIVE")
                .build();
    }
}
