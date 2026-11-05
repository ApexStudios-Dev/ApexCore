package dev.apexstudios.apexcore.api.multiblock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public final class MultiBlockInfo implements Iterable<Vector3ic> {
    private static final Vector3ic LOCAL_ORIGIN = new Vector3i();

    private static final Comparator<Vector3ic> VECTOR_COMPARATOR = Comparator
            .<Vector3ic>comparingLong(pos -> pos.gridDistance(LOCAL_ORIGIN))
            .thenComparingInt(Vector3ic::x)
            .thenComparingInt(Vector3ic::y)
            .thenComparingInt(Vector3ic::z);

    private static final int MAX_MULTI_BLOCK_SIZE = 255;
    private static final Int2ObjectMap<IntegerProperty> PROPERTIES = new Int2ObjectOpenHashMap<>();

    private final List<Vector3ic> positions;
    private final IntegerProperty property;
    private final MultiBlockStateModifier blockStateModifier;
    private final MultiBlockPositionTransformer positionTransformer;

    private MultiBlockInfo(Builder builder) {
        positions = List.copyOf(builder.positions);
        property = PROPERTIES.computeIfAbsent(positions.size() - 1, size -> IntegerProperty.create("multi_block_index", 0, size));

        blockStateModifier = MultiBlockStateModifier.NOOP
                .andThen(this::setIndex)
                .andThen(builder.blockStateModifier);

        positionTransformer = builder.positionTransformer;

        if(positions.size() > MAX_MULTI_BLOCK_SIZE) {
            throw new IllegalStateException("MultiBlock size (" + positions.size() + " blocks) exceeds the maximum allowed limit of " + MAX_MULTI_BLOCK_SIZE + " blocks!");
        }
    }

    public IntegerProperty property() {
        return property;
    }

    public int indexOf(BlockState blockState) {
        return blockState.getValue(property);
    }

    public BlockState setIndex(BlockState blockState, int index) {
        return blockState.setValue(property, index);
    }

    public int size() {
        return positions.size();
    }

    public Vector3ic get(int index) {
        return positions.get(index);
    }

    public Vector3ic getTransformed(BlockState blockState, int index) {
        return positionTransformer.apply(blockState, new Vector3i(get(index)), index);
    }

    public Vector3ic getTransformed(BlockState blockState) {
        return getTransformed(blockState, indexOf(blockState));
    }

    public BlockState applyBlockState(BlockState blockState, int index) {
        return blockStateModifier.apply(blockState, index);
    }

    @Override
    public Iterator<Vector3ic> iterator() {
        return positions.iterator();
    }

    public static BlockPos fromOrigin(BlockPos pos, Vector3ic transformedLocal) {
        return pos.offset(transformedLocal.x(), transformedLocal.y(), transformedLocal.z());
    }

    public static BlockPos toOrigin(BlockPos pos, Vector3ic transformedLocal) {
        return pos.offset(-transformedLocal.x(), -transformedLocal.y(), -transformedLocal.z());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Set<Vector3ic> positions = new TreeSet<>(VECTOR_COMPARATOR);
        private MultiBlockStateModifier blockStateModifier = MultiBlockStateModifier.NOOP;
        private MultiBlockPositionTransformer positionTransformer = MultiBlockPositionTransformer.NOOP;

        private Builder() {
            with(LOCAL_ORIGIN);
        }

        public Builder with(Vector3ic position) {
            positions.add(new Vector3i(position));
            return this;
        }

        public Builder with(Vector3ic position, Vector3ic... positions) {
            with(position);

            for(var pos : positions) {
                with(pos);
            }

            return this;
        }

        public Builder with(int x, int y, int z) {
            return with(new Vector3i(x, y, z));
        }

        public Builder modifying(MultiBlockStateModifier blockStateModifier) {
            this.blockStateModifier = this.blockStateModifier.andThen(blockStateModifier);
            return this;
        }

        public Builder rotating(MultiBlockPositionTransformer positionTransformer) {
            this.positionTransformer = this.positionTransformer.andThen(positionTransformer);
            return this;
        }

        public MultiBlockInfo build() {
            return new MultiBlockInfo(this);
        }
    }
}
