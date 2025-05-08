package dev.apexstudios.apexcore.lib.multiblock;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

public final class ClientMultiBlockExtensions implements IClientBlockExtensions {
    public static final ClientMultiBlockExtensions INSTANCE = new ClientMultiBlockExtensions();

    private ClientMultiBlockExtensions() { }

    @Override
    public boolean addHitEffects(BlockState blockState, Level level, HitResult target, ParticleEngine manager) {
        if(MultiBlock.isMultiBlock(blockState)) {
            var blockHit = (BlockHitResult) target; // neo why is this downcast?
            int index = MultiBlock.getIndex(blockState);

            MultiBlock.forEachPos(blockHit.getBlockPos(), blockState, (otherPos, otherBlockState) -> {
                if(MultiBlock.getIndex(otherBlockState) != index)
                    manager.crack(otherPos, blockHit.getDirection());
            });

            return true;
        }

        return IClientBlockExtensions.super.addHitEffects(blockState, level, target, manager);
    }

    @Override
    public boolean playBreakSound(BlockState blockState, Level level, BlockPos pos) {
        return MultiBlock.isMultiBlock(blockState) ? MultiBlock.getIndex(blockState) != 0 : IClientBlockExtensions.super.playBreakSound(blockState, level, pos);
    }
}
