package me.javivi.kindlyrecipes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;

public class KindlyRecipesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kindlyrecipes")
                .then(Commands.literal("block")
                        .then(Commands.argument("recipe", StringArgumentType.string())
                                .executes(context -> {
                                    String recipeId = StringArgumentType.getString(context, "recipe");
                                    ResourceLocation recipe = new ResourceLocation(recipeId);
                                    RecipeBlocker.blockRecipe(recipe);
                                    return 1;
                                })))
                .then(Commands.literal("unblock")
                        .then(Commands.argument("recipe", StringArgumentType.string())
                                .executes(context -> {
                                    String recipeId = StringArgumentType.getString(context, "recipe");
                                    ResourceLocation recipe = new ResourceLocation(recipeId);
                                    RecipeBlocker.unblockRecipe(recipe);

                                    return 1;
                                })))
                // Recarga todas las recetas, usar sólo cuando sea necesario.
                .then(Commands.literal("reload")
                        .executes(context -> {
                            RecipeBlocker.loadBlockedRecipes();
                            RecipeReloadListener.reloadRecipes(context.getSource().getServer().getRecipeManager());
                            return 1;
                        })));
    }
}

