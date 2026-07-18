package dev.apexstudios.apexcore.client.ghost;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.apexstudios.apexcore.common.ApexCore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.event.RegisterFeatureRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

public final class GhostBlockFeatureRenderer implements FeatureRenderer<GhostBlockFeatureSubmit> {
    public static final FeatureRendererType<GhostBlockFeatureSubmit> ALWAYS_ON_TOP_TYPE = FeatureRendererType.create(ApexCore.id("ghost_blocks/always_on_top"));
    public static final FeatureRendererType<GhostBlockFeatureSubmit> AFTER_TERRAIN_TYPE = FeatureRendererType.create(ApexCore.id("ghost_blocks/after_terrain"));
    private static final OutputTarget PARTICLE_TARGET = new OutputTarget(ApexCore.id("particle_target"), () -> Minecraft.getInstance().levelRenderer.particlesTarget());

    private final boolean alwaysOnTop;
    private final Function<BlockColors, ModelBlockRenderer> blockRendererCache = Util.memoize(blockColors -> new ModelBlockRenderer(false, false, blockColors));
    private final List<StagedVertexBuffer.Draw> draws = new ArrayList<>();
    private @Nullable GpuBufferSlice dynamicTransforms;

    private GhostBlockFeatureRenderer(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
    }

    @SuppressWarnings("resource")
    @Override
    public void prepareGroup(FeatureFrameContext context, List<GhostBlockFeatureSubmit> submits, boolean strictlyOrdered) {
        var vertexBuffer = context.stagedVertexBuffer();
        var blockStateModelSet = context.blockStateModelSet();
        var blockRenderer = blockRendererCache.apply(context.blockColors());

        var draw = vertexBuffer.appendDraw(DefaultVertexFormat.BLOCK, PrimitiveTopology.QUADS, VertexSorting.DISTANCE_TO_ORIGIN);
        var vertexConsumer = new GhostVertexConsumer(vertexBuffer.getVertexBuilder(draw), alwaysOnTop);

        for(var submit : submits) {
            var level = submit.level();

            level.forEachGhost((posKey, ghost) -> {
                var pose = submit.pose().copy();
                var pos = BlockPos.of(posKey);
                pose.translate(pos.getX(), pos.getY(), pos.getZ());

                var blockState = ghost.blockState();
                var offset = ghost.offset();

                blockRenderer.tesselateBlock(
                        putBakedQuad(pose, vertexConsumer),
                        offset.x(),
                        offset.y(),
                        offset.z(),
                        level,
                        pos,
                        blockState,
                        blockStateModelSet.get(blockState),
                        ghost.seed()
                );
            });
        }

        draws.add(draw);
    }

    @Override
    public void finishPrepare(FeatureFrameContext context) {
        dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy());
    }

    @SuppressWarnings({"resource", "deprecation"})
    @Override
    public void executeGroup(FeatureFrameContext context, int groupIndex, List<GhostBlockFeatureSubmit> submits, boolean strictlyOrdered) {
        var draw = draws.get(groupIndex);
        var executeInfo = context.stagedVertexBuffer().getExecuteInfo(draw);

        if (executeInfo == null) {
            return;
        }

        var textureManager = context.textureManager();
        var lightMap = context.lightmap();
        var renderTarget = (alwaysOnTop ? OutputTarget.MAIN_TARGET : PARTICLE_TARGET).getRenderTarget();

        try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "ghost_feature_renderer",
                Objects.requireNonNull(renderTarget.getColorTextureView()),
                Optional.empty(),
                renderTarget.getDepthTextureView(),
                OptionalDouble.empty()
        )) {
            renderPass.setPipeline(RenderPipelines.TRANSLUCENT_BLOCK);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", Objects.requireNonNull(dynamicTransforms));
            renderPass.setVertexBuffer(0, executeInfo.vertexBuffer().slice());
            renderPass.setIndexBuffer(executeInfo.indexBuffer(), executeInfo.indexType());

            renderPass.bindTexture(
                    "Sampler0",
                    textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView(),
                    RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true)
            );

            renderPass.bindTexture(
                    "Sampler2",
                    lightMap,
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
            );

            renderPass.drawIndexed(executeInfo.indexCount(), 1, executeInfo.firstIndex(), executeInfo.baseVertex(), 0);
        }
    }

    @Override
    public void finishExecute(FeatureFrameContext context) {
        draws.clear();
        dynamicTransforms = null;
    }

    private static BlockQuadOutput putBakedQuad(PoseStack.Pose pose, VertexConsumer vertexConsumer) {
        return (x, y, z, quad, instance) -> {
            pose.translate(x, y, z);
            vertexConsumer.putBakedQuad(pose, quad, instance);
            pose.translate(-x, -y, -z);
        };
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(RegisterFeatureRenderersEvent.class, event -> event.register(ALWAYS_ON_TOP_TYPE, new GhostBlockFeatureRenderer(true)));
        NeoForge.EVENT_BUS.addListener(RegisterFeatureRenderersEvent.class, event -> event.register(AFTER_TERRAIN_TYPE, new GhostBlockFeatureRenderer(false)));
    }
}
