package dev.apexstudios.apexcore.lib.data.provider.loot;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

@FunctionalInterface
public interface LootTableSubProvider {
    void accept(ResourceKey<LootTable> lootTableKey, Supplier<LootTable.Builder> lootTable);

    default void accept(ResourceKey<LootTable> lootTableKey, LootTable.Builder lootTable) {
        accept(lootTableKey, () -> lootTable);
    }

    default void accept(ResourceLocation lootTableKey, Supplier<LootTable.Builder> lootTable) {
        accept(ResourceKey.create(Registries.LOOT_TABLE, lootTableKey), lootTable);
    }

    default void accept(ResourceLocation lootTableKey, LootTable.Builder lootTable) {
        accept(lootTableKey, () -> lootTable);
    }
}
