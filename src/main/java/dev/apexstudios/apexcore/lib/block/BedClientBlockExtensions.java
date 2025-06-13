package dev.apexstudios.apexcore.lib.block;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BedClientBlockExtensions extends TemplateClientMultiBlockExtensions {
    private final BedBlock block;

    public BedClientBlockExtensions(BedBlock block) {
        this.block = block;
    }

    @Override
    protected boolean shouldApplyCrack(BlockGetter level, BlockPos pos, BlockState blockState) {
        return blockState.is(block);
    }

    @Override
    protected void translate(BlockGetter level, BlockPos pos, BlockState blockState, BiConsumer<BlockPos, BlockState> consumer) {
        var dir = BedBlock.getConnectedDirection(blockState);
        var otherPos = pos.relative(dir);
        var otherBlockState = level.getBlockState(otherPos);
        consumer.accept(otherPos, otherBlockState);
    }
}
