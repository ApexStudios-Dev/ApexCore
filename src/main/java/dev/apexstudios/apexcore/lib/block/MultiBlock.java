package dev.apexstudios.apexcore.lib.block;

import com.google.common.collect.Lists;
import dev.apexstudios.apexcore.lib.util.ApexUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public interface MultiBlock {
    Pattern multiBlockPattern();

    default int getMultiBlockIndex(BlockState blockState) {
        return blockState.getValue(multiBlockPattern().property());
    }

    default BlockState setMultiBlockIndex(BlockState blockState, int index) {
        return blockState.setValue(multiBlockPattern().property(), index);
    }

    default boolean isMultiBlockOrigin(BlockState blockState) {
        return getMultiBlockIndex(blockState) == 0;
    }

    default Vector3ic getMultiBlockLocalPosition(BlockState blockState) {
        var index = getMultiBlockIndex(blockState);
        return multiBlockPattern().positions().get(index);
    }

    default Vector3ic getMultiBlockLocalPositionRotated(BlockState blockState) {
        var position = getMultiBlockLocalPosition(blockState);
        return multiBlockPattern().rotate(blockState, position);
    }

    default BlockPos getMultiBlockOrigin(BlockPos pos, BlockState blockState) {
        var rotated = getMultiBlockLocalPositionRotated(blockState);
        return pos.offset(-rotated.x(), -rotated.y(), -rotated.z());
    }

    default BlockPos getMultiBlockPos(BlockPos origin, BlockState blockState) {
        var rotated = getMultiBlockLocalPositionRotated(blockState);
        return origin.offset(rotated.x(), rotated.y(), rotated.z());
    }

    default boolean isPlacementValidForMultiBlock(BlockPlaceContext context, BlockState placementBlockState) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var placer = context.getPlayer();

        var pattern = multiBlockPattern();
        var originPlacementBlockState = setMultiBlockIndex(placementBlockState, 0);
        var origin = getMultiBlockOrigin(pos, originPlacementBlockState);

        for(var i = 1; i < pattern.positions().size(); i++) {
            var otherBlockState = setMultiBlockIndex(placementBlockState, i);
            var otherPos = getMultiBlockPos(origin, otherBlockState);

            if(!ApexUtil.isInBounds(level, otherPos))
                return false;
            if(!ApexUtil.canPlace(level, otherPos, otherBlockState, placer))
                return false;
        }

        return true;
    }

    default VoxelShape fixVoxelShape(VoxelShape shape, BlockState blockState, BlockPos pos) {
        var origin = getMultiBlockOrigin(pos, blockState);
        var offset = pos.subtract(origin);
        return shape.move(-offset.getX(), -offset.getY(), -offset.getZ());
    }

    static void setBlocks(LevelWriter level, BlockPos pos, BlockState blockState) {
        ifMultiBlock(blockState, multiBlock -> {
            var pattern = multiBlock.multiBlockPattern();
            var index = multiBlock.getMultiBlockIndex(blockState);
            var origin = multiBlock.getMultiBlockOrigin(pos, blockState);

            for(var i = 0; i < pattern.positions().size(); i++) {
                if(i == index)
                    continue;

                var otherBlockState = multiBlock.setMultiBlockIndex(blockState, i);
                var otherPos = multiBlock.getMultiBlockPos(origin, otherBlockState);
                level.setBlock(otherPos, otherBlockState, Block.UPDATE_ALL);
            }
        });
    }

    static <TLevel extends LevelWriter & BlockGetter> void destroyBlocks(TLevel level, BlockPos pos, BlockState blockState) {
        ifMultiBlock(blockState, multiBlock -> {
            var pattern = multiBlock.multiBlockPattern();
            var index = multiBlock.getMultiBlockIndex(blockState);
            var origin = multiBlock.getMultiBlockOrigin(pos, blockState);

            for(var i = 0; i < pattern.positions().size(); i++) {
                if(i == index)
                    continue;

                var otherPos = multiBlock.getMultiBlockPos(origin, multiBlock.setMultiBlockIndex(blockState, i));
                var otherBlockState = level.getBlockState(otherPos);

                if(otherBlockState.is(blockState.getBlock()))
                    level.destroyBlock(otherPos, false);
            }
        });
    }

    static void ifMultiBlock(BlockState blockState, Consumer<MultiBlock> consumer) {
        if(blockState.getBlock() instanceof MultiBlock multiBlock)
            consumer.accept(multiBlock);
    }

    static Pattern of(Consumer<Builder> consumer) {
        var builder = new Builder();
        consumer.accept(builder);
        return new Builder.PatternImpl(builder);
    }

    static Vector3ic rotate(Vector3ic pos, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new Vector3i(-pos.z(), pos.y(), pos.x());
            case CLOCKWISE_180 -> new Vector3i(-pos.x(), pos.y(), -pos.z());
            case COUNTERCLOCKWISE_90 -> new Vector3i(pos.z(), pos.y(), -pos.x());
            case NONE -> pos;
        };
    }

    static Vector3ic rotateHorizontal(Vector3ic pos, Direction facing) {
        return rotate(pos, horizontalRotation(facing));
    }

    static Rotation horizontalRotation(Direction facing) {
        return switch (facing) {
            case Direction.NORTH -> Rotation.CLOCKWISE_90;
            case Direction.SOUTH -> Rotation.COUNTERCLOCKWISE_90;
            case Direction.EAST -> Rotation.CLOCKWISE_180;
            default -> Rotation.NONE;
        };
    }

    sealed interface Pattern {
        Property<Integer> property();

        List<Vector3ic> positions();

        Vector3ic rotate(BlockState blockState, Vector3ic position);
    }

    final class Builder {
        private final List<Vector3ic> positions = Lists.newArrayList();
        @Nullable private BiFunction<BlockState, Vector3ic, Vector3ic> rotationFunction = null;

        private Builder() {
            with(0, 0, 0);
        }

        public Builder with(Vector3ic position) {
            positions.add(position);
            return this;
        }

        public Builder with(int x, int y, int z) {
            return with(new Vector3i(x, y, z));
        }

        public Builder rotating(BiFunction<BlockState, Vector3ic, Vector3ic> rotationFunction) {
            this.rotationFunction = rotationFunction;
            return this;
        }

        public Builder rotatingHorizontal(Property<Direction> facingProperty) {
            return rotating((blockState, position) -> rotateHorizontal(position, blockState.getValue(facingProperty)));
        }

        public Builder rotateFromInterface(Function<Direction, Rotation> rotationFunction) {
            return rotating((blockState, position) -> {
                var facingProperty = ((FacingBlock) blockState.getBlock()).facingProperty();
                var facing = blockState.getValue(facingProperty);
                return rotate(position, rotationFunction.apply(facing));
            });
        }

        public Builder rotateHorizontalFromInterface() {
            return rotateFromInterface(MultiBlock::horizontalRotation);
        }

        private static final class PatternImpl implements Pattern {
            private static final Int2ObjectMap<IntegerProperty> PROPERTIES = new Int2ObjectOpenHashMap<>();

            private final List<Vector3ic> positions;
            private final IntegerProperty property;
            private final BiFunction<BlockState, Vector3ic, Vector3ic> rotationFunction;

            private PatternImpl(Builder builder) {
                positions = List.copyOf(builder.positions);

                property = PROPERTIES.computeIfAbsent(
                        positions.size(),
                        size -> IntegerProperty.create("multi_block_index", 0, size - 1)
                );

                rotationFunction = Objects.requireNonNullElseGet(builder.rotationFunction, () -> (blockState, pos) -> pos);
            }

            @Override
            public Property<Integer> property() {
                return property;
            }

            @Override
            public List<Vector3ic> positions() {
                return positions;
            }

            @Override
            public Vector3ic rotate(BlockState blockState, Vector3ic position) {
                return rotationFunction.apply(blockState, position);
            }
        }
    }
}
