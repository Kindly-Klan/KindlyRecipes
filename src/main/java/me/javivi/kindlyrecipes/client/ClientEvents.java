package me.javivi.kindlyrecipes.client;

import me.javivi.kindlyrecipes.Kindlyrecipes;
import me.javivi.kindlyrecipes.client.gui.RecipeManagerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Kindlyrecipes.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.OPEN_RECIPE_MANAGER.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // Verificar si el jugador tiene nivel de permiso 2 (OP)
                if (mc.player.hasPermissions(2)) {
                    mc.setScreen(new RecipeManagerScreen());
                } else {
                    // Mostrar mensaje de error si no tiene permisos
                    mc.player.displayClientMessage(
                        Component.translatable("message.kindlyrecipes.no_permission"),
                        true // mostrar en la hotbar
                    );
                }
            }
        }
    }
}