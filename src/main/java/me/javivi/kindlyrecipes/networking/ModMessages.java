package me.javivi.kindlyrecipes.networking;

import me.javivi.kindlyrecipes.Kindlyrecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Kindlyrecipes.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.registerMessage(id(), BlockRecipeC2SPacket.class,
            BlockRecipeC2SPacket::encode,
            BlockRecipeC2SPacket::new,
            BlockRecipeC2SPacket::handle
        );

        INSTANCE.registerMessage(id(), UnblockRecipeC2SPacket.class,
            UnblockRecipeC2SPacket::encode,
            UnblockRecipeC2SPacket::new,
            UnblockRecipeC2SPacket::handle
        );

        INSTANCE.registerMessage(id(), SyncRecipesS2CPacket.class,
            SyncRecipesS2CPacket::encode,
            SyncRecipesS2CPacket::new,
            SyncRecipesS2CPacket::handle
        );
    }
}