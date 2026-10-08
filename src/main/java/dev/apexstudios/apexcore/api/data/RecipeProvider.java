package dev.apexstudios.apexcore.api.data;

import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.triggers.BredAnimalsTrigger;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.EnterBlockTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
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

public abstract class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    protected RecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    public RecipeOutput output() {
        return output;
    }

    public static String getHasName(TagKey<Item> tag) {
        return "has_" + tag.location().getPath().replace("/", "_");
    }

    public static ResourceKey<Recipe<?>> recipeKey(Identifier recipeId) {
        return ResourceKey.create(Registries.RECIPE, recipeId);
    }

    public static ResourceKey<Recipe<?>> recipeKeyWithPrefix(ItemLike item, String prefix) {
        return recipeKey(getDefaultRecipeId(item).withPrefix(prefix));
    }

    public static Identifier getDefaultRecipeId(ItemLike item) {
        return item.asItem().builtInRegistryHolder().key().identifier();
    }

    // @formatter:off
    @Override public void generateForEnabledBlockFamilies(FeatureFlagSet flagSet) {super.generateForEnabledBlockFamilies(flagSet);}
    @Override public void oneToOneConversionRecipe(ItemLike product, ItemLike resource, @Nullable String group) {super.oneToOneConversionRecipe(product, resource, group);}
    @Override public void oneToOneConversionRecipe(ItemLike product, ItemLike resource, @Nullable String group, int productCount) {super.oneToOneConversionRecipe(product, resource, group, productCount);}
    @Override public void oreSmelting(List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group) {super.oreSmelting(smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group);}
    @Override public void oreBlasting(List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group) {super.oreBlasting(smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group);}
    @Override public <T extends AbstractCookingRecipe> void oreCooking(AbstractCookingRecipe.Factory<T> factory, List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group, String fromDesc) {super.oreCooking(factory, smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group, fromDesc);}
    @Override public void netheriteSmithing(Item base, RecipeCategory category, Item result) {super.netheriteSmithing(base, category, result);}
    @Override public void trimSmithing(Item trimTemplate, ResourceKey<TrimPattern> patternId, ResourceKey<Recipe<?>> id) {super.trimSmithing(trimTemplate, patternId, id);}
    @Override public void twoByTwoPacker(RecipeCategory category, ItemLike result, ItemLike ingredient) {super.twoByTwoPacker(category, result, ingredient);}
    @Override public void threeByThreePacker(RecipeCategory category, ItemLike result, ItemLike ingredient, String unlockedBy) {super.threeByThreePacker(category, result, ingredient, unlockedBy);}
    @Override public void threeByThreePacker(RecipeCategory category, ItemLike result, ItemLike ingredient) {super.threeByThreePacker(category, result, ingredient);}
    @Override public void planksFromLog(ItemLike result, TagKey<Item> logs, int count) {super.planksFromLog(result, logs, count);}
    @Override public void planksFromLogs(ItemLike result, TagKey<Item> logs, int count) {super.planksFromLogs(result, logs, count);}
    @Override public void woodFromLogs(ItemLike result, ItemLike log) {super.woodFromLogs(result, log);}
    @Override public void woodenBoat(ItemLike result, ItemLike planks) {super.woodenBoat(result, planks);}
    @Override public void chestBoat(ItemLike chestBoat, ItemLike boat) {super.chestBoat(chestBoat, boat);}
    @Override public RecipeBuilder buttonBuilder(ItemLike result, Ingredient base) {return super.buttonBuilder(result, base);}
    @Override public RecipeBuilder doorBuilder(ItemLike result, Ingredient base) {return super.doorBuilder(result, base);}
    @Override public RecipeBuilder fenceBuilder(ItemLike result, Ingredient base) {return super.fenceBuilder(result, base);}
    @Override public RecipeBuilder fenceGateBuilder(ItemLike result, Ingredient planks) {return super.fenceGateBuilder(result, planks);}
    @Override public void pressurePlate(ItemLike result, ItemLike base) {super.pressurePlate(result, base);}
    @Override public RecipeBuilder pressurePlateBuilder(RecipeCategory category, ItemLike result, Ingredient base) {return super.pressurePlateBuilder(category, result, base);}
    @Override public void slab(RecipeCategory category, ItemLike result, ItemLike base) {super.slab(category, result, base);}
    @Override public void shelf(ItemLike result, ItemLike strippedLogs) {super.shelf(result, strippedLogs);}
    @Override public RecipeBuilder slabBuilder(RecipeCategory category, ItemLike result, Ingredient base) {return super.slabBuilder(category, result, base);}
    @Override public RecipeBuilder stairBuilder(ItemLike result, Ingredient base) {return super.stairBuilder(result, base);}
    @Override public RecipeBuilder trapdoorBuilder(ItemLike result, Ingredient base) {return super.trapdoorBuilder(result, base);}
    @Override public RecipeBuilder signBuilder(ItemLike result, Ingredient planks) {return super.signBuilder(result, planks);}
    @Override public RecipeBuilder hangingSignBuilder(ItemLike result, Ingredient ingredient) {return super.hangingSignBuilder(result, ingredient);}
    @Override public void colorItemWithDye(List<Item> dyes, List<Item> items, String groupName, RecipeCategory category) {super.colorItemWithDye(dyes, items, groupName, category);}
    @Override public void colorWithDye(List<Item> dyes, List<Item> dyedItems, @Nullable Item uncoloredItem, String groupName, RecipeCategory category) {super.colorWithDye(dyes, dyedItems, uncoloredItem, groupName, category);}
    @Override public void carpet(ItemLike result, ItemLike sourceItem) {super.carpet(result, sourceItem);}
    @Override public void bedFromPlanksAndWool(ItemLike result, ItemLike wool) {super.bedFromPlanksAndWool(result, wool);}
    @Override public void banner(ItemLike result, ItemLike wool) {super.banner(result, wool);}
    @Override public void stainedGlassFromGlassAndDye(ItemLike result, ItemLike dye) {super.stainedGlassFromGlassAndDye(result, dye);}
    @Override public void dryGhast(ItemLike result) {super.dryGhast(result);}
    @Override public void harness(ItemLike result, ItemLike wool) {super.harness(result, wool);}
    @Override public void stainedGlassPaneFromStainedGlass(ItemLike result, ItemLike stainedGlass) {super.stainedGlassPaneFromStainedGlass(result, stainedGlass);}
    @Override public void stainedGlassPaneFromGlassPaneAndDye(ItemLike result, ItemLike dye) {super.stainedGlassPaneFromGlassPaneAndDye(result, dye);}
    @Override public void coloredTerracottaFromTerracottaAndDye(ItemLike result, ItemLike dye) {super.coloredTerracottaFromTerracottaAndDye(result, dye);}
    @Override public void concretePowder(ItemLike result, ItemLike dye) {super.concretePowder(result, dye);}
    @Override public void candle(ItemLike result, ItemLike dye) {super.candle(result, dye);}
    @Override public void wall(RecipeCategory category, ItemLike result, ItemLike base) {super.wall(category, result, base);}
    @Override public RecipeBuilder wallBuilder(RecipeCategory category, ItemLike result, Ingredient base) {return super.wallBuilder(category, result, base);}
    @Override public void polished(RecipeCategory category, ItemLike result, ItemLike base) {super.polished(category, result, base);}
    @Override public RecipeBuilder polishedBuilder(RecipeCategory category, ItemLike result, Ingredient base) {return super.polishedBuilder(category, result, base);}
    @Override public void cut(RecipeCategory category, ItemLike result, ItemLike base) {super.cut(category, result, base);}
    @Override public ShapedRecipeBuilder cutBuilder(RecipeCategory category, ItemLike result, Ingredient base) {return super.cutBuilder(category, result, base);}
    @Override public void chiseled(RecipeCategory category, ItemLike result, ItemLike base) {super.chiseled(category, result, base);}
    @Override public void mosaicBuilder(RecipeCategory category, ItemLike result, ItemLike base) {super.mosaicBuilder(category, result, base);}
    @Override public ShapedRecipeBuilder chiseledBuilder(RecipeCategory category, ItemLike result, Ingredient base) {return super.chiseledBuilder(category, result, base);}
    @Override public ShapedRecipeBuilder carpetBuilder(RecipeCategory category, ItemLike result, Ingredient base) {return super.carpetBuilder(category, result, base);}
    @Override public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base) {super.stonecutterResultFromBase(category, result, base);}
    @Override public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base, int count) {super.stonecutterResultFromBase(category, result, base, count);}
    @Override public void smeltingResultFromBase(ItemLike result, ItemLike base) {super.smeltingResultFromBase(result, base);}
    @Override public void nineBlockStorageRecipes(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm) {super.nineBlockStorageRecipes(unpackedFormCategory, unpackedForm, packedFormCategory, packedForm);}
    @Override public void nineBlockStorageRecipesWithCustomPacking(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm, String packingRecipeId, String packingRecipeGroup) {super.nineBlockStorageRecipesWithCustomPacking(unpackedFormCategory, unpackedForm, packedFormCategory, packedForm, packingRecipeId, packingRecipeGroup);}
    @Override public void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm, String unpackingRecipeId, String unpackingRecipeGroup) {super.nineBlockStorageRecipesRecipesWithCustomUnpacking(unpackedFormCategory, unpackedForm, packedFormCategory, packedForm, unpackingRecipeId, unpackingRecipeGroup);}
    @Override public void nineBlockStorageRecipes(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm, String packingRecipeId, @Nullable String packingRecipeGroup, String unpackingRecipeId, @Nullable String unpackingRecipeGroup) {super.nineBlockStorageRecipes(unpackedFormCategory, unpackedForm, packedFormCategory, packedForm, packingRecipeId, packingRecipeGroup, unpackingRecipeId, unpackingRecipeGroup);}
    @Override public void copySmithingTemplate(ItemLike smithingTemplate, ItemLike baseMaterial) {super.copySmithingTemplate(smithingTemplate, baseMaterial);}
    @Override public void copySmithingTemplate(ItemLike smithingTemplate, Ingredient baseMaterials) {super.copySmithingTemplate(smithingTemplate, baseMaterials);}
    @Override public <T extends AbstractCookingRecipe> void cookRecipes(String source, AbstractCookingRecipe.Factory<T> factory, int cookingTime) {super.cookRecipes(source, factory, cookingTime);}
    @Override public <T extends AbstractCookingRecipe> void simpleCookingRecipe(String source, AbstractCookingRecipe.Factory<T> factory, int cookingTime, ItemLike base, ItemLike result, float experience) {super.simpleCookingRecipe(source, factory, cookingTime, base, result, experience);}
    @Override public void waxRecipes(FeatureFlagSet flagSet) {super.waxRecipes(flagSet);}
    @Override public void grate(Block grateBlock, Block material) {super.grate(grateBlock, material);}
    @Override public void copperBulb(Block copperBulb, Block copperMaterial) {super.copperBulb(copperBulb, copperMaterial);}
    @Override public void waxedChiseled(Block result, Block material) {super.waxedChiseled(result, material);}
    @Override public void suspiciousStew(Item item, SuspiciousEffectHolder effectHolder) {super.suspiciousStew(item, effectHolder);}
    @Override public void dyedItem(Item target, String group) {super.dyedItem(target, group);}
    @Override public void dyedShulkerBoxRecipe(Item dye, Item dyedResult) {super.dyedShulkerBoxRecipe(dye, dyedResult);}
    @Override public void dyedBundleRecipe(Item dye, Item dyedResult) {super.dyedBundleRecipe(dye, dyedResult);}
    @Override public void cushionRecipe(Item woolSlab, Item result) {super.cushionRecipe(woolSlab, result);}
    @Override public void generateRecipes(BlockFamily family, FeatureFlagSet flagSet) {super.generateRecipes(family, flagSet);}
    @Override public Block getBaseBlockForCrafting(BlockFamily family, BlockFamily.Variant variant) {return super.getBaseBlockForCrafting(family, variant);}
    @Override public Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimal() {return super.bredAnimal();}
    @Override public Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item) {return super.has(count, item);}
    @Override public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {return super.has(item);}
    @Override public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {return super.has(tag);}
    @Override public Ingredient tag(TagKey<Item> id) {return super.tag(id);}
    @Override public ShapedRecipeBuilder shaped(RecipeCategory category, ItemStackTemplate stack) {return super.shaped(category, stack);}
    @Override public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item) {return super.shaped(category, item);}
    @Override public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item, int count) {return super.shaped(category, item, count);}
    @Override public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStackTemplate result) {return super.shapeless(category, result);}
    @Override public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item) {return super.shapeless(category, item);}
    @Override public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item, int count) {return super.shapeless(category, item, count);}
    public static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) {return net.minecraft.data.recipes.RecipeProvider.insideOf(block);}
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... predicates) {return net.minecraft.data.recipes.RecipeProvider.inventoryTrigger(predicates);}
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) {return net.minecraft.data.recipes.RecipeProvider.inventoryTrigger(predicates);}
    public static String getHasName(ItemLike baseBlock) {return net.minecraft.data.recipes.RecipeProvider.getHasName(baseBlock);}
    public static String getItemName(ItemLike itemLike) {return net.minecraft.data.recipes.RecipeProvider.getHasName(itemLike);}
    public static String getSimpleRecipeName(ItemLike itemLike) {return net.minecraft.data.recipes.RecipeProvider.getSimpleRecipeName(itemLike);}
    public static String getConversionRecipeName(ItemLike product, ItemLike material) {return net.minecraft.data.recipes.RecipeProvider.getConversionRecipeName(product, material);}
    public static String getSmeltingRecipeName(ItemLike product) {return net.minecraft.data.recipes.RecipeProvider.getSmeltingRecipeName(product);}
    public static String getBlastingRecipeName(ItemLike product) {return net.minecraft.data.recipes.RecipeProvider.getBlastingRecipeName(product);}
    // @formatter:on
}
