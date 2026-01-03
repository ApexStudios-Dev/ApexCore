package dev.apexstudios.apexcore.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.Nullable;

public abstract class SimpleMultiBlock extends Block implements MultiBlock {
    public SimpleMultiBlock(Properties properties) {
        super(properties);

        registerDefaultState(defaultBlockState().setValue(getMultiBlockProperty(), 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(getMultiBlockProperty());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var placementBlockState = super.getStateForPlacement(context);

        if(placementBlockState == null || !MultiBlock.canPlace(context, placementBlockState))
            return null;

        return placementBlockState;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, blockState, placer, stack);
        MultiBlock.setBlockStates(level, pos, blockState);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState blockState) {
        super.destroy(level, pos, blockState);
        MultiBlock.destroyBlocks(level, pos, blockState);
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState blockState) {
        super.spawnDestroyParticles(level, player, pos, blockState);
        MultiBlock.spawnDestroyParticles(level, pos, blockState, player);
    }
}
