package dev.apexstudios.apexcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.apexstudios.apexcore.api.FakeItemEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class FakeItemEntityRenderer extends EntityRenderer<FakeItemEntity, ItemEntityRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();

    FakeItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);

        itemModelResolver = context.getItemModelResolver();
        shadowRadius = .15F;
        shadowStrength = .75F;
    }

    @Override
    public ItemEntityRenderState createRenderState() {
        return new ItemEntityRenderState();
    }

    @Override
    public void extractRenderState(FakeItemEntity entity, ItemEntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        var stack = entity.getItem();
        var extensions = IClientItemExtensions.of(stack);

        state.bobOffset = entity.bobOffset;
        state.shouldBob = extensions.shouldBobAsEntity(stack);
        state.shouldSpread = extensions.shouldSpreadAsEntity(stack);
        state.extractItemGroupRenderState(entity, stack, itemModelResolver);
    }

    @Override
    public void submit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if(state.item.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        var boundingBox = state.item.getModelBoundingBox();
        var minOffsetY = -(boundingBox.minY) + ItemEntityRenderer.ITEM_MIN_HOVER_HEIGHT;
        var bob = state.shouldBob ? Mth.sin(state.ageInTicks / 10F + state.bobOffset) * .1F + .1F : 0F;
        poseStack.translate(0F, bob + minOffsetY, 0F);
        poseStack.rotate(Axis.YP, ItemEntity.getSpin(state.ageInTicks, state.bobOffset));
        ItemEntityRenderer.submitMultipleFromCount(poseStack, submitNodeCollector, state.lightCoords, state, random, boundingBox);
        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
