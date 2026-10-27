package dev.apexstudios.apexcore.api.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;

public abstract class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    protected RecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    public static String getHasName(TagKey<Item> tag) {
        return "has_" + tag.location().getPath().replace('/', '_');
    }
}
