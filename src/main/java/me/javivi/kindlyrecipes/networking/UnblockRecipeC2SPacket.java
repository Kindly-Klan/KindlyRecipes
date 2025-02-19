package me.javivi.kindlyrecipes.networking;

import me.javivi.kindlyrecipes.RecipeBlocker;
import me.javivi.kindlyrecipes.RecipeManagerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class UnblockRecipeC2SPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger("KindlyRecipes");
    private final ResourceLocation recipeId;

    public UnblockRecipeC2SPacket(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public UnblockRecipeC2SPacket(FriendlyByteBuf buf) {
        this.recipeId = buf.readResourceLocation();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(recipeId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.hasPermissions(2)) {
                LOGGER.info("Unblocking recipe {} from client request", recipeId);
                RecipeBlocker.unblockRecipe(recipeId);
                RecipeManagerHandler.forceReload(player.getServer());
                
                // Enviar actualización a todos los clientes
                ModMessages.INSTANCE.send(
                    PacketDistributor.ALL.noArg(),
                    new SyncRecipesS2CPacket(RecipeBlocker.getBlockedRecipes())
                );
            }
        });
        return true;
    }
}