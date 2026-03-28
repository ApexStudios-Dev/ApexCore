package dev.apexstudios.apexcore.neoforge.api.data.provider.loot;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

@FunctionalInterface
public interface LootTableSubProvider {
    void accept(ResourceKey<LootTable> lootTableKey, Supplier<LootTable.Builder> lootTable);

    default void accept(ResourceKey<LootTable> lootTableKey, LootTable.Builder lootTable) {
        accept(lootTableKey, () -> lootTable);
    }

    default void accept(Identifier lootTableKey, Supplier<LootTable.Builder> lootTable) {
        accept(ResourceKey.create(Registries.LOOT_TABLE, lootTableKey), lootTable);
    }

    default void accept(Identifier lootTableKey, LootTable.Builder lootTable) {
        accept(lootTableKey, () -> lootTable);
    }
}
