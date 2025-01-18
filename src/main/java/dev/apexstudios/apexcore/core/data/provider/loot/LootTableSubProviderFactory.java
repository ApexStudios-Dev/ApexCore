package dev.apexstudios.apexcore.core.data.provider.loot;

import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

@FunctionalInterface
public interface LootTableSubProviderFactory {
    LootTableSubProviderImpl create(Context context);

    interface Context extends ProviderListenerContext, BiConsumer<ResourceKey<LootTable>, Supplier<LootTable.Builder>> {
        <TRegistry> Stream<? extends Holder<TRegistry>> knownElements(ResourceKey<? extends Registry<TRegistry>> registryType);
    }
}
