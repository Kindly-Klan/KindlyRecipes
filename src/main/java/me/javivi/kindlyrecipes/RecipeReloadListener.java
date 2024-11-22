package me.javivi.kindlyrecipes;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeReloadListener extends SimplePreparableReloadListener<List<Recipe<?>>> {
    private final RecipeManager recipeManager;

    public RecipeReloadListener(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    @Override
    protected List<Recipe<?>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Collection<Recipe<?>> allRecipes = recipeManager.getRecipes();
        System.out.println("Total recetas cargadas del RecipeManager: " + allRecipes.size());

        allRecipes.forEach(recipe -> {
            boolean isBlocked = RecipeBlocker.isRecipeBlocked(recipe.getId());
            System.out.println("Receta: " + recipe.getId() + " bloqueada: " + isBlocked);
        });

        List<Recipe<?>> filteredRecipes = allRecipes.stream()
                .filter(recipe -> !RecipeBlocker.isRecipeBlocked(recipe.getId()))
                .collect(Collectors.toList());

        System.out.println("Total recetas después del filtro de bloqueo: " + filteredRecipes.size());
        return filteredRecipes;
    }

    @Override
    protected void apply(List<Recipe<?>> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        System.out.println("Recetas preparadas: " + prepared.size());
        prepared.forEach(recipe -> System.out.println("Receta cargada: " + recipe.getId()));
        if (prepared.isEmpty()) {
            System.out.println("Todas las recetas han sido bloqueadas o no se han cargado correctamente.");
        } else {
            recipeManager.replaceRecipes(prepared);
        }
    }

    public static void reloadRecipes(RecipeManager recipeManager) {
        RecipeReloadListener listener = new RecipeReloadListener(recipeManager);
        listener.apply(listener.prepare(null, null), null, null);
    }
}
