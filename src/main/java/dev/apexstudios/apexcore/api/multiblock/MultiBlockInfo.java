package dev.apexstudios.apexcore.api.multiblock;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public final class MultiBlockInfo implements Iterable<Vector3ic> {
    private static final Vector3ic LOCAL_ORIGIN = new Vector3i();

    private static final Comparator<Vector3ic> VECTOR_COMPARATOR = Comparator
            .<Vector3ic>comparingLong(pos -> pos.gridDistance(LOCAL_ORIGIN))
            .thenComparingInt(Vector3ic::x)
            .thenComparingInt(Vector3ic::y)
            .thenComparingInt(Vector3ic::z);

    private final List<Vector3ic> positions;
    private final MultiBlockStateModifier blockStateModifier;
    private final MultiBlockPositionTransformer positionTransformer;

    private MultiBlockInfo(Builder builder) {
        positions = List.copyOf(builder.positions);
        blockStateModifier = builder.blockStateModifier;
        positionTransformer = builder.positionTransformer;

        if(positions.size() > IMultiBlock.MAX_MULTI_BLOCK_SIZE) {
            throw new IllegalStateException("MultiBlock size (" + positions.size() + " blocks) exceeds the maximum allowed limit of " + IMultiBlock.MAX_MULTI_BLOCK_SIZE + " blocks!");
        }
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
        private MultiBlockStateModifier blockStateModifier = (blockState, index) -> blockState.setValue(IMultiBlock.MULTI_BLOCK_INDEX, index);
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
