package UI;

import core.OperationCode;
import core.Property;
import objects.Constructor;
import objects.Evolvable;
import objects.GameObject;
import objects.Upgrader;
import objects.units.Unit;
import objects.units.Worker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

public class ChoicePanel extends JPanel {

    private final OperationsPanel operationsPanel;
    private final ArrayList<RoundedButton> buttons;
    private Dimension buttonSize;
    private final ActionListener hideListener, hideThisListener, srListener;
    private final Property<Boolean> showResources;

    public ChoicePanel(OperationsPanel operationsPanel, int cellWidth, int cellHeight, ActionListener hide, Property<Boolean> showResources) {
        this.operationsPanel = operationsPanel;
        this.hideListener = hide;
        this.showResources = showResources;
        hideThisListener = _ -> setVisible(false);
        srListener = _ -> showResources.set(true);

        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        buttons = new ArrayList<>(); // Move, Resource, Build, Upgrades, Evolutions

        buttons.add(new RoundedButton("Move", buttonSize, Color.gray));
        buttons.add(new RoundedButton("Work", buttonSize, Color.gray));
        buttons.add(new RoundedButton("Construct", buttonSize, Color.gray));
        buttons.add(new RoundedButton("Upgrade", buttonSize, Color.gray));
        buttons.add(new RoundedButton("Evolve", buttonSize, Color.gray));

        for(JButton button : buttons)
            button.addActionListener(hideThisListener);
        buttons.getFirst().addActionListener(hide);
        for(JButton buttons : buttons.subList(1, buttons.size()))
            buttons.addActionListener(srListener);

        // Intercepts mouse events
        addMouseListener(new MouseAdapter() {});

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        for(int i = 0; i < buttons.size(); i++) {
            c.gridx = i % 4;
            c.gridy = i / 4;
            c.weightx = 0.25;
            c.weighty = 0.25;
//            c.anchor = c.gridx == 0 ? GridBagConstraints.WEST : GridBagConstraints.EAST;
            add(buttons.get(i), c);
        }

        setVisible(false);
        setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        setOpaque(false);
    }

    public void update(GameObject selected, int cycle) {
        for(RoundedButton button : buttons) {
            button.setVisible(false);
            button.setColor(selected.getPlayer().getAlternativeColor());
            for(ActionListener listener : button.getActionListeners())
                if(listener != hideListener && listener != hideThisListener && listener != srListener)
                    button.removeActionListener(listener);
        }

        buttons.get(1).addActionListener(_ -> operationsPanel.update(selected, OperationCode.RESOURCE, cycle));
        buttons.get(2).addActionListener(_ -> operationsPanel.update(selected, OperationCode.CONSTRUCTION, cycle));
        buttons.get(3).addActionListener(_ -> operationsPanel.update(selected, OperationCode.UPGRADE, cycle));
        buttons.get(4).addActionListener(_ -> operationsPanel.update(selected, OperationCode.EVOLVE, cycle));

        if(selected instanceof Unit)
            buttons.get(0).setVisible(true);
        if(selected instanceof Worker)
            buttons.get(1).setVisible(true);
        if(selected instanceof Constructor)
            buttons.get(2).setVisible(true);
        if(selected instanceof Upgrader)
            buttons.get(3).setVisible(true);
        if(selected instanceof Evolvable)
            buttons.get(4).setVisible(true);

        setVisible(true);
    }

    public void resizePanel(int cellWidth, int cellHeight) {
        setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        for(RoundedButton button : buttons)
            button.resize(buttonSize);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());

        gr.setColor(new Color(210, 210, 210));
        gr.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
        gr.setColor(Color.black);
        gr.setStroke(new BasicStroke(2));
        gr.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);

        gr.dispose();
    }
}
