package general;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {

    private int width, height;

    public RoundedButton(String text, Dimension dim, Color hover) {
        super(text);

        this.width = dim.width;
        this.height = dim.height;

        setBorder(new CustomBorder(Color.black, width, height));
        setContentAreaFilled(false);
        setFocusPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseMoved(e);
                setBorder(new CustomBorder(hover, width, height));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseMoved(e);
                setBorder(new CustomBorder(Color.black, width, height));
            }
        });
    }

    public RoundedButton(String text, int width, int height, Color hover) {
        super(text);

        this.width = width;
        this.height = height;

        setBorder(new CustomBorder(Color.black, width, height));
        setContentAreaFilled(false);
        setFocusPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseMoved(e);
                setBorder(new CustomBorder(hover, width, height));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseMoved(e);
                setBorder(new CustomBorder(Color.black, width, height));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);
        gr.setColor(Color.lightGray);
        gr.fillRoundRect(1, 1, width - 2, height - 2, 10, 10);

        gr.setColor(Color.black);
        gr.drawString(getText(), Math.floorDiv(width - g.getFontMetrics().stringWidth(getText()), 2), Math.floorDiv(height, 2) + Math.floorDiv(g.getFontMetrics().getHeight(), 4));
    }
}
