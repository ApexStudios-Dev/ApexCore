package dev.apexstudios.apexcore.client.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.api.placement.ExtractPlacementRenderState;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.apexcore.mixin.BlockItemAccessor;
import dev.apexstudios.apexcore.mixin.client.BlockModelRenderStateAccessor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;
import net.neoforged.neoforge.common.NeoForge;

public final class PlacementRenderState {
    static final ContextKey<PlacementRenderState> KEY = new ContextKey<>(ApexCore.identifier("placment_renderer"));
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static final Long2ObjectMap<BlockState> TEMP_BLOCK_STATES = new Long2ObjectOpenHashMap<>();

    private final Long2ObjectMap<BlockModelRenderState> blockRenderStates = new Long2ObjectOpenHashMap<>();
    public boolean valid = true;

    public void update(BlockPlaceContext placeContext) {
        TEMP_BLOCK_STATES.clear();
        blockRenderStates.forEach((key, blockRenderState) -> blockRenderState.clear());
        blockRenderStates.clear();
        valid = true;

        var context = createContext(placeContext);
        TEMP_BLOCK_STATES.put(context.getClickedPos().asLong(), getStateForPlacement(context));
        NeoForge.EVENT_BUS.post(new ExtractPlacementRenderState(context, TEMP_BLOCK_STATES));

        var blockModelResolver = Minecraft.getInstance().getBlockModelResolver();

        TEMP_BLOCK_STATES.forEach((key, blockState) -> {
            if(blockState.isAir()) {
                return;
            }

            var blockRenderState = new BlockModelRenderState();
            blockModelResolver.update(blockRenderState, blockState, BLOCK_DISPLAY_CONTEXT);
            blockRenderStates.put(key, blockRenderState);
        });
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(cameraRenderState.pos.scale(-1D));

        blockRenderStates.forEach((key, blockRenderState) -> {
            poseStack.pushPose();
            poseStack.translate(
                    BlockPos.getX(key),
                    BlockPos.getY(key),
                    BlockPos.getZ(key)
            );

            submit(poseStack, nodeCollector, blockRenderState);

            poseStack.popPose();
        });

        poseStack.popPose();
    }

    private BlockPlaceContext createContext(BlockPlaceContext placeContext) {
        var item = (BlockItem) placeContext.getItemInHand().getItem();
        var enabledFeatures = placeContext.getLevel().enabledFeatures();
        valid = item.isEnabled(enabledFeatures) && item.getBlock().isEnabled(enabledFeatures);

        var updatedContext = item.updatePlacementContext(placeContext);

        if(updatedContext == null) {
            valid = false;
        } else {
            placeContext = updatedContext;
            valid = valid && placeContext.canPlace();
        }

        return placeContext;
    }

    private BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        var stack = placeContext.getItemInHand();
        var item = (BlockItem) stack.getItem();
        var block = item.getBlock();
        var blockState = ((BlockItemAccessor) item).PlacementVisualizer$getPlacementState(placeContext);

        if(blockState == null) {
            valid = false;
            blockState = block.getStateForPlacement(placeContext);
        }

        if(blockState == null) {
            valid = false;
            blockState = block.defaultBlockState();
        }

        return stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).apply(blockState);
    }

    private void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, BlockModelRenderState blockRenderState) {
        var modelParts = ((BlockModelRenderStateAccessor) blockRenderState).ApexCore$getModelParts();
        modelParts = modelParts == null ? List.of() : modelParts;

        nodeCollector.submitSpecial(RenderPhaseKeys.ALWAYS_ON_TOP, new BlockModelFeatureRenderer.Submit(
                poseStack.last().copy(),
                Sheets.translucentBlockItemSheet(),
                modelParts,
                blockRenderState.tintLayers().toIntArray(),
                LightCoordsUtil.FULL_SKY,
                valid ? OverlayTexture.NO_OVERLAY : OverlayTexture.pack(OverlayTexture.NO_WHITE_U, OverlayTexture.RED_OVERLAY_V),
                ARGB.color(.75F, CommonColors.WHITE),
                null
        ));
    }
}
