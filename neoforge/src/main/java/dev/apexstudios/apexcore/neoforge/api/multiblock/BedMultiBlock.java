package dev.apexstudios.apexcore.neoforge.api.multiblock;

import dev.apexstudios.apexcore.neoforge.api.block.SimpleBedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jspecify.annotations.Nullable;

public abstract class BedMultiBlock extends SimpleBedBlock implements MultiBlock {
    public BedMultiBlock(@Nullable DyeColor color, Properties properties) {
        super(color, properties);

        registerDefaultState(defaultBlockState().setValue(getMultiBlockProperty(), 0));
    }

    public BedMultiBlock(Properties properties) {
        this(null, properties);
    }

    protected boolean isHead(BlockState blockState) {
        var index = MultiBlock.getIndex(blockState);
        return index == 1 || index == 2;
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
        // bypass vanilla setting the connected block
        // our multi block system will do that for us
        // super.setPlacedBy(level, pos, blockState, placer, stack);

        MultiBlock.setBlockStates(level, pos, blockState, otherBlockState -> {
            var part = isHead(otherBlockState) ? BedPart.HEAD : BedPart.FOOT;
            return otherBlockState.setValue(PART, part);
        });
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState blockState) {
        super.destroy(level, pos, blockState);
        MultiBlock.destroyBlocks(level, pos, blockState);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader p_374508_, ScheduledTickAccess p_374420_, BlockPos p_49529_, Direction p_49526_, BlockPos p_49530_, BlockState p_49527_, RandomSource p_374423_) {
        // vanilla uses this to copy across occupied state
        // and remove invalid block states
        // we dont need that and it causes issues for our multiblocks
        // simply return what ever block state is passed in
        return blockState;
    }

    @Override
    public void setBedOccupied(BlockState blockState, Level level, BlockPos pos, LivingEntity sleeper, boolean occupied) {
        super.setBedOccupied(blockState, level, pos, sleeper, occupied);

        // update connected blocks occupied state
        var direction = getConnectedDirection(blockState);
        var otherPos = pos.relative(direction);
        var otherBlockState = level.getBlockState(otherPos);

        if(otherBlockState.is(blockState.getBlock()))
            super.setBedOccupied(otherBlockState, level, otherPos, sleeper, occupied);
    }
}
