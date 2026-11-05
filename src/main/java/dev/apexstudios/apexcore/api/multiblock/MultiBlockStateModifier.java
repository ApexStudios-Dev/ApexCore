package dev.apexstudios.apexcore.api.multiblock;

import java.util.Objects;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface MultiBlockStateModifier {
    MultiBlockStateModifier NOOP = (blockState, index) -> blockState;

    BlockState apply(BlockState blockState, int index);

    default MultiBlockStateModifier andThen(MultiBlockStateModifier after) {
        Objects.requireNonNull(after);

        if(this == NOOP) {
            return after;
        } else if(after == NOOP) {
            return this;
        }

        return (blockState, index) -> after.apply(apply(blockState, index), index);
    }
}
