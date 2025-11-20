package dev.apexstudios.apexcore.core.data.provider;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jspecify.annotations.Nullable;

public class ItemStackRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ItemStack result;
    private final Ingredient ingredient;
    private final Map<String, Criterion<?>> criteria = Maps.newLinkedHashMap();
    @Nullable private String group = null;
    private final SingleItemRecipe.Factory<?> factory;

    public ItemStackRecipeBuilder(RecipeCategory category, SingleItemRecipe.Factory<?> factory, Ingredient ingredient, ItemStack result) {
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

    @Override
    public ItemStackRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getResult() {
        return result.getItem();
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> registryKey) {
        ensureValid(registryKey);

        var advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(registryKey))
                .rewards(AdvancementRewards.Builder.recipe(registryKey))
                .requirements(AdvancementRequirements.Strategy.OR);

        criteria.forEach(advancement::addCriterion);

        var recipe = factory.create(Objects.requireNonNullElse(group, ""), ingredient, result);
        output.accept(registryKey, recipe, advancement.build(registryKey.identifier().withPrefix("recipes/" + category.getFolderName() + '/')));
    }

    private void ensureValid(ResourceKey<Recipe<?>> registryKey) {
        if(criteria.isEmpty())
            throw new IllegalStateException("No way of obtaining recipe: " + registryKey.identifier());
    }

    public static ItemStackRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemStack result) {
        return new ItemStackRecipeBuilder(category, StonecutterRecipe::new, ingredient, result);
    }
}
