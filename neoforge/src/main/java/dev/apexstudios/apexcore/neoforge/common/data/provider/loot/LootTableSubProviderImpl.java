package dev.apexstudios.apexcore.neoforge.common.data.provider.loot;

import dev.apexstudios.apexcore.neoforge.api.data.provider.loot.LootTableSubProvider;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableSubProviderImpl implements LootTableSubProvider {
    private final LootTableSubProviderFactory.Context context;

    LootTableSubProviderImpl(LootTableSubProviderFactory.Context context) {
        this.context = context;
    }

    @Override
    public void accept(ResourceKey<LootTable> lootTableKey, Supplier<LootTable.Builder> lootTable) {
        context.accept(lootTableKey, lootTable);
    }
}
