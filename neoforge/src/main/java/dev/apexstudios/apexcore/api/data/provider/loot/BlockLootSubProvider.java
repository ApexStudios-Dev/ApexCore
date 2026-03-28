package dev.apexstudios.apexcore.api.data.provider.loot;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public interface BlockLootSubProvider extends LootTableSubProvider {
    Set<Item> VANILLA_EXPLOSION_RESISTANT = VanillaBlockLoot.EXPLOSION_RESISTANT;
    float[] JUNGLE_LEAVES_SAPLING_CHANGES = VanillaBlockLoot.JUNGLE_LEAVES_SAPLING_CHANGES;
    float[] NORMAL_LEAVES_SAPLING_CHANCES = net.minecraft.data.loot.BlockLootSubProvider.NORMAL_LEAVES_SAPLING_CHANCES;
    float[] NORMAL_LEAVES_STICK_CHANCES = net.minecraft.data.loot.BlockLootSubProvider.NORMAL_LEAVES_STICK_CHANCES;

    default void accept(Holder<Block> block, Supplier<LootTable.Builder> lootTable) {
        accept(block.value(), lootTable);
    }

    default void accept(Holder<Block> block, LootTable.Builder lootTable) {
        accept(block, () -> lootTable);
    }

    default void accept(Block block, Supplier<LootTable.Builder> lootTable) {
        accept(block.getLootTable().orElseThrow(() -> new IllegalStateException("Block " + block.builtInRegistryHolder().key().identifier() + " does not have loot table")), lootTable);
    }

    default void accept(Block block, LootTable.Builder lootTable) {
        accept(block, () -> lootTable);
    }

    LootItemCondition.Builder hasSilkTouch();

    LootItemCondition.Builder doesNotHaveSilkTouch();

    LootItemCondition.Builder hasShears();

    LootItemCondition.Builder hasShearsOrSilkTouch();

    LootItemCondition.Builder doesNotHaveShearsOrSilkTouch();

    <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike item, FunctionUserBuilder<T> functionBuilder);

    <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike item, ConditionUserBuilder<T> conditionBuilder);

    LootTable.Builder createSingleItemTable(ItemLike item);

    LootTable.Builder createSilkTouchDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder);

    LootTable.Builder createShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder);

    LootTable.Builder createSilkTouchOrShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder);

    LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike item);

    LootTable.Builder createSingleItemTable(ItemLike item, NumberProvider count);

    LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike item, NumberProvider count);

    LootTable.Builder createSilkTouchOnlyTable(ItemLike item);

    LootTable.Builder createPotFlowerItemTable(ItemLike item);

    LootTable.Builder createSlabItemTable(Block block);

    <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block block, Property<T> property, T value);

    LootTable.Builder createNameableBlockEntityTable(Block block);

    LootTable.Builder createShulkerBoxDrop(Block block);

    LootTable.Builder createCopperOreDrops(Block block);

    LootTable.Builder createLapisOreDrops(Block block);

    LootTable.Builder createRedstoneOreDrops(Block block);

    LootTable.Builder createBannerDrop(Block block);

    LootTable.Builder createBeeNestDrop(Block block);

    LootTable.Builder createBeeHiveDrop(Block block);

    LootTable.Builder createCaveVinesDrop(Block block);

    LootTable.Builder createOreDrop(Block block, Item item);

    LootTable.Builder createMushroomBlockDrop(Block block, ItemLike item);

    LootTable.Builder createGrassDrops(Block block);

    LootTable.Builder createStemDrops(Block block, Item item);

    LootTable.Builder createAttachedStemDrops(Block block, Item item);

    LootTable.Builder createShearsOnlyDrop(ItemLike item);

    LootTable.Builder createShearsOrSilkTouchOnlyDrop(ItemLike item);

    LootTable.Builder createMultifaceBlockDrops(Block block, LootItemCondition.Builder builder);

    LootTable.Builder createMultifaceBlockDrops(Block block);

    LootTable.Builder createMossyCarpetBlockDrops(Block block);

    LootTable.Builder createLeavesDrops(Block leavesBlock, Block saplingBlock, float... chances);

    LootTable.Builder createOakLeavesDrops(Block oakLeavesBlock, Block saplingBlock, float... chances);

    LootTable.Builder createMangroveLeavesDrops(Block block);

    LootTable.Builder createCropDrops(Block cropBlock, Item grownCropItem, Item seedsItem, LootItemCondition.Builder dropGrownCropCondition);

    LootTable.Builder createDoublePlantShearsDrop(Block sheared);

    LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block sheared);

    LootTable.Builder createCandleDrops(Block candleBlock);

    LootTable.Builder createSegmentedBlockDrops(Block block);

    void addNetherVinesDropTable(Block vines, Block plant);

    LootTable.Builder createDoorTable(Block doorBlock);

    void dropPottedContents(Block flowerPot);

    void otherWhenSilkTouch(Block block, Block other);

    void dropOther(Block block, ItemLike item);

    void dropWhenSilkTouch(Block block);

    void dropSelf(Block block);

    static LootTable.Builder createSelfDropDispatchTable(Block block, LootItemCondition.Builder conditionBuilder, LootPoolEntryContainer.Builder<?> alternativeBuilder) {
        return net.minecraft.data.loot.BlockLootSubProvider.createSelfDropDispatchTable(block, conditionBuilder, alternativeBuilder);
    }

    static LootTable.Builder createCandleCakeDrops(Block candleCakeBlock) {
        return net.minecraft.data.loot.BlockLootSubProvider.createCandleCakeDrops(candleCakeBlock);
    }

    static LootTable.Builder noDrop() {
        return net.minecraft.data.loot.BlockLootSubProvider.noDrop();
    }
}
