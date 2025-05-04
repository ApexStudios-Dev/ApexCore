package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.ScheduledForRemoval
public final class RotationBlockComponent extends BaseBlockComponent {
    public static final ComponentType<BlockComponent, RotationBlockComponent, Block, Builder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("rotation"),
            Builder::new,
            RotationBlockComponent::new
    );

    public static final int DEFAULT_SEGMENT = 0;

    private final int segments;
    private final ToIntFunction<BlockPlaceContext> segmentForPlacement;
    private final IntegerProperty property;

    private RotationBlockComponent(ComponentHolder<BlockComponent, Block> holder, Builder builder) {
        super(holder);

        if(builder.segments <= 0)
            throw new IllegalStateException("RotationComponent Segments must be > 0");

        segments = builder.segments;
        segmentForPlacement = Objects.requireNonNullElseGet(builder.segmentForPlacement, () -> context -> DEFAULT_SEGMENT);
        property = IntegerProperty.create("rotation", DEFAULT_SEGMENT, segments);
    }

    public IntegerProperty getProperty() {
        return property;
    }

    public int getSegments() {
        return segments;
    }

    public int get(BlockState blockState) {
        return blockState.getValue(property);
    }

    public BlockState set(BlockState blockState, int segment) {
        return blockState.setValue(property, segment);
    }

    public BlockState setFor(BlockPlaceContext context, BlockState blockState) {
        var segment = segmentForPlacement.applyAsInt(context);
        return set(blockState, segment);
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return set(blockState, DEFAULT_SEGMENT);
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
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return set(blockState, rotation.rotate(get(blockState), segments));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return set(blockState, mirror.mirror(get(blockState), segments));
    }

    public static void register16(ComponentRegistrar<BlockComponent, Block> registrar, UnaryOperator<Builder> additional) {
        registrar.register(COMPONENT_TYPE, builder -> additional.apply(builder
                .segments(16)
                .facingForPlacement(context -> RotationSegment.convertToSegment(context.getRotation() + 180F))
        ));
    }

    public static void register16(ComponentRegistrar<BlockComponent, Block> registrar) {
        register16(registrar, UnaryOperator.identity());
    }

    public static final class Builder implements ComponentBuilder {
        private int segments = -1;
        @Nullable private ToIntFunction<BlockPlaceContext> segmentForPlacement;

        public Builder segments(int segments) {
            this.segments = segments;
            return this;
        }

        public Builder facingForPlacement(ToIntFunction<BlockPlaceContext> segmentForPlacement) {
            this.segmentForPlacement = segmentForPlacement;
            return this;
        }
    }
}
