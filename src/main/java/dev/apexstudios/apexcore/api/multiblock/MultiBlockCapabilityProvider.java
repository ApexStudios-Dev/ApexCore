package dev.apexstudios.apexcore.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface MultiBlockCapabilityProvider<TCapability, TContext extends @Nullable Object> extends IBlockCapabilityProvider<TCapability, TContext> {
    @Nullable TCapability get(Level level, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity, TContext context);

    @Override
    default @Nullable TCapability getCapability(Level level, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity, TContext context) {
        return IMultiBlock.asOrigin(
                level,
                pos,
                blockState,
                (targetPos, targetBlockState) -> get(
                        level,
                        targetPos,
                        targetBlockState,
                        targetPos.equals(pos) ? blockEntity : level.getBlockEntity(targetPos),
                        context
                )
        );
    }
}
