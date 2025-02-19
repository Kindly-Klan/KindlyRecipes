package me.javivi.kindlyrecipes;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class RecipeBlocker {
    private static final Set<ResourceLocation> blockedRecipes = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("KindlyRecipes");
    private static final String CONFIG_FILE = "config/blocked_recipes.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadBlockedRecipes() {
        blockedRecipes.clear();
        Path configPath = Paths.get(CONFIG_FILE);
        
        LOGGER.info("Intentando cargar recetas bloqueadas desde: {}", configPath.toAbsolutePath());
        
        if (!Files.exists(configPath)) {
            LOGGER.info("No se encontró el archivo de configuración, creando uno nuevo...");
            createDefaultConfig(configPath);
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray array = json.getAsJsonArray("blocked_recipes");
            
            for (JsonElement element : array) {
                try {
                    ResourceLocation recipe = new ResourceLocation(element.getAsString());
                    blockedRecipes.add(recipe);
                    LOGGER.debug("Receta bloqueada cargada: {}", recipe);
                } catch (Exception e) {
                    LOGGER.error("Error al cargar receta '{}': {}", element, e.getMessage());
                }
            }
            
            LOGGER.info("Se cargaron {} recetas bloqueadas", blockedRecipes.size());
        } catch (Exception e) {
            LOGGER.error("Error al cargar recetas bloqueadas: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig(Path path) {
        try {
            Files.createDirectories(path.getParent());
            JsonObject json = new JsonObject();
            JsonArray array = new JsonArray();
            // Añadir algunas recetas bloqueadas por defecto para pruebas
            array.add("minecraft:crafting_table");
            array.add("minecraft:furnace");
            json.add("blocked_recipes", array);
            
            String jsonStr = GSON.toJson(json);
            Files.writeString(path, jsonStr);
            LOGGER.info("Archivo de configuración creado con éxito en: {}", path);
        } catch (IOException e) {
            LOGGER.error("Error al crear el archivo de configuración: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void saveBlockedRecipes() {
        try {
            JsonObject json = new JsonObject();
            JsonArray array = new JsonArray();
            blockedRecipes.forEach(recipe -> array.add(recipe.toString()));
            json.add("blocked_recipes", array);
            
            Path configPath = Paths.get(CONFIG_FILE);
            String jsonStr = GSON.toJson(json);
            Files.writeString(configPath, jsonStr);
            
            LOGGER.info("Se guardaron {} recetas bloqueadas", blockedRecipes.size());
        } catch (IOException e) {
            LOGGER.error("Error al guardar recetas bloqueadas: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean isRecipeBlocked(ResourceLocation recipe) {
        return blockedRecipes.contains(recipe);
    }

    public static void blockRecipe(ResourceLocation recipe) {
        if (blockedRecipes.add(recipe)) {
            LOGGER.info("Receta bloqueada: {}", recipe);
            saveBlockedRecipes();
        }
    }

    public static void unblockRecipe(ResourceLocation recipe) {
        if (blockedRecipes.remove(recipe)) {
            LOGGER.info("Receta desbloqueada: {}", recipe);
            saveBlockedRecipes();
        }
    }

    public static Set<ResourceLocation> getBlockedRecipes() {
        return Set.copyOf(blockedRecipes);
    }
    
    public static void reloadBlockedRecipes() {
        LOGGER.info("Recargando recetas bloqueadas...");
        loadBlockedRecipes();
    }
}
