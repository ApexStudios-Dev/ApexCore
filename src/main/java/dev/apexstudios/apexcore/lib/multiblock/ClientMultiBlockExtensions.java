package dev.apexstudios.apexcore.lib.multiblock;

import dev.apexstudios.apexcore.lib.block.TemplateClientMultiBlockExtensions;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class ClientMultiBlockExtensions extends TemplateClientMultiBlockExtensions {
    public static final ClientMultiBlockExtensions INSTANCE = new ClientMultiBlockExtensions();

    private ClientMultiBlockExtensions() { }

    @Override
    protected boolean shouldApplyCrack(BlockGetter level, BlockPos pos, BlockState blockState) {
        return MultiBlock.isMultiBlock(blockState);
    }

    @Override
    protected boolean shouldApplySound(BlockGetter level, BlockPos pos, BlockState blockState) {
        return MultiBlock.getIndex(blockState) != 0;
    }

    @Override
    public boolean shouldRenderBreakingTexture(BlockAndTintGetter level, BlockPos originPos, BlockState originBlockState, BlockPos otherPos, BlockState otherBlockState) {
        if(!shouldApplyCrack(level, originPos, originBlockState) || !shouldApplyCrack(level, otherPos, otherBlockState))
            return false;

        return MultiBlock.getIndex(originBlockState) != MultiBlock.getIndex(otherBlockState);
    }

    @Override
    protected void translate(BlockGetter level, BlockPos pos, BlockState blockState, BiConsumer<BlockPos, BlockState> consumer) {
        var index = MultiBlock.getIndex(blockState);

        MultiBlock.forEachPos(pos, blockState, (otherPos, otherBlockState) -> {
            if(MultiBlock.getIndex(otherBlockState) != index)
                consumer.accept(otherPos, otherBlockState);
        });
    }
}
