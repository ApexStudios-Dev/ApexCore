package dev.apexstudios.apexcore.lib.data.provider.loot;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public interface BlockLootSubProvider extends LootTableSubProvider {
    float[] NORMAL_LEAVES_SAPLING_CHANCES = net.minecraft.data.loot.BlockLootSubProvider.NORMAL_LEAVES_SAPLING_CHANCES;
    float[] NORMAL_LEAVES_STICK_CHANCES = net.minecraft.data.loot.BlockLootSubProvider.NORMAL_LEAVES_STICK_CHANCES;
    float[] JUNGLE_LEAVES_SAPLING_CHANGES = VanillaBlockLoot.JUNGLE_LEAVES_SAPLING_CHANGES;
    Set<Item> VANILLA_EXPLOSION_RESISTANT = VanillaBlockLoot.EXPLOSION_RESISTANT;

    default void accept(Holder<Block> block, Supplier<LootTable.Builder> lootTable) {
        accept(block.value(), lootTable);
    }

    default void accept(Holder<Block> block, LootTable.Builder lootTable) {
        accept(block, () -> lootTable);
    }

    default void accept(Block block, Supplier<LootTable.Builder> lootTable) {
        accept(block.getLootTable().orElseThrow(() -> new IllegalStateException("Block " + block.builtInRegistryHolder().key().location() + " does not have loot table")), lootTable);
    }

    default void accept(Block block, LootTable.Builder lootTable) {
        accept(block, () -> lootTable);
    }

    // must be called prior to any 'accept' calls
    void explosionResistant(ItemLike item);

    boolean isExplosionResistant(ItemLike item);

    default LootItemCondition.Builder hasSilkTouch(HolderLookup.Provider registries) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return MatchTool.toolMatches(ItemPredicate.Builder.item().withSubPredicate(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(enchantments.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1))))));
    }

    default LootItemCondition.Builder doesNotHaveSilkTouch(HolderLookup.Provider registries) {
        return hasSilkTouch(registries).invert();
    }

    default LootItemCondition.Builder hasShears(HolderLookup.Provider registries) {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), Items.SHEARS));
    }

    default LootItemCondition.Builder hasShearsOrSilkTouch(HolderLookup.Provider registries) {
        return hasShears(registries).or(hasSilkTouch(registries));
    }

    default LootItemCondition.Builder doesNotHaveShearsOrSilkTouch(HolderLookup.Provider registries) {
        return hasShearsOrSilkTouch(registries).invert();
    }

    default <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike item, FunctionUserBuilder<T> functionBuilder) {
        return !isExplosionResistant(item.asItem()) ? functionBuilder.apply(ApplyExplosionDecay.explosionDecay()) : functionBuilder.unwrap();
    }

    default <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike item, ConditionUserBuilder<T> conditionBuilder) {
        return !isExplosionResistant(item.asItem()) ? conditionBuilder.when(ExplosionCondition.survivesExplosion()) : conditionBuilder.unwrap();
    }

    default LootTable.Builder createSingleItemTable(ItemLike item) {
        return LootTable.lootTable().withPool(applyExplosionCondition(item, LootPool.lootPool().setRolls(ConstantValue.exactly(1F)).add(LootItem.lootTableItem(item))));
    }

    default LootTable.Builder createSilkTouchDispatchTable(HolderLookup.Provider registries, Block block, LootPoolEntryContainer.Builder<?> builder) {
        return createSelfDropDispatchTable(block, hasSilkTouch(registries), builder);
    }

    default LootTable.Builder createShearsDispatchTable(HolderLookup.Provider registries, Block block, LootPoolEntryContainer.Builder<?> builder) {
        return createSelfDropDispatchTable(block, hasShears(registries), builder);
    }

    default LootTable.Builder createSilkTouchOrShearsDispatchTable(HolderLookup.Provider registries, Block block, LootPoolEntryContainer.Builder<?> builder) {
        return createSelfDropDispatchTable(block, hasShearsOrSilkTouch(registries), builder);
    }

    default LootTable.Builder createSingleItemTableWithSilkTouch(HolderLookup.Provider registries, Block block, ItemLike item) {
        return createSilkTouchDispatchTable(registries, block, applyExplosionCondition(block, LootItem.lootTableItem(item)));
    }

    default LootTable.Builder createSingleItemTable(ItemLike item, NumberProvider count) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(applyExplosionDecay(item, LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(count)))));
    }

    default LootTable.Builder createSingleItemTableWithSilkTouch(HolderLookup.Provider registries, Block block, ItemLike item, NumberProvider count) {
        return createSilkTouchDispatchTable(registries, block, applyExplosionDecay(block, LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(count))));
    }

    default LootTable.Builder createSilkTouchOnlyTable(HolderLookup.Provider registries, ItemLike item) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(hasSilkTouch(registries)).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(item)));
    }

    default LootTable.Builder createPotFlowerItemTable(ItemLike item) {
        return LootTable.lootTable().withPool(applyExplosionCondition(Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Blocks.FLOWER_POT)))).withPool(applyExplosionCondition(item, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(item))));
    }

    default LootTable.Builder createSlabItemTable(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(applyExplosionDecay(block, LootItem.lootTableItem(block).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)))))));
    }

    default <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block block, Property<T> property, T value) {
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))))));
    }

    default LootTable.Builder createNameableBlockEntityTable(Block block) {
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME)))));
    }

    default LootTable.Builder createShulkerBoxDrop(Block block) {
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME).include(DataComponents.CONTAINER).include(DataComponents.LOCK).include(DataComponents.CONTAINER_LOOT)))));
    }

    default LootTable.Builder createCopperOreDrops(HolderLookup.Provider registries, Block block) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchDispatchTable(registries, block, applyExplosionDecay(block, LootItem.lootTableItem(Items.RAW_COPPER).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))).apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
    }

    default LootTable.Builder createLapisOreDrops(HolderLookup.Provider registries, Block block) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchDispatchTable(registries, block, applyExplosionDecay(block, LootItem.lootTableItem(Items.LAPIS_LAZULI).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F))).apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
    }

    default LootTable.Builder createRedstoneOreDrops(HolderLookup.Provider registries, Block block) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchDispatchTable(registries, block, applyExplosionDecay(block, LootItem.lootTableItem(Items.REDSTONE).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 5.0F))).apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
    }

    /*default LootTable.Builder createBannerDrop(Block block) {
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME).include(DataComponents.ITEM_NAME).include(DataComponents.HIDE_ADDITIONAL_TOOLTIP).include(DataComponents.BANNER_PATTERNS).include(DataComponents.RARITY)))));
    }*/

    default LootTable.Builder createBeeNestDrop(HolderLookup.Provider registries, Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(hasSilkTouch(registries)).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(DataComponents.BEES)).apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL))));
    }

    default LootTable.Builder createBeeHiveDrop(HolderLookup.Provider registries, Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).when(hasSilkTouch(registries)).apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(DataComponents.BEES)).apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL)).otherwise(LootItem.lootTableItem(block))));
    }

    default LootTable.Builder createCaveVinesDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.GLOW_BERRIES)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CaveVines.BERRIES, true))));
    }

    default LootTable.Builder createOreDrop(HolderLookup.Provider registries, Block block, Item item) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchDispatchTable(registries, block, applyExplosionDecay(block, LootItem.lootTableItem(item).apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
    }

    default LootTable.Builder createMushroomBlockDrop(HolderLookup.Provider registries, Block block, ItemLike item) {
        return createSilkTouchDispatchTable(registries, block, applyExplosionDecay(block, LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(-6.0F, 2.0F))).apply(LimitCount.limitCount(IntRange.lowerBound(0)))));
    }

    default LootTable.Builder createGrassDrops(HolderLookup.Provider registries, Block block) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createShearsDispatchTable(registries, block, applyExplosionDecay(block, LootItem.lootTableItem(Items.WHEAT_SEEDS).when(LootItemRandomChanceCondition.randomChance(0.125F)).apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE), 2))));
    }

    default LootTable.Builder createStemDrops(Block block, Item item) {
        return LootTable.lootTable().withPool(applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(item).apply(StemBlock.AGE.getPossibleValues(), p_249795_ -> SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, (float)(p_249795_ + 1) / 15.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, p_249795_)))))));
    }

    default LootTable.Builder createAttachedStemDrops(Block block, Item item) {
        return LootTable.lootTable().withPool(applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336F))))));
    }

    default LootTable.Builder createShearsOnlyDrop(HolderLookup.Provider registries, ItemLike item) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(hasShears(registries)).add(LootItem.lootTableItem(item)));
    }

    default LootTable.Builder createShearsOrSilkTouchOnlyDrop(HolderLookup.Provider registries, ItemLike item) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(hasShearsOrSilkTouch(registries)).add(LootItem.lootTableItem(item)));
    }

    default LootTable.Builder createMultifaceBlockDrops(Block block, LootItemCondition.Builder builder) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add(applyExplosionDecay(block, LootItem.lootTableItem(block).when(builder).apply(Direction.values(), p_251536_ -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0F), true).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MultifaceBlock.getFaceProperty(p_251536_), true)))).apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0F), true)))));
    }

    default LootTable.Builder createMultifaceBlockDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add(applyExplosionDecay(block, LootItem.lootTableItem(block).apply(Direction.values(), p_382562_ -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0F), true).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MultifaceBlock.getFaceProperty(p_382562_), true)))).apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0F), true)))));
    }

    default LootTable.Builder createMossyCarpetBlockDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add(applyExplosionDecay(block, LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MossyCarpetBlock.BASE, true))))));
    }

    default LootTable.Builder createLeavesDrops(HolderLookup.Provider registries, Block leavesBlock, Block saplingBlock, float... chances) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchOrShearsDispatchTable(registries, leavesBlock, applyExplosionCondition(leavesBlock, LootItem.lootTableItem(saplingBlock)).when(BonusLevelTableCondition.bonusLevelFlatChance(enchantments.getOrThrow(Enchantments.FORTUNE), chances))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(doesNotHaveShearsOrSilkTouch(registries)).add(applyExplosionDecay(leavesBlock, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))).when(BonusLevelTableCondition.bonusLevelFlatChance(enchantments.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_STICK_CHANCES))));
    }

    default LootTable.Builder createOakLeavesDrops(HolderLookup.Provider registries, Block oakLeavesBlock, Block saplingBlock, float... chances) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createLeavesDrops(registries, oakLeavesBlock, saplingBlock, chances).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F)).when(doesNotHaveShearsOrSilkTouch(registries)).add(applyExplosionCondition(oakLeavesBlock, LootItem.lootTableItem(Items.APPLE)).when(BonusLevelTableCondition.bonusLevelFlatChance(enchantments.getOrThrow(Enchantments.FORTUNE), .005F, .0055555557F, .00625F, .008333334F, .025F))));
    }

    default LootTable.Builder createMangroveLeavesDrops(HolderLookup.Provider registries, Block block) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchOrShearsDispatchTable(registries, block, applyExplosionDecay(Blocks.MANGROVE_LEAVES, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1F, 2F)))).when(BonusLevelTableCondition.bonusLevelFlatChance(enchantments.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_STICK_CHANCES)));
    }

    default LootTable.Builder createCropDrops(HolderLookup.Provider registries, Block cropBlock, Item grownCropItem, Item seedsItem, LootItemCondition.Builder dropGrownCropCondition) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return applyExplosionDecay(cropBlock, LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(grownCropItem).when(dropGrownCropCondition).otherwise(LootItem.lootTableItem(seedsItem)))).withPool(LootPool.lootPool().when(dropGrownCropCondition).add(LootItem.lootTableItem(seedsItem).apply(ApplyBonusCount.addBonusBinomialDistributionCount(enchantments.getOrThrow(Enchantments.FORTUNE), .5714286F, 3)))));
    }

    default LootTable.Builder createDoublePlantShearsDrop(HolderLookup.Provider registries, Block sheared) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(hasShears(registries)).add(LootItem.lootTableItem(sheared).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2F)))));
    }

    default LootTable.Builder createDoublePlantWithSeedDrops(HolderLookup.Provider registries, Block block, Block sheared) {
        var blocks = registries.lookupOrThrow(Registries.BLOCK);
        var lootTable = LootItem.lootTableItem(sheared).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2F))).when(hasShears(registries)).otherwise(applyExplosionCondition(block, LootItem.lootTableItem(Items.WHEAT_SEEDS)).when(LootItemRandomChanceCondition.randomChance(.125F)));
        return LootTable.lootTable().withPool(LootPool.lootPool().add(lootTable).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))), new BlockPos(0, 1, 0)))).withPool(LootPool.lootPool().add(lootTable).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))), new BlockPos(0, -1, 0))));
    }

    default LootTable.Builder createCandleDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(applyExplosionDecay(block, LootItem.lootTableItem(block).apply(List.of(2, 3, 4), amount -> SetItemCountFunction.setCount(ConstantValue.exactly(amount)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, amount)))))));
    }

    /*default LootTable.Builder createPetalsDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F)).add(applyExplosionDecay(block, LootItem.lootTableItem(block).apply(IntStream.rangeClosed(1, 4).boxed().toList(), amount -> SetItemCountFunction.setCount(ConstantValue.exactly(amount)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PinkPetalsBlock.AMOUNT, amount)))))));
    }*/

    default void addNetherVinesDropTable(HolderLookup.Provider registries, Block vines, Block plant) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        var lootTable = createSilkTouchOrShearsDispatchTable(registries, vines, LootItem.lootTableItem(vines).when(BonusLevelTableCondition.bonusLevelFlatChance(enchantments.getOrThrow(Enchantments.FORTUNE), .33F, .55F, .77F, 1F)));
        accept(vines, lootTable);
        accept(plant, lootTable);
    }

    default LootTable.Builder createDoorTable(Block doorBlock) {
        return createSinglePropConditionTable(doorBlock, DoorBlock.HALF, DoubleBlockHalf.LOWER);
    }

    default void dropPottedContents(FlowerPotBlock block) {
        accept(block, createPotFlowerItemTable(block.getPotted()));
    }

    default void otherWhenSilkTouch(HolderLookup.Provider registries, Block block, Block other) {
        accept(block, createSilkTouchOnlyTable(registries, other));
    }

    default void dropOther(Block block, ItemLike item) {
        accept(block, createSingleItemTable(item));
    }

    default void dropWhenSilkTouch(HolderLookup.Provider registries, Block block) {
        otherWhenSilkTouch(registries, block, block);
    }

    default void dropSelf(Block block) {
        dropOther(block, block);
    }

    static LootTable.Builder createSelfDropDispatchTable(Block block, LootItemCondition.Builder conditionBuilder, LootPoolEntryContainer.Builder<?> alternativeBuilder) {
        return net.minecraft.data.loot.BlockLootSubProvider.createSelfDropDispatchTable(block, conditionBuilder, alternativeBuilder);
    }

    static LootTable.Builder createCandleCakeDrops(Block block) {
        return net.minecraft.data.loot.BlockLootSubProvider.createCandleCakeDrops(block);
    }

    static LootTable.Builder noDrop() {
        return net.minecraft.data.loot.BlockLootSubProvider.noDrop();
    }
}
