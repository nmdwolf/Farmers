package UI;

import core.*;
import objects.*;
import objects.units.Unit;
import objects.units.Worker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class ChoicePanel extends JPanel {

    private final OperationsPanel operationsPanel;
    private final HashMap<Integer, RoundedButton> buttons;
    private Dimension buttonSize;
    private final ActionListener hideListener, hideThisListener, showCellResources, showPlayerResources;
    private final UnsafeProperty<Pair<GameObject<?>, Boolean>> target;
    private int buttonCounter;

    public ChoicePanel(OperationsPanel operationsPanel, float cellWidth, float cellHeight, ActionListener hide, Property<InfoPanel.Mode> showResources, UnsafeProperty<Pair<GameObject<?>, Boolean>> target) {
        this.operationsPanel = operationsPanel;
        this.hideListener = hide;
        this.target = target;
        hideThisListener = _ -> setVisible(false);
        showCellResources = _ -> showResources.set(InfoPanel.Mode.CELL);
        showPlayerResources = _ -> showResources.set(InfoPanel.Mode.PLAYER);

        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        buttons = new HashMap<>(); // Move, Resource, Build, Upgrades, Evolutions
        buttonCounter = 0;

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        setVisible(false);
        setPreferredSize(new Dimension((int)(3 * cellWidth), (int)(2 * cellHeight)));
        setOpaque(false);

        addButton(KeyEvent.VK_M, new RoundedButton("Move", buttonSize, Color.gray, InternalSettings.STROKE_WIDTH, true));
        addButton(KeyEvent.VK_W, new RoundedButton("Work", buttonSize, Color.gray, InternalSettings.STROKE_WIDTH, true));
        addButton(KeyEvent.VK_C, new RoundedButton("Construct", buttonSize, Color.gray, InternalSettings.STROKE_WIDTH, true));
        addButton(KeyEvent.VK_U, new RoundedButton("Upgrade", buttonSize, Color.gray, InternalSettings.STROKE_WIDTH, true));
        addButton(KeyEvent.VK_E, new RoundedButton("Evolve", buttonSize, Color.gray, InternalSettings.STROKE_WIDTH, true));
        addButton(KeyEvent.VK_A, new RoundedButton("Attack", buttonSize, Color.gray, InternalSettings.STROKE_WIDTH, true));
        addButton(KeyEvent.VK_X, new RoundedButton("More", buttonSize, Color.gray, InternalSettings.STROKE_WIDTH, true));

        addListeners();
    }

    public void update(GameObject<?> selected, int cycle) {
        for(RoundedButton button : buttons.values()) {
            button.setVisible(false);
            button.setColor(selected.getPlayer().getAlternativeColor());
            for(ActionListener listener : button.getActionListeners())
                if(listener != hideListener && listener != hideThisListener && listener != showCellResources && listener != showPlayerResources)
                    button.removeActionListener(listener);
        }

        if(selected instanceof Unit)
            buttons.get(KeyEvent.VK_M).setVisible(true);

        if(selected instanceof Worker) {
            buttons.get(KeyEvent.VK_W).addActionListener(_ -> operationsPanel.update(selected, OperationCode.RESOURCE, cycle));
            buttons.get(KeyEvent.VK_W).setVisible(true);
        }
        if(selected instanceof Constructor) {
            buttons.get(KeyEvent.VK_C).addActionListener(_ -> operationsPanel.update(selected, OperationCode.CONSTRUCTION, cycle));
            buttons.get(KeyEvent.VK_C).setVisible(true);
        }
        if(!selected.getUpgrades().isEmpty()) {
            buttons.get(KeyEvent.VK_U).addActionListener(_ -> operationsPanel.update(selected, OperationCode.UPGRADE, cycle));
            buttons.get(KeyEvent.VK_U).setVisible(true);
        }
        if(selected instanceof Evolvable) {
            buttons.get(KeyEvent.VK_E).addActionListener(_ -> operationsPanel.update(selected, OperationCode.EVOLVE, cycle));
            buttons.get(KeyEvent.VK_E).setVisible(true);
        }
        if(selected instanceof Aggressive) {
            buttons.get(KeyEvent.VK_A).addActionListener(_ -> target.setOptional(new Pair<>(null, true)));
            buttons.get(KeyEvent.VK_A).setVisible(true);
        }

        if(selected instanceof Operational<?>) {
            buttons.get(KeyEvent.VK_X).addActionListener(_ -> operationsPanel.update(selected, OperationCode.EXTRA, cycle));
            buttons.get(KeyEvent.VK_X).setVisible(true);
        }

        if(buttons.values().stream().anyMatch(RoundedButton::isVisible))
            setVisible(true);
    }

    public void resizePanel(float cellWidth, float cellHeight) {
        setPreferredSize(new Dimension((int)(3 * cellWidth), (int)(2 * cellHeight)));
        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        for(RoundedButton button : buttons.values())
            button.resizeButton(buttonSize);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getModifiersEx() == 0) {
            JButton button = buttons.get(event.getKeyCode());
            if(button != null && button.isVisible()) {
                button.doClick();
                return true;
            }
        }
        return false;
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

    private void addListeners() {
        for(JButton button : buttons.values())
            button.addActionListener(hideThisListener);
        buttons.get(KeyEvent.VK_M).addActionListener(hideListener);
        buttons.get(KeyEvent.VK_W).addActionListener(showCellResources);
        for(Integer key : buttons.keySet())
            if(key != KeyEvent.VK_M && key != KeyEvent.VK_W)
                buttons.get(key).addActionListener(showPlayerResources);


        // Intercepts mouse events
        addMouseListener(new MouseAdapter() {});
    }

    private void addButton(int key, RoundedButton button) {
        buttons.put(key, button);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = buttonCounter % 4;
        c.gridy = buttonCounter / 4;
        c.weightx = 0.25;
        c.weighty = 0.25;
//            c.anchor = c.gridx == 0 ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        add(button, c);
        buttonCounter++;
    }
}
