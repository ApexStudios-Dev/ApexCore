package dev.apexstudios.apexcore.api.data;

import dev.apexstudios.registree.api.Registree;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public abstract class BlockLootProvider extends BlockLootSubProvider {
    private final Registree registree;

    protected BlockLootProvider(Set<Item> explosionResistant, FeatureFlagSet enabledFeatures, Context output, Registree registree) {
        super(explosionResistant, enabledFeatures, output);

        this.registree = registree;
    }

    protected BlockLootProvider(FeatureFlagSet enabledFeatures, Context output, Registree registree) {
        this(Set.of(), enabledFeatures, output, registree);
    }

    protected BlockLootProvider(Set<Item> explosionResistant, Context output, Registree registree) {
        this(explosionResistant, FeatureFlags.DEFAULT_FLAGS, output, registree);
    }

    protected BlockLootProvider(Context output, Registree registree) {
        this(Set.of(), FeatureFlags.DEFAULT_FLAGS, output, registree);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return registree.registrar(Registries.BLOCK).values();
    }

    public static LootTableProvider.SubProviderEntry create(LootTableSubProvider.Factory factory) {
        return new LootTableProvider.SubProviderEntry(factory, LootContextParamSets.BLOCK);
    }
}
