package dev.apexstudios.apexcore.api.data;

import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public abstract class BlockLootProvider extends BlockLootSubProvider {
    protected BlockLootProvider(Set<Item> explosionResistant, FeatureFlagSet enabledFeatures, Context output) {
        super(explosionResistant, enabledFeatures, output);
    }

    protected BlockLootProvider(Context output) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, output);
    }

    // @formatter:off
    @Override public Holder<LootItemCondition> hasSilkTouch() {return super.hasSilkTouch();}
    @Override public LootItemCondition.Builder doesNotHaveSilkTouch() {return super.doesNotHaveSilkTouch();}
    @Override public Holder<LootItemCondition> hasShears() {return super.hasShears();}
    @Override public <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike type, FunctionUserBuilder<T> builder) {return super.applyExplosionDecay(type, builder);}
    @Override public <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike type, ConditionUserBuilder<T> builder) {return super.applyExplosionCondition(type, builder);}
    @Override public LootTable.Builder createSilkTouchDispatchTable(Block original, LootPoolEntryContainer.Builder<?> entry) {return super.createSilkTouchDispatchTable(original, entry);}
    @Override public LootTable.Builder createShearsDispatchTable(Block original, LootPoolEntryContainer.Builder<?> entry) {return super.createShearsDispatchTable(original, entry);}
    @Override public LootTable.Builder createSilkTouchOrShearsDispatchTable(Block original, LootPoolEntryContainer.Builder<?> entry) {return super.createSilkTouchOrShearsDispatchTable(original, entry);}
    @Override public LootTable.Builder createSingleItemTableWithSilkTouch(Block original, ItemLike drop) {return super.createSingleItemTableWithSilkTouch(original, drop);}
    @Override public LootTable.Builder createSingleItemTable(ItemLike drop, Holder<ContextIntProvider> count) {return super.createSingleItemTable(drop, count);}
    @Override public LootTable.Builder createSingleItemTableWithSilkTouch(Block original, ItemLike drop, Holder<ContextIntProvider> count) {return super.createSingleItemTableWithSilkTouch(original, drop, count);}
    @Override public LootTable.Builder createSilkTouchOnlyTable(ItemLike drop) {return super.createSilkTouchOnlyTable(drop);}
    @Override public LootTable.Builder createPotFlowerItemTable(ItemLike flower) {return super.createPotFlowerItemTable(flower);}
    @Override public LootTable.Builder createSlabItemTable(Block slab) {return super.createSlabItemTable(slab);}
    @Override public <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block drop, Property<T> property, T value) {return super.createSinglePropConditionTable(drop, property, value);}
    @Override public LootTable.Builder createNameableBlockEntityTable(Block drop) {return super.createNameableBlockEntityTable(drop);}
    @Override public LootTable.Builder createShulkerBoxDrop(Block shulkerBox) {return super.createShulkerBoxDrop(shulkerBox);}
    @Override public LootTable.Builder createCopperOreDrops(Block block) {return super.createCopperOreDrops(block);}
    @Override public LootTable.Builder createLapisOreDrops(Block block) {return super.createLapisOreDrops(block);}
    @Override public LootTable.Builder createRedstoneOreDrops(Block block) {return super.createRedstoneOreDrops(block);}
    @Override public LootTable.Builder createBannerDrop(Block original) {return super.createBannerDrop(original);}
    @Override public LootTable.Builder createBeeNestDrop(Block original) {return super.createBeeNestDrop(original);}
    @Override public LootTable.Builder createBeeHiveDrop(Block original) {return super.createBeeHiveDrop(original);}
    @Override public LootTable.Builder createCaveVinesDrop(Block original) {return super.createCaveVinesDrop(original);}
    @Override public LootTable.Builder createCopperGolemStatueBlock(Block block) {return super.createCopperGolemStatueBlock(block);}
    @Override public LootTable.Builder createOreDrop(Block original, Item drop) {return super.createOreDrop(original, drop);}
    @Override public LootTable.Builder createMushroomBlockDrop(Block original, ItemLike drop) {return super.createMushroomBlockDrop(original, drop);}
    @Override public LootTable.Builder createGrassDrops(Block original) {return super.createGrassDrops(original);}
    @Override public LootTable.Builder createShearsOnlyDrop(ItemLike drop) {return super.createShearsOnlyDrop(drop);}
    @Override public LootTable.Builder createShearsOrSilkTouchOnlyDrop(ItemLike drop) {return super.createShearsOrSilkTouchOnlyDrop(drop);}
    @Override public LootTable.Builder createMultifaceBlockDrops(Block block, Holder<LootItemCondition> condition) {return super.createMultifaceBlockDrops(block, condition);}
    @Override public LootTable.Builder createMultifaceBlockDrops(Block block) {return super.createMultifaceBlockDrops(block);}
    @Override public LootTable.Builder createMossyCarpetBlockDrops(Block block) {return super.createMossyCarpetBlockDrops(block);}
    @Override public LootTable.Builder createLeavesDrops(Block original, Block sapling, float... saplingChances) {return super.createLeavesDrops(original, sapling, saplingChances);}
    @Override public LootTable.Builder createOakLeavesDrops(Block original, Block sapling, float... saplingChances) {return super.createOakLeavesDrops(original, sapling, saplingChances);}
    @Override public LootTable.Builder createMangroveLeavesDrops(Block block) {return super.createMangroveLeavesDrops(block);}
    @Override public LootTable.Builder createCropDrops(Block original, Item cropDrop, Item seedDrop, LootItemCondition.Builder isMaxAge) {return super.createCropDrops(original, cropDrop, seedDrop, isMaxAge);}
    @Override public LootTable.Builder createDoublePlantShearsDrop(Block block) {return super.createDoublePlantShearsDrop(block);}
    @Override public LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block drop) {return super.createDoublePlantWithSeedDrops(block, drop);}
    @Override public LootTable.Builder createCandleDrops(Block block) {return super.createCandleDrops(block);}
    @Override public void addNetherVinesDropTable(Block vineBlock, Block plantBlock) {super.addNetherVinesDropTable(vineBlock, plantBlock);}
    @Override public LootTable.Builder createDoorTable(Block block) {return super.createDoorTable(block);}
    @Override public void dropPottedContents(Block potted) {super.dropPottedContents(potted);}
    @Override public void otherWhenSilkTouch(Block block, Block other) {super.otherWhenSilkTouch(block, other);}
    @Override public void dropOther(Block block, ItemLike drop) {super.dropOther(block, drop);}
    @Override public void dropWhenSilkTouch(Block block) {super.dropWhenSilkTouch(block);}
    @Override public void dropSelf(Block block) {super.dropSelf(block);}
    @Override public void add(Block block, Function<Block, LootTable.Builder> builder) {super.add(block, builder);}
    @Override public void add(Block block, LootTable.Builder builder) {super.add(block, builder);}
    public static LootTable.Builder createSelfDropDispatchTable(Block original, Holder<LootItemCondition> condition, LootPoolEntryContainer.Builder<?> entry) {return BlockLootSubProvider.createSelfDropDispatchTable(original, condition, entry);}
    public static LootTable.Builder createCandleCakeDrops(Block candle) {return BlockLootSubProvider.createCandleCakeDrops(candle);}
    // @formatter:on
}
