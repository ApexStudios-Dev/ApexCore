package dev.apexstudios.apexcore.common.data.provider;

import com.google.common.collect.Maps;
import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jspecify.annotations.Nullable;
import java.util.Map;

public class ItemStackRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ItemStackTemplate result;
    private final Ingredient ingredient;
    private final Map<String, Criterion<?>> criteria = Maps.newLinkedHashMap();
    private final SingleItemRecipe.Factory<?> factory;

    public ItemStackRecipeBuilder(RecipeCategory category, SingleItemRecipe.Factory<?> factory, Ingredient ingredient, ItemStackTemplate result) {
        this.category = category;
        this.factory = factory;
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public ItemStackRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @DoNotCall
    @Deprecated
    @Override
    public ItemStackRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(result);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> registryKey) {
        ensureValid(registryKey);

        var advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(registryKey))
                .rewards(AdvancementRewards.Builder.recipe(registryKey))
                .requirements(AdvancementRequirements.Strategy.OR);

        criteria.forEach(advancement::addCriterion);

        var recipe = factory.create(new Recipe.CommonInfo(true), ingredient, result);
        output.accept(registryKey, recipe, advancement.build(registryKey.identifier().withPrefix("recipes/" + category.getFolderName() + '/')));
    }

    private void ensureValid(ResourceKey<Recipe<?>> registryKey) {
        if(criteria.isEmpty())
            throw new IllegalStateException("No way of obtaining recipe: " + registryKey.identifier());
    }

    public static ItemStackRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemStackTemplate result) {
        return new ItemStackRecipeBuilder(category, StonecutterRecipe::new, ingredient, result);
    }
}
