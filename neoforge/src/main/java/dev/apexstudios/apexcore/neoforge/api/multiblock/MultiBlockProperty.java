package dev.apexstudios.apexcore.neoforge.api.multiblock;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public final class MultiBlockProperty extends Property<Integer> {
    public static final String DEFAULT_NAME = "multi_block_index";

    private final List<Vector3ic> positions;
    private final IntegerProperty internal;

    public MultiBlockProperty(String name, Collection<Vector3ic> positions) {
        super(name, Integer.class);

        if(positions.size() <= 1)
            throw new IllegalArgumentException("Multi blocks must contain more than the origin point");

        this.positions = List.copyOf(positions);
        internal = IntegerProperty.create(name, 0, positions.size() - 1);
    }

    public List<Vector3ic> getPositions() {
        return positions;
    }

    @Override
    public List<Integer> getPossibleValues() {
        return internal.getPossibleValues();
    }

    @Override
    public String getName(Integer value) {
        return internal.getName(value);
    }

    @Override
    public Optional<Integer> getValue(String value) {
        return internal.getValue(value);
    }

    @Override
    public int getInternalIndex(Integer value) {
        return internal.getInternalIndex(value);
    }

    @Override
    public boolean equals(Object other) {
        if(this == other)
            return true;
        if(!super.equals(other))
            return false;
        if(!(other instanceof MultiBlockProperty property))
            return false;
        if(!internal.equals(property.internal))
            return false;
        return positions.equals(property.positions);
    }

    @Override
    public int generateHashCode() {
        return internal.generateHashCode();
    }

    public static MultiBlockProperty create(String name, Consumer<Builder> consumer) {
        var builder = new Builder();
        consumer.accept(builder);
        return new MultiBlockProperty(name, builder.positions);
    }

    public static MultiBlockProperty create(Consumer<Builder> consumer) {
        return create(DEFAULT_NAME, consumer);
    }

    public static final class Builder {
        private final Set<Vector3ic> positions = Sets.newLinkedHashSet();

        private Builder() {
            with(0, 0, 0);
        }

        public Builder with(int x, int y, int z) {
            return with(new Vector3i(x, y, z));
        }

        public Builder with(Vector3ic position) {
            positions.add(position);
            return this;
        }

        public Builder sized(int sizeX, int sizeY, int sizeZ) {
            for(var y = 0; y < sizeY; y++) {
                for(var x = 0; x < sizeX; x++) {
                    for(var z = 0; z < sizeZ; z++) {
                        with(x, y, z);
                    }
                }
            }

            return this;
        }

        public Builder sized(int size) {
            return sized(size, size, size);
        }
    }
}
