package UI;

import objects.GameObject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class InfoPanel extends JPanel {

    private int width, height;
    private GameObject selected;
    private boolean objectOrCell;

    public InfoPanel() {
        objectOrCell = false;

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setOpaque(false);

        // To intercept mouse motion
        addMouseListener(new MouseAdapter() {});
    }

    public void update(@NotNull GameObject object, boolean objectOrCell) {
        selected = object;
        this.objectOrCell = objectOrCell;
    }

    public void resizePanel(int cellWidth, int cellHeight) {
        width = 2 * cellWidth;
        height = 5 * cellHeight;
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

        if(selected != null) { // Will only be null before any object has been selected, but paint manager will try to paint this panel before that occurs.
            if (objectOrCell)
                CustomMethods.drawString(gr, selected.toString(), 20, 20);
            else
                CustomMethods.drawString(gr, selected.getCell().getDescription(), 20, 20);
        }

        gr.dispose();
    }
}
