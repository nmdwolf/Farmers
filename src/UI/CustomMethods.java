package UI;

import core.Location;
import core.Pair;
import core.player.Player;
import core.upgrade.Upgrade;
import objects.GameObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static core.GameConstants.*;
import static core.GameConstants.CELL_Y_MARGIN;
import static core.GameConstants.SPRITE_SIZE_MAX;

public class CustomMethods {

    private static int ID_COUNT, UPGRADE_COUNT, AWARD_COUNT;

    public static int getNewIdentifier() {
        return ID_COUNT++;
    }

    public static int getNewUpgradeIdentifier() {
        return UPGRADE_COUNT++;
    }

    public static int getNewAwardIdentifier() { return AWARD_COUNT++; }

    /**
     * Removes all upgrades that are already enabled.
     * @param list list of potential upgrades
     * @return filtered upgrades
     */
    public static <T extends Upgrade> java.util.List<T> extractUpgrades(Player player, List<T> list) {
        list.removeIf(upgrade -> !upgrade.isVisible());
        return list;
    }

    public static void printTranspose(int[][] grid, String toReplace, String replacement) {
        int[][] temp = new int[grid.length][grid[0].length];
        for(int x = 0; x < temp.length; x++)
            for(int y = 0; y < temp.length; y++)
                temp[y][x] = grid[x][y];
        System.out.println(Arrays.deepToString(temp).replace("], ", "]\n").replace(toReplace, replacement) + "\n\n");
    }

    public static void drawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }

    /**
     * Sets the rendering hints of the Graphics2D object for maximal quality
     * @param gr Graphics2D object of a JComponent
     * @return optimized Graphics2D object
     */
    public static Graphics2D optimizeGraphics(Graphics2D gr) {
        gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        gr.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
        gr.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        return gr;
    }

    public static BufferedImage getSprite(String path, int width, int height) {
        BufferedImage sprite = null;
        try {
            Image img = ImageIO.read(new File(path)).getScaledInstance(width, height, Image.SCALE_SMOOTH);
            sprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gr = optimizeGraphics(sprite.createGraphics());
            gr.drawImage(img, 0, 0, null);
            gr.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sprite;
    }

    public static BufferedImage selectedSprite(BufferedImage sprite, Color color) {
        BufferedImage img = new BufferedImage(sprite.getWidth() + 2, sprite.getHeight() + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = CustomMethods.optimizeGraphics(img.createGraphics());
        gr.setColor(color);
        gr.setStroke(new BasicStroke(2));
        gr.drawImage(sprite, 1, 1, null);
        gr.drawRect(1, 1, sprite.getWidth(), sprite.getHeight());
        gr.dispose();
        return img;
    }

    public static BufferedImage rotateImage(BufferedImage img, double angle) {
        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));
        int w = img.getWidth();
        int h = img.getHeight();

        // Calculate the new dimensions of the rotated image
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);
        BufferedImage rotated = new BufferedImage(newW, newH, img.getType());

        AffineTransform tf = new AffineTransform();
        tf.translate(newW / 2f, newH / 2f);
        tf.rotate(-angle);
        tf.translate( - w / 2f, - h / 2f);

        Graphics2D g2d = CustomMethods.optimizeGraphics(rotated.createGraphics());
        g2d.drawRenderedImage(img, tf);
        g2d.dispose();

        return rotated;
    }

    public static Pair<Integer, Integer> cellCoordinateTransform(int x, int y) {
        int selectionX = (int)Math.floor((x - CELL_X_MARGIN) / (float)(SPRITE_SIZE_MAX + CELL_X_MARGIN));
        int selectionY = (int)Math.floor((y - CELL_Y_MARGIN) / (float)(SPRITE_SIZE_MAX + CELL_Y_MARGIN));
        return new Pair<>(selectionX, selectionY);
    }

    public static int objectDistance(GameObject obj1, GameObject obj2) {
        Location loc1 = obj1.getCell().getLocation();
        Location loc2 = obj2.getCell().getLocation();
        return loc1.distanceTo(loc2);
    }
}
