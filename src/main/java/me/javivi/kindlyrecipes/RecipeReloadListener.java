package me.javivi.kindlyrecipes;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeReloadListener extends SimplePreparableReloadListener<List<Recipe<?>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger("KindlyRecipes");
    private final RecipeManager recipeManager;

    public RecipeReloadListener(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    @Override
    protected List<Recipe<?>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Collection<Recipe<?>> allRecipes = recipeManager.getRecipes();
        LOGGER.debug("Processing {} recipes from RecipeManager", allRecipes.size());

        List<Recipe<?>> filteredRecipes = allRecipes.stream()
                .filter(recipe -> !RecipeBlocker.isRecipeBlocked(recipe.getId()))
                .collect(Collectors.toList());

        LOGGER.debug("Filtered to {} available recipes", filteredRecipes.size());
        return filteredRecipes;
    }

    @Override
    protected void apply(List<Recipe<?>> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        if (prepared.isEmpty()) {
            LOGGER.warn("All recipes have been blocked - this may not be intentional!");
        } else {
            LOGGER.info("Applying {} recipes to RecipeManager", prepared.size());
            recipeManager.replaceRecipes(prepared);
        }
    }

    public static void reloadRecipes(RecipeManager recipeManager) {
        RecipeReloadListener listener = new RecipeReloadListener(recipeManager);
        listener.apply(listener.prepare(null, null), null, null);
    }
}
