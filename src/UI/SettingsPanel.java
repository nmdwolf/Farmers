package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends Subpanel {

    private final Settings settings;

    public SettingsPanel(Settings settings) {
        this.settings = settings;
        initialize();
    }

    private void initialize() {
        setAlignmentX(LEFT_ALIGNMENT);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        createAudioSection();
        add(new JSeparator(SwingConstants.HORIZONTAL));

        createVisualsSection();
//        add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    private void createAudioSection() {
        JPanel audioBox = new JPanel();
        audioBox.setOpaque(false);
        audioBox.setLayout(new GridBagLayout());
        audioBox.setBorder(new EmptyBorder(10, 10, 10, 10 + (int)UIManager.get("ScrollBar.width")));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        audioBox.add(new JLabel("<html><u>Audio</u></html>"), c);

        JLabel audioLabel = new JLabel("Audio source: ");
        audioLabel.setOpaque(false);
        audioLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        c.gridx = 0;
        c.gridy = 1;
        audioBox.add(audioLabel, c);

        JTextField audioInput = new JTextField(settings.getAudioSource(), 1);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.9;
        audioBox.add(audioInput, c);
        c.weightx = 0; // reset value for other components

        JLabel muteLabel = new JLabel("Mute: ");
        muteLabel.setOpaque(false);
        muteLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
        c.gridx = 0;
        c.gridy = 2;
        audioBox.add(muteLabel, c);

        JCheckBox muteBox = new JCheckBox();
        muteBox.setOpaque(false);
        muteBox.setSelected(!settings.playMusic());
        muteBox.setBorder(new EmptyBorder(4, 0, 0, 0));
        c.gridx = 1;
        c.gridy = 2;
        audioBox.add(muteBox, c);

        JLabel shuffleLabel = new JLabel("Shuffle songs: ");
        shuffleLabel.setOpaque(false);
        shuffleLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
        c.gridx = 0;
        c.gridy = 3;
        audioBox.add(shuffleLabel, c);

        JCheckBox shuffleBox = new JCheckBox();
        shuffleBox.setOpaque(false);
        shuffleBox.setSelected(settings.isShuffled());
        shuffleBox.setBorder(new EmptyBorder(4, 0, 0, 0));
        c.gridx = 1;
        c.gridy = 3;
        audioBox.add(shuffleBox, c);

        JButton applyAudio = new RoundedButton("Load", new Dimension(100, 30), Color.gray);
        applyAudio.addActionListener(_ -> {
            settings.toggleMusic(!muteBox.isSelected());
            settings.toggleShuffle(shuffleBox.isSelected());
            settings.setAudioSource(audioInput.getText());
        });
        c.gridx = 0;
        c.gridy = 4;
        audioBox.add(applyAudio, c);

        add(audioBox);
    }

    private void createVisualsSection() {
        JPanel visualBox = new JPanel();
        visualBox.setOpaque(false);
        visualBox.setLayout(new GridBagLayout());
        visualBox.setBorder(new EmptyBorder(10, 10, 10, 10 + (int)UIManager.get("ScrollBar.width")));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel visualsLabel = new JLabel("<html><u>Visuals</u></html>");
        c.gridx = 0;
        c.gridy = 0;
        visualBox.add(visualsLabel, c);

        JLabel cursorLabel = new JLabel("Custom cursor: ");
        cursorLabel.setOpaque(false);
        cursorLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        c.gridx = 0;
        c.gridy = 1;
        visualBox.add(cursorLabel, c);

        JCheckBox cursorBox = new JCheckBox();
        cursorBox.setOpaque(false);
        cursorBox.setSelected(settings.customCursor());
        cursorBox.setBorder(new EmptyBorder(4, 0, 0, 0));
        cursorBox.addChangeListener(_ -> settings.toggleCursor(cursorBox.isSelected()));
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = .9;
        visualBox.add(cursorBox, c);
        c.weightx = 0;

        JLabel cellArrowLabel = new JLabel("Show work diagrams: ");
        cellArrowLabel.setOpaque(false);
        cellArrowLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
        c.gridx = 0;
        c.gridy = 2;
        visualBox.add(cellArrowLabel, c);

        JCheckBox cellArrowBox = new JCheckBox();
        cellArrowBox.setOpaque(false);
        cellArrowBox.setSelected(settings.showArrows());
        cellArrowBox.setBorder(new EmptyBorder(4, 0, 0, 0));
        cellArrowBox.addChangeListener(_ -> settings.toggleArrows(cellArrowBox.isSelected()));
        c.gridx = 1;
        c.gridy = 2;
        visualBox.add(cellArrowBox, c);

        add(visualBox);
        add(Box.createHorizontalGlue());
    }

}
