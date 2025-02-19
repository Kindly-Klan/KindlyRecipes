package me.javivi.kindlyrecipes.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceLocation;
import me.javivi.kindlyrecipes.RecipeBlocker;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ScrollableRecipeList extends ObjectSelectionList<ScrollableRecipeList.RecipeEntry> {
    private final RecipeManagerScreen parent;
    private static final int MIN_ENTRY_HEIGHT = 24;
    private static final int LINE_HEIGHT = 9;
    private static final int PADDING = 4;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int MAX_TEXT_WIDTH = 170;

    public ScrollableRecipeList(Minecraft minecraft, int width, int height, int top, int bottom, RecipeManagerScreen parent) {
        super(minecraft, width, height, top, bottom, MIN_ENTRY_HEIGHT);
        this.parent = parent;
        this.setRenderBackground(false);
    }

    public void updateRecipes(List<Recipe<?>> recipes) {
        this.clearEntries();
        for (Recipe<?> recipe : recipes) {
            this.addEntry(new RecipeEntry(recipe));
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }

    @Override
    public int getRowWidth() {
        return 240;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.setScrollAmount(this.getScrollAmount() - delta * 16.0D);
        return true;
    }

    public class RecipeEntry extends ObjectSelectionList.Entry<RecipeEntry> {
        private final Recipe<?> recipe;
        private final List<FormattedCharSequence> wrappedText;
        private final Button toggleButton;
        private final int entryHeight;

        public RecipeEntry(Recipe<?> recipe) {
            this.recipe = recipe;
            String recipeId = recipe.getId().toString();
            this.wrappedText = minecraft.font.split(Component.literal(recipeId), MAX_TEXT_WIDTH);
            
            // Calcular altura basada en el texto
            this.entryHeight = Math.max(MIN_ENTRY_HEIGHT, 
                wrappedText.size() * LINE_HEIGHT + PADDING * 2);
            
            this.toggleButton = Button.builder(
                Component.translatable(RecipeBlocker.isRecipeBlocked(recipe.getId()) ? 
                    "gui.kindlyrecipes.unblock" : "gui.kindlyrecipes.block"),
                this::toggleRecipe)
                .pos(0, 0)
                .size(60, 20)
                .build();
        }

        private void toggleRecipe(Button button) {
            ResourceLocation recipeId = recipe.getId();
            if (RecipeBlocker.isRecipeBlocked(recipeId)) {
                RecipeBlocker.unblockRecipe(recipeId);
            } else {
                RecipeBlocker.blockRecipe(recipeId);
            }
            
            button.setMessage(Component.translatable(RecipeBlocker.isRecipeBlocked(recipeId) ? 
                "gui.kindlyrecipes.unblock" : "gui.kindlyrecipes.block"));
                
            // Actualizar la lista después de cambiar el estado
            parent.updateRecipeList();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, 
                         int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            // Fondo cuando está seleccionado
            if (isHovered) {
                graphics.fill(left, top, left + width, top + this.entryHeight - 1, 0x33FFFFFF);
            }
            
            // Renderizar texto envuelto
            int textY = top + PADDING;
            for (FormattedCharSequence line : wrappedText) {
                graphics.drawString(minecraft.font, line, left + 5, textY, TEXT_COLOR);
                textY += LINE_HEIGHT;
            }
            
            // Renderizar botón
            toggleButton.setX(left + width - 65);
            toggleButton.setY(top + (this.entryHeight - 20) / 2);
            toggleButton.render(graphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (toggleButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(recipe.getId().toString());
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        // No renderizar el fondo por defecto
    }

    @Override
    public boolean isFocused() {
        return parent.getFocused() == this;
    }
}