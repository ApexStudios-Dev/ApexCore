package dev.apexstudios.apexcore.api.data.provider.loot;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.common.data.provider.loot.LootTableProviderImpl;
import dev.apexstudios.registree.BaseRegistree;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.lang3.Validate;

public interface LootTableProvider {
    ProviderType<LootTableProvider> PROVIDER_TYPE = LootTableProviderImpl.PROVIDER_TYPE;

    LootTableProvider forContext(ContextKeySet context, Consumer<? extends LootTableSubProvider> consumer);

    default LootTableProvider block(Consumer<BlockLootSubProvider> consumer) {
        return forContext(LootContextParamSets.BLOCK, consumer);
    }

    default LootTableProvider entity(Consumer<EntityLootSubProvider> consumer) {
        return forContext(LootContextParamSets.BLOCK, consumer);
    }

    LootTableProvider requiredLootTable(ResourceKey<LootTable> lootTable);

    <TRegistry> LootTableProvider knownElements(ResourceKey<? extends Registry<TRegistry>> registryType, Supplier<Stream<? extends Holder<TRegistry>>> knownElementsSupplier);

    <TRegistry> LootTableProvider elementFilter(ResourceKey<? extends Registry<TRegistry>> registryType, Predicate<Holder<TRegistry>> filter);

    default <TRegistry> LootTableProvider fromRegistree(BaseRegistree<?> registree, ResourceKey<? extends Registry<TRegistry>> registryType) {
        return knownElements(registryType, () -> registree.holders(registryType));
    }

    default LootTableProvider fromRegistree(BaseRegistree<?> registree) {
        registree.registries().forEach(registryType -> fromRegistree(registree, (ResourceKey) registryType));
        return this;
    }

    default <TRegistry> LootTableProvider knownElements(DeferredHolder<TRegistry, ?>... elements) {
        Objects.checkIndex(0, elements.length);
        var registryType = elements[0].getKey().registryKey();
        return knownElements(registryType, () -> Stream.of(elements).peek(holder -> Validate.isTrue(registryType == holder.getKey().registryKey())));
    }
}
