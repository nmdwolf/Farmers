package UI;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;

public class TranslucentLayerUI<T extends JComponent> extends LayerUI<T> {

    public final static float OPACITY_INCREMENT = .1f;

    private float opacity;
    private final T panel;

    public TranslucentLayerUI(T panel) {
        opacity = 0;
        this.panel = panel;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        super.paint(g2, c);
        g2.dispose();

        resetOpacity();
    }

    public void resetOpacity() {
        if(panel.isVisible())
            opacity = Math.min(1, opacity + OPACITY_INCREMENT);
        else
            opacity = 0;
    }

    public T getPanel() {
        return panel;
    }
}
