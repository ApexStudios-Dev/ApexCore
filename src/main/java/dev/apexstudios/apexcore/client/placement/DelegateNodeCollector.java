package dev.apexstudios.apexcore.client.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class DelegateNodeCollector implements SubmitNodeCollector {
    private final SubmitNodeCollector delegate;

    public DelegateNodeCollector(SubmitNodeCollector delegate) {
        this.delegate = get(delegate);
    }

    @Override
    public OrderedSubmitNodeCollector order(int index) {
        delegate.order(index);
        return this;
    }

    @Override
    public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {
        delegate.submitShadow(poseStack, radius, pieces);
    }

    @Override
    public void submitNameTag(PoseStack poseStack, @Nullable Vec3 pos, int yOffset, Component text, boolean seethrough, int packedLight, CameraRenderState cameraRenderState) {
        delegate.submitNameTag(poseStack, pos, yOffset, text, seethrough, packedLight, cameraRenderState);
    }

    @Override
    public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow, Font.DisplayMode displayMode, int packedLight, int color, int backgroundColor, int outlineColor) {
        delegate.submitText(poseStack, x, y, string, dropShadow, displayMode, packedLight, color, backgroundColor, outlineColor);
    }

    @Override
    public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {
        delegate.submitFlame(poseStack, renderState, rotation);
    }

    @Override
    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
        delegate.submitLeash(poseStack, leashState);
    }

    @Override
    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState renderState, int outlineColor) {
        delegate.submitMovingBlock(poseStack, renderState, outlineColor);
    }

    @Override
    public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> list, int[] ints, int i, int i1, int i2) {
        delegate.submitBlockModel(poseStack, renderType, list, ints, i, i1, i2);
    }

    @Override
    public void submitBreakingBlockModel(PoseStack poseStack, List<BlockStateModelPart> parts, int progress) {
        delegate.submitBreakingBlockModel(poseStack, parts, progress);
    }

    @Override
    public void submitItem(PoseStack poseStack, ItemDisplayContext itemDisplayContext, int i, int i1, int i2, int[] ints, List<BakedQuad> list, ItemStackRenderState.FoilType foilType) {
        delegate.submitItem(poseStack, itemDisplayContext, i, i1, i2, ints, list, foilType);
    }

    @Override
    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer) {
        delegate.submitCustomGeometry(poseStack, renderType, renderer);
    }

    @Override
    public void submitQuadParticleGroup(QuadParticleRenderState renderState) {
        delegate.submitQuadParticleGroup(renderState);
    }

    @Override
    public <S> void submitModel(Model<? super S> model, S renderState, PoseStack poseStack, RenderType renderType, int packedLight, int packedOverlay, int tintColor, @Nullable TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        delegate.submitModel(model, renderState, poseStack, renderType, packedLight, packedOverlay, tintColor, sprite, outlineColor, crumblingOverlay);
    }

    @Override
    public void submitShapeOutline(PoseStack poseStack, VoxelShape shape, RenderType renderType, int color, float width, boolean afterTerrain) {
        delegate.submitShapeOutline(poseStack, shape, renderType, color, width, afterTerrain);
    }

    @Override
    public void submitGizmoPrimitives(DrawableGizmoPrimitives.Group group, CameraRenderState camera, boolean onTop) {
        delegate.submitGizmoPrimitives(group, camera, onTop);
    }

    public static SubmitNodeCollector get(SubmitNodeCollector collector) {
        return collector instanceof DelegateNodeCollector delegated ? delegated.delegate : collector;
    }
}
