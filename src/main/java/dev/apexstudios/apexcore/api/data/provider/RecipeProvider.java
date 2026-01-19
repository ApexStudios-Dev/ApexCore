package dev.apexstudios.apexcore.api.data.provider;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.common.data.provider.RecipeProviderImpl;
import java.util.List;
import java.util.Objects;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.jspecify.annotations.Nullable;

public interface RecipeProvider {
    ProviderType<RecipeProvider> PROVIDER_TYPE = RecipeProviderImpl.PROVIDER_TYPE;

    RecipeOutput output();

    HolderGetter<Item> items();

    void oneToOneConversionRecipe(ItemLike result, ItemLike ingredient, @Nullable String group);

    void oneToOneConversionRecipe(ItemLike result, ItemLike ingredient, @Nullable String group, int resultCount);

    void oreSmelting(List<ItemLike> ingredients, RecipeCategory category, CookingBookCategory cookingBookCategory, ItemLike result, float experience, int cookingTime, String group);

    void oreBlasting(List<ItemLike> ingredients, RecipeCategory category, CookingBookCategory cookingBookCategory, ItemLike result, float experience, int cookingTime, String group);

    <T extends AbstractCookingRecipe> void oreCooking(AbstractCookingRecipe.Factory<T> recipeFactory, List<ItemLike> ingredients, RecipeCategory category, CookingBookCategory cookingBookCategory, ItemLike result, float experience, int cookingTime, String group, String suffix);

    void netheriteSmithing(Item ingredientItem, RecipeCategory category, Item resultItem);

    void trimSmithing(Item template, ResourceKey<TrimPattern> pattern, ResourceKey<Recipe<?>> recipe);

    void twoByTwoPacker(RecipeCategory category, ItemLike packed, ItemLike unpacked);

    void threeByThreePacker(RecipeCategory category, ItemLike packed, ItemLike unpacked, String criterionName);

    void threeByThreePacker(RecipeCategory category, ItemLike packed, ItemLike unpacked);

    void planksFromLog(ItemLike planks, TagKey<Item> logs, int resultCount);

    void planksFromLogs(ItemLike planks, TagKey<Item> logs, int result);

    void woodFromLogs(ItemLike wood, ItemLike log);

    void woodenBoat(ItemLike boat, ItemLike material);

    void chestBoat(ItemLike boat, ItemLike material);

    RecipeBuilder buttonBuilder(ItemLike button, Ingredient material);

    RecipeBuilder doorBuilder(ItemLike door, Ingredient material);

    RecipeBuilder fenceBuilder(ItemLike fence, Ingredient material);

    RecipeBuilder fenceGateBuilder(ItemLike fenceGate, Ingredient material);

    void pressurePlate(ItemLike pressurePlate, ItemLike material);

    RecipeBuilder pressurePlateBuilder(RecipeCategory category, ItemLike pressurePlate, Ingredient material);

    void slab(RecipeCategory category, ItemLike slab, ItemLike material);

    RecipeBuilder slabBuilder(RecipeCategory category, ItemLike slab, Ingredient material);

    RecipeBuilder stairBuilder(ItemLike stairs, Ingredient material);

    RecipeBuilder trapdoorBuilder(ItemLike trapdoor, Ingredient material);

    RecipeBuilder signBuilder(ItemLike sign, Ingredient material);

    void hangingSign(ItemLike sign, ItemLike material);

    void colorItemWithDye(List<Item> p_289675_, List<Item> p_289672_, String p_289641_, RecipeCategory p_423651_);

    void colorWithDye(List<Item> dyes, List<Item> dyeableItems, @Nullable Item dye, String group, RecipeCategory category);

    void carpet(ItemLike carpet, ItemLike material);

    void bedFromPlanksAndWool(ItemLike bed, ItemLike wool);

    void banner(ItemLike banner, ItemLike material);

    void stainedGlassFromGlassAndDye(ItemLike stainedGlass, ItemLike dye);

    void dryGhast(ItemLike p_416739_);

    void harness(ItemLike p_416620_, ItemLike p_416110_);

    void stainedGlassPaneFromStainedGlass(ItemLike stainedGlassPane, ItemLike stainedGlass);

    void stainedGlassPaneFromGlassPaneAndDye(ItemLike stainedGlassPane, ItemLike dye);

    void coloredTerracottaFromTerracottaAndDye(ItemLike terracotta, ItemLike dye);

    void concretePowder(ItemLike concretePowder, ItemLike dye);

    void candle(ItemLike candle, ItemLike dye);

    void wall(RecipeCategory category, ItemLike wall, ItemLike material);

    RecipeBuilder wallBuilder(RecipeCategory category, ItemLike wall, Ingredient material);

    void polished(RecipeCategory category, ItemLike result, ItemLike material);

    RecipeBuilder polishedBuilder(RecipeCategory category, ItemLike result, Ingredient material);

    void cut(RecipeCategory category, ItemLike cutResult, ItemLike material);

    ShapedRecipeBuilder cutBuilder(RecipeCategory category, ItemLike cutResult, Ingredient material);

    void chiseled(RecipeCategory category, ItemLike chiseledResult, ItemLike material);

    void mosaicBuilder(RecipeCategory category, ItemLike result, ItemLike material);

    ShapedRecipeBuilder chiseledBuilder(RecipeCategory category, ItemLike chiseledResult, Ingredient material);

    void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike material);

    void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike material, int resultCount);

    void smeltingResultFromBase(ItemLike result, ItemLike ingredient);

    void nineBlockStorageRecipes(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed);

    void nineBlockStorageRecipesWithCustomPacking(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String packedName, String packedGroup);

    void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String unpackedName, String unpackedGroup);

    void nineBlockStorageRecipes(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String packedName, @Nullable String packedGroup, String unpackedName, @Nullable String unpackedGroup);

    void copySmithingTemplate(ItemLike template, ItemLike baseItem);

    void copySmithingTemplate(ItemLike template, Ingredient baseItem);

    <T extends AbstractCookingRecipe> void cookRecipes(String cookingMethod, AbstractCookingRecipe.Factory<T> recipeFactory, int cookingTime);

    <T extends AbstractCookingRecipe> void simpleCookingRecipe(String cookingMethod, AbstractCookingRecipe.Factory<T> recipeFactory, int cookingTime, ItemLike material, ItemLike result, float experience);

    void grate(Block grateBlock, Block material);

    void copperBulb(Block bulbBlock, Block material);

    void suspiciousStew(Item flowerItem, SuspiciousEffectHolder effect);

    void generateRecipes(BlockFamily blockFamily, FeatureFlagSet requiredFeatures);

    Block getBaseBlock(BlockFamily family, BlockFamily.Variant variant);

    Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item);

    Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike);

    Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag);

    Ingredient tag(TagKey<Item> tag);

    ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result);

    ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count);

    ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStackTemplate result);

    ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result);

    ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count);

    default void variant(BlockFamily family, BlockFamily.Variant variant) {
        var block = Objects.requireNonNull(family.get(variant));
        var provider = variantProvider(variant);
        var baseBlock = getBaseBlock(family, variant);

        if(provider != null) {
            var builder = provider.create(this, block, baseBlock);

            family.getRecipeGroupPrefix()
                    .ifPresent(group -> builder.group(group + (variant == BlockFamily.Variant.CUT ? "" : '_' + variant.getRecipeGroup())));

            builder.unlockedBy(family.getRecipeUnlockedBy().orElseGet(() -> getHasName(baseBlock)), has(baseBlock))
                    .save(output());
        }

        if(variant == BlockFamily.Variant.CRACKED)
            smeltingResultFromBase(block, baseBlock);
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

    static String getSimpleRecipeName(ItemLike item) {
        return net.minecraft.data.recipes.RecipeProvider.getSimpleRecipeName(item);
    }

    static String getConversionRecipeName(ItemLike result, ItemLike ingredient) {
        return net.minecraft.data.recipes.RecipeProvider.getConversionRecipeName(result, ingredient);
    }

    static String getSmeltingRecipeName(ItemLike item) {
        return net.minecraft.data.recipes.RecipeProvider.getSmeltingRecipeName(item);
    }

    static String getBlastingRecipeName(ItemLike item) {
        return net.minecraft.data.recipes.RecipeProvider.getBlastingRecipeName(item);
    }

    static ResourceKey<Recipe<?>> recipeKey(Identifier recipeId) {
        return ResourceKey.create(Registries.RECIPE, recipeId);
    }

    static ResourceKey<Recipe<?>> recipeKeyWithPrefix(ItemLike item, String prefix) {
        return recipeKey(getDefaultRecipeId(item).withPrefix(prefix));
    }

    static ResourceKey<Recipe<?>> recipeKeyWithSuffix(ItemLike item, String suffix) {
        return recipeKey(getDefaultRecipeId(item).withSuffix(suffix));
    }

    static Identifier getDefaultRecipeId(ItemLike item) {
        return item.asItem().builtInRegistryHolder().unwrapKey().orElseThrow().identifier();
    }

    @Nullable
    static FamilyRecipeProvider variantProvider(BlockFamily.Variant variant) {
        var vanilla = net.minecraft.data.recipes.RecipeProvider.SHAPE_BUILDERS.get(variant);
        return vanilla == null ? null : (provider, ingredient, result) -> vanilla.create(((RecipeProviderImpl) provider).delegate, ingredient, result);
    }

    @FunctionalInterface
    interface FamilyRecipeProvider {
        RecipeBuilder create(RecipeProvider provider, ItemLike ingredient, ItemLike result);
    }
}
