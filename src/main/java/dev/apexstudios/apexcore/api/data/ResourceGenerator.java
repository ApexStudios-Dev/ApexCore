package dev.apexstudios.apexcore.api.data;

import dev.apexstudios.apexcore.api.data.pack.FeaturePackGenerator;
import dev.apexstudios.apexcore.api.data.pack.ModPackGenerator;
import dev.apexstudios.apexcore.common.data.ResourceGeneration;
import java.util.function.Consumer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.commons.lang3.function.Consumers;

public interface ResourceGenerator {
    ModPackGenerator pack();

    FeaturePackGenerator pack(String packId);

    static void of(GatherDataEvent event, Consumer<ResourceGenerator> consumer) {
        var generator = new ResourceGeneration();
        consumer.accept(generator);
        generator.generate(event.getModContainer(), event::getResourceManager, event.getLookupProvider(), event.getGenerator());
    }

    static void of(IEventBus modBus, Consumer<ResourceGenerator> consumer) {
        modBus.addListener(GatherDataEvent.Client.class, event -> of(event, consumer));
        modBus.addListener(GatherDataEvent.Server.class, event -> of(event, consumer));
    }

    static void simple(GatherDataEvent event) {
        of(event, Consumers.nop());
    }

    static void simple(IEventBus modBus) {
        of(modBus, Consumers.nop());
    }
}
