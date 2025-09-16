package UI;

import core.GameConstants;
import core.OperationCode;
import core.Pair;
import core.Property;
import objects.*;
import objects.units.Unit;
import objects.units.Worker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

public class ChoicePanel extends JPanel {

    private final OperationsPanel operationsPanel;
    private final ArrayList<RoundedButton> buttons;
    private Dimension buttonSize;
    private final ActionListener hideListener, hideThisListener, showCellResources, showPlayerResources;
    private final Property<Pair<GameObject<?>, Boolean>> target;

    public ChoicePanel(OperationsPanel operationsPanel, float cellWidth, float cellHeight, ActionListener hide, Property<InfoPanel.Mode> showResources, Property<Pair<GameObject<?>, Boolean>> target) {
        this.operationsPanel = operationsPanel;
        this.hideListener = hide;
        this.target = target;
        hideThisListener = _ -> setVisible(false);
        showCellResources = _ -> showResources.set(InfoPanel.Mode.CELL);
        showPlayerResources = _ -> showResources.set(InfoPanel.Mode.PLAYER);

        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        buttons = new ArrayList<>(); // Move, Resource, Build, Upgrades, Evolutions

        buttons.add(new RoundedButton("Move", buttonSize, Color.gray, GameConstants.STROKE_WIDTH, true));
        buttons.add(new RoundedButton("Work", buttonSize, Color.gray, GameConstants.STROKE_WIDTH, true));
        buttons.add(new RoundedButton("Construct", buttonSize, Color.gray, GameConstants.STROKE_WIDTH, true));
        buttons.add(new RoundedButton("Upgrade", buttonSize, Color.gray, GameConstants.STROKE_WIDTH, true));
        buttons.add(new RoundedButton("Evolve", buttonSize, Color.gray, GameConstants.STROKE_WIDTH, true));
        buttons.add(new RoundedButton("Attack", buttonSize, Color.gray, GameConstants.STROKE_WIDTH, true));

        addListeners();
        setLayout(cellWidth, cellHeight);
    }

    public void update(GameObject<?> selected, int cycle) {
        for(RoundedButton button : buttons) {
            button.setVisible(false);
            button.setColor(selected.getPlayer().getAlternativeColor());
            for(ActionListener listener : button.getActionListeners())
                if(listener != hideListener && listener != hideThisListener && listener != showCellResources && listener != showPlayerResources)
                    button.removeActionListener(listener);
        }

        if(selected instanceof Unit)
            buttons.get(0).setVisible(true);

        if(selected instanceof Worker) {
            buttons.get(1).addActionListener(_ -> operationsPanel.update(selected, OperationCode.RESOURCE, cycle));
            buttons.get(1).setVisible(true);
        }
        if(selected instanceof Constructor) {
            buttons.get(2).addActionListener(_ -> operationsPanel.update(selected, OperationCode.CONSTRUCTION, cycle));
            buttons.get(2).setVisible(true);
        }
        if(!selected.getUpgrades().isEmpty()) {
            buttons.get(3).addActionListener(_ -> operationsPanel.update(selected, OperationCode.UPGRADE, cycle));
            buttons.get(3).setVisible(true);
        }
        if(selected instanceof Evolvable) {
            buttons.get(4).addActionListener(_ -> operationsPanel.update(selected, OperationCode.EVOLVE, cycle));
            buttons.get(4).setVisible(true);
        }
        if(selected instanceof Aggressive) {
            buttons.get(5).addActionListener(_ -> target.set(new Pair<>(null, true)));
            buttons.get(5).setVisible(true);
        }

        if(buttons.stream().anyMatch(RoundedButton::isVisible))
            setVisible(true);
    }

    public void resizePanel(float cellWidth, float cellHeight) {
        setPreferredSize(new Dimension((int)(3 * cellWidth), (int)(2 * cellHeight)));
        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        for(RoundedButton button : buttons)
            button.resizeButton(buttonSize);
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
        AbstractAction doClick = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RoundedButton src = (RoundedButton)e.getSource();
                if(src.isVisible())
                    src.doClick();
            }
        };
        buttons.get(0).getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('M', InputEvent.CTRL_DOWN_MASK), "click");
        buttons.get(0).getActionMap().put("click", doClick);
        buttons.get(1).getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK), "click");
        buttons.get(1).getActionMap().put("click", doClick);
        buttons.get(2).getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK), "click");
        buttons.get(2).getActionMap().put("click", doClick);
        buttons.get(3).getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('U', InputEvent.CTRL_DOWN_MASK), "click");
        buttons.get(3).getActionMap().put("click", doClick);
        buttons.get(4).getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK), "click");
        buttons.get(4).getActionMap().put("click", doClick);
        buttons.get(5).getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK), "click");
        buttons.get(5).getActionMap().put("click", doClick);


        for(JButton button : buttons)
            button.addActionListener(hideThisListener);
        buttons.getFirst().addActionListener(hideListener);
        buttons.get(1).addActionListener(showCellResources);
        for(JButton buttons : buttons.subList(2, buttons.size()))
            buttons.addActionListener(showPlayerResources);


        // Intercepts mouse events
        addMouseListener(new MouseAdapter() {});
    }

    private void setLayout(double width, double height) {
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
        setPreferredSize(new Dimension((int)(3 * width), (int)(2 * height)));
        setOpaque(false);
    }
}
