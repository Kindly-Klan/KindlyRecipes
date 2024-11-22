package me.javivi.kindlyrecipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class RecipeBlocker {
    private static final Set<ResourceLocation> blockedRecipes = new HashSet<>();
    private static final Logger LOGGER = Logger.getLogger("RecipeBlocker");

    public static void loadBlockedRecipes() {
        blockedRecipes.clear();
        try (FileReader reader = new FileReader("config/blocked_recipes.json")) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray array = json.getAsJsonArray("blocked_recipes");
            for (var element : array) {
                ResourceLocation recipe = new ResourceLocation(element.getAsString());
                blockedRecipes.add(recipe);
                LOGGER.info("Receta bloqueada: " + recipe);
            }
        } catch (IOException e) {
            LOGGER.severe("Error, no puedo registrar recetas bloqueadas, ¿error de formato?, ¿el archivo?");
        }
        LOGGER.info("Total recetas bloqueadas: " + blockedRecipes.size());
    }

    public static boolean isRecipeBlocked(ResourceLocation recipe) {
        boolean isBlocked = blockedRecipes.contains(recipe);
        LOGGER.info("¿Receta " + recipe + " bloqueada? " + isBlocked);
        return isBlocked;
    }

    public static void blockRecipe(ResourceLocation recipe) {
        blockedRecipes.add(recipe);
        LOGGER.info("Receta bloqueada: " + recipe);
    }
    // Todavía no implementado
    public static void unblockRecipe(ResourceLocation recipe) {
        blockedRecipes.remove(recipe);
        LOGGER.info("Receta desbloqueada: " + recipe);
    }

    public static Set<ResourceLocation> getBlockedRecipes() {
        return blockedRecipes;
    }
}
