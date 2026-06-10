package dev.apexstudios.apexcore.client.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.apexcore.mixin.BlockItemAccessor;
import dev.apexstudios.apexcore.mixin.client.BlockModelRenderStateAccessor;
import java.util.List;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class PlacementRenderState {
    static final ContextKey<PlacementRenderState> KEY = new ContextKey<>(ApexCore.identifier("placment_renderer"));
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    public final BlockModelRenderState blockRenderState = new BlockModelRenderState();
    public BlockPos pos = BlockPos.ZERO;
    public boolean valid = true;

    private PlacementRenderState() {

    }

    public BlockModelFeatureRenderer.Submit asExtendedModelSubmit(PoseStack poseStack) {
        var accessor = (BlockModelRenderStateAccessor) blockRenderState;

        var modelParts = accessor.ApexCore$getModelParts();
        modelParts = modelParts == null ? List.of() : modelParts;

        return new BlockModelFeatureRenderer.Submit(
                poseStack.last().copy(),
                Sheets.translucentBlockItemSheet(),
                modelParts,
                blockRenderState.tintLayers().toIntArray(),
                LightCoordsUtil.FULL_SKY,
                valid ? OverlayTexture.NO_OVERLAY : OverlayTexture.pack(OverlayTexture.NO_WHITE_U, OverlayTexture.RED_OVERLAY_V),
                ARGB.color(.75F, CommonColors.WHITE),
                null
        );
    }

    public static @Nullable PlacementRenderState create(Level level, Player player, InteractionHand hand, BlockHitResult hitResult, BlockModelResolver blockModelResolver) {
        var stack = player.getItemInHand(hand);

        if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem item)) {
            return null;
        }

        var block = item.getBlock();

        if(block == Blocks.AIR) {
            return null;
        }

        var placeContext = new BlockPlaceContext(level, player, hand, stack, hitResult);
        var newPlaceContext = item.updatePlacementContext(placeContext);
        var enabledFeatures = level.enabledFeatures();
        var valid = item.isEnabled(enabledFeatures) && block.isEnabled(enabledFeatures);

        if(newPlaceContext == null) {
            valid = false;
        } else {
            placeContext = newPlaceContext;
            valid = valid && placeContext.canPlace();
        }

        var blockState = ((BlockItemAccessor) item).PlacementVisualizer$getPlacementState(placeContext);

        if(blockState == null) {
            valid = false;
            blockState = block.getStateForPlacement(placeContext);
        }

        if(blockState == null) {
            valid = false;
            blockState = block.defaultBlockState();
        }

        var pos = placeContext.getClickedPos();

        if(!blockState.canSurvive(level, pos)) {
            valid = false;
        }

        blockState = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).apply(blockState);

        var placementRenderState = new PlacementRenderState();
        blockModelResolver.update(placementRenderState.blockRenderState, blockState, BLOCK_DISPLAY_CONTEXT);
        placementRenderState.pos = pos.immutable();
        placementRenderState.valid = valid;
        return placementRenderState;
    }
}
