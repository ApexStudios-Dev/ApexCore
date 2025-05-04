package dev.apexstudios.apexcore.lib.component;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval
public interface ComponentBuilder {
    Supplier<ComponentBuilder> NOOP = Suppliers.memoize(() -> new ComponentBuilder() { });
}
