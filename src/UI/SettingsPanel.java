package UI;

import core.Property;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class SettingsPanel extends JPanel {

    private final Property<Boolean> cursorFlag;
    private final Property<String> audioSource;

    public SettingsPanel(Property<Boolean> cursor, Property<String> audioSource) {
        cursorFlag = cursor;
        this.audioSource = audioSource;
        initialize();
    }

    private void initialize() {
        setLayout(new GridLayout(0, 1, 5, 5));
        setOpaque(false);

        JPanel audioBox = new JPanel();
        audioBox.setOpaque(false);
        audioBox.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        audioBox.add(new JLabel("<html><u>Audio</u></html>"), c);
        JLabel audioLabel = new JLabel("Audio source: ");
        audioLabel.setOpaque(false);
        c.gridx = 0;
        c.gridy = 1;
        audioBox.add(audioLabel, c);
        JTextField audioInput = new JTextField(audioSource.get(), 1);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.9;
        audioBox.add(audioInput, c);
        JButton applyAudio = new JButton("Load");
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = .2;
        audioBox.add(applyAudio, c);
        applyAudio.addActionListener(evt -> audioSource.set(audioInput.getText()));
        audioBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(audioBox);
        add(new JSeparator(SwingConstants.HORIZONTAL));

        Box visualBox = Box.createVerticalBox();
        visualBox.add(new JLabel("<html><u>Visuals</u></html>"));
        JCheckBox cursorBox = new JCheckBox("Custom cursor enabled: ");
        cursorBox.setHorizontalTextPosition(SwingConstants.LEFT);
        cursorBox.setSelected(true);
        cursorBox.setOpaque(false);
        cursorBox.addChangeListener(e -> cursorFlag.set(cursorBox.isSelected()));
        visualBox.add(cursorBox);
        visualBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(visualBox);
        add(new JSeparator(SwingConstants.HORIZONTAL));

        addMouseListener(new MouseAdapter() {});
    }

    public void resize(int cellWidth, int cellHeight) {
        setPreferredSize(new Dimension(4 * cellWidth, 5 * cellHeight));
        setBorder(new CustomBorder(Color.BLACK, getWidth(), getHeight()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        gr.setColor(Color.lightGray);
        gr.fillRect(2, 2, getWidth() - 4, getHeight() - 4);
        gr.dispose();
    }
}
