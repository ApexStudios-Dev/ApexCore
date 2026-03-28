package dev.apexstudios.apexcore.neoforge.api;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class ItemOnlyFluid extends Fluid {
    @Override public abstract FluidType getFluidType();
    @Override public abstract Item getBucket();

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.ofNullable(getFluidType().getSound(SoundActions.BUCKET_FILL));
    }

    @Override
    protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected Vec3 getFlow(BlockGetter blockReader, BlockPos pos, FluidState fluidState) {
        return Vec3.ZERO;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 0;
    }

    @Override
    protected float getExplosionResistance() {
        return 1F;
    }

    @Override
    public float getHeight(FluidState fluidState, BlockGetter level, BlockPos pos) {
        return level.getFluidState(pos.above()).is(this) ? 1F : fluidState.getOwnHeight();
    }

    @Override
    public float getOwnHeight(FluidState fluidState) {
        // return (float) fluidState.getAmount() / (BlockStateProperties.MAX_LEVEL_8 + 1);
        // return 8 / 9;
        return .8888888889F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState fluidState) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSource(FluidState fluidState) {
        return true;
    }

    @Override
    public int getAmount(FluidState fluidState) {
        return BlockStateProperties.MAX_LEVEL_8;
    }

    @Override
    public VoxelShape getShape(FluidState fluidState, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    public static Fluid simple(Supplier<? extends FluidType> fluidType, ItemLike bucket) {
        return new Simple(fluidType, bucket);
    }

    public static Fluid simple(Supplier<? extends FluidType> fluidType) {
        return simple(fluidType, Items.AIR);
    }

    public static Supplier<Fluid> simpleFactory(Supplier<? extends FluidType> fluidType, ItemLike bucket) {
        return () -> simple(fluidType, bucket);
    }

    public static Supplier<Fluid> simpleFactory(Supplier<? extends FluidType> fluidType) {
        return () -> simple(fluidType);
    }

    private static final class Simple extends ItemOnlyFluid {
        private final Supplier<? extends FluidType> fluidType;
        private final ItemLike bucket;

        private Simple(Supplier<? extends FluidType> fluidType, ItemLike bucket) {
            super();

            this.fluidType = fluidType;
            this.bucket = bucket;
        }

        @Override
        public FluidType getFluidType() {
            return fluidType.get();
        }

        @Override
        public Item getBucket() {
            return bucket.asItem();
        }
    }
}
