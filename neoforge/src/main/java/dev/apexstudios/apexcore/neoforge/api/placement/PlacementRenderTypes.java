package dev.apexstudios.apexcore.neoforge.api.placement;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.apexstudios.apexcore.xplat.common.ApexCoreXplat;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public interface PlacementRenderTypes {
    // Render type for block ghosting effect
    BiFunction<Identifier, Boolean, RenderType> TRANSLUCENT_NO_DEPTH = Util.memoize((texture, outline) -> RenderType.create("translucent_no_depth", RenderSetup
            .builder(Pipelines.TRANSLUCENT_NO_DEPTH)
            .useLightmap()
            .useOverlay()
            .withTexture("Sampler0", texture, () -> RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true))
            .sortOnUpload()
            .bufferSize(RenderType.SMALL_BUFFER_SIZE)
            .setOutputTarget(OutputTarget.OUTLINE_TARGET)
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
                // .withVertexShader("core/rendertype_translucent_moving_block")
                // .withFragmentShader("core/rendertype_translucent_moving_block")
                .withSampler("Sampler0")
                // .withSampler("Sampler2")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                // .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)

                .withLocation(ApexCoreXplat.identifier("pipeline/translucent_no_depth"))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))

                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withSampler("Sampler1")
                .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
                .withShaderDefine("EMISSIVE")
                .build();
    }
}
