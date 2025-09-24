package UI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static core.GameConstants.SPRITE_SIZE_MAX;

public class Sprite {
    private final static HashMap<String, Optional<BufferedImage>> sprites = new HashMap<>();
    private final static HashMap<String, String> spriteLocations = new HashMap<>();
    private static int spriteSize = SPRITE_SIZE_MAX;

    /**
     * Registers a new sprite corresponding to the specified class identifier.
     * @param name class identifier
     * @param fileName sprite location
     */
    public static void registerSprite(String name, String fileName) {
        spriteLocations.put(name, fileName);
        sprites.put(name, loadSprite("src/img/" + fileName + ".png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX));
    }

    public static void resizeSprites(int size) {
        spriteSize = size;
        for(String name : sprites.keySet())
            sprites.put(name, loadSprite("src/img/" + spriteLocations.get(name) + ".png", spriteSize, spriteSize));
    }

    /**
     * Retrieves a sprite corresponding to the specified class identifier (if available).
     * @param name class identifier
     * @return sprite (if available)
     */
    public static Optional<BufferedImage> getSprite(String name) {
        return sprites.get(name);
    }

    public static Optional<BufferedImage> loadSprite(String path, int width, int height) {
        try {
            Image img = ImageIO.read(new File(path)).getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage sprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gr = CustomMethods.optimizeGraphics(sprite.createGraphics());
            gr.drawImage(img, 0, 0, null);
            gr.dispose();
            return Optional.of(sprite);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static void setSpriteSize(int size) {
        spriteSize = size;
    }

    public static int getSpriteSize() {
        return spriteSize;
    }

    @Deprecated
    public static BufferedImage getSelectedSprite(BufferedImage sprite, Color color) {
        BufferedImage img = new BufferedImage(sprite.getWidth() + 2, sprite.getHeight() + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = CustomMethods.optimizeGraphics(img.createGraphics());
        gr.setColor(color);
        gr.setStroke(new BasicStroke(2));
        gr.drawImage(sprite, 1, 1, null);
        gr.drawRect(1, 1, sprite.getWidth(), sprite.getHeight());
        gr.dispose();
        return img;
    }
}
