package dev.apexstudios.apexcore.lib.block;

import java.util.function.BiConsumer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

public abstract class TemplateClientMultiBlockExtensions implements IClientBlockExtensions, IRenderBreakingTextureExtension {
    protected abstract boolean shouldApplyCrack(BlockGetter level, BlockPos pos, BlockState blockState);

    protected boolean shouldApplySound(BlockGetter level, BlockPos pos, BlockState blockState) {
        return false;
    }

    @Override
    public boolean shouldRenderBreakingTexture(BlockAndTintGetter level, BlockPos originPos, BlockState originBlockState, BlockPos otherPos, BlockState otherBlockState) {
        return shouldApplyCrack(level, originPos, originBlockState);
    }

    @Override
    public boolean shouldApplyBreakingTexture(BlockAndTintGetter level, BlockPos pos, BlockState blockState) {
        return shouldApplyCrack(level, pos, blockState);
    }

    protected abstract void translate(BlockGetter level, BlockPos pos, BlockState blockState, BiConsumer<BlockPos, BlockState> consumer);

    @Override
    public boolean addHitEffects(BlockState blockState, Level level, HitResult target, ParticleEngine manager) {
        var blockHit = (BlockHitResult) target; // neo why is this downcast?
        var pos = blockHit.getBlockPos();

        if(shouldApplyCrack(level, pos, blockState)) {
            var side = blockHit.getDirection();

            // TODO: Where did 'manager.crack' go
            /*translate(level, pos, blockState, (otherPos, otherBlockState) -> {
                if(shouldApplyCrack(level, otherPos, otherBlockState))
                    manager.crack(otherPos, side);
            });*/

            return true;
        }

        return IClientBlockExtensions.super.addHitEffects(blockState, level, target, manager);
    }

    @Override
    public boolean playBreakSound(BlockState blockState, Level level, BlockPos pos) {
        return shouldApplySound(level, pos, blockState);
    }

    @Override
    public final void translateBreakingTexture(BlockAndTintGetter level, BlockPos pos, BlockState blockState, BiConsumer<BlockPos, BlockState> consumer) {
        translate(level, pos, blockState, consumer);
    }
}
