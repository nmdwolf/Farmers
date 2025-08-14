package general;

import core.OperationCode;
import objects.GameObject;
import objects.resources.Resource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

public class OperationPanel extends JPanel {

    private final OperationCode code;
    private final RoundedButton[] buttons;
    private Dimension buttonSize;
    private final ActionListener hide;

    public OperationPanel(int cellWidth, int cellHeight, OperationCode code) {
        buttons = new RoundedButton[16];
        hide = e -> setVisible(false);
        this.code = code;

        addMouseListener(new MouseAdapter() {});

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Dimension buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);

        for(int i = 0; i < buttons.length; i++) {
            buttons[i] = new RoundedButton("", buttonSize, Color.gray);
            buttons[i].addActionListener(hide);

            c.gridx = i % 4;
            c.gridy = Math.floorDiv(i, 4);
            c.weightx = 0.5;
            c.weighty = 0.5;
            add(buttons[i], c);
        }

        setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        setOpaque(false);
    }

    public void update(GameObject selected, int cycle) {
        for(RoundedButton button : buttons) {
            button.setColor(selected.getPlayer().getAlternativeColor());
            button.setVisible(false);
            for(ActionListener listener : button.getActionListeners())
                if(listener != hide)
                    button.removeActionListener(listener);
        }

        OperationsList contracts = selected.getOperations(cycle, code);
        for(int i = 0; i < contracts.size(); i++) {
            final int step = i;
            buttons[i].setText(contracts.getDescription(i));
            buttons[i].setVisible(true);
            buttons[i].addActionListener(actionEvent -> {
                contracts.get(step).perform();
            });
        }
    }

    public void resize(int cellWidth, int cellHeight) {
        setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        for(RoundedButton button : buttons)
            button.setPreferredSize(buttonSize);
        revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);

        gr.setColor(new Color(255, 255, 255, 200));
        gr.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
    }
}
