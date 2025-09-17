package dev.apexstudios.apexcore.lib.block;

import java.util.function.BiConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

            translate(level, pos, blockState, (otherPos, otherBlockState) -> {
                if(shouldApplyCrack(level, otherPos, otherBlockState))
                    crack((ClientLevel) level, otherPos, side, manager);
            });

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

    protected static void crack(ClientLevel level, BlockPos pos, Direction direction, ParticleEngine manager) {
        // Copied from ClientLevel#addBreakingBlockEffect
        var blockstate = level.getBlockState(pos);
        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();
        var offset = .1F;
        var bounds = blockstate.getShape(level, pos).bounds();
        var crackX = x + level.random.nextDouble() * (bounds.maxX - bounds.minX - .2F) + offset + bounds.minX;
        var crackY = y + level.random.nextDouble() * (bounds.maxY - bounds.minY - .2F) + offset + bounds.minY;
        var crackZ = z + level.random.nextDouble() * (bounds.maxZ - bounds.minZ - .2F) + offset + bounds.minZ;

        if(direction == Direction.DOWN)
            crackY = y + bounds.minY - offset;
        if(direction == Direction.UP)
            crackY = y + bounds.maxY + offset;
        if(direction == Direction.NORTH)
            crackZ = z + bounds.minZ - offset;
        if(direction == Direction.SOUTH)
            crackZ = z + bounds.maxZ + offset;
        if(direction == Direction.WEST)
            crackX = x + bounds.minX - offset;
        if(direction == Direction.EAST)
            crackX = x + bounds.maxX + offset;

        manager.add(new TerrainParticle(level, crackX, crackY, crackZ, 0D, 0D, 0D, blockstate, pos)
                .updateSprite(blockstate, pos)
                .setPower(.2F)
                .scale(.6F)
        );
    }
}
