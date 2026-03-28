package dev.apexstudios.apexcore.neoforge.common.data.provider.loot;

import dev.apexstudios.apexcore.neoforge.api.data.provider.loot.BlockLootSubProvider;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
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

final class BlockLootSubProviderImpl extends LootTableSubProviderImpl implements BlockLootSubProvider {
    private final net.minecraft.data.loot.BlockLootSubProvider delegate;

    BlockLootSubProviderImpl(LootTableSubProviderFactory.Context context) {
        super(context);

        delegate = new net.minecraft.data.loot.BlockLootSubProvider(VANILLA_EXPLOSION_RESISTANT, context.enabledFeatures(), context.registries()) {
            // clear out this provider to ensure nothing generates
            @Override
            public void generate() { }

            @Override
            public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) { }

            @Override
            public Iterable<Block> getKnownBlocks() {
                return Collections.emptyList();
            }

            // redirect loot table gen into our custom providers
            @Override
            public void add(Block block, LootTable.Builder builder) {
                accept(block, builder);
            }

            @Override
            public void add(Block block, Function<Block, LootTable.Builder> factory) {
                accept(block, () -> factory.apply(block));
            }
        };
    }

    // region: Delegates
    @Override
    public LootItemCondition.Builder hasSilkTouch() {
        return delegate.hasSilkTouch();
    }

    @Override
    public LootItemCondition.Builder doesNotHaveSilkTouch() {
        return delegate.doesNotHaveSilkTouch();
    }

    @Override
    public LootItemCondition.Builder hasShears() {
        return delegate.hasShears();
    }

    @Override
    public LootItemCondition.Builder hasShearsOrSilkTouch() {
        return delegate.hasShearsOrSilkTouch();
    }

    @Override
    public LootItemCondition.Builder doesNotHaveShearsOrSilkTouch() {
        return delegate.doesNotHaveShearsOrSilkTouch();
    }

    @Override
    public <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike item, FunctionUserBuilder<T> functionBuilder) {
        return delegate.applyExplosionDecay(item, functionBuilder);
    }

    @Override
    public <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike item, ConditionUserBuilder<T> conditionBuilder) {
        return delegate.applyExplosionCondition(item, conditionBuilder);
    }

    @Override
    public LootTable.Builder createSingleItemTable(ItemLike item) {
        return delegate.createSingleItemTable(item);
    }

    @Override
    public LootTable.Builder createSilkTouchDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return delegate.createSilkTouchDispatchTable(block, builder);
    }

    @Override
    public LootTable.Builder createShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return delegate.createShearsDispatchTable(block, builder);
    }

    @Override
    public LootTable.Builder createSilkTouchOrShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return delegate.createSilkTouchOrShearsDispatchTable(block, builder);
    }

    @Override
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike item) {
        return delegate.createSingleItemTableWithSilkTouch(block, item);
    }

    @Override
    public LootTable.Builder createSingleItemTable(ItemLike item, NumberProvider count) {
        return delegate.createSingleItemTable(item, count);
    }

    @Override
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike item, NumberProvider count) {
        return delegate.createSingleItemTableWithSilkTouch(block, item, count);
    }

    @Override
    public LootTable.Builder createSilkTouchOnlyTable(ItemLike item) {
        return delegate.createSilkTouchOnlyTable(item);
    }

    @Override
    public LootTable.Builder createPotFlowerItemTable(ItemLike item) {
        return delegate.createPotFlowerItemTable(item);
    }

    @Override
    public LootTable.Builder createSlabItemTable(Block block) {
        return delegate.createSlabItemTable(block);
    }

    @Override
    public <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block block, Property<T> property, T value) {
        return delegate.createSinglePropConditionTable(block, property, value);
    }

    @Override
    public LootTable.Builder createNameableBlockEntityTable(Block block) {
        return delegate.createNameableBlockEntityTable(block);
    }

    @Override
    public LootTable.Builder createShulkerBoxDrop(Block block) {
        return delegate.createShulkerBoxDrop(block);
    }

    @Override
    public LootTable.Builder createCopperOreDrops(Block block) {
        return delegate.createCopperOreDrops(block);
    }

    @Override
    public LootTable.Builder createLapisOreDrops(Block block) {
        return delegate.createLapisOreDrops(block);
    }

    @Override
    public LootTable.Builder createRedstoneOreDrops(Block block) {
        return delegate.createRedstoneOreDrops(block);
    }

    @Override
    public LootTable.Builder createBannerDrop(Block block) {
        return delegate.createBannerDrop(block);
    }

    @Override
    public LootTable.Builder createBeeNestDrop(Block block) {
        return delegate.createBeeNestDrop(block);
    }

    @Override
    public LootTable.Builder createBeeHiveDrop(Block block) {
        return delegate.createBeeHiveDrop(block);
    }

    @Override
    public LootTable.Builder createCaveVinesDrop(Block block) {
        return delegate.createCaveVinesDrop(block);
    }

    @Override
    public LootTable.Builder createOreDrop(Block block, Item item) {
        return delegate.createOreDrop(block, item);
    }

    @Override
    public LootTable.Builder createMushroomBlockDrop(Block block, ItemLike item) {
        return delegate.createMushroomBlockDrop(block, item);
    }

    @Override
    public LootTable.Builder createGrassDrops(Block block) {
        return delegate.createGrassDrops(block);
    }

    @Override
    public LootTable.Builder createStemDrops(Block block, Item item) {
        return delegate.createStemDrops(block, item);
    }

    @Override
    public LootTable.Builder createAttachedStemDrops(Block block, Item item) {
        return delegate.createAttachedStemDrops(block, item);
    }

    @Override
    public LootTable.Builder createShearsOnlyDrop(ItemLike item) {
        return delegate.createShearsOnlyDrop(item);
    }

    @Override
    public LootTable.Builder createShearsOrSilkTouchOnlyDrop(ItemLike item) {
        return delegate.createShearsOrSilkTouchOnlyDrop(item);
    }

    @Override
    public LootTable.Builder createMultifaceBlockDrops(Block block, LootItemCondition.Builder builder) {
        return delegate.createMultifaceBlockDrops(block, builder);
    }

    @Override
    public LootTable.Builder createMultifaceBlockDrops(Block block) {
        return delegate.createMultifaceBlockDrops(block);
    }

    @Override
    public LootTable.Builder createMossyCarpetBlockDrops(Block block) {
        return delegate.createMossyCarpetBlockDrops(block);
    }

    @Override
    public LootTable.Builder createLeavesDrops(Block leavesBlock, Block saplingBlock, float... chances) {
        return delegate.createLeavesDrops(leavesBlock, saplingBlock, chances);
    }

    @Override
    public LootTable.Builder createOakLeavesDrops(Block oakLeavesBlock, Block saplingBlock, float... chances) {
        return delegate.createOakLeavesDrops(oakLeavesBlock, saplingBlock, chances);
    }

    @Override
    public LootTable.Builder createMangroveLeavesDrops(Block block) {
        return delegate.createMangroveLeavesDrops(block);
    }

    @Override
    public LootTable.Builder createCropDrops(Block cropBlock, Item grownCropItem, Item seedsItem, LootItemCondition.Builder dropGrownCropCondition) {
        return delegate.createCropDrops(cropBlock, grownCropItem, seedsItem, dropGrownCropCondition);
    }

    @Override
    public LootTable.Builder createDoublePlantShearsDrop(Block sheared) {
        return delegate.createDoublePlantShearsDrop(sheared);
    }

    @Override
    public LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block sheared) {
        return delegate.createDoublePlantWithSeedDrops(block, sheared);
    }

    @Override
    public LootTable.Builder createCandleDrops(Block candleBlock) {
        return delegate.createCandleDrops(candleBlock);
    }

    @Override
    public LootTable.Builder createSegmentedBlockDrops(Block block) {
        return delegate.createSegmentedBlockDrops(block);
    }

    @Override
    public void addNetherVinesDropTable(Block vines, Block plant) {
        delegate.addNetherVinesDropTable(vines, plant);
    }

    @Override
    public LootTable.Builder createDoorTable(Block doorBlock) {
        return delegate.createDoorTable(doorBlock);
    }

    @Override
    public void dropPottedContents(Block flowerPot) {
        delegate.dropPottedContents(flowerPot);
    }

    @Override
    public void otherWhenSilkTouch(Block block, Block other) {
        delegate.otherWhenSilkTouch(block, other);
    }

    @Override
    public void dropOther(Block block, ItemLike item) {
        delegate.dropOther(block, item);
    }

    @Override
    public void dropWhenSilkTouch(Block block) {
        delegate.dropWhenSilkTouch(block);
    }

    @Override
    public void dropSelf(Block block) {
        delegate.dropSelf(block);
    }
    // endregion
}
