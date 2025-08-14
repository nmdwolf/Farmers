package general;

import core.OperationCode;
import objects.Constructor;
import objects.Evolvable;
import objects.GameObject;
import objects.Upgrader;
import objects.units.Worker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

public class ChoicePanel extends JPanel {

    private final OperationPanel resourcePanel;
    private final RoundedButton[] buttons;
    private OperationsList actions;
    private Dimension buttonSize;
    private final ActionListener hide;

    public ChoicePanel(OperationPanel resourcePanel, int cellWidth, int cellHeight) {
        this.resourcePanel = resourcePanel;
        hide = e -> setVisible(false);

        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        buttons = new RoundedButton[4]; // Resource, Build, Upgrades, Evolutions

        buttons[0] = new RoundedButton("Work", buttonSize, Color.gray);
        buttons[1] = new RoundedButton("Construct", buttonSize, Color.gray);
        buttons[2] = new RoundedButton("Upgrade", buttonSize, Color.gray);
        buttons[3] = new RoundedButton("Evolve", buttonSize, Color.gray);

        for(JButton button : buttons)
            button.addActionListener(hide);

        // Intercepts mouse events
        addMouseListener(new MouseAdapter() {});

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        for(int i = 0; i < 4; i++) {
            c.gridx = i % 2;
            c.gridy = i / 2;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.anchor = c.gridx == 0 ? GridBagConstraints.WEST : GridBagConstraints.EAST;
            add(buttons[i], c);
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
                if(listener != hide)
                    button.removeActionListener(listener);
        }

        buttons[0].addActionListener(event -> {
            actions = selected.getOperations(cycle, OperationCode.RESOURCE);
            resourcePanel.update(selected, cycle);
        });
        buttons[1].addActionListener(event -> actions = selected.getOperations(cycle, OperationCode.CONSTRUCTION));
        buttons[2].addActionListener(event -> actions = selected.getOperations(cycle, OperationCode.UPGRADE));
        buttons[3].addActionListener(event -> actions = selected.getOperations(cycle, OperationCode.EVOLVE));

        if(selected != null) {
            if(selected instanceof Worker)
                buttons[0].setVisible(true);
            if(selected instanceof Constructor)
                buttons[1].setVisible(true);
            if(selected instanceof Upgrader)
                buttons[2].setVisible(true);
            if(selected instanceof Evolvable)
                buttons[3].setVisible(true);
        }
        setVisible(true);
    }

    public void resize(int cellWidth, int cellHeight) {
        setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        for(RoundedButton button : buttons)
            button.resize(buttonSize);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);

        gr.setColor(new Color(255, 255, 255, 200));
        gr.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
    }
}
