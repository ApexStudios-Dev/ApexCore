package dev.apexstudios.apexcore.lib.data.provider.loot;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public interface BlockLootSubProvider extends LootTableSubProvider {
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
        return MatchTool.toolMatches(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(enchantments.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1))))).build()));
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

    default LootTable.Builder createSlabItemTable(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(applyExplosionDecay(block, LootItem.lootTableItem(block).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)))))));
    }

    default <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block block, Property<T> property, T value) {
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))))));
    }

    default LootTable.Builder createNameableBlockEntityTable(Block block) {
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME)))));
    }

    default LootTable.Builder createDoorTable(Block doorBlock) {
        return createSinglePropConditionTable(doorBlock, DoorBlock.HALF, DoubleBlockHalf.LOWER);
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

    static LootTable.Builder noDrop() {
        return net.minecraft.data.loot.BlockLootSubProvider.noDrop();
    }
}
