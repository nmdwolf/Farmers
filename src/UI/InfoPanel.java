package UI;

import objects.GameObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class InfoPanel extends JPanel {

    private int width, height;
    private GameObject selected;

    public InfoPanel() {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // To intercept mouse motion
        addMouseListener(new MouseAdapter() {});

        setOpaque(false);
    }

    public void update(GameObject object) {
        selected = object;
    }

    public void resize(int cellWidth, int cellHeight) {
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

        if(selected != null)
            CustomMethods.drawString(gr, selected.toString(), 20, 20);

        gr.dispose();
    }
}
