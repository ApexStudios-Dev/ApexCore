package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public final class FluidLoggedBlockComponent extends BaseBlockComponent implements BucketPickup, LiquidBlockContainer {
    public static final ComponentType<BlockComponent, FluidLoggedBlockComponent, Builder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("fluid_logged"),
            Builder::new,
            FluidLoggedBlockComponent::new
    );

    private final BooleanProperty property;
    private final Fluid fluid;
    private final UnaryOperator<ItemStack> bucketModifier;
    private final Predicate<FluidState> isMatchingFluid;

    private FluidLoggedBlockComponent(ComponentHolder<BlockComponent> holder, Builder builder) {
        super(holder);

        fluid = builder.fluid instanceof FlowingFluid flowing ? flowing.getSource() : builder.fluid;
        bucketModifier = builder.bucketModifier;
        isMatchingFluid = Objects.requireNonNullElseGet(builder.isMatchingFluid, () -> fluidState -> fluidState.is(fluid));

        property = Util.make(() -> {
            if(fluid.isSame(Fluids.WATER))
                return BlockStateProperties.WATERLOGGED;

            var fluidName = Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(fluid));
            var propertyName = fluidName.getPath() + "_logged";
            var namespace = fluidName.getNamespace();

            if(!namespace.equals(ResourceLocation.DEFAULT_NAMESPACE))
                propertyName = namespace + '_';

            return BooleanProperty.create(propertyName);
        });
    }

    public boolean get(BlockState blockState) {
        return blockState.getValue(property);
    }

    public BlockState set(BlockState blockState, boolean fluidLogged) {
        return blockState.setValue(property, fluidLogged);
    }

    public BlockState setFor(BlockPlaceContext context, BlockState blockState) {
        var fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return set(blockState, matches(fluidState));
    }

    public boolean matches(FluidState fluidState) {
        return fluidState.isSourceOfType(fluid) && isMatchingFluid.test(fluidState);
    }

    public boolean matches(Fluid fluid) {
        return matches(fluid.defaultFluidState());
    }

    @Override
    public ItemStack pickupBlock(@Nullable Player player, LevelAccessor level, BlockPos pos, BlockState blockState) {
        if(get(blockState)) {
            level.setBlock(pos, set(blockState, false), Block.UPDATE_ALL);

            if(!blockState.canSurvive(level, pos))
                level.destroyBlock(pos, true);

            var bucket = fluid.getBucket().getDefaultInstance();
            return bucketModifier.apply(bucket);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return fluid.getPickupSound();
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState blockState, Fluid fluid) {
        return matches(fluid);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if(!get(blockState) && isMatchingFluid.test(fluidState)) {
            if(!level.isClientSide()) {
                var fluid = fluidState.getType();
                level.setBlock(pos, set(blockState, true), Block.UPDATE_ALL);
                level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
            }

            return true;
        }

        return false;
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return set(blockState, false);
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(property);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context, BlockState blockState) {
        return setFor(context, blockState);
    }

    @Override
    public FluidState getFluidState(BlockState blockState, FluidState fluidState) {
        if(!get(blockState))
            return fluidState;

        return fluid instanceof FlowingFluid flowing ? flowing.getSource(false) : fluid.defaultFluidState();
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction facing, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        if(get(blockState))
            tickAccess.scheduleTick(pos, fluid, fluid.getTickDelay(level));

        return super.updateShape(blockState, level, tickAccess, pos, facing, neighborPos, neighborBlockState, random);
    }

    public static void registerWater(ComponentRegistrar<BlockComponent> registrar) {
        registrar.register(COMPONENT_TYPE, builder -> builder
                .fluid(Fluids.WATER)
                .matchingFluid(fluidState -> fluidState.is(FluidTags.WATER))
        );
    }

    public static void registerLava(ComponentRegistrar<BlockComponent> registrar) {
        registrar.register(COMPONENT_TYPE, builder -> builder
                .fluid(Fluids.LAVA)
                .matchingFluid(fluidState -> fluidState.is(FluidTags.LAVA))
        );
    }

    public static final class Builder implements ComponentBuilder {
        private Fluid fluid = Fluids.WATER;
        private UnaryOperator<ItemStack> bucketModifier = UnaryOperator.identity();
        @Nullable private Predicate<FluidState> isMatchingFluid;

        public Builder fluid(Fluid fluid) {
            this.fluid = fluid;
            return this;
        }

        public Builder bucketModifier(UnaryOperator<ItemStack> bucketModifier) {
            this.bucketModifier = bucketModifier;
            return this;
        }

        public Builder matchingFluid(Predicate<FluidState> isMatchingFluid) {
            this.isMatchingFluid = isMatchingFluid;
            return this;
        }
    }
}
