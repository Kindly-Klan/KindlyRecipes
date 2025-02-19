package me.javivi.kindlyrecipes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class KindlyRecipesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kindlyrecipes")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("block")
                        .then(Commands.argument("recipe", StringArgumentType.string())
                                .executes(context -> blockRecipe(context))))
                .then(Commands.literal("unblock")
                        .then(Commands.argument("recipe", StringArgumentType.string())
                                .executes(context -> unblockRecipe(context))))
                .then(Commands.literal("reload")
                        .executes(KindlyRecipesCommand::reloadRecipes))
                .then(Commands.literal("list")
                        .executes(KindlyRecipesCommand::listBlockedRecipes)));
    }

    private static int blockRecipe(CommandContext<CommandSourceStack> context) {
        String recipeId = StringArgumentType.getString(context, "recipe");
        try {
            ResourceLocation recipe = new ResourceLocation(recipeId);
            if (RecipeBlocker.isRecipeBlocked(recipe)) {
                context.getSource().sendFailure(Component.literal("Recipe " + recipeId + " is already blocked"));
                return 0;
            }
            RecipeBlocker.blockRecipe(recipe);
            RecipeManagerHandler.forceReload(context.getSource().getServer());
            context.getSource().sendSuccess(() -> 
                Component.literal("Successfully blocked recipe: " + recipeId), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Invalid recipe ID format: " + recipeId));
            return 0;
        }
    }

    private static int unblockRecipe(CommandContext<CommandSourceStack> context) {
        String recipeId = StringArgumentType.getString(context, "recipe");
        try {
            ResourceLocation recipe = new ResourceLocation(recipeId);
            if (!RecipeBlocker.isRecipeBlocked(recipe)) {
                context.getSource().sendFailure(Component.literal("Recipe " + recipeId + " is not blocked"));
                return 0;
            }
            RecipeBlocker.unblockRecipe(recipe);
            RecipeManagerHandler.forceReload(context.getSource().getServer());
            context.getSource().sendSuccess(() -> 
                Component.literal("Successfully unblocked recipe: " + recipeId), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Invalid recipe ID format: " + recipeId));
            return 0;
        }
    }

    private static int reloadRecipes(CommandContext<CommandSourceStack> context) {
        RecipeManagerHandler.forceReload(context.getSource().getServer());
        context.getSource().sendSuccess(() -> 
            Component.literal("Successfully reloaded recipes"), true);
        return 1;
    }

    private static int listBlockedRecipes(CommandContext<CommandSourceStack> context) {
        var blockedRecipes = RecipeBlocker.getBlockedRecipes();
        if (blockedRecipes.isEmpty()) {
            context.getSource().sendSuccess(() -> 
                Component.literal("No recipes are currently blocked"), false);
        } else {
            context.getSource().sendSuccess(() -> 
                Component.literal("Currently blocked recipes:"), false);
            blockedRecipes.forEach(recipe -> 
                context.getSource().sendSuccess(() -> 
                    Component.literal("- " + recipe), false));
        }
        return 1;
    }
}

