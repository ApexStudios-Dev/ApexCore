package dev.apexstudios.apexcore.lib.placement;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.core.placement.BlockPlacementRender;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.util.context.ContextKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

public final class RegisterBlockPlacementRendererEvent extends Event implements IModBusEvent {
    private final Map<ContextKey<?>, BlockPlacementRender<?>> registry = Maps.newConcurrentMap();

    private RegisterBlockPlacementRendererEvent() {
    }

    public <T> void register(ContextKey<T> key, ExtractBlockPlacementState<T> extractor, SubmitBlockPlacementState<T> renderer) {
        if(registry.putIfAbsent(key, new BlockPlacementRender<>(key, extractor, renderer)) != null)
            throw new IllegalStateException("Duplicate BlockPlacementRenderer: " + key.name());
    }

    @ApiStatus.Internal
    public static Map<ContextKey<?>, BlockPlacementRender<?>> register(Consumer<RegisterBlockPlacementRendererEvent> consumer) {
        var event = new RegisterBlockPlacementRendererEvent();
        consumer.accept(event);
        ModLoader.postEventWrapContainerInModOrder(event);
        return Map.copyOf(event.registry);
    }
}
