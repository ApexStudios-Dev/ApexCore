package dev.apexstudios.apexcore.api.block.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class WaterLoggedBlockBehavior extends BlockBehavior {
    public static final BlockBehaviorType<WaterLoggedBlockBehavior> TYPE = new BlockBehaviorType<>(WaterLoggedBlockBehavior::new);
    public static final BooleanProperty PROPERTY = BlockStateProperties.WATERLOGGED;

    private WaterLoggedBlockBehavior(BlockBehaviorRegistration registration) {
        super(registration);

        registration.booleanProperty(PROPERTY);
    }

    public boolean get(BlockState blockState) {
        return blockState.getValueOrElse(PROPERTY, false);
    }

    public BlockState set(BlockState blockState, boolean waterlogged) {
        return blockState.trySetValue(PROPERTY, waterlogged);
    }

    public @Nullable FluidState getFluidState(BlockState blockState) {
        return get(blockState) ? Fluids.WATER.getSource(false) : null;
    }

    public boolean canPlaceLiquid(@Nullable LivingEntity user, BlockGetter level, BlockPos pos, BlockState blockState, Fluid fluid) {
        return !get(blockState) && fluid.isSame(Fluids.WATER);
    }

    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if(get(blockState) || !fluidState.is(Fluids.WATER)) {
            return false;
        }

        if(!level.isClientSide()) {
            var fluid = fluidState.getType();

            level.setBlock(pos, set(blockState, true), Block.UPDATE_ALL);
            level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
        }

        return true;
    }

    public ItemStack pickupBlock(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos, BlockState blockState) {
        if(!get(blockState)) {
            return ItemStack.EMPTY;
        }

        var newBlockState = set(blockState, false);
        level.setBlock(pos, newBlockState, Block.UPDATE_ALL);

        if(!newBlockState.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }

        return new ItemStack(Items.WATER_BUCKET);
    }

    public Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }

    public Optional<SoundEvent> getPickupSound(BlockState blockState) {
        return get(blockState) ? getPickupSound() : Optional.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext context) {
        return set(blockState, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }
}
