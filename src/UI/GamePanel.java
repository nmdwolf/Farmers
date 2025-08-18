package UI;

import core.*;
import objects.GameObject;
import objects.buildings.Building;
import objects.resources.Resource;
import objects.units.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import static javax.swing.MenuSelectionManager.defaultManager;

import static core.GameConstants.*;
import static core.GameConstants.NUMBER_OF_CELLS_IN_VIEW;

public class GamePanel extends JFrame {

    // UI elements
    private JMenu playerMenu, viewMenu, cellMenu;
    private JMenuItem cycleLabel, popLabel;
    private JMenuItem[] playerLabels, resourceLabels;
    private final InfoPanel infoPanel;
    private final ChoicePanel choicePanel;
    private final OperationPanel operationsPanel;
    private final CellPanel cellPanel;
    private final SpringLayout layout;
    private final JLayeredPane contentPanel;
    private final SettingsPanel settingsPanel;
    private final JPanel tintedGlassPanel;

    private int cellWidth, cellHeight, poolSize, screenWidth, screenHeight;
    private final Property<Player> player;
    private int mouseX, mouseY;

    private final Main parent;
    private final Property<Integer> cycle, current;
    private final Property<Boolean> clicked, cursorFlag;
    private final Property<GameObject> selected;
    private final Property<String> audioSource;
    private int travelDistance;
    private Location clickPos, destination;
    private Location[] hoverPath;
    private final Grid cells;

    public GamePanel(Main main, Grid cells,
                     Property<Integer> current, Property<Integer> cycle, Property<Player> player, Property<String> audioSource) {
        parent = main;
        this.cells = cells;
        this.current = current;
        this.cycle = cycle;
        this.player = player;
        this.audioSource = audioSource;

        screenWidth = 0;
        screenHeight = 0;
        cellWidth = 0;
        cellHeight = 0;
        poolSize = 0;
        clicked = new Property<>(false);
        cursorFlag = new Property<>(false);
        selected = new Property<>();
        clickPos = new Location(0, 0, 0);

        operationsPanel = new OperationPanel(cellWidth, cellHeight);
        choicePanel = new ChoicePanel(operationsPanel, cellWidth, cellHeight);
        infoPanel = new InfoPanel();
        cellPanel = new CellPanel(selected);
        settingsPanel = new SettingsPanel(cursorFlag, audioSource);
        layout = new SpringLayout();
        contentPanel = constructContentPanel();
        tintedGlassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(255, 255, 255, 128));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        selected.bind(() -> {
            if(selected.get() != null) {
                infoPanel.update(selected.get());
                infoPanel.setVisible(true);
                choicePanel.update(selected.get(), cycle.get());
            }
            refreshWindow();
        });
    }

    /**
     * Constructs the game frame.
     */
    public void initialize() {
        super.setTitle("Game of Ages");

        addResizeListener(); // Will initialize size parameters on first call

        contentPanel.add(choicePanel, Integer.valueOf(1));
        contentPanel.add(operationsPanel, Integer.valueOf(1));
        contentPanel.add(cellPanel, Integer.valueOf(0));
        contentPanel.add(infoPanel, Integer.valueOf(1));
        contentPanel.add(tintedGlassPanel, Integer.valueOf(2));
        contentPanel.add(settingsPanel, Integer.valueOf(3));

        super.setVisible(true);
        super.setExtendedState(MAXIMIZED_BOTH);
        resetScales();

        initializeCursor();
        addMouseInputs();
        addKeyInputs();
        addMenu();
        addSettingsPanel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        contentPanel.requestFocus();
    }

    public JLayeredPane constructContentPanel() {
        JLayeredPane contentPanel = new JLayeredPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());
                drawObjects(gr);

                gr.setColor(player.get().getAlternativeColor());
                gr.setStroke(new BasicStroke(2));
                Cell p = posToCell(mouseX, mouseY, 0);
                gr.drawRect(p.getX() * cellWidth, p.getY() * cellHeight, cellWidth, cellHeight);
                if (hoverPath != null) {
                    gr.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                            0, new float[]{9}, 0));
                    Cell vp = player.get().getViewPoint();
                    Cell current = cells.get(hoverPath[0]).fetch(-vp.getX(), -vp.getY(), -vp.getZ());
                    for(int i = 1; i < hoverPath.length - 1; i++) {
                        current = current.fetch(hoverPath[i].x(), hoverPath[i].y(), hoverPath[i].z());
                        Location previous = hoverPath[i];
                        Location next = hoverPath[i + 1];

                        if(previous.x() == 1)
                            gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), current.getX() * cellWidth, Math.round((current.getY() + 0.5f) * cellHeight));
                        else if(previous.x() == -1)
                            gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), (current.getX() + 1) * cellWidth, Math.round((current.getY() + 0.5f) * cellHeight));
                        else if(previous.y() == 1)
                            gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), current.getY()  * cellHeight);
                        else if(previous.y() == -1)
                            gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), (current.getY() + 1)  * cellHeight);

                        if(next.x() == 1)
                            gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), (current.getX() + 1) * cellWidth, Math.round((current.getY() + 0.5f) * cellHeight));
                        else if(next.x() == -1)
                            gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), current.getX() * cellWidth, Math.round((current.getY() + 0.5f) * cellHeight));
                        else if(next.y() == 1)
                            gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), (current.getY() + 1)  * cellHeight);
                        else if(next.y() == -1)
                            gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), current.getY() * cellHeight);
                    }
                    current = current.fetch(hoverPath[hoverPath.length - 1].x(), hoverPath[hoverPath.length - 1].y(), hoverPath[hoverPath.length - 1].z());
                    gr.drawString(String.valueOf(travelDistance), Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight));
                }
                gr.dispose();
            }
        };

        contentPanel.setOpaque(false);
        super.setContentPane(contentPanel);
        contentPanel.setLayout(layout);

        return contentPanel;
    }

    public void initializeCursor() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension size = tk.getBestCursorSize(10, 10);
        Image img = new ImageIcon("src/img/Cursor.png").getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
        setCursor(tk.createCustomCursor(img, new Point(size.width / 2, size.height / 2), "customCursor"));
    }

    public void addKeyInputs() {
        // Cycle players on "n" stroke
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('n'), "next_player");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "left");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "right");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "up");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "down");

        contentPanel.getActionMap().put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected.get() == null)
                    hidePanels();
                else
                    cellPanel.setVisible(false);
            }
        });
        contentPanel.getActionMap().put("next_player", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hoverPath = null;
                destination = null;
                selected.set(null);
                current.set(current.get() + 1);

                hidePanels();
                refreshWindow();
            }
        });
        contentPanel.getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.get().getViewPoint().getX() > 0) {
                    player.get().changeViewpoint(player.get().getViewPoint().fetch(-1, 0, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.get().getViewPoint().getX() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    player.get().changeViewpoint(player.get().getViewPoint().fetch(1, 0, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.get().getViewPoint().getY() > 0) {
                    player.get().changeViewpoint(player.get().getViewPoint().fetch(0, -1, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.get().getViewPoint().getY() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    player.get().changeViewpoint(player.get().getViewPoint().fetch(0, 1, 0));
                    refreshWindow();
                }
            }
        });
    }

    public void addMouseInputs() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                mouseX = e.getX();
                mouseY = e.getY();
                Cell mousePos = posToCell(mouseX, mouseY, 0);

                // Moves info panel to make sure that the underlying cells are reachable.
                if(infoPanel != null && selected.get() != null) {
                    layout.putConstraint(SpringLayout.WEST, infoPanel, (mousePos.getX() >= 7) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                    refreshWindow();
                }

                if(selected.get() != null && (selected.get() instanceof Unit) && (hoverPath == null || !mousePos.getLocation().equals(destination))) {
                    Pair<Motion, Location> motion = parent.getShortestAdmissiblePath(selected.get(), mousePos.fetch(player.get().getViewPoint()));
                    if (motion != null && motion.key().length() > 0) {
                        hoverPath = motion.key().getRelativePath();
                        destination = motion.value();
                        travelDistance = motion.key().length();
                    }
                    else {
                        hoverPath = null;
                        destination = null;
                    }
                }

                refreshWindow();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                if(GAME_3D) {
                    if (e.getWheelRotation() != 0 && !player.get().isViewLocked()) {
                        player.get().changeViewpoint(player.get().getViewPoint().fetch(0, 0, e.getWheelRotation()));
                        viewMenu.setText("View: " + (player.get().getViewPoint().getZ()));
                        selected.set(null);

                        hidePanels();
                        refreshWindow();
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                clickPos = player.get().getViewPoint().fetch(posToCell(e.getX(), e.getY(), player.get().getViewPoint().getZ())).getLocation();
                hoverPath = null;
                destination = null;

                // Any existing panels should be hidden on click
                hidePanels();

                /*
                 * left-click events
                 */
                if (SwingUtilities.isLeftMouseButton(e)) {

                    // Unselect GameObject on left click
                    if (clicked.get())
                        selected.set(null);

                    // If clicked on cell with objects, show info panel
                    if (player.get().getObjects().stream().anyMatch(obj -> obj.getCell().getLocation().equals(clickPos))) {
                        cellPanel.update(cells.get(clickPos), player.get());
                        cellPanel.setVisible(true);
                        clicked.set(true);
                    } else
                        clicked.set(false);

                    // right-click events
                } else if (SwingUtilities.isRightMouseButton(e) && (selected.get() instanceof Unit)) {
                    Motion motion = parent.getShortestAdmissiblePath(selected.get(), cells.get(clickPos)).key();

                    hoverPath = null;
                    destination = null;
                    if (cells.get(clickPos).getUnitSpace() - cells.get(clickPos).getUnitOccupied() >= selected.get().getSpace()
                            && motion.length() != 0) {
                        ((Unit) selected.get()).changeEnergy(-motion.length());
                        parent.motionToThread(motion);
                    }
                }

                // Redraw game panel
                refreshWindow();
            }
        };
        contentPanel.addMouseListener(mouseAdapter);
        contentPanel.addMouseMotionListener(mouseAdapter);
        contentPanel.addMouseWheelListener(mouseAdapter);
    }

    public void addResizeListener() {
        // Rescale game elements on screen resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                resetScales();

                tintedGlassPanel.setPreferredSize(new Dimension(screenWidth, screenHeight));
                layout.putConstraint(SpringLayout.WEST, tintedGlassPanel, 0, SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, tintedGlassPanel, 0, SpringLayout.NORTH, contentPanel);

                choicePanel.resize(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, choicePanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, choicePanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, contentPanel);

                operationsPanel.resize(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, operationsPanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, operationsPanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, contentPanel);

                cellPanel.setPreferredSize(new Dimension((NUMBER_OF_CELLS_IN_VIEW - 2) * cellWidth + 40, (NUMBER_OF_CELLS_IN_VIEW - 2) * cellHeight + 40));
                layout.putConstraint(SpringLayout.WEST, cellPanel, cellWidth - 20, SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, cellPanel, cellHeight - 20, SpringLayout.NORTH, contentPanel);

                infoPanel.resize(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, infoPanel, (clickPos.x() - player.get().getViewPoint().getX()) >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, infoPanel, 10, SpringLayout.NORTH, contentPanel);

                settingsPanel.resize(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, settingsPanel, 2 * cellWidth, SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, settingsPanel, cellHeight, SpringLayout.NORTH, contentPanel);

                hidePanels();
                refreshWindow();
            }
        });
    }

    public void addMenu() {
        // Create menus
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        playerMenu = new JMenu("Player: " + player.get().getName());
        viewMenu = new JMenu("View: " + player.get().getViewPoint().getZ());
        cellMenu = new JMenu("Nothing to show");
        JMenu settingsMenu = new JMenu("Settings");
        menubar.add(playerMenu);
        menubar.add(viewMenu);
        menubar.add(cellMenu);
        menubar.add(settingsMenu);

        // Enables mouse hover coloring
        playerMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                playerMenu.doClick();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                defaultManager().clearSelectedPath();
            }
        });
        viewMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                viewMenu.doClick();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                defaultManager().clearSelectedPath();
            }
        });
        cellMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                cellMenu.doClick();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                defaultManager().clearSelectedPath();
            }
        });

        // Initializes resource labels for player and cell menus
        playerLabels = new JMenuItem[Resource.values().length];
        for(int i = 0; i < Resource.values().length; i++) {
            JMenuItem label = new JMenuItem(Resource.values()[i].name + ": " + player.get().getResource(Resource.values()[i]));
            playerMenu.add(label);
            playerLabels[i] = label;
        }
        resourceLabels = new JMenuItem[Resource.values().length];
        for(int i = 0; i < Resource.values().length; i++) {
            JMenuItem label = new JMenuItem(Resource.values()[i].name + ": N/A");
            cellMenu.add(label);
            resourceLabels[i] = label;
        }

        cycleLabel = new JMenuItem("Cycle: " + 1);
        viewMenu.add(cycleLabel);
        popLabel = new JMenuItem("Population: " + player.get().getPop() + "/" + player.get().getPopCap());
        playerMenu.add(popLabel);

        JMenuItem viewItem = new JMenuItem("Audio & Visuals");
        viewItem.addActionListener(e -> {
            settingsPanel.setVisible(true);
            contentPanel.requestFocus();
        });
        settingsMenu.add(viewItem);
    }

    public void addSettingsPanel() {

        tintedGlassPanel.setOpaque(false);

        settingsPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                tintedGlassPanel.setVisible(true);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                tintedGlassPanel.setVisible(false);
            }
        });

        cursorFlag.bind(() -> {
            if(cursorFlag.get())
                initializeCursor();
            else
                setCursor(Cursor.getDefaultCursor());
        });

        settingsPanel.setVisible(false);
    }

    public void showMessagePanel(String text) {

        int[] lines = {1};
        StringBuilder output = new StringBuilder();
        for(char c : text.toCharArray()) {
            output.append(c);
            if(getContentPane().getGraphics().getFontMetrics().stringWidth(output.toString()) >= 2 * cellWidth - 20) {
                output.append("\n");
                lines[0]++;
            }
        }

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);

                gr.setColor(new Color(200, 150, 0, 76));
                gr.fillRoundRect(0, 0, 2 * cellWidth, getHeight(), 10, 10);
                gr.setColor(Color.black);
                CustomMethods.customDrawString(gr, output.toString(), 10, 10);
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                getContentPane().remove(panel);
            }
        });

        panel.setPreferredSize(new Dimension(2 * cellWidth, 50 * lines[0]));
        panel.setBorder(new CustomBorder(Color.black, 2 * cellWidth, 50 * lines[0]));
        panel.setOpaque(false);

        getContentPane().add(panel);
        SpringLayout layout = (SpringLayout) getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, panel, screenWidth - 2 * cellWidth - 50, SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, panel, 50, SpringLayout.NORTH, getContentPane());
    }

    /**
     * Recalculates screen size and rescales derived dimensions
     */
    public void resetScales() {
        screenWidth = getContentPane().getWidth();
        screenHeight = getContentPane().getHeight();
        cellWidth = Math.round(screenWidth / (float)NUMBER_OF_CELLS_IN_VIEW);
        cellHeight = Math.round(screenHeight / (float)NUMBER_OF_CELLS_IN_VIEW);
        poolSize = Math.min(Math.round(cellWidth / 2f), Math.round(cellHeight / 2f));
    }

    /**
     * Draws currently visible/relevant game objects
     * @param gr Graphics object of the game panel
     */
    public void drawObjects(Graphics2D gr) {

        /*
         * Redraw background image
         */
        for(int i = 0; i < NUMBER_OF_CELLS_IN_VIEW -1; i++) {
            gr.fillRect((i + 1) * cellWidth - 1, 0, 1, screenHeight - getInsets().top);
            gr.fillRect( 0,  (i + 1) * cellHeight - 1, screenWidth, 1);
        }

        /*
         * Draw all (visible) cells
         */

        for(int x = 0; x < NUMBER_OF_CELLS_IN_VIEW; x++) {
            for(int y = 0; y < NUMBER_OF_CELLS_IN_VIEW; y++) {
                Cell vp = player.get().getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));
                if (player.get().hasSpotted(cell)) {
                    if (cell.isField()) {
                        gr.setColor(new Color(200, 120, 0, 100));
                        gr.fillRect(x * cellWidth, y * cellHeight, cellWidth - 1, cellHeight - 1);
                    }

                    if (cell.isForest()) {
                        gr.setColor(new Color(20, 150, 20));
//                        gr.fillArc(x * cellWidth - Math.round(poolSize / 2f), y * cellHeight - Math.round(poolSize / 2f), poolSize, poolSize, 270, 90);
                        gr.fillArc(x * cellWidth - poolSize, (y + 1) * cellHeight - poolSize - 1, 2 * poolSize, 2 * poolSize, 0, 90);
                    }

                    if (cell.isRiver()) {
                        gr.setColor(new Color(0, 100, 255));
                        if (cell.getX() < NUMBER_OF_CELLS - 2 && cell.getY() < NUMBER_OF_CELLS - 2 && cell.fetch(1, 0, 0).isRiver() && cell.fetch(0, 1, 0).isRiver() && cell.fetch(1, 1, 0).isRiver())
                            gr.fillRoundRect(Math.round((x + 0.5f) * cellWidth - (poolSize / 2f)), Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), cellWidth + poolSize, cellHeight + poolSize, poolSize, poolSize);
                        else {
                            boolean singleFlag = true;
                            if (cell.getX() < NUMBER_OF_CELLS - 1 && cell.fetch(1, 0, 0).isRiver())
                                gr.fillRect(Math.round((x + 0.5f) * cellWidth), Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), Math.round(cellWidth / 2f) + 1, poolSize);
                            if (cell.getX() > 0 && cell.fetch(-1, 0, 0).isRiver())
                                gr.fillRect(x * cellWidth - 1, Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), Math.round(cellWidth / 2f), poolSize);
                            if (cell.getY() < NUMBER_OF_CELLS - 1 && cell.fetch(0, 1, 0).isRiver()) {
                                gr.fillRect(Math.round((x + 0.5f) * cellWidth - (poolSize / 2f)), Math.round((y + 0.5f) * cellHeight), poolSize, Math.round(cellHeight / 2f));
                                singleFlag = false;
                            }
                            if (cell.getY() > 0 && cell.fetch(0, -1, 0).isRiver()) {
                                gr.fillRect(Math.round((x + 0.5f) * cellWidth - (poolSize / 2f)), y * cellHeight, poolSize, Math.round(cellHeight / 2f));
                                singleFlag = false;
                            }

                            if (singleFlag)
                                gr.fillOval(Math.round((x + 0.5f) * cellWidth - poolSize), Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), poolSize * 2, poolSize);
                            else
                                gr.fillOval(Math.round((x + 0.5f) * cellWidth - (poolSize / 2f)), Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), poolSize, poolSize);
                        }
                    }
                }

                /*
                 * Draw all (visible/relevant) game objects
                 * taking into account their state
                 */
                final AtomicInteger unitCounter = new AtomicInteger();
                final AtomicInteger buildingCounter = new AtomicInteger();

                for(GameObject object : cell.getContent()) {
                    gr.setColor(object == selected.get() ? object.getPlayer().getAlternativeColor() : object.getPlayer().getColor());
                    BufferedImage sprite = object.getSprite(false);
                    if(object instanceof Unit u) {
                        if(sprite != null) {
                            if(object == selected.get())
                                gr.drawImage(CustomMethods.selectedSprite(sprite, gr.getColor()), 5 + x * cellWidth + UNIT_SPRITE_SIZE * unitCounter.get(), y * cellHeight + 10, null);
                            else
                                gr.drawImage(sprite, 5 + x * cellWidth + UNIT_SPRITE_SIZE * unitCounter.get(), y * cellHeight + 10, null);
                        }
                        else {
                            gr.drawString(object.getToken(), 5 + x * cellWidth + 10 * unitCounter.get(), y * cellHeight + 15);
                            if(u.getStatus() == Status.WORKING) {
                                int pieces = u.getCycleLength() / 4;
                                int remainder = u.getCurrentStep() % pieces;
                                int part = u.getCurrentStep() / pieces;
                                int segmentWidth = (int)((float)cellWidth / pieces);
                                int segmentHeight = (int)((float)cellHeight / pieces);

                                if(part > 0) {
                                    gr.drawLine(x * cellWidth, y * cellHeight + 15 + 2, (x + 1) * cellWidth, y * cellHeight + 15 + 2);
                                    if(part > 1) {
                                        gr.drawLine((x + 1) * cellWidth, y * cellHeight + 15 + 2, (x + 1) * cellWidth, (y + 1) * cellHeight + 15 + 2);
                                        if(part > 2) {
                                            gr.drawLine(x * cellWidth, (y + 1) * cellHeight + 15 + 2, (x + 1) * cellWidth, (y + 1) * cellHeight + 15 + 2);
                                            if(part > 3)
                                                gr.drawLine(x * cellWidth, y * cellHeight + 15 + 2, x * cellWidth, (y + 1) * cellHeight + 15 + 2);
                                            else
                                                gr.drawLine(x * cellWidth, (y + 1) * cellHeight + 15 + 2, x * cellWidth, y * cellHeight + 15 + 2 - remainder * segmentHeight);
                                        } else
                                            gr.drawLine((x + 1) * cellWidth, (y + 1) * cellHeight + 15 + 2, (x + 1) * cellWidth - remainder * segmentWidth, (y + 1) * cellHeight + 15 + 2);
                                    } else
                                        gr.drawLine((x + 1) * cellWidth, y * cellHeight + 15 + 2, (x + 1) * cellWidth, y * cellHeight + 15 + 2 + remainder * segmentHeight);
                                } else
                                    gr.drawLine(x * cellWidth, y * cellHeight + 15 + 2, x * cellWidth + remainder * segmentWidth, y * cellHeight + 15 + 2);

                                //gr.drawLine(5 + x * cellWidth + 10 * unitCounter.get(), y * cellHeight + 15 + 2, x * cellWidth + 10 * unitCounter.get() + gr.getFontMetrics().stringWidth(object.getToken()), y * cellHeight + 15 + 2);
                                u.step();
                            }
                        }
                        unitCounter.incrementAndGet();
                    }

                    if(object instanceof Building) {
                        if(sprite != null) {
                            if(object == selected.get())
                                gr.drawImage(CustomMethods.selectedSprite(sprite, gr.getColor()), 5 + x * cellWidth + BUILDING_SPRITE_SIZE * buildingCounter.get(), (y + 1) * cellHeight - BUILDING_SPRITE_SIZE - 10, null);
                            else
                                gr.drawImage(sprite, 5 + x * cellWidth + BUILDING_SPRITE_SIZE * buildingCounter.get(), (y + 1) * cellHeight - BUILDING_SPRITE_SIZE - 10, null);
                        }
                        else {
                            gr.drawString(object.getToken(), 5 + x * cellWidth + 10 * buildingCounter.get(), (y + 1) * cellHeight - gr.getFontMetrics().getHeight() + 5 + 2);
//                                if(b instanceof Worker)
//                                    if (b.checkStatus(CONTRACT))
//                                        gr.drawLine(5 + x * cellWidth + 10 * buildingCounter.get(), (y + 1) * cellHeight - gr.getFontMetrics().getHeight() + 5 + 2, x * cellWidth + 10 * buildingCounter.get() + gr.getFontMetrics().stringWidth(b.getToken()), (y + 1) * cellHeight - gr.getFontMetrics().getHeight() + 5 + 2);
                        }
                        buildingCounter.incrementAndGet();
                    }
                }
            }
        }

        for(int x = -1; x < NUMBER_OF_CELLS_IN_VIEW + 1; x++) {
            for(int y = -1; y < NUMBER_OF_CELLS_IN_VIEW + 1; y++) {
                Cell vp = player.get().getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));

                if(player.get().hasSpotted(cell)){
                    if (cell.getHeatLevel() <= COLD_LEVEL) {
                        gr.setColor(Color.blue);
                        gr.fillOval((x + 1) * cellWidth - 15, y * cellHeight + 10, 5, 5);
                    } else if (cell.getHeatLevel() >= HOT_LEVEL) {
                        gr.setColor(Color.red);
                        gr.fillOval((x + 1) * cellWidth - 15, y * cellHeight + 10, 5, 5);
                    }
                }

                if (!player.get().hasSpotted(cell)) {
                    gr.setColor(Color.lightGray);
                    gr.fillRect(x * cellWidth - 1, y * cellHeight - 1, cellWidth + 1, cellHeight + 1);
                } else if (!player.get().hasDiscovered(cell)) {
                    gr.setColor(new Color(192, 192, 192, 200));
                    gr.fillRect(x * cellWidth - 1, y * cellHeight - 1, cellWidth + 1, cellHeight + 1);
                }
            }
        }
    }

    /**
     * Refreshes the game window components that are not drawn (menus, ...)
     */
    public void refreshWindow() {
        playerMenu.setText("Player: " + player.get().getName());
        cycleLabel.setText("Cycle: " + cycle.get());

        popLabel.setText("Population: " + player.get().getPop() + "/" + player.get().getPopCap());

        for(int i = 0; i < Resource.values().length; i++)
            playerLabels[i].setText(Resource.values()[i].name + ": " + player.get().getResource(Resource.values()[i]));

        Cell cell = cells.get(clickPos);
        for(int i = 0; i < Resource.values().length; i++)
            resourceLabels[i].setText(Resource.values()[i].name + ": " + cell.getResource(Resource.values()[i]));

        cellMenu.setText("Space: " + cell.getUnitOccupied() + "/" + cell.getUnitSpace() +
                " | " + cell.getBuildingOccupied() + "/" + cell.getBuildingSpace());

        contentPanel.repaint();
        contentPanel.revalidate();
    }

    public void hidePanels() {
        infoPanel.setVisible(false);
        operationsPanel.setVisible(false);
        choicePanel.setVisible(false);
        cellPanel.setVisible(false);
        settingsPanel.setVisible(false);
    }

    /**
     * Converts screen coordinates to a core.Location object
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param l elevation
     * @return core.Location representation of the coordinates
     */
    private Cell posToCell(int x, int y, int l) {
        return cells.get(new Location(Math.floorDiv(x, cellWidth), Math.floorDiv(y, cellHeight), l));
    }
}
