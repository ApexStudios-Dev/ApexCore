package dev.apexstudios.apexcore.neoforge.api.multiblock;

import com.google.errorprone.annotations.ForOverride;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;

public interface MultiBlock {
    MultiBlockProperty getMultiBlockProperty();

    @ApiStatus.NonExtendable
    default List<Vector3ic> getMultiBlockLocationPositions() {
        return getMultiBlockProperty().getPositions();
    }

    @ApiStatus.NonExtendable
    default int getMultiBlockSize() {
        return getMultiBlockLocationPositions().size();
    }

    @ForOverride
    default Rotation getMultiBlockRotation(BlockState blockState) {
        if(blockState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            var facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
            return rotation(facing);
        }

        return Rotation.NONE;
    }

    @Nullable
    static MultiBlock asMultiBlock(BlockState blockState) {
        return blockState.getBlock() instanceof MultiBlock multiBlock ? multiBlock : null;
    }

    static MultiBlock asMultiBlockOrThrow(BlockState blockState) {
        return Objects.requireNonNull(asMultiBlock(blockState));
    }

    static void asMultiBlock(BlockState blockState, Consumer<MultiBlock> consumer) {
        var multiBlock = asMultiBlock(blockState);

        if(multiBlock != null)
            consumer.accept(multiBlock);
    }

    static boolean testMultiBlock(BlockState blockState, Predicate<MultiBlock> predicate) {
        var multiBlock = asMultiBlock(blockState);
        return multiBlock != null && predicate.test(multiBlock);
    }

    static boolean isMultiBlock(BlockState blockState) {
        return asMultiBlock(blockState) != null;
    }

    static int getIndex(BlockState blockState) {
        var multiBlock = asMultiBlock(blockState);
        return multiBlock == null ? 0 : blockState.getValue(multiBlock.getMultiBlockProperty());
    }

    static BlockState setIndex(BlockState blockState, int index) {
        var multiBlock = asMultiBlock(blockState);
        return multiBlock == null ? blockState : blockState.setValue(multiBlock.getMultiBlockProperty(), index);
    }

    static Vector3ic getLocalPos(BlockState blockState) {
        var multiBlock = asMultiBlock(blockState);

        if(multiBlock == null)
            return new Vector3i();

        var index = getIndex(blockState);
        var localPos = multiBlock.getMultiBlockLocationPositions().get(index);
        var rotation = multiBlock.getMultiBlockRotation(blockState);
        return rotate(localPos, rotation);
    }

    static BlockPos getOrigin(BlockPos worldPos, BlockState blockState) {
        if(isMultiBlock(blockState)) {
            var localPos = getLocalPos(blockState);
            return worldPos.offset(-localPos.x(), -localPos.y(), -localPos.z());
        }

        return worldPos;
    }

    static BlockPos getWorldPos(BlockPos origin, BlockState blockState) {
        if(isMultiBlock(blockState)) {
            var localPos = getLocalPos(blockState);
            return origin.offset(localPos.x(), localPos.y(), localPos.z());
        }

        return origin;
    }

    static void forEachPos(BlockPos worldPos, BlockState blockState, BiConsumer<BlockPos, BlockState> consumer) {
        asMultiBlock(blockState, multiBlock -> {
            var origin = getOrigin(worldPos, blockState);

            for(var i = 0; i < multiBlock.getMultiBlockSize(); i++) {
                var otherBlockState = setIndex(blockState, i);
                var otherPos = getWorldPos(origin, otherBlockState);
                consumer.accept(otherPos, otherBlockState);
            }
        });
    }

    static boolean testEachPos(BlockPos worldPos, BlockState blockState, BiPredicate<BlockPos, BlockState> predicate) {
        return testMultiBlock(blockState, multiBlock -> {
            var origin = getOrigin(worldPos, blockState);

            for(var i = 0; i < multiBlock.getMultiBlockSize(); i++) {
                var otherBlockState = setIndex(blockState, i);
                var otherPos = getWorldPos(origin, otherBlockState);

                if(!predicate.test(otherPos, otherBlockState))
                    return false;
            }

            return true;
        });
    }

    static boolean canPlace(BlockPlaceContext context, BlockState placementBlockState) {
        return testMultiBlock(placementBlockState, multiBlock -> {
            var level = context.getLevel();
            var worldPos = context.getClickedPos();
            var worldBorder = level.getWorldBorder();
            var collisionContext = CollisionContext.placementContext(context.getPlayer());

            return testEachPos(worldPos, placementBlockState, (otherPos, otherBlockState) -> {
                if(!level.getBlockState(otherPos).canBeReplaced(context))
                    return false;
                if(!level.isInWorldBounds(otherPos))
                    return false;
                if(!worldBorder.isWithinBounds(otherPos))
                    return false;
                if(!level.isUnobstructed(otherBlockState, otherPos, collisionContext))
                    return false;
                return true;
            });
        });
    }

    static void setBlockStates(Level level, BlockPos worldPos, BlockState blockState, UnaryOperator<BlockState> blockStateMutator) {
        if(!isMultiBlock(blockState))
            return;

        int index = getIndex(blockState);

        forEachPos(worldPos, blockState, (otherPos, otherBlockState) -> {
            if(getIndex(otherBlockState) != index)
                level.setBlock(otherPos, blockStateMutator.apply(otherBlockState), Block.UPDATE_ALL);
        });
    }

    static void setBlockStates(Level level, BlockPos worldPos, BlockState blockState) {
        setBlockStates(level, worldPos, blockState, UnaryOperator.identity());
    }

    static void destroyBlocks(LevelAccessor level, BlockPos worldPos, BlockState blockState) {
        if(!isMultiBlock(blockState))
            return;

        int index = getIndex(blockState);

        forEachPos(worldPos, blockState, (otherPos, otherBlockState) -> {
            if(getIndex(otherBlockState) != index && level.getBlockState(otherPos).is(otherBlockState.getBlock()))
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        });
    }

    static Rotation rotation(Direction facing) {
        return switch (facing) {
            case Direction.NORTH -> Rotation.CLOCKWISE_90;
            case Direction.SOUTH -> Rotation.COUNTERCLOCKWISE_90;
            case Direction.EAST -> Rotation.CLOCKWISE_180;
            default -> Rotation.NONE;
        };
    }

    static Vector3ic rotate(Vector3ic position, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new Vector3i(-position.z(), position.y(), position.x());
            case CLOCKWISE_180 -> new Vector3i(-position.x(), position.y(), -position.z());
            case COUNTERCLOCKWISE_90 -> new Vector3i(position.z(), position.y(), -position.x());
            default -> position;
        };
    }

    @Nullable
    static BlockEntity getBlockEntity(BlockGetter level, BlockPos worldPos, BlockState blockState) {
        var origin = getOrigin(worldPos, blockState);
        return level.getBlockEntity(origin);
    }

    @Nullable
    static BlockEntity getBlockEntity(BlockGetter level, BlockPos worldPos) {
        return getBlockEntity(level, worldPos, level.getBlockState(worldPos));
    }

    static VoxelShape fixShape(VoxelShape shape, BlockState blockState, BlockPos worldPos) {
        if(!isMultiBlock(blockState))
            return shape;

        var origin = getOrigin(worldPos, blockState);
        var offset = worldPos.subtract(origin);
        return shape.move(offset.multiply(-1));
    }

    static boolean isSameMultiBlock(BlockGetter level, BlockPos pos, BlockPos other) {
        var blockState = level.getBlockState(pos);
        var otherBlockState = level.getBlockState(other);

        if(!blockState.is(otherBlockState.getBlock())) {
            return false;
        }

        if(!isMultiBlock(blockState) || !isMultiBlock(otherBlockState)) {
            return false;
        }

        var origin = getOrigin(pos, blockState);
        var otherOrigin = getOrigin(other, otherBlockState);

        return origin.equals(otherOrigin);
    }
}
