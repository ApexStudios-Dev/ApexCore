package dev.apexstudios.apexcore.lib.component.block.types;

import com.google.common.collect.Lists;
import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public final class MultiBlockComponent extends BaseBlockComponent {
    public static final ComponentType<BlockComponent, MultiBlockComponent, Builder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("multi_block"),
            Builder::new,
            MultiBlockComponent::new
    );

    public static final int ORIGIN_INDEX = 0;

    private static final Int2ObjectMap<IntegerProperty> PROPERTIES = new Int2ObjectOpenHashMap<>();

    private final List<Vector3ic> localPositions;
    private final IntegerProperty property;
    private final BiFunction<BlockState, Vector3ic, Vector3ic> rotationFunction;

    private MultiBlockComponent(ComponentHolder<BlockComponent> holder, Builder builder) {
        super(holder);

        localPositions = List.copyOf(builder.positions);

        property = PROPERTIES.computeIfAbsent(
                localPositions.size(),
                size -> IntegerProperty.create("multi_block_index", MultiBlockComponent.ORIGIN_INDEX, size - 1)
        );

        rotationFunction = Objects.requireNonNullElseGet(builder.rotationFunction, () -> (blockState, pos) -> pos);
    }

    public int size() {
        return localPositions.size();
    }

    public IntegerProperty property() {
        return property;
    }

    public List<Vector3ic> localPositions() {
        return localPositions;
    }

    public int indexOf(BlockState blockState) {
        return blockState.getValue(property);
    }

    public BlockState withIndex(BlockState blockState, int index) {
        return blockState.setValue(property, index);
    }

    public BlockPos getOrigin(BlockPos pos, BlockState blockState) {
        var rotatedLocalPosition = getRotatedLocalPosition(blockState);
        return pos.offset(-rotatedLocalPosition.x(), -rotatedLocalPosition.y(), -rotatedLocalPosition.z());
    }

    public BlockPos getPos(BlockPos origin, BlockState blockState) {
        var rotatedLocalPosition = getRotatedLocalPosition(blockState);
        return origin.offset(rotatedLocalPosition.x(), rotatedLocalPosition.y(), rotatedLocalPosition.z());
    }

    public Vector3ic getRotatedLocalPosition(BlockState blockState) {
        var index = indexOf(blockState);
        var localPosition = localPositions.get(index);
        return rotate(blockState, localPosition);
    }

    Vector3ic rotate(BlockState blockState, Vector3ic pos) {
        return rotationFunction.apply(blockState, pos);
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return blockState.setValue(property, ORIGIN_INDEX);
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(property);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context, BlockState blockState) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var placer = context.getPlayer();

        var placementBlockState = withIndex(blockState, ORIGIN_INDEX);
        var index = indexOf(placementBlockState);
        var origin = getOrigin(pos, placementBlockState);

        for(var i = MultiBlockComponent.ORIGIN_INDEX; i < localPositions.size(); i++){
            if(i == index)
                continue;

            var otherBlockState = withIndex(placementBlockState, i);
            var otherPos = getPos(origin, otherBlockState);

            if(!ApexUtil.isInBounds(level, otherPos))
                return null;
            if(!ApexUtil.canPlace(level, otherPos, otherBlockState, placer))
                return null;
        }

        return placementBlockState;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        var index = indexOf(blockState);
        var origin = getOrigin(pos, blockState);

        for(var i = MultiBlockComponent.ORIGIN_INDEX; i < localPositions.size(); i++){
            if(i == index)
                continue;

            var otherBlockState = withIndex(blockState, i);
            var otherPos = getPos(origin, otherBlockState);
            level.setBlock(otherPos, otherBlockState, Block.UPDATE_ALL);
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        var index = indexOf(blockState);
        var origin = getOrigin(pos, blockState);

        for(var i = MultiBlockComponent.ORIGIN_INDEX; i < localPositions.size(); i++){
            if(i == index)
                continue;

            var otherPos = getPos(origin, withIndex(blockState, i));
            var otherBlockState = level.getBlockState(otherPos);

            if(otherBlockState.is(blockState.getBlock()))
                level.destroyBlock(otherPos, false);
        }
    }

    public static BlockPos getBlockEntityPos(BlockPos pos, BlockState blockState) {
        var multiBlock = BlockComponentHelper.getComponent(blockState, BlockComponentTypes.MULTI_BLOCK);

        if(multiBlock != null && multiBlock.indexOf(blockState) != MultiBlockComponent.ORIGIN_INDEX)
            return multiBlock.getOrigin(pos, blockState);

        return pos;
    }

    public static BlockPos getBlockEntityPos(BlockGetter level, BlockPos pos) {
        return getBlockEntityPos(pos, level.getBlockState(pos));
    }

    public static VoxelShape fixVoxelShape(VoxelShape shape, MultiBlockComponent multiBlock, BlockState blockState, BlockPos pos) {
        var origin = multiBlock.getOrigin(pos, blockState);
        var offset = pos.subtract(origin);
        return shape.move(-offset.getX(), -offset.getY(), -offset.getZ());
    }

    public static final class Builder implements ComponentBuilder {
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

        public Builder rotating(Function<BlockState, Property<Direction>> propertyLookup) {
            return rotating((blockState, pos) -> {
                var property = propertyLookup.apply(blockState);
                var facing = blockState.getValue(property);

                return rotate(pos, switch (facing) {
                    case Direction.NORTH -> Rotation.CLOCKWISE_90;
                    case Direction.SOUTH -> Rotation.COUNTERCLOCKWISE_90;
                    case Direction.EAST -> Rotation.CLOCKWISE_180;
                    default -> Rotation.NONE;
                });
            });
        }

        public Builder rotating(Property<Direction> property) {
            return rotating(blockState -> property);
        }

        public Builder rotatingFromComponent() {
            return rotating(blockState -> BlockComponentHelper.getComponentOrThrow(blockState, BlockComponentTypes.FACING).getProperty());
        }

        private static Vector3ic rotate(Vector3ic pos, Rotation rotation) {
            return switch (rotation) {
                case CLOCKWISE_90 -> new Vector3i(-pos.z(), pos.y(), pos.x());
                case CLOCKWISE_180 -> new Vector3i(-pos.x(), pos.y(), -pos.z());
                case COUNTERCLOCKWISE_90 -> new Vector3i(pos.z(), pos.y(), -pos.x());
                case NONE -> pos;
            };
        }
    }
}
