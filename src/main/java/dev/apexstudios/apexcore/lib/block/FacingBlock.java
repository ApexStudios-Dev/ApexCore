package dev.apexstudios.apexcore.lib.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public interface FacingBlock {
    default Property<Direction> facingProperty() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    default Direction defaultFacing() {
        return Direction.NORTH;
    }

    default Direction facingForPlacement(BlockPlaceContext context) {
        return context.getHorizontalDirection().getOpposite();
    }

    default Direction rotate(Rotation rotation, Direction facing) {
        return rotation.rotate(facing);
    }

    default Direction mirror(Mirror mirror, Direction facing) {
        return rotate(mirror.getRotation(facing), facing);
    }
}
