package dev.apexstudios.apexcore.api.ghost;

import dev.apexstudios.apexcore.mixin.BlockItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public interface GhostPlacementExtractor {
    Vector3fc NO_OFFSET = new Vector3f(0F);

    default int getBlockCount(ItemStack stack) {
        return 1;
    }

    default @Nullable BlockPlaceContext updateContext(BlockPlaceContext context, CollisionContext collisionContext) {
        return ((BlockItem) context.getItemInHand().getItem()).updatePlacementContext(context);
    }

    default @Nullable BlockState getBlockState(BlockPlaceContext context, CollisionContext collisionContext, int index) {
        return ((BlockItemAccessor) context.getItemInHand().getItem()).ApexCore$getPlacementState(context);
    }

    default BlockState getDefaultBlockState(BlockPlaceContext context, CollisionContext collisionContext, int index) {
        return ((BlockItem) context.getItemInHand().getItem()).getBlock().defaultBlockState();
    }

    default BlockPos getPos(BlockPlaceContext context, CollisionContext collisionContext, BlockState blockState, int index) {
        return context.getClickedPos();
    }

    default ModelData getModelData(BlockPlaceContext context, CollisionContext collisionContext, BlockPos pos, BlockState blockState, int index) {
        return ModelData.EMPTY;
    }

    default long getSeed(BlockPlaceContext context, CollisionContext collisionContext, BlockPos pos, BlockState blockState, int index) {
        return blockState.getSeed(pos);
    }

    default Vector3fc getOffset(BlockPlaceContext context, CollisionContext collisionContext, BlockPos pos, BlockState blockState, int index) {
        return NO_OFFSET;
    }

    default BlockState applyComponents(BlockPlaceContext context, CollisionContext collisionContext, BlockPos pos, BlockState blockState, int index) {
        return context.getItemInHand().getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).apply(blockState);
    }

    default boolean canPlaceAt(BlockPlaceContext context, CollisionContext collisionContext, BlockPos pos, BlockState blockState, int index) {
        var level = context.getLevel();

        if(!level.isInValidBounds(pos) || !level.isInWorldBounds(pos)) {
            return false;
        }

        if(!level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }

        return ((BlockItemAccessor) context.getItemInHand().getItem()).ApexCore$canPlace(context, blockState);
    }
}
