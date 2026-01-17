package dev.apexstudios.apexcore.api.block.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class WaterLoggedBlockBehavior extends SimplePropertyBlockBehavior<Boolean, BooleanProperty> {
    public static final BlockBehaviorType<WaterLoggedBlockBehavior> TYPE = new BlockBehaviorType<>(WaterLoggedBlockBehavior::new);

    private WaterLoggedBlockBehavior(BlockBehaviorRegistration registration) {
        super(registration, BlockStateProperties.WATERLOGGED, false);
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

    @Override
    protected BlockState copyProperties(BlockState blockState, BlockState neighborBlockState) {
        return blockState;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!player.isSecondaryUseActive() && stack.getItem() instanceof BucketItem bucket) {
            var waterlogged = get(blockState);
            var fluid = bucket.getContent();

            if((waterlogged && fluid.isSame(Fluids.EMPTY)) || (!waterlogged && fluid.isSame(Fluids.WATER))) {
                var result = stack.use(level, player, hand);

                if(result.consumesAction()) {
                    return result;
                }
            }
        }

        return super.useItemOn(stack, blockState, level, pos, player, hand, hitResult);
    }
}
