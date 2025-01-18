package dev.apexstudios.apexcore.lib.component;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;

public interface ComponentBuilder {
    Supplier<ComponentBuilder> NOOP = Suppliers.memoize(() -> new ComponentBuilder() { });
}
