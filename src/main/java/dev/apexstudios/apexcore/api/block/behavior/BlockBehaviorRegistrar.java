package dev.apexstudios.apexcore.api.block.behavior;

import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.NonExtendable
public interface BlockBehaviorRegistrar {
    void register(BlockBehaviorType<?>... types);
}
