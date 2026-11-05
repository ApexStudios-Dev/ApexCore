package dev.apexstudios.apexcore.api.multiblock;

import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Vector3i;

@FunctionalInterface
public interface MultiBlockPositionTransformer {
    MultiBlockPositionTransformer NOOP = (blockState, pos, index) -> pos;

    MultiBlockPositionTransformer CLOCKWISE_90 = (blockState, pos, index) -> pos.set(-pos.z(), pos.y(), pos.x());
    MultiBlockPositionTransformer CLOCKWISE_180 = (blockState, pos, index) -> pos.set(-pos.x(), pos.y(), -pos.z());
    MultiBlockPositionTransformer COUNTERCLOCKWISE_90 = (blockState, pos, index) -> pos.set(pos.z(), pos.y(), -pos.x());

    Vector3i apply(BlockState blockState, Vector3i pos, int index);

    default MultiBlockPositionTransformer andThen(MultiBlockPositionTransformer after) {
        Objects.requireNonNull(after);

        if(this == NOOP) {
            return after;
        } else if(after == NOOP) {
            return this;
        }

        return (blockState, pos, index) -> after.apply(blockState, apply(blockState, pos, index), index);
    }

    static MultiBlockPositionTransformer facing(Property<Direction> property) {
        return (blockState, pos, index) -> forRotation(rotationFromFacing(blockState.getValue(property))).apply(blockState, pos, index);
    }

    static MultiBlockPositionTransformer forRotation(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> CLOCKWISE_90;
            case CLOCKWISE_180 -> CLOCKWISE_180;
            case COUNTERCLOCKWISE_90 -> COUNTERCLOCKWISE_90;
            default -> NOOP;
        };
    }

    static Rotation rotationFromFacing(Direction direction) {
        return switch (direction) {
            case SOUTH -> Rotation.CLOCKWISE_90;
            case NORTH -> Rotation.COUNTERCLOCKWISE_90;
            case WEST -> Rotation.CLOCKWISE_180;
            default -> Rotation.NONE;
        };
    }
}
