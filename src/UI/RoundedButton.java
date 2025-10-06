package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static core.InternalSettings.GRAY;
import static core.InternalSettings.STROKE_WIDTH;

public class RoundedButton extends JButton {

    private int width, height;
    private BufferedImage img;
    private Color hover;
    private boolean ghost;
    private String text;
    private final boolean hasHotkey;
    private final int borderWidth;

    public RoundedButton(String text, Dimension dim, Color color) {
        this(text, dim, color, STROKE_WIDTH);
    }

    public RoundedButton(String text, Dimension dim, Color color, int borderWidth) {
        this(text, dim, color, borderWidth, false);
    }

    public RoundedButton(String text, Dimension dim, Color color, int borderWidth, boolean hasHotkey) {
        hover = color;
        ghost = false;
        this.text = text;
        this.borderWidth = borderWidth;
        this.hasHotkey = hasHotkey;

        this.width = dim.width;
        this.height = dim.height;

        initialize();
    }

    public RoundedButton(String text, BufferedImage img, Dimension dim, Color color) {
        hover = color;

        this.img = img;
        this.text = text;
        this.width = dim.width;
        this.height = dim.height;
        this.borderWidth = STROKE_WIDTH;
        this.hasHotkey = false;

        initialize();
    }

    public void initialize() {
        setBorder(new CustomBorder(Color.black, borderWidth));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setPreferredSize(new Dimension(this.width, this.height));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseMoved(e);
                setBorder(new CustomBorder(ghost ? GRAY : hover, borderWidth));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseMoved(e);
                setBorder(new CustomBorder(ghost ? GRAY : Color.black, borderWidth));
            }
        });
    }

    public void setColor(Color color) {
        hover = color;
    }

    public void resizeButton(Dimension dim) {
        this.width = dim.width;
        this.height = dim.height;
        setPreferredSize(dim);
        setBorder(new CustomBorder(ghost ? GRAY : Color.black, borderWidth));
    }

    public void enableGhost(boolean flag) {
        ghost = flag;
        setEnabled(!flag);
        setBorder(new CustomBorder(ghost ? GRAY : Color.black, borderWidth));
    }

    public void updateText(String text) {
        this.text = text;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());

        if(!ghost) {
            gr.setColor(GRAY);
            gr.fillRoundRect(1, 1, width - 2, height - 2, CustomBorder.RADIUS, CustomBorder.RADIUS);

            if (img != null)
                gr.drawImage(img, Math.round((width - img.getWidth()) / 2f), Math.round((height - img.getHeight()) / 2f), null);
            else {
                gr.setColor(Color.black);
                gr.drawString(text, (width - g.getFontMetrics().stringWidth(text)) / 2, height / 2 + g.getFontMetrics().getHeight() / 4);
                if(hasHotkey)
                    gr.drawLine(Math.floorDiv(width - g.getFontMetrics().stringWidth(text), 2), Math.floorDiv(height, 2) + Math.floorDiv(g.getFontMetrics().getHeight(), 4) + 2, Math.floorDiv(width - g.getFontMetrics().stringWidth(text), 2) + (int)gr.getFont().getStringBounds(text.substring(0, 1), gr.getFontRenderContext()).getWidth(), Math.floorDiv(height, 2) + Math.floorDiv(g.getFontMetrics().getHeight(), 4) + 2);
            }
        }

        gr.dispose();
    }
}
