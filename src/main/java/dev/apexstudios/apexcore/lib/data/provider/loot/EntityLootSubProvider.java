package dev.apexstudios.apexcore.lib.data.provider.loot;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public interface EntityLootSubProvider extends LootTableSubProvider {
    default void accept(Holder<EntityType<?>> entityType, Supplier<LootTable.Builder> lootTable) {
        accept(entityType.value(), lootTable);
    }

    default void accept(Holder<EntityType<?>> entityType, LootTable.Builder lootTable) {
        accept(entityType, () -> lootTable);
    }

    default void accept(EntityType<?> entityType, Supplier<LootTable.Builder> lootTable) {
        accept(entityType.getDefaultLootTable().orElseThrow(() -> new IllegalStateException("Entity " + entityType + " has no loot table")), lootTable);
    }

    default void accept(EntityType<?> entityType, LootTable.Builder lootTable) {
        accept(entityType, () -> lootTable);
    }

    default AnyOfCondition.Builder shouldSmeltLoot(HolderLookup.Provider registries) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return AnyOfCondition.anyOf(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true))), LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(ItemPredicate.Builder.item().withSubPredicate(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(enchantments.getOrThrow(EnchantmentTags.SMELTS_LOOT), MinMaxBounds.Ints.ANY))))))));
    }

    default LootItemCondition.Builder killedByFrog(HolderLookup.Provider registries) {
        var entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);
        return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of(entityTypes, EntityType.FROG)));
    }

    /*default LootItemCondition.Builder killedByFrogVariant(HolderLookup.Provider registries, ResourceKey<FrogVariant> frogVariant) {
        var entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);
        var frogVariants = registries.lookupOrThrow(Registries.FROG_VARIANT);
        return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of(entityTypes, EntityType.FROG).subPredicate(EntitySubPredicates.frogVariant(frogVariants.getOrThrow(frogVariant)))));
    }*/

    default LootTable.Builder elderGuardianLootTable(HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.PRISMARINE_SHARD).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))).apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(0.0F, 1.0F))))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.COD).setWeight(3).apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(0.0F, 1.0F))).apply(SmeltItemFunction.smelted().when(shouldSmeltLoot(registries)))).add(LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(2).apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(0.0F, 1.0F)))).add(EmptyLootItem.emptyItem())).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Blocks.WET_SPONGE)).when(LootItemKilledByPlayerCondition.killedByPlayer())).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(NestedLootTable.lootTableReference(BuiltInLootTables.FISHING_FISH).apply(SmeltItemFunction.smelted().when(shouldSmeltLoot(registries)))).when(LootItemKilledByPlayerCondition.killedByPlayer()).when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, 0.025F, 0.01F))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(4)).add(LootItem.lootTableItem(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE).setWeight(1)));
    }

    static LootPool.Builder createSheepDispatchPool(Map<DyeColor, ResourceKey<LootTable>> lootTables) {
        return net.minecraft.data.loot.EntityLootSubProvider.createSheepDispatchPool(lootTables);
    }
}
