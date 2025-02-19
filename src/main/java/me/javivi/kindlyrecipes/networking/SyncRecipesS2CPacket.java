package me.javivi.kindlyrecipes.networking;

import me.javivi.kindlyrecipes.RecipeBlocker;
import me.javivi.kindlyrecipes.client.gui.RecipeManagerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SyncRecipesS2CPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger("KindlyRecipes");
    private final Set<ResourceLocation> blockedRecipes;

    public SyncRecipesS2CPacket(Set<ResourceLocation> blockedRecipes) {
        this.blockedRecipes = blockedRecipes;
    }

    public SyncRecipesS2CPacket(FriendlyByteBuf buf) {
        blockedRecipes = new HashSet<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            blockedRecipes.add(buf.readResourceLocation());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(blockedRecipes.size());
        blockedRecipes.forEach(buf::writeResourceLocation);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Asegurarnos de que esto se ejecuta en el cliente
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LOGGER.info("Received {} blocked recipes from server", blockedRecipes.size());
                RecipeBlocker.syncBlockedRecipes(blockedRecipes);
                RecipeManagerScreen.onRecipesSynced(); // Actualizar la UI si está abierta
            });
        });
        return true;
    }
}