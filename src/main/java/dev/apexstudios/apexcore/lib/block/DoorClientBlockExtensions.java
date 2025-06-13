package dev.apexstudios.apexcore.lib.block;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class DoorClientBlockExtensions extends TemplateClientMultiBlockExtensions {
    private final DoorBlock block;

    public DoorClientBlockExtensions(DoorBlock block) {
        this.block = block;
    }

    @Override
    protected boolean shouldApplyCrack(BlockGetter level, BlockPos pos, BlockState blockState) {
        return blockState.is(block);
    }

    @Override
    protected void translate(BlockGetter level, BlockPos pos, BlockState blockState, BiConsumer<BlockPos, BlockState> consumer) {
        var half = blockState.getValue(DoorBlock.HALF);
        var otherPos = pos.relative(half.getDirectionToOther());
        var otherBlockState = level.getBlockState(otherPos);
        consumer.accept(otherPos, otherBlockState);
    }
}
