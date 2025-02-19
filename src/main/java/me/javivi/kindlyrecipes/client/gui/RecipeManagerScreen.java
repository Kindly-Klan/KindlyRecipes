package me.javivi.kindlyrecipes.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import me.javivi.kindlyrecipes.RecipeBlocker;
import me.javivi.kindlyrecipes.Kindlyrecipes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.javivi.kindlyrecipes.networking.ModMessages;
import me.javivi.kindlyrecipes.networking.BlockRecipeC2SPacket;
import me.javivi.kindlyrecipes.networking.UnblockRecipeC2SPacket;

import java.util.*;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class RecipeManagerScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("KindlyRecipes");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Kindlyrecipes.MOD_ID, "textures/gui/recipe_manager.png");
    private EditBox searchBox;
    private Button toggleButton;
    private ScrollableRecipeList recipeList;
    private List<Recipe<?>> availableRecipes;
    private String searchText = "";
    private boolean showingBlocked = false;

    public RecipeManagerScreen() {
        super(Component.translatable("screen.kindlyrecipes.recipe_manager"));
        this.availableRecipes = new ArrayList<>();
        LOGGER.debug("Inicializando RecipeManagerScreen");
    }

    @Override
    protected void init() {
        LOGGER.debug("Inicializando interfaz del gestor de recetas");
        int centerX = width / 2;
        int centerY = height / 2;

        // Caja de búsqueda con mejor estilo
        this.searchBox = new EditBox(this.font, centerX - 100, centerY - 100, 200, 20, 
            Component.translatable("gui.kindlyrecipes.search")) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                int borderColor = this.isFocused() ? 0xFFFFFFFF : 0xFF555555;
                graphics.fill(this.getX() - 1, this.getY() - 1, 
                            this.getX() + this.width + 1, this.getY() + this.height + 1, 
                            borderColor);
                graphics.fill(this.getX(), this.getY(), 
                            this.getX() + this.width, this.getY() + this.height, 
                            0xFF000000);
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
                
                // Mostrar placeholder si está vacío y no tiene foco
                if (this.getValue().isEmpty() && !this.isFocused()) {
                    graphics.drawString(font, 
                        Component.translatable("gui.kindlyrecipes.search").getString(), 
                        this.getX() + 4, 
                        this.getY() + (this.height - 8) / 2, 
                        0xFF666666);
                }
            }
        };
        this.searchBox.setMaxLength(50);
        this.searchBox.setResponder(this::updateSearch);
        addRenderableWidget(this.searchBox);

        // Botón de alternar vista
        this.toggleButton = Button.builder(
            Component.translatable("gui.kindlyrecipes.show_" + (showingBlocked ? "available" : "blocked")),
            button -> toggleRecipeView())
            .pos(centerX - 100, centerY - 70)
            .size(200, 20)
            .build();
        addRenderableWidget(this.toggleButton);

        // Lista de recetas
        this.recipeList = new ScrollableRecipeList(
            minecraft, width, height, 
            centerY - 40, centerY + 90,
            this);
        addRenderableWidget(this.recipeList);
        
        // Establecer el foco inicial en la lista
        setFocused(this.recipeList);

        // Cargar recetas iniciales
        updateRecipeList();
        LOGGER.debug("Interfaz inicializada. Mostrando {} recetas", availableRecipes.size());
    }

    private void updateSearch(String text) {
        this.searchText = text.toLowerCase();
        updateRecipeList();
    }

    private void toggleRecipeView() {
        this.showingBlocked = !this.showingBlocked;
        this.toggleButton.setMessage(
            Component.translatable("gui.kindlyrecipes.show_" + (showingBlocked ? "available" : "blocked"))
        );
        updateRecipeList();
    }

    private boolean recipeMatchesSearch(Recipe<?> recipe) {
        if (searchText.isEmpty()) {
            return true;
        }
        
        String search = searchText.toLowerCase();
        
        // Buscar en el ID de la receta
        if (recipe.getId().toString().toLowerCase().contains(search)) {
            return true;
        }
        
        // Buscar en el nombre traducido del resultado
        if (recipe.getResultItem(minecraft.level.registryAccess())
                .getDisplayName().getString().toLowerCase().contains(search)) {
            return true;
        }
        
        // Buscar en los ingredientes
        return recipe.getIngredients().stream()
            .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
            .map(itemStack -> itemStack.getDisplayName().getString().toLowerCase())
            .anyMatch(name -> name.contains(search));
    }

    public void updateRecipeList() {
        if (minecraft == null || minecraft.level == null) {
            LOGGER.warn("No se puede actualizar la lista de recetas: minecraft o level es null");
            return;
        }

        Set<ResourceLocation> blockedRecipes = RecipeBlocker.getBlockedRecipes();
        LOGGER.debug("Recetas bloqueadas cargadas: {}", blockedRecipes.size());

        this.availableRecipes = minecraft.level.getRecipeManager().getRecipes().stream()
            .filter(recipe -> {
                boolean isBlocked = blockedRecipes.contains(recipe.getId());
                return showingBlocked ? isBlocked : !isBlocked;
            })
            .filter(this::recipeMatchesSearch)
            .collect(Collectors.toList());

        LOGGER.debug("Mostrando {} recetas ({} bloqueadas)", 
            availableRecipes.size(), 
            showingBlocked ? "mostrando" : "ocultando");

        this.recipeList.updateRecipes(availableRecipes);
        
        // También actualizar el botón de alternar vista
        if (this.toggleButton != null) {
            this.toggleButton.setMessage(
                Component.translatable("gui.kindlyrecipes.show_" + (showingBlocked ? "available" : "blocked"))
            );
        }
    }

    // Método estático para actualizar todas las instancias abiertas
    public static void onRecipesSynced() {
        if (Minecraft.getInstance().screen instanceof RecipeManagerScreen screen) {
            screen.updateRecipeList();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        
        // Render background texture with fallback
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        try {
            RenderSystem.setShaderTexture(0, TEXTURE);
            graphics.blit(TEXTURE, width / 2 - 120, height / 2 - 120, 0, 0, 240, 240);
        } catch (Exception e) {
            graphics.fill(width / 2 - 120, height / 2 - 120, width / 2 + 120, height / 2 + 120, 0xAA000000);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);

        // Render title
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 110, 0xFFFFFF);
        
        // Mostrar cantidad de recetas y resultados de búsqueda
        String countText = availableRecipes.size() + " recetas " + 
            (showingBlocked ? "bloqueadas" : "disponibles") +
            (!searchText.isEmpty() ? " (buscando: " + searchText + ")" : "");
        graphics.drawCenteredString(font, countText, width / 2, height / 2 + 95, 0xAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.recipeList != null && this.recipeList.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        // Si el campo de búsqueda tiene el foco, no procesar teclas adicionales
        if (this.searchBox.isFocused()) {
            return false;
        }
        
        // Permitir que la lista maneje las teclas cuando tiene el foco
        return this.recipeList.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.searchBox.isMouseOver(mouseX, mouseY)) {
            setFocused(this.searchBox);
        } else if (this.recipeList.isMouseOver(mouseX, mouseY)) {
            setFocused(this.recipeList);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public class RecipeEntry extends ObjectSelectionList.Entry<RecipeEntry> {
        private final Recipe<?> recipe;

        public RecipeEntry(Recipe<?> recipe) {
            this.recipe = recipe;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            // Render recipe entry
        }

        @SuppressWarnings("unused")
        private void toggleRecipe(Button button) {
            ResourceLocation recipeId = recipe.getId();
            if (RecipeBlocker.isRecipeBlocked(recipeId)) {
                // Enviar paquete al servidor para desbloquear
                ModMessages.INSTANCE.sendToServer(new UnblockRecipeC2SPacket(recipeId));
            } else {
                // Enviar paquete al servidor para bloquear
                ModMessages.INSTANCE.sendToServer(new BlockRecipeC2SPacket(recipeId));
            }
            
            // El botón se actualizará cuando recibamos la respuesta del servidor
        }

        @Override
        public Component getNarration() {
            return Component.translatable("gui.narration.recipe", recipe.getId().toString());
        }
    }
}