package me.javivi.kindlyrecipes;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("recipeblocker")
public class Kindlyrecipes {
    public static final String MOD_ID = "recipeblocker";

    public Kindlyrecipes() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        RecipeBlocker.loadBlockedRecipes();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        KindlyRecipesCommand.register(event.getServer().getCommands().getDispatcher());
        RecipeBlocker.loadBlockedRecipes();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        RecipeManager recipeManager = event.getServer().getRecipeManager();
        RecipeReloadListener.reloadRecipes(recipeManager);
        System.out.println("Recetas aplicadas después del inicio del servidor.");
    }
}



