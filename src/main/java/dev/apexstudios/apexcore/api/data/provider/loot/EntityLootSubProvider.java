package dev.apexstudios.apexcore.api.data.provider.loot;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import java.util.function.Supplier;

public interface EntityLootSubProvider extends LootTableSubProvider {
    default void accept(Holder<EntityType<?>> entityType, Supplier<LootTable.Builder> lootTable) {
        accept(entityType.value(), lootTable);
    }

    default void accept(Holder<EntityType<?>> entityType, LootTable.Builder lootTable) {
        accept(entityType, () -> lootTable);
    }

    default void accept(EntityType<?> entityType, Supplier<LootTable.Builder> lootTable) {
        accept(entityType.getDefaultLootTable().orElseThrow(() -> new IllegalStateException("Entity " + entityType + " has no loot table")), lootTable);
    }

    default void accept(EntityType<?> entityType, LootTable.Builder lootTable) {
        accept(entityType, () -> lootTable);
    }

    AnyOfCondition.Builder shouldSmeltLoot();

    LootItemCondition.Builder killedByFrog(HolderGetter<EntityType<?>> entityTypeRegistry);

    LootItemCondition.Builder killedByFrogVariant(HolderGetter<EntityType<?>> entityTypeRegistry, HolderGetter<FrogVariant> variantRegistry, ResourceKey<FrogVariant> variantKey);

    static LootPool.Builder createSheepDispatchPool(ColorCollection<ResourceKey<LootTable>> lootTables) {
        return net.minecraft.data.loot.EntityLootSubProvider.createSheepDispatchPool(lootTables);
    }
}
