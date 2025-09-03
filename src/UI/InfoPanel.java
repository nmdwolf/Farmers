package UI;

import core.Property;
import objects.GameObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class InfoPanel extends JPanel {

    public enum Mode {OBJECT, CELL, PLAYER}

    private int width, height;
    private final Property<GameObject> selected;
    private Mode mode;

    public InfoPanel(Property<GameObject> selected) {
        mode = Mode.OBJECT;
        this.selected = selected;

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setOpaque(false);

        // To intercept mouse motion
        addMouseListener(new MouseAdapter() {});
    }

    public void update(Mode mode) {
        this.mode = mode;
    }

    public void resizePanel(float cellWidth, float cellHeight) {
        width = (int)(2 * cellWidth);
        height = (int)(5 * cellHeight);
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());

        gr.setColor(Color.lightGray);
        gr.fillRoundRect(1, 1, width - 3, height - 3, 10, 10);
        gr.setColor(Color.black);
        gr.setStroke(new BasicStroke(2));
        gr.drawRoundRect(1, 1, width - 3, height - 3, 10, 10);

        // Will only be null before any object has been selected, but paint manager will try to paint this panel before that occurs.
        selected.ifPresent(s -> {
            switch(mode) {
                case OBJECT: {
                    CustomMethods.drawString(gr, s.toString(), 20, 20);
                    break;
                }
                case CELL: {
                    String desc = "Cell content:\n\n" + s.getCell().getDescription();
                    CustomMethods.drawString(gr, desc, 20, 20);
                    break;
                }
                case PLAYER: {
                    String desc = "Player resources:\n\n" + s.getPlayer().getResources().toString().replace(", ", "\n");
                    CustomMethods.drawString(gr, desc, 20, 20);
                    break;
                }
            }
        });

        gr.dispose();
    }
}
