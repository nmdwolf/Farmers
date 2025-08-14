package general;

import core.OperationCode;
import objects.GameObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

public class OperationPanel extends JPanel {

    private final OperationCode code;
    private final RoundedButton[] buttons;
    private Dimension buttonSize;
    private final ActionListener hide, next, previous;
    private int actionPage;

    public OperationPanel(int cellWidth, int cellHeight, OperationCode code) {
        buttons = new RoundedButton[14];
        actionPage = 0;
        hide = e -> setVisible(false);
        next = e -> actionPage++;
        previous = e -> actionPage--;
        this.code = code;

        addMouseListener(new MouseAdapter() {});

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Dimension buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);

        for(int i = 0; i < buttons.length; i++) {
            buttons[i] = new RoundedButton("", buttonSize, Color.gray);
            buttons[i].addActionListener(hide);
            if(i == 7)
                buttons[i].addActionListener(next);
            else if (i == 15)
                buttons[i].addActionListener(previous);

            c.gridx = i % 4;
            c.gridy = Math.floorDiv(i, 4);
            c.weightx = 0.5;
            c.weighty = 0.5;
            if(c.gridx < 2)
                c.anchor = GridBagConstraints.WEST;
            else
                c.anchor = GridBagConstraints.EAST;
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
                if(listener != hide && listener != next && listener != previous)
                    button.removeActionListener(listener);

        }

        OperationsList contracts = selected.getOperations(cycle, code);
        for(int i = 0; i < contracts.size(); i++) {
            final int step = (i >= 7) ? i + 1 : i;
            buttons[step].setText(contracts.getDescription(i));
            buttons[step].setVisible(true);
            buttons[step].addActionListener(actionEvent -> {
                contracts.get(step).perform();
            });
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
