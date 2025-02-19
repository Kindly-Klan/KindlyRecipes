package me.javivi.kindlyrecipes;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.javivi.kindlyrecipes.networking.ModMessages;

@Mod(Kindlyrecipes.MOD_ID)
public class Kindlyrecipes {
    public static final String MOD_ID = "kindlyrecipes";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Kindlyrecipes() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Loading recipe configurations...");
        event.enqueueWork(() -> {
            ModMessages.register();
            RecipeBlocker.loadBlockedRecipes();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        KindlyRecipesCommand.register(event.getServer().getCommands().getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        RecipeManager recipeManager = event.getServer().getRecipeManager();
        RecipeReloadListener.reloadRecipes(recipeManager);
        LOGGER.info("Recipe manager initialized and recipes applied");
    }
}



