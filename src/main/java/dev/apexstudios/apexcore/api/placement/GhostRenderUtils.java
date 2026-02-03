package dev.apexstudios.apexcore.api.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.apexstudios.apexcore.client.placement.GhostVertexConsumer;
import java.util.function.BiConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public interface GhostRenderUtils {
    static PoseStack toStack(PoseStack.Pose pose) {
        var stack = new PoseStack();
        stack.last().set(pose);
        return stack;
    }

    static void submitGhost(OrderedSubmitNodeCollector collector, PoseStack poseStack, RenderType renderType, BiConsumer<PoseStack, VertexConsumer> action) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> action.accept(toStack(pose), new GhostVertexConsumer(consumer)));
    }

    static <T> void submitModel(OrderedSubmitNodeCollector collector, PoseStack poseStack, RenderType renderType, Model<T> model, T modelState, @Nullable TextureAtlasSprite sprite, int light, int overlay, int tintColor, boolean validPlacement) {
        submitGhost(collector, poseStack, PlacementRenderTypes.translucentNoDepth(extractTexture(sprite, renderType)), (stack, consumer) -> {
            model.setupAnim(modelState);
            model.renderToBuffer(stack, wrap(sprite, consumer), light, overlay, validPlacement ? tintColor : CommonColors.SOFT_RED);
        });
    }

    static void submitModelPart(OrderedSubmitNodeCollector collector, PoseStack poseStack, RenderType renderType, ModelPart modelPart, @Nullable TextureAtlasSprite sprite, int light, int overlay, int tintColor, boolean validPlacement) {
        submitGhost(
                collector,
                poseStack,
                PlacementRenderTypes.translucentNoDepth(extractTexture(sprite, renderType)),
                (stack, consumer) -> modelPart.render(stack, wrap(sprite, consumer), light, overlay, validPlacement ? tintColor : CommonColors.SOFT_RED)
        );
    }

    static Identifier extractTexture(@Nullable TextureAtlasSprite sprite, RenderType renderType) {
        if(sprite != null) {
            return sprite.atlasLocation();
        }

        var texture = renderType.state.textures.get("Sampler0");

        if(texture != null) {
            return texture.location();
        }

        return TextureAtlas.LOCATION_BLOCKS;
    }

    static VertexConsumer wrap(@Nullable TextureAtlasSprite sprite, VertexConsumer consumer) {
        return sprite == null ? consumer : sprite.wrap(consumer);
    }
}
