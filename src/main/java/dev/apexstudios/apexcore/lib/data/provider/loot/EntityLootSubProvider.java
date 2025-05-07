package dev.apexstudios.apexcore.lib.data.provider.loot;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.advancements.critereon.DataComponentMatchers;
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
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

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
        return AnyOfCondition.anyOf(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true))), LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(enchantments.getOrThrow(EnchantmentTags.SMELTS_LOOT), MinMaxBounds.Ints.ANY)))).build())))));
    }
}
