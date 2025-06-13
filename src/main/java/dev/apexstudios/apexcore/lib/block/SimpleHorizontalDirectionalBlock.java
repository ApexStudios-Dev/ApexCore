package dev.apexstudios.apexcore.lib.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class SimpleHorizontalDirectionalBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SimpleHorizontalDirectionalBlock> CODEC = simpleCodec(SimpleHorizontalDirectionalBlock::new);

    public SimpleHorizontalDirectionalBlock(Properties properties) {
        super(properties);

        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<? extends SimpleHorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
