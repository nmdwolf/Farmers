package general;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class CustomMethods {

    private static int CONSTRUCT_COUNT, UPGRADE_COUNT;

    public static int getNewIdentifier() {
        return CONSTRUCT_COUNT++;
    }

    public static int getNewUpgradeIdentifier() {
        return UPGRADE_COUNT++;
    }

    public static void printTranspose(int[][] grid, String toReplace, String replacement) {
        int[][] temp = new int[grid.length][grid[0].length];
        for(int x = 0; x < temp.length; x++)
            for(int y = 0; y < temp.length; y++)
                temp[y][x] = grid[x][y];
        System.out.println(Arrays.deepToString(temp).replace("], ", "]\n").replace(toReplace, replacement) + "\n\n");
    }

    public static void customDrawString(Graphics g, String text, int x, int y) {
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
}
