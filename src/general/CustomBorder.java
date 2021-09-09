package general;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class CustomBorder extends AbstractBorder
{
    private Color borderColour;
    private int rectWidth;
    private int rectHeight;

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
            Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);
            gr.setColor(borderColour);
            gr.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            gr.drawArc(x + 1, y + 1, 10, 10, 90, 90);
            gr.drawArc(x + rectWidth - 11, y + 1, 10, 10, 0, 90);
            gr.drawArc(x + 1, y + rectHeight - 11, 10, 10, 180, 90);
            gr.drawArc(x + rectWidth - 11, y + rectHeight - 11, 10, 10, 270, 90);
            gr.fill(new Rectangle2D.Double(x, y + 5, 2, rectHeight - 10));
            gr.fill(new Rectangle2D.Double(x + 5, y, rectWidth - 10, 2));
            gr.fill(new Rectangle2D.Double(x + rectWidth - 2, y + 5, 2, rectHeight - 10));
            gr.fill(new Rectangle2D.Double(x + 5, y + rectHeight - 2, rectWidth - 10, 2));
        }
    }

    @Override
    public Insets getBorderInsets(Component c)
    {
        return (getBorderInsets(c, new Insets(0, 0, 0, 0)));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets)
    {
        insets.left = insets.top = insets.right = insets.bottom = 0;
        return insets;
    }

    @Override
    public boolean isBorderOpaque()
    {
        return true;
    }
}