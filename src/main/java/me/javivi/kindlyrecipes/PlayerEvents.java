package me.javivi.kindlyrecipes;

import me.javivi.kindlyrecipes.networking.ModMessages;
import me.javivi.kindlyrecipes.networking.SyncRecipesS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = Kindlyrecipes.MOD_ID)
public class PlayerEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger("KindlyRecipes");

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            LOGGER.info("Sincronizando recetas bloqueadas con jugador: {}", serverPlayer.getName().getString());
            ModMessages.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> serverPlayer),
                new SyncRecipesS2CPacket(RecipeBlocker.getBlockedRecipes())
            );
        }
    }
}