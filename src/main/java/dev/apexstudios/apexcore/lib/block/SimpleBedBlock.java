package dev.apexstudios.apexcore.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
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
}
