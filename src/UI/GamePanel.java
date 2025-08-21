package UI;

import core.*;
import objects.GameObject;
import objects.resources.Resource;
import objects.units.Unit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

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
    private final OperationsPanel operationsPanel;
    private final CellPanel cellPanel;
    private final SpringLayout layout;
    private final JLayeredPane contentPanel;
    private final SettingsPanel settingsPanel;
    private final JPanel tintedGlassPanel;

    private int cellWidth, cellHeight, poolSize, screenWidth, screenHeight;
    @NotNull private final Property<Player> player;
    private int mouseX, mouseY;

    private final Main parent;
    @NotNull private final Property<Integer> cycle, current;
    @NotNull private final Property<Boolean> clicked, cursorFlag;
    @NotNull private final Property<GameObject> selected;
    private int travelDistance;
    private Location clickPos, destination;
    @NotNull private final Property<Location[]> hoverPath;
    private final Grid cells;

    public GamePanel(Main main, Grid cells,
                     @NotNull Property<Integer> current, @NotNull Property<Integer> cycle, @NotNull Property<Player> player,
                     @NotNull Property<String> audioSource, @NotNull Property<Boolean> playMusic, @NotNull Property<Boolean> shuffleMusic) {
        parent = main;
        this.cells = cells;
        this.current = current;
        this.cycle = cycle;
        this.player = player;

        screenWidth = 1;
        screenHeight = 1;
        cellWidth = 1;
        cellHeight = 1;
        poolSize = 1;
        clicked = new Property<>(false);
        cursorFlag = new Property<>(true);
        selected = new Property<>();
        Property<Boolean> cellArrowProperty = new Property<>(SHOW_CELL_ARROWS);
        clickPos = new Location(0, 0, 0);
        hoverPath = new Property<>();

        operationsPanel = new OperationsPanel(cellWidth, cellHeight);
        choicePanel = new ChoicePanel(operationsPanel, cellWidth, cellHeight, event -> hidePanels());
        infoPanel = new InfoPanel();
        cellPanel = new CellPanel(selected, cellArrowProperty);
        settingsPanel = new SettingsPanel(cursorFlag, audioSource, playMusic, shuffleMusic, cellArrowProperty);
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
            selected.get().ifPresentOrElse(obj -> {
                infoPanel.update(obj);
                infoPanel.setVisible(true);
                choicePanel.update(obj, cycle.getFlat());
            }, () -> {
                choicePanel.setVisible(false);
                infoPanel.setVisible(false);
            });
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

        setVisible(true);
        setExtendedState(MAXIMIZED_BOTH);
        resetScales();

        setCustomCursor();
        addMouseInputs();
        addKeyInputs();
        addMenu();
        initializeSettingsPanel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        contentPanel.requestFocus();
    }

    public JLayeredPane constructContentPanel() {
        JLayeredPane contentPanel = new JLayeredPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(cellWidth > 0 && cellHeight > 0) {
                    Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D) g.create());

                    drawCells(gr);
                    drawDetails(gr);

                    gr.setColor(player.getFlat().getAlternativeColor());
                    gr.setStroke(new BasicStroke(2));
                    Cell p = posToCell(mouseX, mouseY, 0);
                    gr.drawRect(p.getX() * cellWidth, p.getY() * cellHeight, cellWidth, cellHeight);
                    hoverPath.get().ifPresent(loc -> drawPath(gr));
                    gr.dispose();
                }
            }
        };

        contentPanel.setOpaque(false);
        super.setContentPane(contentPanel);
        contentPanel.setLayout(layout);

        return contentPanel;
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
                selected.get().ifPresentOrElse(obj -> hidePanels(), () -> cellPanel.setVisible(false));
            }
        });
        contentPanel.getActionMap().put("next_player", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hoverPath.set(null);
                destination = null;
                selected.set(null);
                current.set(current.getFlat() + 1);

                hidePanels();
                refreshWindow();
            }
        });
        contentPanel.getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.getFlat().getViewPoint().getX() > 0) {
                    player.getFlat().changeViewpoint(player.getFlat().getViewPoint().fetch(-1, 0, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.getFlat().getViewPoint().getX() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    player.getFlat().changeViewpoint(player.getFlat().getViewPoint().fetch(1, 0, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.getFlat().getViewPoint().getY() > 0) {
                    player.getFlat().changeViewpoint(player.getFlat().getViewPoint().fetch(0, -1, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.getFlat().getViewPoint().getY() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    player.getFlat().changeViewpoint(player.getFlat().getViewPoint().fetch(0, 1, 0));
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
                selected.get().ifPresent( obj -> {
                    if(infoPanel != null) {
                        layout.putConstraint(SpringLayout.WEST, infoPanel, (mousePos.getX() >= 7) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                        refreshWindow();
                    }

                    if((obj instanceof Unit) && (hoverPath.get().isEmpty() || !mousePos.getLocation().equals(destination)) && !mousePos.fetch(player.getFlat().getViewPoint()).isEndOfMap()) {
                        Pair<Motion, Location> motion = parent.getShortestAdmissiblePath(obj,
                                mousePos.fetch(player.getFlat().getViewPoint()));
                        if (motion != null && motion.key().length() > 0) {
                            hoverPath.set(motion.key().getRelativePath());
                            destination = motion.value();
                            travelDistance = motion.key().length();
                        }
                        else {
                            hoverPath.set(null);
                            destination = null;
                        }
                    }
                });

                refreshWindow();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                if(GAME_3D) {
                    if (e.getWheelRotation() != 0 && !player.getFlat().isViewLocked()) {
                        player.getFlat().changeViewpoint(player.getFlat().getViewPoint().fetch(0, 0,
                                e.getWheelRotation()));
                        viewMenu.setText("View: " + (player.getFlat().getViewPoint().getZ()));
                        selected.set(null);

                        hidePanels();
                        refreshWindow();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Location oldPos = clickPos;
                clickPos = player.getFlat().getViewPoint().fetch(posToCell(e.getX(), e.getY(),
                        player.getFlat().getViewPoint().getZ())).getLocation();

                if(!cells.get(clickPos).isEndOfMap()) {
                    hoverPath.set(null);
                    destination = null;

                    // Any existing panels should be hidden on click
                    hidePanels();

                    /*
                     * left-click events
                     */
                    if (SwingUtilities.isLeftMouseButton(e)) {

                        // Unselect GameObject on left click
                        if (clicked.getFlat())
                            selected.set(null);

                        // If clicked on cell with objects, show info panel
                        if (player.getFlat().getObjects().stream().anyMatch(obj -> obj.getCell().getLocation().equals(clickPos))) {
                            cellPanel.update(cells.get(clickPos), player.getFlat());
                            cellPanel.setVisible(true);
                            clicked.set(true);
                        } else
                            clicked.set(false);

                        // right-click events
                    } else if (SwingUtilities.isRightMouseButton(e)) {

                        selected.get().ifPresent(obj -> {
                            if(obj instanceof Unit unit) {
                                Pair<Motion, Location> motion =
                                        parent.getShortestAdmissiblePath(unit, cells.get(clickPos));

                                if(motion != null) {
                                    hoverPath.set(null);
                                    destination = null;
                                    if (cells.get(clickPos).getUnitSpace() - cells.get(clickPos).getUnitOccupied() >= unit.getSpace()
                                            && motion.key().length() != 0) {
                                        unit.changeEnergy(-motion.key().length());
                                        parent.motionToThread(motion.key());
                                    }
                                }
                            }
                        });
                    }

                    // Redraw game panel
                    refreshWindow();
                } else
                    clickPos = oldPos;
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

                choicePanel.resizePanel(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, choicePanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, choicePanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, contentPanel);

                operationsPanel.resizePanel(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, operationsPanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, operationsPanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, contentPanel);

                cellPanel.setPreferredSize(new Dimension((NUMBER_OF_CELLS_IN_VIEW - 2) * cellWidth + 40, (NUMBER_OF_CELLS_IN_VIEW - 2) * cellHeight + 40));
                layout.putConstraint(SpringLayout.WEST, cellPanel, cellWidth - 20, SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, cellPanel, cellHeight - 20, SpringLayout.NORTH, contentPanel);

                infoPanel.resize(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, infoPanel,
                        (clickPos.x() - player.getFlat().getViewPoint().getX()) >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
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
        playerMenu = new JMenu("Player: " + player.getFlat().getName());
        viewMenu = new JMenu("View: " + player.getFlat().getViewPoint().getZ());
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
            JMenuItem label =
                    new JMenuItem(Resource.values()[i].name + ": " + player.getFlat().getResource(Resource.values()[i]));
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
        popLabel = new JMenuItem("Population: " + player.getFlat().getPop() + "/" + player.getFlat().getPopCap());
        playerMenu.add(popLabel);

        JMenuItem viewItem = new JMenuItem("Audio & Visuals");
        viewItem.addActionListener(e -> {
            settingsPanel.setVisible(true);
            contentPanel.requestFocus();
        });
        settingsMenu.add(viewItem);
    }

    public void initializeSettingsPanel() {

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
            if(cursorFlag.getFlat())
                setCustomCursor();
            else
                setCursor(Cursor.getDefaultCursor());
        });
        cursorFlag.set(CUSTOM_CURSOR);

        settingsPanel.setVisible(false);
    }

    public void setCustomCursor() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension size = tk.getBestCursorSize(10, 10);
        Image img = new ImageIcon("src/img/Cursor.png").getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
        setCursor(tk.createCustomCursor(img, new Point(size.width / 2, size.height / 2), "customCursor"));
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
                CustomMethods.drawString(gr, output.toString(), 10, 10);
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
     * Draws cells
     * @param gr Graphics object of the game panel
     */
    private void drawCells(Graphics2D gr) {

        /*
         * Redraw background image
         */
        for (int i = 0; i < NUMBER_OF_CELLS_IN_VIEW - 1; i++) {
            gr.fillRect((i + 1) * cellWidth - 1, 0, 1, screenHeight - getInsets().top);
            gr.fillRect(0, (i + 1) * cellHeight - 1, screenWidth, 1);
        }

        /*
         * Draw all (visible) cells
         */

        for (int x = 0; x < NUMBER_OF_CELLS_IN_VIEW; x++) {
            for (int y = 0; y < NUMBER_OF_CELLS_IN_VIEW; y++) {
                Cell vp = player.getFlat().getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));

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
        }
    }

    /**
     * Draws path for selected unit
     * @param gr Graphics object of the game panel
     */
    private void drawPath(Graphics2D gr) {
        gr.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{9}, 0));

        hoverPath.get().ifPresent(path -> {
            Cell vp = player.getFlat().getViewPoint();
            Cell current = cells.get(path[0]).fetch(-vp.getX(), -vp.getY(), -vp.getZ());
            for (int i = 1; i < path.length - 1; i++) {
                current = current.fetch(path[i].x(), path[i].y(), path[i].z());
                Location previous = path[i];
                Location next = path[i + 1];

                if (previous.x() == 1)
                    gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), current.getX() * cellWidth, Math.round((current.getY() + 0.5f) * cellHeight));
                else if (previous.x() == -1)
                    gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), (current.getX() + 1) * cellWidth, Math.round((current.getY() + 0.5f) * cellHeight));
                else if (previous.y() == 1)
                    gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), current.getY() * cellHeight);
                else if (previous.y() == -1)
                    gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), (current.getY() + 1) * cellHeight);

                if (next.x() == 1)
                    gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), (current.getX() + 1) * cellWidth, Math.round((current.getY() + 0.5f) * cellHeight));
                else if (next.x() == -1)
                    gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), current.getX() * cellWidth, Math.round((current.getY() + 0.5f) * cellHeight));
                else if (next.y() == 1)
                    gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), (current.getY() + 1) * cellHeight);
                else if (next.y() == -1)
                    gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), current.getY() * cellHeight);
            }

            Location finalMove = path[path.length - 1];
            current = current.fetch(finalMove.x(), finalMove.y(), finalMove.z());
            gr.drawString(String.valueOf(travelDistance), Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight));
        });
    }

    /**
     * Draws currently visible/relevant game objects
     * @param gr Graphics object of the game panel
     */
    private void drawDetails(Graphics2D gr) {

        for(int x = 0; x < NUMBER_OF_CELLS_IN_VIEW; x++) {
            for(int y = 0; y < NUMBER_OF_CELLS_IN_VIEW; y++) {
                Cell vp = player.getFlat().getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));

                gr.setColor(player.getFlat().getColor());
                List<Player> playersInCell = new ArrayList<>(cell.getContent().stream().map(GameObject::getPlayer).distinct().toList());
                if(playersInCell.contains(player.getFlat())) {
                    gr.fillOval(x * cellWidth + 5, y * cellHeight + 5, 10, 10);
                    playersInCell.remove(player.getFlat());
                }

                if(cell.isEndOfMap()) {
                    gr.setColor(Color.black);
                    gr.fillRect(x * cellWidth - 1, y * cellHeight - 1, cellWidth + 10, cellHeight + 1);
                } else if (!player.getFlat().hasSpotted(cell)) {
                    gr.setColor(Color.lightGray);
                    gr.fillRect(x * cellWidth - 1, y * cellHeight - 1, cellWidth + 1, cellHeight + 1);
                } else if (!player.getFlat().hasDiscovered(cell)) {
                    gr.setColor(new Color(192, 192, 192, 200));
                    gr.fillRect(x * cellWidth - 1, y * cellHeight - 1, cellWidth + 1, cellHeight + 1);
                } else {
                    if (cell.getHeatLevel() <= COLD_LEVEL)
                        gr.drawImage(CLOUD, (x + 1) * cellWidth - CLOUD.getWidth() - 5,
                                y * cellHeight + 5, null);
                    else if (cell.getHeatLevel() >= HOT_LEVEL)
                        gr.drawImage(SUN, (x + 1) * cellWidth - SUN.getWidth() - 5,
                                y * cellHeight + 5, null);

                    for(int i = 0; i < playersInCell.size(); i++) {
                        gr.setColor(playersInCell.get(i).getColor());
                        gr.drawOval(x * cellWidth + 5 + 15 * (i + 1), y * cellHeight + 5, 10, 10);
                    }
                }
            }
        }
    }

    /**
     * Refreshes the game window components that are not drawn (menus, ...)
     */
    public void refreshWindow() {
        playerMenu.setText("Player: " + player.getFlat().getName());
        cycleLabel.setText("Cycle: " + cycle.get());

        popLabel.setText("Population: " + player.getFlat().getPop() + "/" + player.getFlat().getPopCap());

        for(int i = 0; i < Resource.values().length; i++)
            playerLabels[i].setText(Resource.values()[i].name + ": " + player.getFlat().getResource(Resource.values()[i]));

        Cell cell = cells.get(clickPos);
        for(int i = 0; i < Resource.values().length; i++)
            resourceLabels[i].setText(Resource.values()[i].name + ": " + cell.getResource(Resource.values()[i]));

        cellMenu.setText("Space: " + cell.getUnitOccupied() + "/" + cell.getUnitSpace() +
                " | " + cell.getBuildingOccupied() + "/" + cell.getBuildingSpace());

        cellPanel.update();
        contentPanel.revalidate();
        contentPanel.repaint();
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
