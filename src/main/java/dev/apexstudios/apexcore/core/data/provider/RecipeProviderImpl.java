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
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.jetbrains.annotations.Nullable;

public final class RecipeProviderImpl implements BaseProvider, RecipeProvider {
    public static final ProviderType<RecipeProvider> PROVIDER_TYPE = ProviderType.register(ApexCore.identifier("recipes"), RecipeProviderImpl::new);

    private final RecipeOutputImpl output = new RecipeOutputImpl();
    private final HolderGetter<Item> items;

    private RecipeProviderImpl(ProviderListenerContext context) {
        items = context.registries().lookupOrThrow(Registries.ITEM);
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

    private static final class RecipeOutputImpl implements RecipeOutput {
        private final Set<ResourceKey<Recipe<?>>> recipes = Sets.newHashSet();
        private final List<RecipeData> recipeData = Lists.newArrayList();

        @Override
        public void accept(ResourceKey<Recipe<?>> registryKey, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
            if(!recipes.add(registryKey))
                throw new IllegalStateException("Duplicate recipe: " + registryKey.location());

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
