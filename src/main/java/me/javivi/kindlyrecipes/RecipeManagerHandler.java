package me.javivi.kindlyrecipes;

import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Kindlyrecipes.MOD_ID)
public class RecipeManagerHandler {

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        RecipeBlocker.loadBlockedRecipes(); // FN, carga recetas bloqueadas
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        RecipeBlocker.loadBlockedRecipes();
        event.addListener(new RecipeReloadListener(event.getServerResources().getRecipeManager()));
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        RecipeBlocker.loadBlockedRecipes();
    }
}
/*

Vale, KindlyRecipes es muy delicado, es por eso
que en varias partes del código llamo a RecipeBlocker.loadBlockedRecipes(),
ya que Minecraft carga en muchas partes de su "ciclo de vida" las recetas.
Se puede mejorar? Sí. Funciona? También. Así que es lo que hay, por lo menos hasta migrar a Fabric.

 */