package dev.apexstudios.apexcore.api.block.behavior;

import java.util.function.Function;

public final class BlockBehaviorType<TBehavior extends BlockBehavior> {
    final Function<BlockBehaviorRegistration, TBehavior> factory;

    public BlockBehaviorType(Function<BlockBehaviorRegistration, TBehavior> factory) {
        this.factory = factory;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(!(obj instanceof BlockBehaviorType<?> other)) {
            return false;
        }

        return factory.equals(other.factory);
    }

    @Override
    public int hashCode() {
        return factory.hashCode();
    }
}
