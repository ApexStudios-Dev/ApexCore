package dev.apexstudios.apexcore.lib.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import dev.apexstudios.apexcore.core.ApexCore;
import java.util.function.BiFunction;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public interface ApexRenderTypes {
    BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_NO_DEPTH = Util.memoize((texture, outline) -> RenderType.create(
            ApexCore.id("entity_translucent_no_depth"),
            RenderType.TRANSIENT_BUFFER_SIZE, true, true,
            Pipelines.ENTITY_TRANSLUCENT_NO_DEPTH,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(outline)
    ));

    RenderType TRANSLUCENT_NO_DEPTH = RenderType.create(
            ApexCore.id("translucent_no_depth"),
            RenderType.SMALL_BUFFER_SIZE, true, true,
            Pipelines.TRANSLUCENT_NO_DEPTH,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .createCompositeState(true)
    );

    static RenderType entityTranslucentNoDepth(ResourceLocation texture, boolean outline) {
        return ENTITY_TRANSLUCENT_NO_DEPTH.apply(texture, outline);
    }

    static RenderType entityTranslucentNoDepth(ResourceLocation texture) {
        return entityTranslucentNoDepth(texture, true);
    }

    static RenderType translucentNoDepth() {
        return TRANSLUCENT_NO_DEPTH;
    }

    static void register(IEventBus modBus) {
        Pipelines.register(modBus);
    }

    interface Pipelines {
        RenderPipeline ENTITY_TRANSLUCENT_NO_DEPTH = RenderPipelines.ENTITY_TRANSLUCENT.toBuilder()
                .withLocation(ApexCore.identifier("pipeline/entity_translucent_no_depth"))
                .withCull(true)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build();

        RenderPipeline TRANSLUCENT_NO_DEPTH = RenderPipelines.TRANSLUCENT.toBuilder()
                .withLocation(ApexCore.identifier("pipeline/translucent_no_depth"))
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build();

        private static void register(IEventBus modBus) {
            modBus.addListener(RegisterRenderPipelinesEvent.class, event -> {
                event.registerPipeline(ENTITY_TRANSLUCENT_NO_DEPTH);
                event.registerPipeline(TRANSLUCENT_NO_DEPTH);
            });
        }
    }
}
