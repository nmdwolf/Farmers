package UI;

import core.InternalSettings;
import core.Settings;
import core.player.Mission;
import core.player.MissionArchive;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class MissionPanel extends Subpanel {

    private final Settings settings;
    private final InternalSettings internalSettings;

    public MissionPanel(@NotNull Settings settings, @NotNull InternalSettings internalSettings) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.settings = settings;
        this.internalSettings = internalSettings;
    }

    public void update(MissionArchive archive) {
        removeAll();
        for(Mission m : archive.getCompleted())
            add(createBlock(m.getDescription(), true));
        for(Mission m : archive.getRemaining())
            add(createBlock(m.getDescription(), false));
    }

    private JPanel createBlock(String text, boolean completed) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(CustomMethods.compoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), new CustomBorder(Color.gray, 3), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        panel.setPreferredSize(new Dimension(getWidth() - getInsets().left - getInsets().right, (int)internalSettings.getCellHeight()));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)internalSettings.getCellHeight()));
        panel.setMinimumSize(new Dimension(0, (int)internalSettings.getCellHeight()));
        panel.setOpaque(false);

        JTextArea area = new JTextArea(text);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFocusable(false);
        area.setPreferredSize(new Dimension(0, 0));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;

        if(completed) {

            gbc.weightx = 3.;
            panel.add(area, gbc);

            JPanel icon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D) g.create());
                    int size = Math.min(this.getWidth(), this.getHeight()) - this.getInsets().top;
                    gr.drawImage(InternalSettings.CHECK.getScaledInstance(size, size, Image.SCALE_SMOOTH), getWidth() - size - 5, 0, null);
                    gr.dispose();
                }
            };
            icon.setOpaque(false);

            gbc.gridx = 1;
            gbc.weightx = 1;
            panel.add(icon, gbc);
        } else {
            gbc.weightx = 1;
            panel.add(area, gbc);
        }

        area.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                panel.setBorder(CustomMethods.compoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), new CustomBorder(new Color(200, 150, 0), 3), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                panel.setBorder(CustomMethods.compoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), new CustomBorder(Color.gray, 3), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
            }
        });

        return panel;
    }
}
