package UI;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class CustomBorder extends AbstractBorder
{
    public static final int RADIUS = 15;
    public static final int STROKE_WIDTH = 4;

    private final Color borderColour;
    private final int rectWidth, rectHeight;

    public CustomBorder(Color colour, Dimension dim)
    {
        borderColour = colour;
        rectWidth = dim.width;
        rectHeight = dim.height;
    }

    public CustomBorder(Color colour, int width, int height)
    {
        borderColour = colour;
        rectWidth = width;
        rectHeight = height;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        super.paintBorder(c, g, x, y, width, height);
        if (g instanceof Graphics2D) {
            Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());
            gr.setColor(borderColour);
            gr.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//
//            gr.drawArc(x + 1, y + 1, 10, 10, 90, 90);
//            gr.drawArc(x + rectWidth - 11, y + 1, 10, 10, 0, 90);
//            gr.drawArc(x + 1, y + rectHeight - 11, 10, 10, 180, 90);
//            gr.drawArc(x + rectWidth - 11, y + rectHeight - 11, 10, 10, 270, 90);
//            gr.fill(new Rectangle2D.Double(x, y + 5, 2, rectHeight - 10));
//            gr.fill(new Rectangle2D.Double(x + 5, y, rectWidth - 10, 2));
//            gr.fill(new Rectangle2D.Double(x + rectWidth - 2, y + 5, 2, rectHeight - 10));
//            gr.fill(new Rectangle2D.Double(x + 5, y + rectHeight - 2, rectWidth - 10, 2));
            gr.drawRoundRect(x + 1, y + 1, width - 2, height - 2, RADIUS, RADIUS);
            gr.dispose();
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return (getBorderInsets(c, new Insets(STROKE_WIDTH, STROKE_WIDTH, STROKE_WIDTH, STROKE_WIDTH )));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = STROKE_WIDTH;
        return insets;
    }

    @Override
    public boolean isBorderOpaque()
    {
        return true;
    }
}