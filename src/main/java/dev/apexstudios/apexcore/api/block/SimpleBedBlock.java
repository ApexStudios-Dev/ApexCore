package dev.apexstudios.apexcore.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SimpleBedBlock extends BedBlock {
    public SimpleBedBlock(@Nullable DyeColor color, Properties properties) {
        super(color, properties);
    }

    public SimpleBedBlock(Properties properties) {
        this(null, properties);
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos pos, Entity entity, double fallDistance) {
        if(BlockHelper.shouldBounceOnBlock(pos, blockState, entity)) {
            super.fallOn(level, blockState, pos, entity, fallDistance);
        } else {
            // apply damage ignoring the bounce negation
            // must match code in Block (super.super.fallOn)
            entity.causeFallDamage(fallDistance, 1F, entity.damageSources().fall());
        }
    }
}
