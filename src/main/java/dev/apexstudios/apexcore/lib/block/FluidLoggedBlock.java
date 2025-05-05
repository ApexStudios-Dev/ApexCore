package dev.apexstudios.apexcore.lib.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public interface FluidLoggedBlock extends SimpleWaterloggedBlock {
    default Property<Boolean> fluidLoggedProperty() {
        return BlockStateProperties.WATERLOGGED;
    }

    default Fluid fluidLoggedFluid() {
        return Fluids.WATER;
    }

    default FluidState getFluidLoggedState() {
        return Fluids.WATER.getSource(false);
    }

    default boolean isFluidLoggedForPlacement(BlockPlaceContext context) {
        return context.getLevel().isFluidAtPosition(context.getClickedPos(), fluidState -> fluidState.is(fluidLoggedFluid()));
    }

    @Override
    default boolean canPlaceLiquid(@Nullable LivingEntity placer, BlockGetter level, BlockPos pos, BlockState blockState, Fluid fluid) {
        return fluid.isSame(fluidLoggedFluid());
    }

    @Override
    default boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if(!blockState.getValue(fluidLoggedProperty()) && fluidState.is(fluidLoggedFluid())) {
            if(!level.isClientSide()) {
                level.setBlock(pos, blockState.setValue(fluidLoggedProperty(), true), Block.UPDATE_ALL);
                level.scheduleTick(pos, fluidLoggedFluid(), fluidLoggedFluid().getTickDelay(level));
            }

            return true;
        }

        return false;
    }

    @Override
    default ItemStack pickupBlock(@Nullable LivingEntity placer, LevelAccessor level, BlockPos pos, BlockState blockState) {
        if(blockState.getValue(fluidLoggedProperty())) {
            level.setBlock(pos, blockState.setValue(fluidLoggedProperty(), false), Block.UPDATE_ALL);

            if(!blockState.canSurvive(level, pos))
                level.destroyBlock(pos, true);

            return new ItemStack(fluidLoggedFluid().getBucket());
        }

        return ItemStack.EMPTY;
    }

    @Override
    default Optional<SoundEvent> getPickupSound() {
        return fluidLoggedFluid().getPickupSound();
    }
}
