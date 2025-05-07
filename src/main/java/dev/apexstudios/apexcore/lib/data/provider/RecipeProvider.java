package dev.apexstudios.apexcore.lib.data.provider;

import dev.apexstudios.apexcore.core.data.provider.RecipeProviderImpl;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import java.util.Map;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public interface RecipeProvider {
    ProviderType<RecipeProvider> PROVIDER_TYPE = RecipeProviderImpl.PROVIDER_TYPE;
    Map<BlockFamily.Variant, net.minecraft.data.recipes.RecipeProvider.FamilyRecipeProvider> SHAPE_BUILDERS = net.minecraft.data.recipes.RecipeProvider.SHAPE_BUILDERS;

    RecipeOutput output();

    HolderGetter<Item> items();

    default Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(items(), item).withCount(count));
    }

    default Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(items(), itemLike));
    }

    default Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(items(), tag));
    }

    default Ingredient tag(TagKey<Item> tag) {
        return Ingredient.of(items().getOrThrow(tag));
    }

    default ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return ShapedRecipeBuilder.shaped(items(), category, result);
    }

    default ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return ShapedRecipeBuilder.shaped(items(), category, result, count);
    }

    default ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStack result) {
        return ShapelessRecipeBuilder.shapeless(items(), category, result);
    }

    default ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return ShapelessRecipeBuilder.shapeless(items(), category, result);
    }

    default ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return ShapelessRecipeBuilder.shapeless(items(), category, result, count);
    }

    static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) {
        return net.minecraft.data.recipes.RecipeProvider.insideOf(block);
    }

    static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... items) {
        return net.minecraft.data.recipes.RecipeProvider.inventoryTrigger(items);
    }

    static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) {
        return net.minecraft.data.recipes.RecipeProvider.inventoryTrigger(predicates);
    }

    static String getHasName(ItemLike itemLike) {
        return net.minecraft.data.recipes.RecipeProvider.getHasName(itemLike);
    }

    static String getHasName(TagKey<Item> tag) {
        return "has_" + tag.location().getPath().replace("/", "_");
    }

    static String getItemName(ItemLike itemLike) {
        return net.minecraft.data.recipes.RecipeProvider.getItemName(itemLike);
    }

    static String getSimpleRecipeName(ItemLike itemLike) {
        return net.minecraft.data.recipes.RecipeProvider.getSimpleRecipeName(itemLike);
    }

    static ResourceKey<Recipe<?>> recipeKey(ResourceLocation recipeId) {
        return ResourceKey.create(Registries.RECIPE, recipeId);
    }

    static ResourceKey<Recipe<?>> recipeKeyWithPrefix(ItemLike item, String prefix) {
        return recipeKey(RecipeBuilder.getDefaultRecipeId(item).withPrefix(prefix));
    }

    static ResourceKey<Recipe<?>> recipeKeyWithSuffix(ItemLike item, String suffix) {
        return recipeKey(RecipeBuilder.getDefaultRecipeId(item).withSuffix(suffix));
    }
}
