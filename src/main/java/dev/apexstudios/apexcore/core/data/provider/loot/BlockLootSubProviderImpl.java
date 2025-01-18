package dev.apexstudios.apexcore.core.data.provider.loot;

import com.google.common.collect.Sets;
import dev.apexstudios.apexcore.lib.data.provider.loot.BlockLootSubProvider;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

final class BlockLootSubProviderImpl extends LootTableSubProviderImpl implements BlockLootSubProvider {
    private final Set<Item> explosionResistant = Sets.newHashSet();

    BlockLootSubProviderImpl(LootTableSubProviderFactory.Context context) {
        super(context);
    }

    @Override
    public void explosionResistant(ItemLike item) {
        explosionResistant.add(item.asItem());
    }

    @Override
    public boolean isExplosionResistant(ItemLike itemLike) {
        var item = itemLike.asItem();
        return explosionResistant.contains(item) || BlockLootSubProvider.VANILLA_EXPLOSION_RESISTANT.contains(item);
    }
}
