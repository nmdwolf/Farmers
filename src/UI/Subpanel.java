package UI;

import core.InternalSettings;

import javax.swing.*;
import java.awt.*;

public class Subpanel extends JPanel {

    public Subpanel() {
        setBorder(new CustomBorder(Color.black));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());

        gr.setColor(InternalSettings.GRAY);
        gr.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
        gr.dispose();
    }
}
