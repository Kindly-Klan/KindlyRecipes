package me.javivi.kindlyrecipes.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // Caja de búsqueda
        this.searchBox = new EditBox(this.font, centerX - 100, centerY - 100, 200, 20, 
            Component.translatable("gui.kindlyrecipes.search"));
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
            .filter(recipe -> searchText.isEmpty() || 
                recipe.getId().toString().toLowerCase().contains(searchText))
            .collect(Collectors.toList());

        LOGGER.debug("Mostrando {} recetas ({} bloqueadas)", 
            availableRecipes.size(), 
            showingBlocked ? "mostrando" : "ocultando");

        this.recipeList.updateRecipes(availableRecipes);
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
            // Si falla la textura, usar un fondo oscuro
            graphics.fill(width / 2 - 120, height / 2 - 120, width / 2 + 120, height / 2 + 120, 0xAA000000);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);

        // Render title
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 110, 0xFFFFFF);
        
        // Mostrar cantidad de recetas
        String countText = availableRecipes.size() + " recetas " + (showingBlocked ? "bloqueadas" : "disponibles");
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
}