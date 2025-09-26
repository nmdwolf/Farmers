package UI;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class CustomMethods {

    private static int ID_COUNT, UPGRADE_COUNT, AWARD_COUNT;

    public static int getNewIdentifier() {
        return ID_COUNT++;
    }

    public static int getNewUpgradeIdentifier() {
        return UPGRADE_COUNT++;
    }

    public static int getNewAwardIdentifier() { return AWARD_COUNT++; }

    public static void printTranspose(int[][] grid, String toReplace, String replacement) {
        int[][] temp = new int[grid.length][grid[0].length];
        for(int x = 0; x < temp.length; x++)
            for(int y = 0; y < temp.length; y++)
                temp[y][x] = grid[x][y];
    }

    /**
     * Takes a {@code String} containing newline characters and adds a new line at every such character.
     * @param g graphics object used to draw the string
     * @param text string to display
     * @param x horizontal coordinate of first character
     * @param y vertical coordinate of first character
     */
    public static void drawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }

    public static void drawString(Graphics g, String text, int x, int y, int width) {
        StringBuilder output = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for(char c : text.toCharArray()) {
            temp.append(c);
            if(g.getFont().getStringBounds(temp.toString(),
                    ((Graphics2D)g).getFontRenderContext()).getWidth() >= (2 * width - 4 - 4 - 20)) { // TODO check if this is always valid
                output.append(temp);
                output.append("\n");
                temp = new StringBuilder();
            }
        }
        output.append(temp);
    }

    public static int maxLineWidth(Graphics g, String s) {
        int maxWidth = 0;
        for(String ss : s.split("\n"))
            maxWidth = Math.max(maxWidth, (int)g.getFont().getStringBounds(ss, ((Graphics2D)g).getFontRenderContext()).getWidth());
        return maxWidth;
    }

    /**
     * Sets the rendering hints of the Graphics2D object for maximal quality
     * @param gr Graphics2D object of a JComponent
     * @return optimized Graphics2D object
     */
    public static Graphics2D optimizeGraphics(Graphics2D gr) {
        gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        gr.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        return gr;
    }

    /**
     * Rotates a given image about its geometric center over the specified angle.
     * @param img original image to be rotated
     * @param angle rotation angle in radians
     * @return rotated image
     */
    public static BufferedImage rotateImage(BufferedImage img, double angle) {
        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));
        int w = img.getWidth();
        int h = img.getHeight();
        int maxDim = (int) Math.ceil(Math.sqrt(w * w + h * h));

        // Calculate the new dimensions of the rotated image
//        double newW = Math.floor(w * cos + h * sin);
//        double newH = Math.floor(h * cos + w * sin);

        int newW = maxDim; // Always using the maximum size ensures that the center coordinate is fixed
        int newH = maxDim;
        BufferedImage rotated = new BufferedImage(newW, newH, img.getType());

        AffineTransform tf = new AffineTransform();
        tf.translate(newW / 2f, newH / 2f);
        tf.rotate(-angle);
        tf.translate( -w / 2f, -h / 2f);

        Graphics2D g2d = CustomMethods.optimizeGraphics(rotated.createGraphics());
        g2d.drawRenderedImage(img, tf);
        g2d.dispose();

        return rotated;
    }

}
