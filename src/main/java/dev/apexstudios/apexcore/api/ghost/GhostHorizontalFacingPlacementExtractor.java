package dev.apexstudios.apexcore.api.ghost;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;

public interface GhostHorizontalFacingPlacementExtractor extends GhostPlacementExtractor {
    default Property<Direction> getFacingProperty() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    default Direction getFacing(BlockPlaceContext context) {
        return context.getHorizontalDirection().getOpposite();
    }

    @Override
    default BlockState getDefaultBlockState(BlockPlaceContext context, CollisionContext collisionContext, int index) {
        return GhostPlacementExtractor.super.getDefaultBlockState(context, collisionContext, index)
                .setValue(getFacingProperty(), getFacing(context));
    }
}
