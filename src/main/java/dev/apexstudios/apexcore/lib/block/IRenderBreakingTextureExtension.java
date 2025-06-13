package dev.apexstudios.apexcore.lib.block;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;

public interface IRenderBreakingTextureExtension {
    @Nullable
    static IRenderBreakingTextureExtension of(Block block) {
        return IClientBlockExtensions.of(block) instanceof IRenderBreakingTextureExtension render ? render : null;
    }

    @Nullable
    static IRenderBreakingTextureExtension of(BlockState blockState) {
        return of(blockState.getBlock());
    }

    default boolean shouldApplyBreakingTexture(BlockAndTintGetter level, BlockPos pos, BlockState blockState) {
        return true;
    }

    default boolean shouldRenderBreakingTexture(BlockAndTintGetter level, BlockPos originPos, BlockState originBlockState, BlockPos otherPos, BlockState otherBlockState) {
        return true;
    }

    void translateBreakingTexture(BlockAndTintGetter level, BlockPos pos, BlockState blockState, BiConsumer<BlockPos, BlockState> consumer);
}
