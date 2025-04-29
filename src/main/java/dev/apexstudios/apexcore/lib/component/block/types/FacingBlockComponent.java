package dev.apexstudios.apexcore.lib.component.block.types;

import com.google.common.collect.Sets;
import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHolder;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentRegistrar;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentType;
import dev.apexstudios.apexcore.lib.util.ApexUtil;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public final class FacingBlockComponent extends BaseBlockComponent {
    public static final BlockComponentType<FacingBlockComponent, Builder> COMPONENT_TYPE = BlockComponentType.register(
            ApexCore.identifier("facing"),
            Builder::new,
            FacingBlockComponent::new
    );

    private final EnumProperty<Direction> property;
    private final Direction defaultFacing;
    private final Function<BlockPlaceContext, Direction> facingForPlacement;
    private final Set<Property<Direction>> compatibilities;

    private FacingBlockComponent(BlockComponentHolder holder, Builder builder) {
        super(holder);

        property = EnumProperty.create("facing_component", Direction.class, builder.directions.toArray(Direction[]::new));
        defaultFacing = Objects.requireNonNullElseGet(builder.defaultFacing, () -> property.getPossibleValues().getFirst());
        facingForPlacement = Objects.requireNonNullElseGet(builder.facingForPlacement, () -> context -> getDefaultFacing());
        compatibilities = Set.copyOf(builder.compatibilities);
        ComponentHelper.validateCompatibilities(property, compatibilities);
    }

    public EnumProperty<Direction> getProperty() {
        return property;
    }

    public Direction getDefaultFacing() {
        return defaultFacing;
    }

    public Direction get(BlockState blockState) {
        return blockState.getValue(property);
    }

    public BlockState set(BlockState blockState, Direction facing) {
        for(var compatibility : compatibilities) {
            blockState = blockState.setValue(compatibility, facing);
        }

        return blockState.setValue(property, facing);
    }

    public BlockState setFor(BlockPlaceContext context, BlockState blockState) {
        var facing = facingForPlacement.apply(context);
        return set(blockState, facing);
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        for(var compatibility : compatibilities) {
            blockState = blockState.setValue(compatibility, defaultFacing);
        }

        return set(blockState, defaultFacing);
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(property);
        compatibilities.forEach(consumer);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context, BlockState blockState) {
        return setFor(context, blockState);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return set(blockState, rotation.rotate(get(blockState)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(get(blockState)));
    }

    public static void registerHorizontal(BlockComponentRegistrar registrar, UnaryOperator<Builder> additional) {
        registrar.register(COMPONENT_TYPE, builder -> additional.apply(builder
                .allowing(Direction.Plane.HORIZONTAL)
                .defaultFacing(Direction.NORTH)
                .facingForPlacement(context -> context.getHorizontalDirection().getOpposite())
                .supporting(BlockStateProperties.HORIZONTAL_FACING)
        ));
    }

    public static void registerHorizontal(BlockComponentRegistrar registrar) {
        registerHorizontal(registrar, UnaryOperator.identity());
    }

    public static final class Builder {
        private final Set<Direction> directions = EnumSet.noneOf(Direction.class);
        @Nullable private Direction defaultFacing = null;
        @Nullable private Function<BlockPlaceContext, Direction> facingForPlacement;
        private final Set<Property<Direction>> compatibilities = Sets.newHashSet();

        public Builder allowing(Direction... directions) {
            Collections.addAll(this.directions, directions);
            return this;
        }

        public Builder allowing(Direction.Plane plane) {
            ApexUtil.addAll(directions, plane);
            return this;
        }

        public Builder disallowing(Direction... directions) {
            ApexUtil.removeAll(this.directions, directions);
            return this;
        }

        public Builder disallowing(Direction plane) {
            ApexUtil.removeAll(directions, plane);
            return this;
        }

        public Builder defaultFacing(Direction defaultFacing) {
            this.defaultFacing = defaultFacing;
            return allowing(defaultFacing);
        }

        public Builder facingForPlacement(Function<BlockPlaceContext, Direction> facingForPlacement) {
            this.facingForPlacement = facingForPlacement;
            return this;
        }

        public Builder supporting(Property<Direction> property, Property<Direction>... properties) {
            Collections.addAll(compatibilities, properties);
            return supporting(property);
        }

        public Builder supporting(Property<Direction> property) {
            compatibilities.add(property);
            return this;
        }
    }
}
