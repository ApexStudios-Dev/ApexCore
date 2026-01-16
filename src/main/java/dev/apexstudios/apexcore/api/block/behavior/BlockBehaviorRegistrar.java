package dev.apexstudios.apexcore.api.block.behavior;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.function.Consumer;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.NonExtendable
public interface BlockBehaviorRegistrar {
    @CanIgnoreReturnValue
    <TBehavior extends BlockBehavior, TProperties> BlockBehaviorRegistrar register(BlockBehaviorType<TBehavior, TProperties> type, Consumer<TProperties> propertiesCallback);

    @CanIgnoreReturnValue
    default BlockBehaviorRegistrar register(BlockBehaviorType<?, ?> type) {
        return register(type, Consumers.nop());
    }
}
