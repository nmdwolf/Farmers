package UI;

import core.*;
import core.contracts.AttackContract;
import core.player.Player;
import objects.Aggressive;
import objects.GameObject;
import core.Status;
import objects.buildings.TownHall;
import objects.units.Unit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static javax.swing.MenuSelectionManager.defaultManager;

import static core.GameConstants.*;
import static core.GameConstants.NUMBER_OF_CELLS_IN_VIEW;

public class GameFrame extends JFrame {

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
    private Font font;

    private int screenWidth, screenHeight;
    private float cellWidth, cellHeight, poolSize;
    @NotNull private final Property<Player> player;
    private int mouseX, mouseY;

    @NotNull private final Main parent;
    @NotNull private final Property<Integer> cycle, current;
    @NotNull private final Property<Boolean> clicked, cursorFlag;
    @NotNull private final Property<Main.GameState> gameState;
    @NotNull private final Property<InfoPanel.Mode> showResources;
    @NotNull private final Property<GameObject> selected;
    @NotNull private final Property<Pair<GameObject, Boolean>> target;
    private int travelDistance;
    private Location clickPos, destination;
    @NotNull private final Property<Location[]> hoverPath;
    @NotNull private final Grid cells;
    private boolean gps;
    private final HashSet<Motion> motions;

    public GameFrame(@NotNull Main main, @NotNull Grid cells, @NotNull Property<Integer> cycle, @NotNull Property<Integer> current, @NotNull Property<Player> player, @NotNull Property<String> audioSource, @NotNull Property<Boolean> playMusic, @NotNull Property<Boolean> shuffleMusic, @NotNull Property<Main.GameState> gameState) {
        parent = main;
        this.cells = cells;
        this.cycle = cycle;
        this.current = current;
        this.player = player;

        // Initial values to avoid 0 issues
        screenWidth = INITIAL_SCREEN_SIZE;
        screenHeight = INITIAL_SCREEN_SIZE;
        cellWidth = (float)INITIAL_SCREEN_SIZE / NUMBER_OF_CELLS_IN_VIEW;
        cellHeight = (float)INITIAL_SCREEN_SIZE / NUMBER_OF_CELLS_IN_VIEW;
        poolSize = cellWidth;

        clicked = new Property<>(false);
        cursorFlag = new Property<>(true);
        selected = new Property<>();
        target = new Property<>(new Pair<>(null, false));
        Property<Boolean> cellArrowProperty = new Property<>(SHOW_CELL_ARROWS);
        clickPos = new Location(0, 0, 0);
        hoverPath = new Property<>();
        showResources = new Property<>(InfoPanel.Mode.OBJECT);
        this.gameState = gameState;
        gps = false;
        motions = new HashSet<>();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setFont();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        operationsPanel = new OperationsPanel(cellWidth, cellHeight);
        choicePanel = new ChoicePanel(operationsPanel, cellWidth, cellHeight, _ -> hidePanels(true), showResources, target);
        infoPanel = new InfoPanel(selected);
        cellPanel = new CellPanel(cells.get(new Location(0, 0, 0)), player.getUnsafe(), selected, target, cellArrowProperty, gameState);
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

        showResources.bind(prop -> selected.get().ifPresent(_ -> infoPanel.update(prop)));
        selected.bind(_ -> {
            hidePanels(false);
            selected.get().ifPresent(obj -> {
                showResources.set(InfoPanel.Mode.OBJECT);
                infoPanel.setVisible(true);
                if(obj.getPlayer().equals(player.getUnsafe()))
                    choicePanel.update(obj, cycle.getUnsafe());
            });
            refreshWindow();
        });
        target.bind(pair -> {
            if (!pair.value()) {
                selected.ifPresent(fighter ->
                    ((Unit<?>) fighter).addContract(new AttackContract(fighter, ((Aggressive) fighter).getAttackCost(), pair.key())));
                selected.set(null);
            }
        });
    }

    public void updateContent(boolean forceReload) {
        cellPanel.updateContent(forceReload);
        SwingUtilities.invokeLater(() -> {
            refreshWindow();
            contentPanel.revalidate();
            contentPanel.repaint();
            for (String text : player.getUnsafe().getMessages())
                showMessagePanel(text);
        });
    }

    /**
     * Constructs the game frame.
     */
    public void initialize() {
        super.setTitle("The Game of Ages");

        addResizeListener();
        setSize(new Dimension(INITIAL_SCREEN_SIZE, INITIAL_SCREEN_SIZE));

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

    /**
     * Sets the font of the entire application.
     */
    private void setFont() {
        InputStream is = GameFrame.class.getResourceAsStream("/UI/fonts/FiraSans-Regular.otf");
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, is);
            font = font.deriveFont(Font.PLAIN, 14);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            is.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put (key, new FontUIResource(font));
//            new FontUIResource("Berlin Sans FB", Font.PLAIN,14)
        }
    }

    private JLayeredPane constructContentPanel() {
        JLayeredPane contentPanel = new JLayeredPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(cellWidth > 0 && cellHeight > 0) {
                    Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D) g.create());

                    drawCells(gr);
                    drawDetails(gr);

                    gr.setColor(player.getUnsafe().getAlternativeColor());
                    gr.setStroke(new BasicStroke(2));
                    Cell p = posToCellCoord(mouseX, mouseY, 0);
                    gr.drawRect((int)(p.getX() * cellWidth), (int)(p.getY() * cellHeight), (int)cellWidth, (int)cellHeight);
                    drawPaths(gr);

                    gr.dispose();
                }
            }
        };

        contentPanel.setOpaque(false);
        super.setContentPane(contentPanel);
        contentPanel.setLayout(layout);

        return contentPanel;
    }

    /**
     * Adds {@code KeyListener}s to the game window.
     */
    private void addKeyInputs() {
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('n'), "next_player");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "left");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "right");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "up");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "down");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                InputEvent.CTRL_DOWN_MASK), "gps");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                InputEvent.CTRL_DOWN_MASK), "missions");

        contentPanel.getActionMap().put("missions", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMessagePanel(player.getUnsafe().getCurrentMission());
            }
        });
        contentPanel.getActionMap().put("gps", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gps = !gps;
            }
        });
        contentPanel.getActionMap().put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selected.get().ifPresentOrElse(_ -> hidePanels(true), () -> cellPanel.setVisible(false));
            }
        });
        contentPanel.getActionMap().put("next_player", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hoverPath.set(null);
                destination = null;
                selected.set(null);
                current.set(current.getUnsafe() + 1);
                cellPanel.generateCycleAnimation();
                gameState.set(Main.GameState.ANIMATING);
            }
        });
        contentPanel.getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.getUnsafe().getViewPoint().getX() > 0) {
                    player.getUnsafe().changeViewpoint(player.getUnsafe().getViewPoint().fetch(-1, 0, 0));
                    if(cellPanel.isVisible())
                        cellPanel.updateContent(cellPanel.getCurrentCell().fetch(-1, 0, 0), player.getUnsafe());
                }
            }
        });
        contentPanel.getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.getUnsafe().getViewPoint().getX() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    player.getUnsafe().changeViewpoint(player.getUnsafe().getViewPoint().fetch(1, 0, 0));
                    if(cellPanel.isVisible())
                        cellPanel.updateContent(cellPanel.getCurrentCell().fetch(1, 0, 0), player.getUnsafe());
                }
            }
        });
        contentPanel.getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.getUnsafe().getViewPoint().getY() > 0) {
                    player.getUnsafe().changeViewpoint(player.getUnsafe().getViewPoint().fetch(0, -1, 0));
                    if(cellPanel.isVisible())
                        cellPanel.updateContent(cellPanel.getCurrentCell().fetch(0, -1, 0), player.getUnsafe());
                }
            }
        });
        contentPanel.getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(player.getUnsafe().getViewPoint().getY() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    player.getUnsafe().changeViewpoint(player.getUnsafe().getViewPoint().fetch(0, 1, 0));
                    if(cellPanel.isVisible())
                        cellPanel.updateContent(cellPanel.getCurrentCell().fetch(0, 1, 0), player.getUnsafe());
                }
            }
        });
    }

    /**
     * Adds {@code MouseListener}s to the game window.
     */
    private void addMouseInputs() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                mouseX = e.getX(); // TODO Merge clickPos and mousePos variables!
                mouseY = e.getY();
                Cell mousePos = posToCellCoord(mouseX, mouseY, player.getUnsafe().getViewPoint().getZ());

                // Moves info panel to make sure that the underlying cells are reachable.
                selected.get().ifPresent( obj -> {
                    if(infoPanel != null)
                        layout.putConstraint(SpringLayout.WEST, infoPanel, (mousePos.getX() > (NUMBER_OF_CELLS_IN_VIEW - 4)) ? 10 : (int)(screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);

                    if((obj instanceof Unit) && (hoverPath.get().isEmpty() || !mousePos.getLocation().equals(destination)) && !mousePos.fetch(player.getUnsafe().getViewPoint().getLocation()).isEndOfMap()) {
                        Pair<Motion, Location> motion = parent.getShortestAdmissiblePath(obj, mousePos.fetch(player.getUnsafe().getViewPoint().getLocation()));

                        if (motion != null && motion.key().length() > 0) {
                            hoverPath.set(motion.key().getRelativePath());
                            destination = motion.value();
                            travelDistance = motion.key().length();
                        } else {
                            hoverPath.set(null);
                            destination = null;
                        }
                    }
                });
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                if(GAME_3D) {
                    if (e.getWheelRotation() != 0 && !player.getUnsafe().isViewLocked()) {
                        player.getUnsafe().changeViewpoint(player.getUnsafe().getViewPoint().fetch(0, 0,
                                e.getWheelRotation()));
                        viewMenu.setText("View: " + (player.getUnsafe().getViewPoint().getZ()));
                        selected.set(null);

                        hidePanels(true);
                        refreshWindow();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Location oldPos = clickPos;
                clickPos = player.getUnsafe().getViewPoint().fetch(posToCellCoord(e.getX(), e.getY(),
                        player.getUnsafe().getViewPoint().getZ()).getLocation()).getLocation();

                if(!cells.get(clickPos).isEndOfMap()) {
                    hoverPath.set(null);
                    destination = null;

                    // Any existing panels should be hidden on click
                    hidePanels(true);

                    /*
                     * left-click events
                     */
                    if (SwingUtilities.isLeftMouseButton(e)) {

                        // Unselect GameObject on left click
                        if (clicked.getUnsafe())
                            selected.set(null);

                        // If clicked on cell with objects, show info panel
                        // TODO Change this check to cells within view distance
                        if (player.getUnsafe().getObjects().stream().anyMatch(obj -> obj.getCell().getLocation().equals(clickPos))) {
                            cellPanel.updateContent(cells.get(clickPos), player.getUnsafe());
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
                                    if (cells.get(clickPos).getUnitSpace() - cells.get(clickPos).getUnitOccupied() >= unit.getSize()
                                            && motion.key().length() != 0) {
                                        unit.changeEnergy(-motion.key().length());
                                        motionToThread(motion.key());
                                    }
                                }
                            }
                        });
                        selected.set(null);
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

    /**
     * Adds a {@code ComponentListener} to the game window that reacts to window resizing.
     */
    private void addResizeListener() {
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
                layout.putConstraint(SpringLayout.NORTH, choicePanel, (int)(screenHeight - 3 * cellHeight), SpringLayout.NORTH, contentPanel);

                operationsPanel.resizePanel(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, operationsPanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, operationsPanel, (int)(screenHeight - 3 * cellHeight), SpringLayout.NORTH, contentPanel);

                cellPanel.setPreferredSize(new Dimension((int)((NUMBER_OF_CELLS_IN_VIEW - 2) * cellWidth + 40), ( int)((NUMBER_OF_CELLS_IN_VIEW - 2) * cellHeight + 40)));
                layout.putConstraint(SpringLayout.WEST, cellPanel, (int)(cellWidth - 20), SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, cellPanel, (int)(cellHeight - 20), SpringLayout.NORTH, contentPanel);

                infoPanel.resizePanel(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, infoPanel,
                        (clickPos.x() - player.getUnsafe().getViewPoint().getX()) >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f) ? 10 : (int)(screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, infoPanel, 10, SpringLayout.NORTH, contentPanel);

                settingsPanel.resizePanel(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, settingsPanel, (int)(2 * cellWidth), SpringLayout.WEST, contentPanel);
                layout.putConstraint(SpringLayout.NORTH, settingsPanel, (int)cellHeight, SpringLayout.NORTH, contentPanel);

                hidePanels(true); // little cheat while setting up the game frame (DO NOT REMOVE!!)
                refreshWindow();
            }
        });
    }

    /**
     * Adds menus to the game window.
     * These are responsible for listing the current Player's stats, the selected Cell's stats and providing access to other functionalities such as settings.
     */
    private void addMenu() {
        // Create menus
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        playerMenu = new JMenu("Player: " + player.getUnsafe().getName());
        viewMenu = new JMenu("View: " + player.getUnsafe().getViewPoint().getZ());
        cellMenu = new JMenu("Nothing to show");
        JMenu settingsMenu = new JMenu("Settings");
        menubar.add(playerMenu);
        menubar.add(viewMenu);
        menubar.add(cellMenu);
        menubar.add(settingsMenu);

        MouseAdapter menuEntered = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                ((JMenu)e.getComponent()).doClick();
            }
        };

        MouseAdapter menuLeft = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseExited(e);
                defaultManager().clearSelectedPath();
            }
        };

        playerMenu.addMouseListener(menuEntered);
        viewMenu.addMouseListener(menuEntered);
        cellMenu.addMouseListener(menuEntered);
        settingsMenu.addMouseListener(menuEntered);
        getContentPane().addMouseListener(menuLeft);

        // Initializes resource labels for player and cell menus
        String[] resources = player.getUnsafe().getResources().keySet().toArray(String[]::new);
        playerLabels = new JMenuItem[resources.length];
        for(int i = 0; i < resources.length; i++) {
            JMenuItem label =
                    new JMenuItem(resources[i] + ": " + player.getUnsafe().getResource(resources[i]));
            playerMenu.add(label);
            playerLabels[i] = label;
        }
        resourceLabels = new JMenuItem[resources.length];
        for(int i = 0; i < resources.length; i++) {
            JMenuItem label = new JMenuItem(resources[i] + ": N/A");
            cellMenu.add(label);
            resourceLabels[i] = label;
        }

        cycleLabel = new JMenuItem("Cycle: " + 1);
        viewMenu.add(cycleLabel);
        popLabel = new JMenuItem("Population: " + player.getUnsafe().getPop() + "/" + player.getUnsafe().getPopCap());
        playerMenu.add(popLabel);

        JMenuItem viewItem = new JMenuItem("Audio & Visuals");
        viewItem.addActionListener(_ -> {
            settingsPanel.setVisible(true);
            contentPanel.requestFocus();
        });
        settingsMenu.add(viewItem);
    }

    /**
     * Adds an {@code InfoPanel} to the game window.
     */
    private void initializeSettingsPanel() {

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

        cursorFlag.bind(prop -> {
            if(prop)
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
        int lines = 1; // TODO Implement dynamic box height based on number of lines of text.
        StringBuilder output = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for(char c : text.toCharArray()) {
            temp.append(c);
            if(font.getStringBounds(temp.toString(),
                    ((Graphics2D)getContentPane().getGraphics()).getFontRenderContext()).getWidth() >= (2 * cellWidth - 4 - 4 - 20)) {
                output.append(temp);
                output.append("\n");
                temp = new StringBuilder();
                lines++;
            }
        }
        output.append(temp);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());

                gr.setColor(new Color(200, 150, 0, 76));
                gr.fillRoundRect(0, 0, (int)(2 * cellWidth), getHeight(), 10, 10);
                gr.setColor(Color.black);
                CustomMethods.drawString(gr, output.toString(), 10, 10);

                gr.dispose();
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                getContentPane().remove(panel);
            }
        });

        panel.setPreferredSize(new Dimension((int)(2 * cellWidth), 50 * lines));
        panel.setBorder(new CustomBorder(Color.black, (int)(2 * cellWidth), 50 * lines));
        panel.setOpaque(false);

        player.bindSingle(_ -> getContentPane().remove(panel));

        getContentPane().add(panel);
        SpringLayout layout = (SpringLayout) getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, panel, (int)(screenWidth - 2 * cellWidth - 50), SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, panel, 50, SpringLayout.NORTH, getContentPane());
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
            gr.fillRect((int)((i + 1) * cellWidth - 1), 0, 1, screenHeight - getInsets().top);
            gr.fillRect(0, (int)((i + 1) * cellHeight - 1), screenWidth, 1);
        }

        /*
         * Draw all (visible) cells
         */

        for (int x = 0; x < NUMBER_OF_CELLS_IN_VIEW; x++) {
            for (int y = 0; y < NUMBER_OF_CELLS_IN_VIEW; y++) {
                Cell vp = player.getUnsafe().getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));

                if (cell.isField()) {
                    gr.setColor(new Color(200, 120, 0, 100));
                    gr.fillRect((int)(x * cellWidth), (int)(y * cellHeight), (int)(cellWidth - 1), (int)(cellHeight - 1));
                }

                if (cell.isForest()) {
                    gr.setColor(new Color(20, 150, 20));
    //                        gr.fillArc(x * cellWidth - Math.round(poolSize / 2f), y * cellHeight - Math.round(poolSize / 2f), poolSize, poolSize, 270, 90);
                    gr.fillArc((int)(x * cellWidth - poolSize), (int)((y + 1) * cellHeight - poolSize - 1), (int)(2 * poolSize), (int)(2 * poolSize), 0, 90);
                }

                if (cell.isRiver()) {
                    gr.setColor(new Color(0, 100, 255));
                    if (cell.getX() < NUMBER_OF_CELLS - 2 && cell.getY() < NUMBER_OF_CELLS - 2 && cell.fetch(1, 0, 0).isRiver() && cell.fetch(0, 1, 0).isRiver() && cell.fetch(1, 1, 0).isRiver())
                        gr.fillRoundRect(Math.round((x + 0.5f) * cellWidth - (poolSize / 2f)), Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), (int)(cellWidth + poolSize), (int)(cellHeight + poolSize), (int)poolSize, (int)poolSize);
                    else {
                        boolean singleFlag = true;
                        if (cell.getX() < NUMBER_OF_CELLS - 1 && cell.fetch(1, 0, 0).isRiver())
                            gr.fillRect(Math.round((x + 0.5f) * cellWidth), Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), Math.round(cellWidth / 2f) + 1, (int)poolSize);
                        if (cell.getX() > 0 && cell.fetch(-1, 0, 0).isRiver())
                            gr.fillRect((int)(x * cellWidth - 1), Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), Math.round(cellWidth / 2f), (int)poolSize);
                        if (cell.getY() < NUMBER_OF_CELLS - 1 && cell.fetch(0, 1, 0).isRiver()) {
                            gr.fillRect(Math.round((x + 0.5f) * cellWidth - (poolSize / 2f)), Math.round((y + 0.5f) * cellHeight), (int)poolSize, Math.round(cellHeight / 2f));
                            singleFlag = false;
                        }
                        if (cell.getY() > 0 && cell.fetch(0, -1, 0).isRiver()) {
                            gr.fillRect(Math.round((x + 0.5f) * cellWidth - (poolSize / 2f)), (int)(y * cellHeight), (int)poolSize, Math.round(cellHeight / 2f));
                            singleFlag = false;
                        }

                        if (singleFlag)
                            gr.fillOval(Math.round((x + 0.5f) * cellWidth - poolSize), Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), (int)(poolSize * 2), (int)poolSize);
                        else
                            gr.fillOval(Math.round((x + 0.5f) * cellWidth - (poolSize / 2f)), Math.round((y + 0.5f) * cellHeight - (poolSize / 2f)), (int)poolSize, (int)poolSize);
                    }
                }
            }
        }
    }

    /**
     * Draws the motion path for the currently selected unit.
     * @param gr Graphics object of the game panel
     */
    private void drawPaths(Graphics2D gr) {

        hoverPath.get().ifPresent(path -> {
            Cell vp = player.getUnsafe().getViewPoint();
            Cell current = cells.get(path[0]).fetch(vp.getLocation().negative());

            drawPath(gr, path, current);
        });

        motions.stream().filter(motion -> motion.getObject().getPlayer().equals(player.getUnsafe())).forEach(motion -> {
            Cell vp = motion.getObject().getPlayer().getViewPoint();
            Cell current = cells.get(motion.current()).fetch(vp.getLocation().negative());
            drawPath(gr, motion.getRemainingRelativePath(), current);
        });
    }

    private void drawPath(Graphics2D gr, Location[] path, Cell current) {
        // Draws the initial marker
        gr.fillOval(Math.round((current.getX() + 0.5f) * cellWidth) - 5, Math.round((current.getY() + 0.5f) * cellHeight) - 5, 10, 10);

        Stroke oldStroke = gr.getStroke();
        gr.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{9}, 0));

        for (int i = 1; i < path.length - 1; i++) {
            current = current.fetch(path[i].x(), path[i].y(), path[i].z());
            Location previous = path[i];
            Location next = path[i + 1];

            if (previous.x() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), (int)(current.getX() * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight));
            else if (previous.x() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), (int)((current.getX() + 1) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight));
            else if (previous.y() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), (int)(current.getY() * cellHeight));
            else if (previous.y() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), (int)((current.getY() + 1) * cellHeight));

            if (next.x() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), (int)((current.getX() + 1) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight));
            else if (next.x() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), (int)(current.getX() * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight));
            else if (next.y() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), (int)((current.getY() + 1) * cellHeight));
            else if (next.y() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight), Math.round((current.getX() + 0.5f) * cellWidth), (int)(current.getY() * cellHeight));
        }

        Location finalMove = path[path.length - 1];
        current = current.fetch(finalMove.x(), finalMove.y(), finalMove.z());

        // Draws the destination circle with corresponding distance
        gr.setStroke(oldStroke);
        gr.drawOval(Math.round((current.getX() + 0.5f) * cellWidth) - 10, Math.round((current.getY() + 0.5f) * cellHeight) - 10, 20, 20);
        String distanceText = String.valueOf(travelDistance);
        gr.drawString(distanceText, (current.getX() + 0.5f) * cellWidth - gr.getFontMetrics().stringWidth(distanceText) / 2f, (current.getY() + 0.5f) * cellHeight + (float)(gr.getFont().createGlyphVector(gr.getFontRenderContext(), distanceText).getVisualBounds().getHeight() / 2));
    }

    /**
     * Draws currently visible/relevant game objects
     * @param gr Graphics object of the game panel
     */
    private void drawDetails(Graphics2D gr) {

        for(int x = 0; x < NUMBER_OF_CELLS_IN_VIEW; x++) {
            for(int y = 0; y < NUMBER_OF_CELLS_IN_VIEW; y++) {
                Cell vp = player.getUnsafe().getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));

                gr.setColor(player.getUnsafe().getColor());
                List<Player> playersInCell = new ArrayList<>(cell.getContent().stream().map(GameObject::getPlayer).distinct().toList());
                if(playersInCell.contains(player.getUnsafe())) {
                    gr.fillOval((int)(x * cellWidth + 5), (int)(y * cellHeight + 5), 10, 10);
                    playersInCell.remove(player.getUnsafe());
                }

                if(cell.isEndOfMap()) {
                    gr.setColor(Color.black);
                    gr.fillRect((int)(x * cellWidth - 1), (int)(y * cellHeight - 1), (int)cellWidth + 10, (int)cellHeight + 1);
                } else if (!player.getUnsafe().hasSpotted(cell)) {
                    gr.setColor(Color.lightGray);
                    gr.fillRect((int)(x * cellWidth - 1), (int)(y * cellHeight - 1), (int)cellWidth + 1, (int)cellHeight + 1);
                } else if (!player.getUnsafe().hasDiscovered(cell)) {
                    gr.setColor(new Color(192, 192, 192, 200));
                    gr.fillRect((int)(x * cellWidth - 1), (int)(y * cellHeight - 1), (int)cellWidth + 1, (int)cellHeight + 1);
                } else {
                    if (cell.getHeatLevel() <= COLD_LEVEL)
                        gr.drawImage(CLOUD, (int)((x + 1) * cellWidth - CLOUD.getWidth() - 5),
                                (int)(y * cellHeight + 5), null);
                    else if (cell.getHeatLevel() >= HOT_LEVEL)
                        gr.drawImage(SUN, (int)((x + 1) * cellWidth - SUN.getWidth() - 5),
                                (int)(y * cellHeight + 5), null);

                    for(int i = 0; i < playersInCell.size(); i++) {
                        gr.setColor(playersInCell.get(i).getColor());
                        gr.drawOval((int)(x * cellWidth + 5 + 15 * (i + 1)), (int)(y * cellHeight + 5), 10, 10);
                    }
                }
            }
        }

        // In case GPS mode is enabled, the route to the nearest TownHall is indicated (minimalistic).
        if(gps) {

            Cell vp = player.getUnsafe().getViewPoint();
            Cell nearestTownHall =
                    player.getUnsafe().getObjects().stream()
                            .filter(obj -> obj instanceof TownHall)
                            .min((obj1, obj2) -> Integer.min(obj1.getCell().getLocation().distanceTo(vp.getLocation()), obj2.getCell().getLocation().distanceTo(vp.getLocation())))
                            .map(GameObject::getCell)
                            .orElse(vp);

            gr.setColor(Color.black);
            Stroke oldStroke =  gr.getStroke();
            gr.setStroke(new BasicStroke(5));
            Location drawLoc = nearestTownHall.getLocation().add(vp.getLocation().negative());
            gr.drawRoundRect((int)(drawLoc.x() * cellWidth - 10), (int)(drawLoc.y() * cellHeight - 10), (int)cellWidth + 20,
                    (int)cellHeight + 20, 20, 20);
            gr.setStroke(oldStroke);

            if (drawLoc.x() >= NUMBER_OF_CELLS_IN_VIEW)
                gr.fillOval(contentPanel.getWidth() - 60, contentPanel.getHeight() / 2 - 10, 20, 20);
            else if(drawLoc.x() < 0)
                gr.fillOval(40, contentPanel.getHeight() / 2 - 10, 20, 20);

            if (drawLoc.y() >= NUMBER_OF_CELLS_IN_VIEW)
                gr.fillOval(contentPanel.getWidth() / 2 - 10, contentPanel.getHeight() - 60, 20, 20);
            else if(drawLoc.y() < 0)
                gr.fillOval(contentPanel.getWidth() / 2 - 10, 40, 20, 20);
        }
    }

    /**
     * Refreshes the game window components that are not drawn (menus, ...)
     */
    public void refreshWindow() {
        playerMenu.setText("Player: " + player.getUnsafe().getName());
        cycleLabel.setText("Cycle: " + cycle.getUnsafe());

        popLabel.setText("Population: " + player.getUnsafe().getPop() + "/" + player.getUnsafe().getPopCap());

        String[] resources = player.getUnsafe().getResources().keySet().toArray(String[]::new);
        for(int i = 0; i < resources.length; i++)
            playerLabels[i].setText(resources[i] + ": " + player.getUnsafe().getResource(resources[i]));

        Cell cell = cells.get(clickPos);
        for(int i = 0; i < resources.length; i++)
            resourceLabels[i].setText(resources[i] + ": " + cell.getResource(resources[i]));

        cellMenu.setText("Space: " + cell.getUnitOccupied() + "/" + cell.getUnitSpace() +
                " | " + cell.getBuildingOccupied() + "/" + cell.getBuildingSpace());
    }

    /**
     * Recalculates screen size and rescales derived dimensions.
     */
    public void resetScales() {
        screenWidth = getContentPane().getWidth();
        screenHeight = getContentPane().getHeight();
        cellWidth = Math.round(screenWidth / (float)NUMBER_OF_CELLS_IN_VIEW);
        cellHeight = Math.round(screenHeight / (float)NUMBER_OF_CELLS_IN_VIEW);
        poolSize = Math.min(Math.round(cellWidth / 2f), Math.round(cellHeight / 2f));
    }

    public void hidePanels(boolean alsoCellPanel) {
        infoPanel.setVisible(false);
        operationsPanel.setVisible(false);
        choicePanel.setVisible(false);
        settingsPanel.setVisible(false);
        if(alsoCellPanel)
            cellPanel.setVisible(false);
    }

    /**
     * Converts screen coordinates to a {@code Cell} object (not taking into account the {@code Player} viewpoint).
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param h elevation
     * @return Cell representation of the coordinates
     */
    @NotNull
    private Cell posToCellCoord(int x, int y, int h) {
        return cells.get(new Location((int)(x / cellWidth), (int)(y / cellHeight), h));
    }

    /**
     * Turns a given Motion object into a thread that moves the object along the given path.
     * If the thread is interrupted, the path is completed instantly.
     * TODO Add motions that span multiple cycles
     * @param motion path to complete
     */
    public void motionToThread(Motion motion) {
        motions.add(motion);

        motion.getObject().setStatus(Status.WALKING);
        ActionListener taskPerformer = evt -> {
            if(!motion.isDone()) {
                Location next = motion.next();
                parent.moveObject(motion.getObject(), motion.getObject().getCell().getLocation().add(next.x(), next.y(), next.z()));
                refreshWindow();
            }

            if(motion.isDone()) {
                motion.getObject().setStatus(Status.IDLE);
                motions.remove(motion);
                ((Timer)evt.getSource()).stop();
            }
        };
        new Timer(motion.getObject().getAnimationDelay(), taskPerformer).start();
    }

    public void cycleAnimation() {
        cellPanel.cycleAnimation();
    }
}
