package dev.apexstudios.apexcore.lib.data.provider;

import com.google.common.collect.ImmutableMap;
import dev.apexstudios.apexcore.core.data.provider.ItemStackRecipeBuilder;
import dev.apexstudios.apexcore.core.data.provider.RecipeProviderImpl;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.data.recipes.SmithingTrimRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.neoforged.neoforge.common.DataMapHooks;
import org.jetbrains.annotations.Nullable;

public interface RecipeProvider {
    ProviderType<RecipeProvider> PROVIDER_TYPE = RecipeProviderImpl.PROVIDER_TYPE;

    Map<BlockFamily.Variant, FamilyRecipeProvider> SHAPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, FamilyRecipeProvider>builder()
            .put(BlockFamily.Variant.BUTTON, (provider, ingredient, material) -> provider.buttonBuilder(ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.CHISELED, (provider, ingredient, material) -> provider.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.CUT, (provider, ingredient, material) -> provider.cutBuilder(RecipeCategory.BUILDING_BLOCKS, ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.DOOR, (provider, ingredient, material) -> provider.doorBuilder(ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.CUSTOM_FENCE, (provider, ingredient, material) -> provider.fenceBuilder(ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.FENCE, (provider, ingredient, material) -> provider.fenceBuilder(ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (provider, ingredient, material) -> provider.fenceGateBuilder(ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.FENCE_GATE, (provider, ingredient, material) -> provider.fenceGateBuilder(ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.SIGN, (provider, ingredient, material) -> provider.signBuilder(ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.SLAB, (provider, ingredient, material) -> provider.slabBuilder(RecipeCategory.BUILDING_BLOCKS, ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.STAIRS, (provider, ingredient, material) -> provider.stairBuilder(ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.PRESSURE_PLATE, (provider, ingredient, material) -> provider.pressurePlateBuilder(RecipeCategory.REDSTONE, ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.POLISHED, (provider, ingredient, material) -> provider.polishedBuilder(RecipeCategory.BUILDING_BLOCKS, ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.TRAPDOOR, (provider, ingredient, material) -> provider.trapdoorBuilder(ingredient, Ingredient.of(material)))
            .put(BlockFamily.Variant.WALL, (provider, ingredient, material) -> provider.wallBuilder(RecipeCategory.DECORATIONS, ingredient, Ingredient.of(material)))
            .build();

    RecipeOutput output();

    HolderGetter<Item> items();

    default void generateForEnabledBlockFamilies(FeatureFlagSet enabledFeatures) {
        BlockFamilies.getAllFamilies().filter(BlockFamily::shouldGenerateRecipe).forEach(family -> generateRecipes(family, enabledFeatures));
    }

    default void oneToOneConversionRecipe(ItemLike result, ItemLike ingredient, @Nullable String group) {
        oneToOneConversionRecipe(result, ingredient, group, 1);
    }

    default void oneToOneConversionRecipe(ItemLike result, ItemLike ingredient, @Nullable String group, int resultCount) {
        shapeless(RecipeCategory.MISC, result, resultCount).requires(ingredient).group(group).unlockedBy(getHasName(ingredient), has(ingredient)).save(output(), getConversionRecipeName(result, ingredient));
    }

    default void oreSmelting(List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) {
        oreCooking(RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, ingredients, category, result, experience, cookingTime, group, "_from_smelting");
    }

    default void oreBlasting(List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) {
        oreCooking(RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, ingredients, category, result, experience, cookingTime, group, "_from_blasting");
    }

    default <T extends AbstractCookingRecipe> void oreCooking(RecipeSerializer<T> serializer, AbstractCookingRecipe.Factory<T> recipeFactory, List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group, String suffix) {
        for (var ingredient : ingredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(ingredient), category, result, experience, cookingTime, serializer, recipeFactory).group(group).unlockedBy(getHasName(ingredient), has(ingredient)).save(output(), getItemName(result) + suffix + "_" + getItemName(ingredient));
        }
    }

    default void netheriteSmithing(Item ingredientItem, RecipeCategory category, Item resultItem) {
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(ingredientItem), tag(ItemTags.NETHERITE_TOOL_MATERIALS), category, resultItem).unlocks("has_netherite_ingot", has(ItemTags.NETHERITE_TOOL_MATERIALS)).save(output(), getItemName(resultItem) + "_smithing");
    }

    default void trimSmithing(Item templateItem, ResourceKey<Recipe<?>> key) {
        SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(templateItem), tag(ItemTags.TRIMMABLE_ARMOR), tag(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC).unlocks("has_smithing_trim_template", has(templateItem)).save(output(), key);
    }

    default void twoByTwoPacker(RecipeCategory category, ItemLike packed, ItemLike unpacked) {
        shaped(category, packed, 1).define('#', unpacked).pattern("##").pattern("##").unlockedBy(getHasName(unpacked), has(unpacked)).save(output());
    }

    default void threeByThreePacker(RecipeCategory category, ItemLike packed, ItemLike unpacked, String criterionName) {
        shapeless(category, packed).requires(unpacked, 9).unlockedBy(criterionName, has(unpacked)).save(output());
    }

    default void threeByThreePacker(RecipeCategory category, ItemLike packed, ItemLike unpacked) {
        threeByThreePacker(category, packed, unpacked, getHasName(unpacked));
    }

    default void planksFromLog(ItemLike planks, TagKey<Item> logs, int resultCount) {
        shapeless(RecipeCategory.BUILDING_BLOCKS, planks, resultCount).requires(logs).group("planks").unlockedBy("has_log", has(logs)).save(output());
    }

    default void planksFromLogs(ItemLike planks, TagKey<Item> logs, int result) {
        shapeless(RecipeCategory.BUILDING_BLOCKS, planks, result).requires(logs).group("planks").unlockedBy("has_logs", has(logs)).save(output());
    }

    default void woodFromLogs(ItemLike wood, ItemLike log) {
        shaped(RecipeCategory.BUILDING_BLOCKS, wood, 3).define('#', log).pattern("##").pattern("##").group("bark").unlockedBy("has_log", has(log)).save(output());
    }

    default void woodenBoat(ItemLike boat, ItemLike material) {
        shaped(RecipeCategory.TRANSPORTATION, boat).define('#', material).pattern("# #").pattern("###").group("boat").unlockedBy("in_water", insideOf(Blocks.WATER)).save(output());
    }

    default void chestBoat(ItemLike boat, ItemLike material) {
        shapeless(RecipeCategory.TRANSPORTATION, boat).requires(Blocks.CHEST).requires(material).group("chest_boat").unlockedBy("has_boat", has(ItemTags.BOATS)).save(output());
    }

    default RecipeBuilder buttonBuilder(ItemLike button, Ingredient material) {
        return shapeless(RecipeCategory.REDSTONE, button).requires(material);
    }

    default RecipeBuilder doorBuilder(ItemLike door, Ingredient material) {
        return shaped(RecipeCategory.REDSTONE, door, 3).define('#', material).pattern("##").pattern("##").pattern("##");
    }

    default RecipeBuilder fenceBuilder(ItemLike fence, ItemLike stick, int count, Ingredient material) {
        return shaped(RecipeCategory.DECORATIONS, fence, count).define('W', material).define('#', stick).pattern("W#W").pattern("W#W");
    }

    default RecipeBuilder fenceBuilder(ItemLike fence, ItemLike stick, Ingredient material) {
        return fenceBuilder(fence, stick, 3, material);
    }

    default RecipeBuilder fenceBuilder(ItemLike fence, Ingredient material) {
        return fenceBuilder(fence, Items.STICK, 3, material);
    }

    default RecipeBuilder fenceGateBuilder(ItemLike fenceGate, Ingredient material) {
        return shaped(RecipeCategory.REDSTONE, fenceGate).define('#', Items.STICK).define('W', material).pattern("#W#").pattern("#W#");
    }

    default void pressurePlate(ItemLike pressurePlate, ItemLike material) {
        pressurePlateBuilder(RecipeCategory.REDSTONE, pressurePlate, Ingredient.of(material)).unlockedBy(getHasName(material), has(material)).save(output());
    }

    default RecipeBuilder pressurePlateBuilder(RecipeCategory category, ItemLike pressurePlate, Ingredient material) {
        return shaped(category, pressurePlate).define('#', material).pattern("##");
    }

    default void slab(RecipeCategory category, ItemLike slab, ItemLike material) {
        slabBuilder(category, slab, Ingredient.of(material)).unlockedBy(getHasName(material), has(material)).save(output());
    }

    default RecipeBuilder slabBuilder(RecipeCategory category, ItemLike slab, Ingredient material) {
        return shaped(category, slab, 6).define('#', material).pattern("###");
    }

    default RecipeBuilder stairBuilder(ItemLike stairs, Ingredient material) {
        return shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4).define('#', material).pattern("#  ").pattern("## ").pattern("###");
    }

    default RecipeBuilder trapdoorBuilder(ItemLike trapdoor, Ingredient material) {
        return shaped(RecipeCategory.REDSTONE, trapdoor, 2).define('#', material).pattern("###").pattern("###");
    }

    default RecipeBuilder signBuilder(ItemLike sign, Ingredient material) {
        return shaped(RecipeCategory.DECORATIONS, sign, 3).group("sign").define('#', material).define('X', Items.STICK).pattern("###").pattern("###").pattern(" X ");
    }

    default void hangingSign(ItemLike sign, ItemLike material) {
        shaped(RecipeCategory.DECORATIONS, sign, 6).group("hanging_sign").define('#', material).define('X', Items.CHAIN).pattern("X X").pattern("###").pattern("###").unlockedBy("has_stripped_logs", has(material)).save(output());
    }

    default void colorBlockWithDye(List<ItemLike> dyes, List<ItemLike> dyeableItems, String group) {
        colorWithDye(dyes, dyeableItems, null, group, RecipeCategory.BUILDING_BLOCKS);
    }

    default void colorWithDye(List<ItemLike> dyeItems, List<ItemLike> dyeableItems, @Nullable ItemLike dye, String group, RecipeCategory category) {
        for (var i = 0; i < dyeItems.size(); i++) {
            var dyeItem = dyeItems.get(i);
            var dyeableItem = dyeableItems.get(i);
            var items = dyeableItems.stream().filter(p_288265_ -> !p_288265_.equals(dyeableItem));

            if (dye != null) {
                items = Stream.concat(items, Stream.of(dye));
            }

            shapeless(category, dyeableItem).requires(dyeItem).requires(Ingredient.of(items)).group(group).unlockedBy("has_needed_dye", has(dyeItem)).save(output(), "dye_" + getItemName(dyeableItem));
        }
    }

    default void carpet(ItemLike carpet, ItemLike material) {
        shaped(RecipeCategory.DECORATIONS, carpet, 3).define('#', material).pattern("##").group("carpet").unlockedBy(getHasName(material), has(material)).save(output());
    }

    default void bedFromPlanksAndWool(ItemLike bed, ItemLike wool) {
        shaped(RecipeCategory.DECORATIONS, bed).define('#', wool).define('X', ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").unlockedBy(getHasName(wool), has(wool)).save(output());
    }

    default void banner(ItemLike banner, ItemLike material) {
        shaped(RecipeCategory.DECORATIONS, banner).define('#', material).define('|', Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").unlockedBy(getHasName(material), has(material)).save(output());
    }

    default void stainedGlassFromGlassAndDye(ItemLike stainedGlass, ItemLike dye) {
        shaped(RecipeCategory.BUILDING_BLOCKS, stainedGlass, 8).define('#', Blocks.GLASS).define('X', dye).pattern("###").pattern("#X#").pattern("###").group("stained_glass").unlockedBy("has_glass", has(Blocks.GLASS)).save(output());
    }

    default void stainedGlassPaneFromStainedGlass(ItemLike stainedGlassPane, ItemLike stainedGlass) {
        shaped(RecipeCategory.DECORATIONS, stainedGlassPane, 16).define('#', stainedGlass).pattern("###").pattern("###").group("stained_glass_pane").unlockedBy("has_glass", has(stainedGlass)).save(output());
    }

    default void stainedGlassPaneFromGlassPaneAndDye(ItemLike stainedGlassPane, ItemLike dye) {
        shaped(RecipeCategory.DECORATIONS, stainedGlassPane, 8).define('#', Blocks.GLASS_PANE).define('$', dye).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").unlockedBy("has_glass_pane", has(Blocks.GLASS_PANE)).unlockedBy(getHasName(dye), has(dye)).save(output(), getConversionRecipeName(stainedGlassPane, Blocks.GLASS_PANE));
    }

    default void coloredTerracottaFromTerracottaAndDye(ItemLike terracotta, ItemLike dye) {
        shaped(RecipeCategory.BUILDING_BLOCKS, terracotta, 8).define('#', Blocks.TERRACOTTA).define('X', dye).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").unlockedBy("has_terracotta", has(Blocks.TERRACOTTA)).save(output());
    }

    default void concretePowder(ItemLike concretePowder, ItemLike dye) {
        shapeless(RecipeCategory.BUILDING_BLOCKS, concretePowder, 8).requires(dye).requires(Blocks.SAND, 4).requires(Blocks.GRAVEL, 4).group("concrete_powder").unlockedBy("has_sand", has(Blocks.SAND)).unlockedBy("has_gravel", has(Blocks.GRAVEL)).save(output());
    }

    default void candle(ItemLike candle, ItemLike dye) {
        shapeless(RecipeCategory.DECORATIONS, candle).requires(Blocks.CANDLE).requires(dye).group("dyed_candle").unlockedBy(getHasName(dye), has(dye)).save(output());
    }

    default void wall(RecipeCategory category, ItemLike wall, ItemLike material) {
        wallBuilder(category, wall, Ingredient.of(material)).unlockedBy(getHasName(material), has(material)).save(output());
    }

    default RecipeBuilder wallBuilder(RecipeCategory category, ItemLike wall, Ingredient material) {
        return shaped(category, wall, 6).define('#', material).pattern("###").pattern("###");
    }

    default void polished(RecipeCategory category, ItemLike result, ItemLike material) {
        polishedBuilder(category, result, Ingredient.of(material)).unlockedBy(getHasName(material), has(material)).save(output());
    }

    default RecipeBuilder polishedBuilder(RecipeCategory category, ItemLike result, Ingredient material) {
        return shaped(category, result, 4).define('S', material).pattern("SS").pattern("SS");
    }

    default void cut(RecipeCategory category, ItemLike cutResult, ItemLike material) {
        cutBuilder(category, cutResult, Ingredient.of(material)).unlockedBy(getHasName(material), has(material)).save(output());
    }

    default ShapedRecipeBuilder cutBuilder(RecipeCategory category, ItemLike cutResult, Ingredient material) {
        return shaped(category, cutResult, 4).define('#', material).pattern("##").pattern("##");
    }

    default void chiseled(RecipeCategory category, ItemLike chiseledResult, ItemLike material) {
        chiseledBuilder(category, chiseledResult, Ingredient.of(material)).unlockedBy(getHasName(material), has(material)).save(output());
    }

    default void mosaicBuilder(RecipeCategory category, ItemLike result, ItemLike material) {
        shaped(category, result).define('#', material).pattern("#").pattern("#").unlockedBy(getHasName(material), has(material)).save(output());
    }

    default ShapedRecipeBuilder chiseledBuilder(RecipeCategory category, ItemLike chiseledResult, Ingredient material) {
        return shaped(category, chiseledResult).define('#', material).pattern("#").pattern("#");
    }

    default void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike material) {
        stonecutterResultFromBase(category, result, material, 1);
    }

    default void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike material, int resultCount) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(material), category, result, resultCount).unlockedBy(getHasName(material), has(material)).save(output(), getConversionRecipeName(result, material) + "_stonecutting");
    }

    default void stonecutterResultFromBase(RecipeCategory category, ItemLike material, ItemStack result) {
        ItemStackRecipeBuilder.stonecutting(Ingredient.of(material), category, result).unlockedBy(getHasName(material), has(material)).save(output(), getConversionRecipeName(result::getItem, material) + "_stonecutting");
    }

    default void smeltingResultFromBase(ItemLike result, ItemLike ingredient) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ingredient), RecipeCategory.BUILDING_BLOCKS, result, 0.1F, 200).unlockedBy(getHasName(ingredient), has(ingredient)).save(output());
    }

    default void nineBlockStorageRecipes(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed) {
        nineBlockStorageRecipes(unpackedCategory, unpacked, packedCategory, packed, getSimpleRecipeName(packed), null, getSimpleRecipeName(unpacked), null);
    }

    default void nineBlockStorageRecipesWithCustomPacking(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String packedName, String packedGroup) {
        nineBlockStorageRecipes(unpackedCategory, unpacked, packedCategory, packed, packedName, packedGroup, getSimpleRecipeName(unpacked), null);
    }

    default void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String unpackedName, String unpackedGroup) {
        nineBlockStorageRecipes(unpackedCategory, unpacked, packedCategory, packed, getSimpleRecipeName(packed), null, unpackedName, unpackedGroup);
    }

    default void nineBlockStorageRecipes(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String packedName, @Nullable String packedGroup, String unpackedName, @Nullable String unpackedGroup) {
        var output = output();
        shapeless(unpackedCategory, unpacked, 9).requires(packed).group(unpackedGroup).unlockedBy(getHasName(packed), has(packed)).save(output, ResourceKey.create(Registries.RECIPE, ResourceLocation.parse(unpackedName)));
        shaped(packedCategory, packed).define('#', unpacked).pattern("###").pattern("###").pattern("###").group(packedGroup).unlockedBy(getHasName(unpacked), has(unpacked)).save(output, ResourceKey.create(Registries.RECIPE, ResourceLocation.parse(packedName)));
    }

    default void copySmithingTemplate(ItemLike template, ItemLike baseItem) {
        shaped(RecipeCategory.MISC, template, 2).define('#', Items.DIAMOND).define('C', baseItem).define('S', template).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(getHasName(template), has(template)).save(output());
    }

    default void copySmithingTemplate(ItemLike template, Ingredient baseItem) {
        shaped(RecipeCategory.MISC, template, 2).define('#', Items.DIAMOND).define('C', baseItem).define('S', template).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(getHasName(template), has(template)).save(output());
    }

    default <T extends AbstractCookingRecipe> void simpleCookingRecipe(String cookingMethod, RecipeSerializer<T> cookingSerializer, AbstractCookingRecipe.Factory<T> recipeFactory, int cookingTime, ItemLike material, ItemLike result, float experience) {
        SimpleCookingRecipeBuilder.generic(Ingredient.of(material), RecipeCategory.FOOD, result, experience, cookingTime, cookingSerializer, recipeFactory).unlockedBy(getHasName(material), has(material)).save(output(), getItemName(result) + "_from_" + cookingMethod);
    }

    default void waxRecipes(FeatureFlagSet requiredFeatures) {
        DataMapHooks.INVERSE_WAXABLES_DATAMAP.forEach((waxed, unwaxed) -> {
            if (unwaxed.requiredFeatures().isSubsetOf(requiredFeatures))
                shapeless(RecipeCategory.BUILDING_BLOCKS, unwaxed).requires(waxed).requires(Items.HONEYCOMB).group(getItemName(unwaxed)).unlockedBy(getHasName(waxed), has(waxed)).save(output(), getConversionRecipeName(unwaxed, Items.HONEYCOMB));
        });
    }

    default void grate(Block grateBlock, Block material) {
        shaped(RecipeCategory.BUILDING_BLOCKS, grateBlock, 4).define('M', material).pattern(" M ").pattern("M M").pattern(" M ").unlockedBy(getHasName(material), has(material)).save(output());
    }

    default void copperBulb(Block bulbBlock, Block material) {
        shaped(RecipeCategory.REDSTONE, bulbBlock, 4).define('C', material).define('R', Items.REDSTONE).define('B', Items.BLAZE_ROD).pattern(" C ").pattern("CBC").pattern(" R ").unlockedBy(getHasName(material), has(material)).save(output());
    }

    default void suspiciousStew(Item flowerItem, SuspiciousEffectHolder effect) {
        var stew = new ItemStack(Items.SUSPICIOUS_STEW.builtInRegistryHolder(), 1, DataComponentPatch.builder().set(DataComponents.SUSPICIOUS_STEW_EFFECTS, effect.getSuspiciousEffects()).build());
        shapeless(RecipeCategory.FOOD, stew).requires(Items.BOWL).requires(Items.BROWN_MUSHROOM).requires(Items.RED_MUSHROOM).requires(flowerItem).group("suspicious_stew").unlockedBy(getHasName(flowerItem), has(flowerItem)).save(output(), getItemName(stew.getItem()) + "_from_" + getItemName(flowerItem));
    }

    default void generateRecipes(BlockFamily blockFamily, FeatureFlagSet requiredFeatures) {
        blockFamily.getVariants().forEach((variant, block) -> {
            if (block.requiredFeatures().isSubsetOf(requiredFeatures)) {
                var provider = SHAPE_BUILDERS.get(variant);
                var baseBlock = getBaseBlock(blockFamily, variant);

                if (provider != null) {
                    var builder = provider.create(this, block, baseBlock);
                    blockFamily.getRecipeGroupPrefix().ifPresent(p_293701_ -> builder.group(p_293701_ + (variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getRecipeGroup())));
                    builder.unlockedBy(blockFamily.getRecipeUnlockedBy().orElseGet(() -> getHasName(baseBlock)), has(baseBlock));
                    builder.save(output());
                }

                if (variant == BlockFamily.Variant.CRACKED)
                    smeltingResultFromBase(block, baseBlock);
            }
        });
    }

    default Block getBaseBlock(BlockFamily family, BlockFamily.Variant variant) {
        if (variant == BlockFamily.Variant.CHISELED) {
            if (!family.getVariants().containsKey(BlockFamily.Variant.SLAB))
                throw new IllegalStateException("Slab is not defined for the family.");
            else
                return family.get(BlockFamily.Variant.SLAB);
        } else
            return family.getBaseBlock();
    }

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

    static String getConversionRecipeName(ItemLike result, ItemLike ingredient) {
        return net.minecraft.data.recipes.RecipeProvider.getConversionRecipeName(result, ingredient);
    }

    static String getSmeltingRecipeName(ItemLike itemLike) {
        return net.minecraft.data.recipes.RecipeProvider.getSmeltingRecipeName(itemLike);
    }

    static String getBlastingRecipeName(ItemLike itemLike) {
        return net.minecraft.data.recipes.RecipeProvider.getBlastingRecipeName(itemLike);
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

    @FunctionalInterface
    interface FamilyRecipeProvider {
        RecipeBuilder create(RecipeProvider recipeProvider, ItemLike ingredient, ItemLike result);
    }
}
