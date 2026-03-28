package dev.apexstudios.apexcore.common.data.provider.loot;

import dev.apexstudios.apexcore.api.data.provider.loot.EntityLootSubProvider;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

final class EntityLootSubProviderImpl extends LootTableSubProviderImpl implements EntityLootSubProvider {
    private final net.minecraft.data.loot.EntityLootSubProvider delegate;

    EntityLootSubProviderImpl(LootTableSubProviderFactory.Context context) {
        super(context);

        delegate = new net.minecraft.data.loot.EntityLootSubProvider(FeatureFlags.REGISTRY.allFlags(), context.enabledFeatures(), context.registries()) {
            // clear out this provider to ensure nothing generates
            @Override
            public void generate() { }

            @Override
            public Stream<EntityType<?>> getKnownEntityTypes() {
                return Stream.empty();
            }

            @Override
            public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) { }

            // redirect loot table gen into our custom providers
            @Override
            public void add(EntityType<?> entityType, LootTable.Builder builder) {
                accept(entityType, builder);
            }

            @Override
            public void add(EntityType<?> entityType, ResourceKey<LootTable> defaultLootTable, LootTable.Builder builder) {
                accept(entityType.getDefaultLootTable().orElse(defaultLootTable), builder);
            }
        };
    }

    // region: Delegates
    @Override
    public AnyOfCondition.Builder shouldSmeltLoot() {
        return delegate.shouldSmeltLoot();
    }

    @Override
    public LootItemCondition.Builder killedByFrog(HolderGetter<EntityType<?>> entityTypeRegistry) {
        return delegate.killedByFrog(entityTypeRegistry);
    }

    @Override
    public LootItemCondition.Builder killedByFrogVariant(HolderGetter<EntityType<?>> entityTypeRegistry, HolderGetter<FrogVariant> variantRegistry, ResourceKey<FrogVariant> variantKey) {
        return delegate.killedByFrogVariant(entityTypeRegistry, variantRegistry, variantKey);
    }
    // endregion
}
