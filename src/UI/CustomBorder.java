package UI;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static core.GameConstants.STROKE_WIDTH;

public class CustomBorder extends AbstractBorder
{
    public static final int RADIUS = 15;

    private final Color borderColour;
    private final int thickness;
    private final float correction;

    public CustomBorder(Color colour)
    {
        this(colour, STROKE_WIDTH);
    }

    public CustomBorder(Color colour, int thickness)
    {
        borderColour = colour;
        this.thickness = thickness;
        correction = (thickness % 2 == 1) ? .5f : 0;
    }

    @Override
    // TODO Fix pixel splitting
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);
        if (g instanceof Graphics2D) {
            Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());
            gr.setColor(borderColour);
            gr.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gr.draw(new RoundRectangle2D.Float(x + thickness / 2f + correction, y + thickness / 2f + correction, width - thickness - 2 * correction, height - thickness - 2 * correction, RADIUS, RADIUS));
            gr.dispose();
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return (getBorderInsets(c, new Insets(thickness, thickness, thickness, thickness )));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = thickness;
        return insets;
    }

    @Override
    public boolean isBorderOpaque()
    {
        return true;
    }
}