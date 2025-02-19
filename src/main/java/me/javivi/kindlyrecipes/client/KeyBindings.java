package me.javivi.kindlyrecipes.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import me.javivi.kindlyrecipes.Kindlyrecipes;

@Mod.EventBusSubscriber(modid = Kindlyrecipes.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static final KeyMapping OPEN_RECIPE_MANAGER = new KeyMapping(
            "key.kindlyrecipes.open_manager",
            InputConstants.KEY_R,
            "key.categories.kindlyrecipes"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_RECIPE_MANAGER);
    }
}