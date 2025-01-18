package dev.apexstudios.apexcore.lib.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.apexstudios.apexcore.core.ApexCore;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public interface ApexRenderTypes {
    RenderStateShard.DepthTestStateShard DEPTH_TEST_NOT_EQUAL = new RenderStateShard.DepthTestStateShard("!=", GL11.GL_NOTEQUAL);

    BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_NO_DEPTH = Util.memoize((texture, outline) -> RenderType.create(
            ApexCore.id("entity_translucent_no_depth"),
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            RenderType.TRANSIENT_BUFFER_SIZE, true, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(DEPTH_TEST_NOT_EQUAL)
                    .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(outline)
    ));

    RenderType TRANSLUCENT_NO_DEPTH = RenderType.create(
            ApexCore.id("translucent_no_depth"),
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            RenderType.SMALL_BUFFER_SIZE, true, true,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    // .setCullState(RenderStateShard.CULL)
                    // .setOverlayState(RenderStateShard.OVERLAY)
                    // .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(DEPTH_TEST_NOT_EQUAL)
                    // .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true)
    );

    RenderType LINES_NO_DEPTH = RenderType.create(
            ApexCore.id("lines_no_depth"),
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            RenderType.TRANSIENT_BUFFER_SIZE,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(DEPTH_TEST_NOT_EQUAL)
                    .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false)
    );

    static RenderType entityTranslucentNoDepth(ResourceLocation texture, boolean outline) {
        return ENTITY_TRANSLUCENT_NO_DEPTH.apply(texture, outline);
    }

    static RenderType entityTranslucentNoDepth(ResourceLocation texture) {
        return entityTranslucentNoDepth(texture, true);
    }

    static RenderType linesNoDepth() {
        return LINES_NO_DEPTH;
    }

    static RenderType translucentNoDepth() {
        return RenderType.create(
                ApexCore.id("translucent_no_depth"),
                DefaultVertexFormat.BLOCK,
                VertexFormat.Mode.QUADS,
                RenderType.SMALL_BUFFER_SIZE, true, true,
                RenderType.CompositeState.builder()
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                        .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        // .setCullState(RenderStateShard.CULL)
                        // .setOverlayState(RenderStateShard.OVERLAY)
                        // .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setDepthTestState(DEPTH_TEST_NOT_EQUAL)
                        // .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
                        .createCompositeState(true)
        );
    }

    static void register() {

    }
}
