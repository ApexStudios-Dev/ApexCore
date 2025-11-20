package dev.apexstudios.apexcore.core.data.provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import dev.apexstudios.apexcore.lib.data.provider.RecipeProvider;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderOutputContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.jspecify.annotations.Nullable;

public final class RecipeProviderImpl implements BaseProvider, RecipeProvider {
    public static final ProviderType<RecipeProvider> PROVIDER_TYPE = ProviderType.register(ApexCore.identifier("recipes"), RecipeProviderImpl::new);

    private final RecipeOutputImpl output = new RecipeOutputImpl();
    private final HolderGetter<Item> items;
    public final net.minecraft.data.recipes.RecipeProvider delegate;

    private RecipeProviderImpl(ProviderListenerContext context) {
        items = context.registries().lookupOrThrow(Registries.ITEM);
        delegate = new net.minecraft.data.recipes.RecipeProvider(context.registries(), output) {
            // clear out this provider to ensure nothing generates
            @Override
            public void buildRecipes() { }

            @Override
            public void generateForEnabledBlockFamilies(FeatureFlagSet enabledFeatures) {
                // NOOP: super generates all vanilla BlockFamilies via 'BlockFamilies.getAllFamilies()'
            }
        };
    }

    @Override
    public RecipeOutput output() {
        return output;
    }

    @Override
    public HolderGetter<Item> items() {
        return items;
    }

    @Override
    public CompletableFuture<?> generate(CachedOutput cache, ProviderOutputContext context) {
        var recipePathProvider = context.elementPathProvider(Registries.RECIPE);
        var advancementPathProvider = context.elementPathProvider(Registries.ADVANCEMENT);

        return CompletableFuture.allOf(output.recipeData.stream()
                .map(data -> data.save(cache, context, recipePathProvider, advancementPathProvider))
                .toArray(CompletableFuture[]::new)
        );
    }

    // region: Delegates
    @Override
    public void oneToOneConversionRecipe(ItemLike result, ItemLike ingredient, @Nullable String group) {
        delegate.oneToOneConversionRecipe(result, ingredient, group);
    }

    @Override
    public void oneToOneConversionRecipe(ItemLike result, ItemLike ingredient, @Nullable String group, int resultCount) {
        delegate.oneToOneConversionRecipe(result, ingredient, group, resultCount);
    }

    @Override
    public void oreSmelting(List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) {
        delegate.oreSmelting(ingredients, category, result, experience, cookingTime, group);
    }

    @Override
    public void oreBlasting(List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) {
        delegate.oreBlasting(ingredients, category, result, experience, cookingTime, group);
    }

    @Override
    public <T extends AbstractCookingRecipe> void oreCooking(RecipeSerializer<T> serializer, AbstractCookingRecipe.Factory<T> recipeFactory, List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group, String suffix) {
        delegate.oreCooking(serializer, recipeFactory, ingredients, category, result, experience, cookingTime, group, suffix);
    }

    @Override
    public void netheriteSmithing(Item ingredientItem, RecipeCategory category, Item resultItem) {
        delegate.netheriteSmithing(ingredientItem, category, resultItem);
    }

    @Override
    public void trimSmithing(Item template, ResourceKey<TrimPattern> pattern, ResourceKey<Recipe<?>> recipe) {
        delegate.trimSmithing(template, pattern, recipe);
    }

    @Override
    public void twoByTwoPacker(RecipeCategory category, ItemLike packed, ItemLike unpacked) {
        delegate.twoByTwoPacker(category, packed, unpacked);
    }

    @Override
    public void threeByThreePacker(RecipeCategory category, ItemLike packed, ItemLike unpacked, String criterionName) {
        delegate.threeByThreePacker(category, packed, unpacked, criterionName);
    }

    @Override
    public void threeByThreePacker(RecipeCategory category, ItemLike packed, ItemLike unpacked) {
        delegate.threeByThreePacker(category, packed, unpacked);
    }

    @Override
    public void planksFromLog(ItemLike planks, TagKey<Item> logs, int resultCount) {
        delegate.planksFromLog(planks, logs, resultCount);
    }

    @Override
    public void planksFromLogs(ItemLike planks, TagKey<Item> logs, int result) {
        delegate.planksFromLogs(planks, logs, result);
    }

    @Override
    public void woodFromLogs(ItemLike wood, ItemLike log) {
        delegate.woodFromLogs(wood, log);
    }

    @Override
    public void woodenBoat(ItemLike boat, ItemLike material) {
        delegate.woodenBoat(boat, material);
    }

    @Override
    public void chestBoat(ItemLike boat, ItemLike material) {
        delegate.chestBoat(boat, material);
    }

    @Override
    public RecipeBuilder buttonBuilder(ItemLike button, Ingredient material) {
        return delegate.buttonBuilder(button, material);
    }

    @Override
    public RecipeBuilder doorBuilder(ItemLike door, Ingredient material) {
        return delegate.doorBuilder(door, material);
    }

    @Override
    public RecipeBuilder fenceBuilder(ItemLike fence, Ingredient material) {
        return delegate.fenceBuilder(fence, material);
    }

    @Override
    public RecipeBuilder fenceGateBuilder(ItemLike fenceGate, Ingredient material) {
        return delegate.fenceGateBuilder(fenceGate, material);
    }

    @Override
    public void pressurePlate(ItemLike pressurePlate, ItemLike material) {
        delegate.pressurePlate(pressurePlate, material);
    }

    @Override
    public RecipeBuilder pressurePlateBuilder(RecipeCategory category, ItemLike pressurePlate, Ingredient material) {
        return delegate.pressurePlateBuilder(category, pressurePlate, material);
    }

    @Override
    public void slab(RecipeCategory category, ItemLike slab, ItemLike material) {
        delegate.slab(category, slab, material);
    }

    @Override
    public RecipeBuilder slabBuilder(RecipeCategory category, ItemLike slab, Ingredient material) {
        return delegate.slabBuilder(category, slab, material);
    }

    @Override
    public RecipeBuilder stairBuilder(ItemLike stairs, Ingredient material) {
        return delegate.stairBuilder(stairs, material);
    }

    @Override
    public RecipeBuilder trapdoorBuilder(ItemLike trapdoor, Ingredient material) {
        return delegate.trapdoorBuilder(trapdoor, material);
    }

    @Override
    public RecipeBuilder signBuilder(ItemLike sign, Ingredient material) {
        return delegate.signBuilder(sign, material);
    }

    @Override
    public void hangingSign(ItemLike sign, ItemLike material) {
        delegate.hangingSign(sign, material);
    }

    @Override
    public void colorItemWithDye(List<Item> p_289675_, List<Item> p_289672_, String p_289641_, RecipeCategory p_423651_) {
        delegate.colorItemWithDye(p_289675_, p_289672_, p_289641_, p_423651_);
    }

    @Override
    public void colorWithDye(List<Item> dyes, List<Item> dyeableItems, @Nullable Item dye, String group, RecipeCategory category) {
        delegate.colorWithDye(dyes, dyeableItems, dye, group, category);
    }

    @Override
    public void carpet(ItemLike carpet, ItemLike material) {
        delegate.carpet(carpet, material);
    }

    @Override
    public void bedFromPlanksAndWool(ItemLike bed, ItemLike wool) {
        delegate.bedFromPlanksAndWool(bed, wool);
    }

    @Override
    public void banner(ItemLike banner, ItemLike material) {
        delegate.banner(banner, material);
    }

    @Override
    public void stainedGlassFromGlassAndDye(ItemLike stainedGlass, ItemLike dye) {
        delegate.stainedGlassFromGlassAndDye(stainedGlass, dye);
    }

    @Override
    public void dryGhast(ItemLike p_416739_) {
        delegate.dryGhast(p_416739_);
    }

    @Override
    public void harness(ItemLike p_416620_, ItemLike p_416110_) {
        harness(p_416620_, p_416110_);
    }

    @Override
    public void stainedGlassPaneFromStainedGlass(ItemLike stainedGlassPane, ItemLike stainedGlass) {
        delegate.stainedGlassPaneFromStainedGlass(stainedGlassPane, stainedGlass);
    }

    @Override
    public void stainedGlassPaneFromGlassPaneAndDye(ItemLike stainedGlassPane, ItemLike dye) {
        delegate.stainedGlassPaneFromGlassPaneAndDye(stainedGlassPane, dye);
    }

    @Override
    public void coloredTerracottaFromTerracottaAndDye(ItemLike terracotta, ItemLike dye) {
        delegate.coloredTerracottaFromTerracottaAndDye(terracotta, dye);
    }

    @Override
    public void concretePowder(ItemLike concretePowder, ItemLike dye) {
        delegate.concretePowder(concretePowder, dye);
    }

    @Override
    public void candle(ItemLike candle, ItemLike dye) {
        delegate.candle(candle, dye);
    }

    @Override
    public void wall(RecipeCategory category, ItemLike wall, ItemLike material) {
        delegate.wall(category, wall, material);
    }

    @Override
    public RecipeBuilder wallBuilder(RecipeCategory category, ItemLike wall, Ingredient material) {
        return delegate.wallBuilder(category, wall, material);
    }

    @Override
    public void polished(RecipeCategory category, ItemLike result, ItemLike material) {
        delegate.polished(category, result, material);
    }

    @Override
    public RecipeBuilder polishedBuilder(RecipeCategory category, ItemLike result, Ingredient material) {
        return delegate.polishedBuilder(category, result, material);
    }

    @Override
    public void cut(RecipeCategory category, ItemLike cutResult, ItemLike material) {
        delegate.cut(category, cutResult, material);
    }

    @Override
    public ShapedRecipeBuilder cutBuilder(RecipeCategory category, ItemLike cutResult, Ingredient material) {
        return delegate.cutBuilder(category, cutResult, material);
    }

    @Override
    public void chiseled(RecipeCategory category, ItemLike chiseledResult, ItemLike material) {
        delegate.chiseled(category, chiseledResult, material);
    }

    @Override
    public void mosaicBuilder(RecipeCategory category, ItemLike result, ItemLike material) {
        delegate.mosaicBuilder(category, result, material);
    }

    @Override
    public ShapedRecipeBuilder chiseledBuilder(RecipeCategory category, ItemLike chiseledResult, Ingredient material) {
        return delegate.chiseledBuilder(category, chiseledResult, material);
    }

    @Override
    public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike material) {
        delegate.stonecutterResultFromBase(category, result, material);
    }

    @Override
    public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike material, int resultCount) {
        delegate.stonecutterResultFromBase(category, result, material, resultCount);
    }

    @Override
    public void smeltingResultFromBase(ItemLike result, ItemLike ingredient) {
        delegate.smeltingResultFromBase(result, ingredient);
    }

    @Override
    public void nineBlockStorageRecipes(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed) {
        delegate.nineBlockStorageRecipes(unpackedCategory, unpacked, packedCategory, packed);
    }

    @Override
    public void nineBlockStorageRecipesWithCustomPacking(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String packedName, String packedGroup) {
        delegate.nineBlockStorageRecipesWithCustomPacking(unpackedCategory, unpacked, packedCategory, packed, packedName, packedGroup);
    }

    @Override
    public void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String unpackedName, String unpackedGroup) {
        delegate.nineBlockStorageRecipesRecipesWithCustomUnpacking(unpackedCategory, unpacked, packedCategory, packed, unpackedName, unpackedGroup);
    }

    @Override
    public void nineBlockStorageRecipes(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String packedName, @Nullable String packedGroup, String unpackedName, @Nullable String unpackedGroup) {
        delegate.nineBlockStorageRecipes(unpackedCategory, unpacked, packedCategory, packed, packedName, packedGroup, unpackedName, unpackedGroup);
    }

    @Override
    public void copySmithingTemplate(ItemLike template, ItemLike baseItem) {
        delegate.copySmithingTemplate(template, baseItem);
    }

    @Override
    public void copySmithingTemplate(ItemLike template, Ingredient baseItem) {
        delegate.copySmithingTemplate(template, baseItem);
    }

    @Override
    public <T extends AbstractCookingRecipe> void cookRecipes(String cookingMethod, RecipeSerializer<T> cookingSerializer, AbstractCookingRecipe.Factory<T> recipeFactory, int cookingTime) {
        delegate.cookRecipes(cookingMethod, cookingSerializer, recipeFactory, cookingTime);
    }

    @Override
    public <T extends AbstractCookingRecipe> void simpleCookingRecipe(String cookingMethod, RecipeSerializer<T> cookingSerializer, AbstractCookingRecipe.Factory<T> recipeFactory, int cookingTime, ItemLike material, ItemLike result, float experience) {
        delegate.simpleCookingRecipe(cookingMethod, cookingSerializer, recipeFactory, cookingTime, material, result, experience);
    }

    @Override
    public void grate(Block grateBlock, Block material) {
        delegate.grate(grateBlock, material);
    }

    @Override
    public void copperBulb(Block bulbBlock, Block material) {
        delegate.copperBulb(bulbBlock, material);
    }

    @Override
    public void suspiciousStew(Item flowerItem, SuspiciousEffectHolder effect) {
        delegate.suspiciousStew(flowerItem, effect);
    }

    @Override
    public void generateRecipes(BlockFamily blockFamily, FeatureFlagSet requiredFeatures) {
        delegate.generateRecipes(blockFamily, requiredFeatures);
    }

    @Override
    public Block getBaseBlock(BlockFamily family, BlockFamily.Variant variant) {
        return delegate.getBaseBlock(family, variant);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item) {
        return delegate.has(count, item);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
        return delegate.has(itemLike);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return delegate.has(tag);
    }

    @Override
    public Ingredient tag(TagKey<Item> tag) {
        return delegate.tag(tag);
    }

    @Override
    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return delegate.shaped(category, result);
    }

    @Override
    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return delegate.shaped(category, result, count);
    }

    @Override
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStack result) {
        return delegate.shapeless(category, result);
    }

    @Override
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return delegate.shapeless(category, result);
    }

    @Override
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return delegate.shapeless(category, result, count);
    }
    // endregion

    private static final class RecipeOutputImpl implements RecipeOutput {
        private final Set<ResourceKey<Recipe<?>>> recipes = Sets.newHashSet();
        private final List<RecipeData> recipeData = Lists.newArrayList();

        @Override
        public void accept(ResourceKey<Recipe<?>> registryKey, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
            if(!recipes.add(registryKey))
                throw new IllegalStateException("Duplicate recipe: " + registryKey.identifier());

            recipeData.add(new RecipeData(
                    new RecipeHolder<>(registryKey, recipe),
                    advancement,
                    conditions
            ));
        }

        @Override
        public Advancement.Builder advancement() {
            return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
        }

        @Override
        public void includeRootAdvancement() {
        }
    }

    private record RecipeData(
            RecipeHolder<?> recipe,
            @Nullable AdvancementHolder advancement,
            ICondition[] conditions
    ) {
        public CompletableFuture<?> save(CachedOutput cache, ProviderOutputContext context, PackOutput.PathProvider recipePathProvider, PackOutput.PathProvider advancementPathProvider) {
            return CompletableFuture.allOf(
                    saveRecipe(cache, context, recipePathProvider),
                    saveAdvancement(cache, context, advancementPathProvider)
            );
        }

        public CompletableFuture<?> saveRecipe(CachedOutput cache, ProviderOutputContext context, PackOutput.PathProvider pathProvider) {
            return DataProvider.saveStable(
                    cache,
                    context.registries(),
                    Recipe.CONDITIONAL_CODEC,
                    Optional.of(new WithConditions<>(recipe.value(), conditions)),
                    pathProvider.json(recipe.id())
            );
        }

        public CompletableFuture<?> saveAdvancement(CachedOutput cache, ProviderOutputContext context, PackOutput.PathProvider pathProvider) {
            if(advancement == null)
                return CompletableFuture.completedFuture(null);

            return DataProvider.saveStable(
                    cache,
                    context.registries(),
                    Advancement.CONDITIONAL_CODEC,
                    Optional.of(new WithConditions<>(advancement.value(), conditions)),
                    pathProvider.json(advancement.id())
            );
        }
    }
}
