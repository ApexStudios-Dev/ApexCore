package dev.apexstudios.apexcore.api.block.behavior;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class SimplePropertyBlockBehavior<TValue extends Comparable<TValue>, TProperty extends Property<TValue>> extends BlockBehavior {
    private final TProperty property;
    private final TValue defaultValue;

    protected SimplePropertyBlockBehavior(BlockBehaviorRegistration registration, TProperty property, TValue defaultValue) {
        super(registration);

        this.defaultValue = defaultValue;
        this.property = registration.property(property, defaultValue);
    }

    public final TValue defaultValue() {
        return defaultValue;
    }

    public final TProperty property() {
        return property;
    }

    public final TValue get(BlockState blockState) {
        return blockState.getValueOrElse(property(), defaultValue());
    }

    public final BlockState set(BlockState blockState, TValue value) {
        return blockState.trySetValue(property(), value);
    }

    @Override
    protected BlockState copyProperties(BlockState blockState, BlockState neighborBlockState) {
        return set(blockState, get(neighborBlockState));
    }
}
