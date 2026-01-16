package dev.apexstudios.apexcore.api.block.behavior;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public final class BlockBehaviorType<TBehavior extends BlockBehavior, TProperties> {
    private final BiFunction<BlockBehaviorRegistration, TProperties, TBehavior> factory;
    private final Supplier<TProperties> propertiesFactory;

    private BlockBehaviorType(BiFunction<BlockBehaviorRegistration, TProperties, TBehavior> factory, Supplier<TProperties> propertiesFactory) {
        this.factory = factory;
        this.propertiesFactory = propertiesFactory;
    }

    TBehavior create(BlockBehaviorRegistration registration, Consumer<TProperties> propertiesCallback) {
        var properties = propertiesFactory.get();
        propertiesCallback.accept(properties);
        return factory.apply(registration, properties);
    }

    public static <TBehavior extends BlockBehavior, TProperties> BlockBehaviorType<TBehavior, TProperties> create(BiFunction<BlockBehaviorRegistration, TProperties, TBehavior> factory, Supplier<TProperties> propertiesFactory) {
        return new BlockBehaviorType<>(factory, propertiesFactory);
    }

    @SuppressWarnings({"DataFlowIssue", "NullableProblems"})
    public static <TBehavior extends BlockBehavior> BlockBehaviorType<TBehavior, @Nullable Void> create(Function<BlockBehaviorRegistration, TBehavior> factory) {
        return create((registration, properties) -> factory.apply(registration), () -> null);
    }
}
