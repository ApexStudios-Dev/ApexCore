package dev.apexstudios.apexcore.api.ghost;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public interface GhostBedPartPlacementExtractor extends GhostHorizontalFacingPlacementExtractor {
    default Property<BedPart> getBedPartProperty() {
        return BlockStateProperties.BED_PART;
    }

    default BedPart getBedPart(int index) {
        return index == 0 ? BedPart.FOOT : BedPart.HEAD;
    }

    @Override
    default Direction getFacing(BlockPlaceContext context) {
        return context.getHorizontalDirection();
    }

    @Override
    default int getBlockCount(ItemStack stack) {
        return 2;
    }

    @Override
    default @Nullable BlockState getBlockState(BlockPlaceContext context, CollisionContext collisionContext, int index) {
        var blockState = GhostHorizontalFacingPlacementExtractor.super.getBlockState(context, collisionContext, index);

        if(blockState != null && index != 0) {
            blockState = blockState.cycle(getBedPartProperty());
        }

        return blockState;
    }

    @Override
    default BlockState getDefaultBlockState(BlockPlaceContext context, CollisionContext collisionContext, int index) {
        var blockState = GhostHorizontalFacingPlacementExtractor.super.getDefaultBlockState(context, collisionContext, index);
        return blockState.setValue(getBedPartProperty(), getBedPart(index));
    }

    @Override
    default BlockPos getPos(BlockPlaceContext context, CollisionContext collisionContext, BlockState blockState, int index) {
        var pos = GhostHorizontalFacingPlacementExtractor.super.getPos(context, collisionContext, blockState, index);
        var direction = BedBlock.getConnectedDirection(blockState).getOpposite();
        return pos.relative(direction, index);
    }

    @Override
    default boolean canPlaceAt(BlockPlaceContext context, CollisionContext collisionContext, BlockPos pos, BlockState blockState, int index) {
        if(!GhostHorizontalFacingPlacementExtractor.super.canPlaceAt(context, collisionContext, pos, blockState, index)) {
            return false;
        }

        if(index == 0) {
            return true;
        }

        var relative = pos.relative(blockState.getValue(getFacingProperty()));
        return context.getLevel().getBlockState(relative).canBeReplaced(context);
    }
}
