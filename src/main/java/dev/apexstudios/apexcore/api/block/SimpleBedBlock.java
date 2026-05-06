package dev.apexstudios.apexcore.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SimpleBedBlock extends BedBlock {
    public SimpleBedBlock(@Nullable DyeColor color, Properties properties) {
        super(color, properties);
    }

    public SimpleBedBlock(Properties properties) {
        this(null, properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return null;
    }

    protected boolean isBouncy(BlockState blockState) {
        return true;
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos pos, Entity entity, double fallDistance) {
        if(isBouncy(blockState)) {
            super.fallOn(level, blockState, pos, entity, fallDistance);
        } else {
            // apply damage ignoring the bounce negation
            // must match code in Block (super.super.fallOn)
            entity.causeFallDamage(fallDistance, 1F, entity.damageSources().fall());
        }
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        // same code as to how Entity gets the BlockState
        var effectPos = entity.getOnPosLegacy();
        var effectBlockState = level.getBlockState(effectPos);

        if(isBouncy(effectBlockState)) {
            super.updateEntityMovementAfterFallOn(level, entity);
        } else {
            // update movement ignoring the bounce
            // must match code in Block (super.super.updateEntityMovementAfterFallOn)
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1D, 0D, 1D));
        }
    }
}
