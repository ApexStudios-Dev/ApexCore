package dev.apexstudios.apexcore.neoforge.client.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.apexstudios.apexcore.neoforge.api.placement.PlacementRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class GhostNodeStorage extends DelegateNodeCollector {
    private final boolean validPlacement;
    private final ModelBlockRenderer blockRenderer;

    public GhostNodeStorage(SubmitNodeCollector delegate, boolean validPlacement) {
        super(delegate);

        this.validPlacement = validPlacement;

        var client = Minecraft.getInstance();
        var ao = client.options.ambientOcclusion().get();
        blockRenderer = new ModelBlockRenderer(ao, false, client.getBlockColors());
    }

    @Override
    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer renderer) {
        submitCustomGeometry(poseStack, renderType, null, renderer);
    }

    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, @Nullable TextureAtlasSprite sprite, CustomGeometryRenderer renderer) {
        super.submitCustomGeometry(poseStack, PlacementRenderTypes.translucentNoDepth(extractTexture(sprite, renderType)), (pose, consumer) -> renderer.render(pose, new GhostVertexConsumer(consumer)));
    }

    private void submitBlock(PoseStack poseStack, RenderType renderType, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model) {
        submitCustomGeometry(poseStack, renderType, (pose, buffer) -> blockRenderer.tesselateBlock(
                blockOutput(pose, buffer),
                0F, 0F, 0F,
                level,
                pos,
                blockState,
                model,
                blockState.getSeed(pos)
        ));
    }

    private BlockQuadOutput blockOutput(PoseStack.Pose pose, VertexConsumer buffer) {
        var stack = toStack(pose);

        return (x, y, z, quad, instance) -> {
            instance.setOverlayCoords(overlayCoords(instance.overlayCoords()));

            stack.pushPose();
            stack.translate(x, y, z);
            buffer.putBakedQuad(pose, quad, instance);
            stack.popPose();
        };
    }

    @Override
    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState renderState) {
        submitBlock(
                poseStack,
                RenderTypes.translucentMovingBlock(),
                renderState,
                renderState.blockPos,
                renderState.blockState,
                Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderState.blockState)
        );
    }

    @Override
    public <S> void submitModel(Model<? super S> model, S renderState, PoseStack poseStack, RenderType renderType, int packedLight, int packedOverlay, int tintColor, @Nullable TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        submitCustomGeometry(poseStack, renderType, sprite, (pose, consumer) -> {
            model.setupAnim(renderState);
            model.renderToBuffer(toStack(pose), wrap(sprite, consumer), packedLight, overlayCoords(packedOverlay), tintColor);
        });
    }

    @Override
    public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int packedLight, int packedOverlay, @Nullable TextureAtlasSprite sprite, boolean sheeted, boolean hasFoil, int tintColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int outlineColor) {
        submitCustomGeometry(
                poseStack,
                renderType,
                sprite,
                (pose, consumer) -> modelPart.render(toStack(pose), wrap(sprite, consumer), packedLight, overlayCoords(packedOverlay), tintColor)
        );
    }

    public int overlayCoords(int overlayCoords) {
        return validPlacement ? overlayCoords : OverlayTexture.pack(OverlayTexture.RED_OVERLAY_V, OverlayTexture.NO_WHITE_U);
    }

    public int tint(int tint) {
        return validPlacement ? tint : CommonColors.SOFT_RED;
    }

    public static PoseStack toStack(PoseStack.Pose pose) {
        var stack = new PoseStack();
        stack.last().set(pose);
        return stack;
    }

    public static Identifier extractTexture(@Nullable TextureAtlasSprite sprite, RenderType renderType) {
        if(sprite != null) {
            return sprite.atlasLocation();
        }

        var texture = renderType.state.textures.get("Sampler0");

        if(texture != null) {
            return texture.location();
        }

        return TextureAtlas.LOCATION_BLOCKS;
    }

    public static VertexConsumer wrap(@Nullable TextureAtlasSprite sprite, VertexConsumer consumer) {
        return sprite == null ? consumer : sprite.wrap(consumer);
    }
}
