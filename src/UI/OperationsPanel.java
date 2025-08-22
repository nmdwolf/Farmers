package UI;

import core.OperationCode;
import core.Pair;
import objects.GameObject;
import objects.buildings.Foundation;
import objects.units.Worker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

import static core.GameConstants.GRAY;

public class OperationsPanel extends JPanel {

    private final RoundedButton[] buttons;
    private Dimension buttonSize;
    private final ActionListener hide;
    private int actionPage;

    public OperationsPanel(int cellWidth, int cellHeight) {
        buttons = new RoundedButton[16];
        actionPage = 0;
        hide = _ -> setVisible(false);
        ActionListener next = _ -> actionPage++;
        ActionListener previous = _ -> actionPage--;

        addMouseListener(new MouseAdapter() {});

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);

        for(int i = 0; i < buttons.length; i++) {
            switch(i) {
                case 7:
                    buttons[i] = new RoundedButton("Next", buttonSize, Color.gray);
                    buttons[i].addActionListener(next);
                    break;
                case 15:
                    buttons[i] = new RoundedButton("Previous", buttonSize, Color.gray);
                    buttons[i].addActionListener(previous);
                    break;
                default:
                    buttons[i] = new RoundedButton("", buttonSize, Color.gray);
                    buttons[i].addActionListener(hide);
            }

            c.gridx = i % 4;
            c.gridy = Math.floorDiv(i % 8, 4);
            c.weightx = 0.5;
            c.weighty = 0.5;
//            c.anchor = c.gridx < 2 ? GridBagConstraints.WEST : GridBagConstraints.EAST;
            add(buttons[i], c);
        }

        setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        setOpaque(false);
    }

    public void update(GameObject selected, OperationCode code, int cycle) {

        for(int i = 0; i < 16; i++) {
            RoundedButton button = buttons[i];
            button.setColor(selected.getPlayer().getAlternativeColor());
            if(i != 7 && i != 15) {
                button.enableGhost(true);
                for (ActionListener listener : button.getActionListeners())
                    if (listener != hide)
                        button.removeActionListener(listener);
            }
        }

        OperationsList operations = selected.getOperations(cycle, code);
        selected.getCell().getContent().stream().filter(obj -> obj instanceof Foundation<?> f && f.getContract().isIdle()).map(obj -> new Pair<>(obj.getClassLabel(), ((Foundation<?>) obj).getContract())).forEach(pair -> operations.put("Continue " + pair.key(), () -> ((Worker)selected).transferContract(pair.value())));

        for(int i = 0; i < operations.size(); i++) {
            final int step = (i >= 7) ? i + 1 : i;
            buttons[step].updateText(operations.getDescription(i));
            buttons[step].enableGhost(false);
            buttons[step].addActionListener(_ -> operations.get(step).perform());
        }

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

        gr.setColor(GRAY);
        gr.fillRoundRect(1, 1, getWidth(), getHeight(), 30, 30);
        gr.setColor(Color.black);
        gr.setStroke(new BasicStroke(2));
        gr.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);

        gr.dispose();
    }
}
