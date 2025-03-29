package dev.apexstudios.apexcore.lib.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.core.placement.FluidVertexConsumer;
import dev.apexstudios.apexcore.core.placement.GhostVertexConsumer;
import dev.apexstudios.apexcore.lib.level.FakeLevel;
import dev.apexstudios.apexcore.lib.util.ApexRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface BlockPlacementRenderer {
    boolean renderForHand(Level level, Player player, InteractionHand hand, BlockHitResult hitResult, Camera camera, PoseStack pose, MultiBufferSource.BufferSource buffers);

    static void renderAt(Camera camera, PoseStack pose, Runnable runnable) {
        var camPos = camera.getPosition();

        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);

        runnable.run();

        pose.popPose();
    }

    static void renderLevel(BlockPlaceContext context, PoseStack pose, boolean canBePlaced) {
        var buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        renderBlockStates(context, buffers, pose, canBePlaced);
        buffers.endBatch();

        renderFluidStates(context, buffers, pose, canBePlaced);
        buffers.endBatch();

        renderBlockEntities(context, buffers, pose, canBePlaced);
        buffers.endBatch();
    }

    static void renderBlockStates(BlockPlaceContext context, MultiBufferSource.BufferSource buffers, PoseStack pose, boolean canBePlaced) {
        var level = (FakeLevel) context.getLevel();
        level.positions().forEach(pos -> renderBlockState(level, pos, level.getBlockState(pos), buffers, pose, canBePlaced));
    }

    static void renderBlockState(Level level, BlockPos pos, BlockState blockState, MultiBufferSource.BufferSource buffers, PoseStack pose, boolean canBePlaced) {
        if(blockState.getRenderShape() == RenderShape.INVISIBLE)
            return;

        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var modelRenderer = blockRenderer.getModelRenderer();
        var model = blockRenderer.getBlockModel(blockState);

        pose.pushPose();
        // pose.translate(blockState.getOffset(pos)); // 'tesselateBlock' does the offset translation for us
        pose.translate(pos.getX(), pos.getY(), pos.getZ());

        var modelParts = model.collectParts(level, pos, blockState, RandomSource.create(blockState.getSeed(pos)));
        var overlay = canBePlaced ? OverlayTexture.NO_OVERLAY : OverlayTexture.pack(OverlayTexture.RED_OVERLAY_V, OverlayTexture.NO_WHITE_U);
        modelRenderer.tesselateBlock(level, modelParts, blockState, pos, pose, new GhostVertexConsumer(buffers.getBuffer(ApexRenderTypes.entityTranslucentNoDepth(TextureAtlas.LOCATION_BLOCKS)), 170), false, overlay);

        pose.popPose();
    }

    static void renderFluidStates(BlockPlaceContext context, MultiBufferSource.BufferSource buffers, PoseStack pose, boolean canBePlaced) {
        var level = (FakeLevel) context.getLevel();
        level.positions().forEach(pos -> renderFluidState(level, pos, level.getBlockState(pos), buffers, pose, canBePlaced));
    }

    static void renderFluidState(BlockAndTintGetter level, BlockPos pos, BlockState blockState, MultiBufferSource.BufferSource buffers, PoseStack pose, boolean canBePlaced) {
        renderFluidState(level, pos, blockState, blockState.getFluidState(), buffers, pose, canBePlaced);
    }

    static void renderFluidState(BlockAndTintGetter level, BlockPos pos, BlockState blockState, FluidState fluidState, MultiBufferSource.BufferSource buffers, PoseStack pose, boolean canBePlaced) {
        if(fluidState.isEmpty())
            return;

        pose.pushPose();
        pose.translate(pos.getX(), pos.getY(), pos.getZ());

        Minecraft.getInstance().getBlockRenderer().renderLiquid(
                pos,
                level,
                new GhostVertexConsumer(new FluidVertexConsumer(buffers.getBuffer(ApexRenderTypes.translucentNoDepth()), pose, pos), 170),
                blockState,
                fluidState
        );

        pose.popPose();
    }

    static void renderBlockEntities(BlockPlaceContext context, MultiBufferSource.BufferSource buffers, PoseStack pose, boolean canBePlaced) {
        var level = (FakeLevel) context.getLevel();
        level.positions().forEach(pos -> renderBlockEntity(level.getBlockEntity(pos), buffers, pose, canBePlaced));
    }

    static void renderBlockEntity(@Nullable BlockEntity blockEntity, MultiBufferSource.BufferSource buffers, PoseStack pose, boolean canBePlaced) {
        if(blockEntity == null)
            return;

        var pos = blockEntity.getBlockPos();

        pose.pushPose();
        pose.translate(pos.getX(), pos.getY(), pos.getZ());

        Minecraft.getInstance().getBlockEntityRenderDispatcher().render(blockEntity, 0F, pose, buffers);

        pose.popPose();
    }

    static BlockState getDefaultBlockState(LevelReader realLevel, BlockPlaceContext context, BlockState blockState) {
        var pos = context.getClickedPos();

        if(blockState.hasProperty(BlockStateProperties.WATERLOGGED))
            blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, realLevel.getFluidState(pos).getType().isSame(Fluids.WATER));
        if(blockState.hasProperty(BlockStateProperties.ROTATION_16))
            blockState = blockState.setValue(BlockStateProperties.ROTATION_16, RotationSegment.convertToSegment(context.getRotation() + 180F));

        if(blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            var facing = context.getHorizontalDirection();

            if(blockState.getBlock() instanceof AbstractFurnaceBlock)
                facing = facing.getOpposite();
            else if(blockState.is(BlockTags.ANVIL))
                facing = facing.getClockWise();

            blockState = blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        } else if(blockState.hasProperty(BlockStateProperties.FACING)) {
            var facing = context.getClickedFace();

            if(blockState.is(Tags.Blocks.BARRELS))
                facing = context.getNearestLookingDirection().getOpposite();

            blockState = blockState.setValue(BlockStateProperties.FACING, facing);
        }

        return PlacementRenderEvent.getDefaultBlockState(realLevel, context, blockState);
    }
}
