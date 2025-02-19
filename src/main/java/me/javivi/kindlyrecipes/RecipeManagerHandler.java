package me.javivi.kindlyrecipes;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = Kindlyrecipes.MOD_ID)
public class RecipeManagerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("KindlyRecipes");
    private static boolean hasLoadedRecipes = false;

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        loadRecipesIfNeeded();
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        loadRecipesIfNeeded();
        event.addListener(new RecipeReloadListener(event.getServerResources().getRecipeManager()));
    }

    private static synchronized void loadRecipesIfNeeded() {
        if (!hasLoadedRecipes) {
            LOGGER.debug("Loading blocked recipes configuration");
            RecipeBlocker.loadBlockedRecipes();
            hasLoadedRecipes = true;
        }
    }

    public static void forceReload(MinecraftServer server) {
        hasLoadedRecipes = false;
        loadRecipesIfNeeded();
        if (server != null) {
            RecipeReloadListener.reloadRecipes(server.getRecipeManager());
        }
    }
}