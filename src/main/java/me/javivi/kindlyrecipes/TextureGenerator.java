package me.javivi.kindlyrecipes;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextureGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("KindlyRecipes");
    private static final int TEXTURE_SIZE = 256;
    private static final String MOD_ID = "kindlyrecipes";

    public static void generateBackgroundTexture() {
        try {
            // Obtener la ruta correcta para los recursos del mod
            Path resourcePath = FMLPaths.GAMEDIR.get()
                .resolve("src/main/resources/assets/" + MOD_ID + "/textures/gui");
            
            // Crear directorios si no existen
            Files.createDirectories(resourcePath);
            
            // Generar la textura
            BufferedImage image = createBackgroundTexture();
            
            // Guardar la textura en la ubicación correcta
            Path texturePath = resourcePath.resolve("recipe_manager.png");
            ImageIO.write(image, "PNG", texturePath.toFile());
            
            LOGGER.info("Textura generada en: {}", texturePath);
            
            // También guardar en la carpeta de ejecución para desarrollo
            Path runPath = FMLPaths.GAMEDIR.get()
                .resolve("run/resourcepacks/" + MOD_ID + "/assets/" + MOD_ID + "/textures/gui");
            Files.createDirectories(runPath);
            ImageIO.write(image, "PNG", runPath.resolve("recipe_manager.png").toFile());
            
        } catch (Exception e) {
            LOGGER.error("Error al generar la textura: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static BufferedImage createBackgroundTexture() {
        BufferedImage image = new BufferedImage(TEXTURE_SIZE, TEXTURE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        try {
            // Configurar renderizado de alta calidad
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Fondo con degradado oscuro
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(20, 20, 20, 230),
                TEXTURE_SIZE, TEXTURE_SIZE, new Color(30, 30, 30, 230)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
            
            // Borde con brillo
            for (int i = 0; i < 3; i++) {
                float alpha = 0.3f - (i * 0.08f);
                g2d.setColor(new Color(1f, 1f, 1f, alpha));
                g2d.setStroke(new BasicStroke(2 - i * 0.5f));
                g2d.drawRect(i, i, TEXTURE_SIZE - 2*i - 1, TEXTURE_SIZE - 2*i - 1);
            }
            
            // Decoraciones
            int margin = 20;
            g2d.setColor(new Color(255, 255, 255, 25));
            g2d.drawLine(margin, margin, TEXTURE_SIZE - margin, margin);
            g2d.drawLine(margin, TEXTURE_SIZE - margin, TEXTURE_SIZE - margin, TEXTURE_SIZE - margin);
            
            return image;
        } finally {
            g2d.dispose();
        }
    }
}