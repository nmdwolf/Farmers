package UI;

import core.*;
import core.contracts.AttackContract;
import core.player.Player;
import objects.Aggressive;
import objects.Energetic;
import objects.GameObject;
import core.Status;
import objects.buildings.TownHall;
import objects.buildings.Wall;
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
    private final MissionPanel missionPanel;
    private final JScrollPane settingsScroller, missionScroller;
    private final JPanel tintedGlassPanel;
    private Font font;

    @NotNull private final Property<Player> player;
    private int mouseX, mouseY;

    @NotNull private final Main parent;
    @NotNull private final Settings settings;
    @NotNull private final Property<Main.GameState> gameState;
    @NotNull private final Property<Integer> cycle, playerCounter;
    @NotNull private final Property<Boolean> clicked;
    @NotNull private final Property<InfoPanel.Mode> showResources;
    @NotNull private final UnsafeProperty<GameObject<?>> selected;
    @NotNull private final UnsafeProperty<Pair<GameObject<?>, Boolean>> target;
    private int travelDistance;
    private Location clickPos, destination;
    @NotNull private final UnsafeProperty<Location[]> hoverPath;
    @NotNull private final Grid cells;
    private boolean gps;
    private Timer resizeTimer;
    private final HashSet<Motion> motions;

    public GameFrame(@NotNull Main main, @NotNull Grid cells, @NotNull Property<Integer> cycle, @NotNull Property<Integer> playerCounter, @NotNull Property<Player> player, @NotNull Property<Main.GameState> gameState, @NotNull Settings settings) {
        parent = main;
        this.settings = settings;
        this.gameState = gameState;
        this.cells = cells;
        this.cycle = cycle;
        this.playerCounter = playerCounter;
        this.player = player;
        settings.initialize(this);

        clicked = new Property<>(false);
        selected = new UnsafeProperty<>();
        target = new UnsafeProperty<>(new Pair<>(null, false));
        clickPos = new Location(0, 0, 0);
        hoverPath = new UnsafeProperty<>();
        showResources = new Property<>(InfoPanel.Mode.OBJECT);
        gps = false;
        motions = new HashSet<>();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setFont();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        operationsPanel = new OperationsPanel(settings.getCellWidth(), settings.getCellHeight());
        choicePanel = new ChoicePanel(operationsPanel, settings.getCellWidth(), settings.getCellHeight(), _ -> hidePanels(true), showResources, target);
        infoPanel = new InfoPanel(selected);
        cellPanel = new CellPanel(cells.get(new Location(0, 0, 0)), player.get(), selected, target, gameState, settings);
        settingsPanel = new SettingsPanel(settings);
        settingsScroller = CustomMethods.wrapPanel(settingsPanel);
        missionPanel = new MissionPanel(settings);
        missionScroller = CustomMethods.wrapPanel(missionPanel);

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

        showResources.bind(prop -> selected.ifPresent(_ -> infoPanel.update(prop)));
        selected.bind(_ -> {
            hidePanels(false);
            selected.ifPresent(obj -> {
                if(obj instanceof Energetic<?> en) // Calculate path finding costs
                    cells.populateDistanceMatrix(cells.get(clickPos), player.get(), en.getEnergy());
                showResources.set(InfoPanel.Mode.OBJECT);
                infoPanel.setVisible(true);
                if(obj.getPlayer().equals(player.get()))
                    choicePanel.update(obj, cycle.get());
            });
            refreshMenubar();
        });
        target.bindIfPresent(pair -> {
            if (pair.key() != null && !pair.value()) {
                selected.ifPresent(fighter -> {
                    if(Grid.objectDistance(fighter, pair.key()) <= ((Aggressive<?>) fighter).getRange()) // TODO Fix this
                        ((Unit<?>) fighter).addContract(new AttackContract(fighter, ((Aggressive<?>) fighter).getAttackCost(), pair.key()));
                });
                selected.set(null);
            }
        });
        player.bind(p -> missionPanel.update(p.getMissionArchive()));
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
        contentPanel.add(settingsScroller, Integer.valueOf(3));
        contentPanel.add(missionScroller, Integer.valueOf(3));

        setVisible(true);
        setExtendedState(MAXIMIZED_BOTH);
        refresh();

        setCustomCursor();
        addMouseInputs();
        addKeyInputs();
        addMenu();
        initializeSettingsPanel();

        hidePanels(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        contentPanel.requestFocus();
    }

    public void updateContent(boolean forceReload) {
        SwingUtilities.invokeLater(() -> {
            refreshMenubar();
            missionPanel.update(player.get().getMissionArchive());
            contentPanel.revalidate();
            cellPanel.updateContent(forceReload);
            contentPanel.repaint();
        });
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
                if(settings.getCellWidth() > 0 && settings.getCellHeight() > 0) {
                    Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D) g.create());

                    drawCells(gr);
                    drawDetails(gr);

                    gr.setColor(player.get().getAlternativeColor());
                    gr.setStroke(new BasicStroke(2));
                    Cell p = posToCellCoord(mouseX, mouseY, 0);
                    gr.drawRect((int)(p.getX() * settings.getCellWidth()), (int)(p.getY() * settings.getCellHeight()), (int)settings.getCellWidth(), (int)settings.getCellHeight());
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
     * Adds key input handling to the game window.
     */
    private void addKeyInputs() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event -> {
            boolean done = true;
            if (event.getID() == KeyEvent.KEY_PRESSED) {
                if ((event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.VK_M -> showMessageBox(player.get().getMissionArchive().getNextDescription());
                        case KeyEvent.VK_G -> gps = !gps;
                        default -> done = false;
                    }
                } else if ((event.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.VK_M -> missionScroller.setVisible(true);
                        case KeyEvent.VK_S -> settingsScroller.setVisible(true);
                        default -> done = false;
                    }
                } else switch(event.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        selected.get().ifPresentOrElse(_ -> hidePanels(true), () -> cellPanel.setVisible(false));
                        break;
                    case KeyEvent.VK_N:
                        if (gameState.get() == Main.GameState.PLAYING) {
                            hoverPath.set(null);
                            selected.set(null);
                            playerCounter.set(playerCounter.get() + 1);
                            cellPanel.generateCycleAnimation();
                        }
                        break;
                    case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN:
                        dispatchArrowKeys(event);
                        break;
                    default:
                        if(choicePanel.isVisible())
                            return choicePanel.dispatchKeyEvent(event);
                        else
                            return false;
                }
            } else
                return false;

            return done;
        });
    }

    private void dispatchArrowKeys(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if(player.get().getViewPoint().getX() > 0) {
                    player.get().changeViewpoint(player.get().getViewPoint().fetch(-1, 0, 0));
                    if(cellPanel.isVisible())
                        cellPanel.updateContent(cellPanel.getCurrentCell().fetch(-1, 0, 0), player.get());
                }
                break;
            case KeyEvent.VK_RIGHT:
                if(player.get().getViewPoint().getX() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    player.get().changeViewpoint(player.get().getViewPoint().fetch(1, 0, 0));
                    if(cellPanel.isVisible())
                        cellPanel.updateContent(cellPanel.getCurrentCell().fetch(1, 0, 0), player.get());
                }
                break;
            case KeyEvent.VK_UP:
                if(player.get().getViewPoint().getY() > 0) {
                    player.get().changeViewpoint(player.get().getViewPoint().fetch(0, -1, 0));
                    if(cellPanel.isVisible())
                        cellPanel.updateContent(cellPanel.getCurrentCell().fetch(0, -1, 0), player.get());
                }
                break;
            case KeyEvent.VK_DOWN:
                if(player.get().getViewPoint().getY() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    player.get().changeViewpoint(player.get().getViewPoint().fetch(0, 1, 0));
                    if(cellPanel.isVisible())
                        cellPanel.updateContent(cellPanel.getCurrentCell().fetch(0, 1, 0), player.get());
                }
        }
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
                Cell mousePos = posToCellCoord(mouseX, mouseY, player.get().getViewPoint().getZ());
                Cell absoluteMousePos = mousePos.fetch(player.get().getViewPoint().getLocation());

                // Moves info panel to make sure that the underlying cells are reachable.
                selected.ifPresent( obj -> {
                    if(infoPanel != null)
                        layout.putConstraint(SpringLayout.WEST, infoPanel, (mousePos.getX() > (NUMBER_OF_CELLS_IN_VIEW - 4)) ? 10 : (int)(settings.getScreenWidth() - 2 * settings.getCellWidth() - 30), SpringLayout.WEST, contentPanel);

                    if(obj.getPlayer().equals(player.get()) && (obj instanceof Unit<?> unit) && (hoverPath.get().isEmpty() || !mousePos.getLocation().equals(destination)) && !absoluteMousePos.isEndOfMap()) {
                        ArrayList<Location> path = cells.getShortestAdmissiblePath(absoluteMousePos);

                        if (path != null) {
                            Motion motion = new Motion(unit, path, cells.getPathDistance(absoluteMousePos));
                            if(motion.length() > 0) {
                                hoverPath.setOptional(motion.getRelativePath());
                                travelDistance = cells.getPathDistance(absoluteMousePos);
                            } else
                                hoverPath.set(null);
                        }
                    }

                    destination = mousePos.getLocation();
                });
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                if(GAME_3D) {
                    if (e.getWheelRotation() != 0 && !player.get().isViewLocked()) {
                        player.get().changeViewpoint(player.get().getViewPoint().fetch(0, 0,
                                e.getWheelRotation()));
                        viewMenu.setText("View: " + (player.get().getViewPoint().getZ()));
                        selected.set(null);

                        hidePanels(true);
                        refreshMenubar();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Location oldPos = clickPos;
                clickPos = player.get().getViewPoint().fetch(posToCellCoord(e.getX(), e.getY(),
                        player.get().getViewPoint().getZ()).getLocation()).getLocation();

                if(!cells.get(clickPos).isEndOfMap()) {
                    hoverPath.set(null);

                    // Any existing panels should be hidden on click
                    hidePanels(true);

                    /*
                     * left-click events
                     */
                    if (SwingUtilities.isLeftMouseButton(e)) {

                        // Unselect GameObject on left click
                        if (clicked.get() && !target.get().map(Pair::value).orElse(false))
                            selected.set(null);

                        // If clicked on cell with objects, show info panel
                        // TODO Change this check to cells within view distance
                        if (player.get().getObjects().stream().anyMatch(obj -> obj.getCell().getLocation().equals(clickPos))) {
                            cellPanel.updateContent(cells.get(clickPos), player.get());
                            cellPanel.setVisible(true);
                            clicked.set(true);
                        } else
                            clicked.set(false);

                        // right-click events
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        selected.ifPresent(obj -> {

                            if(obj instanceof Unit<?> unit) {
                                ArrayList<Location> path = cells.getShortestAdmissiblePath(cells.get(clickPos));

                                if (path != null) {
                                    Motion motion = new Motion(unit, path, cells.getPathDistance(cells.get(clickPos)));
                                    hoverPath.set(null);
                                    if (cells.get(clickPos).getUnitSpace() - cells.get(clickPos).getUnitOccupied() >= unit.getSize()
                                            && motion.length() != 0) {
                                        unit.changeEnergy(-travelDistance);
                                        motionToThread(motion);
                                    }
                                }
                            }
                        });
                        selected.set(null);
                    }
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
        resizeTimer = new Timer(100, _ -> {
            SwingUtilities.invokeLater(() -> {
                refresh();
                updateContent(true);
            });

        });
        resizeTimer.setRepeats(false);

        // Rescale game elements on screen resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                resizeTimer.restart();
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
        playerMenu = new JMenu("Player: " + player.get().getName());
        viewMenu = new JMenu("View: " + player.get().getViewPoint().getZ());
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
        String[] resources = player.get().getResources().keySet().toArray(String[]::new);
        playerLabels = new JMenuItem[resources.length];
        for(int i = 0; i < resources.length; i++) {
            JMenuItem label =
                    new JMenuItem(resources[i] + ": " + player.get().getResource(resources[i]));
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
        popLabel = new JMenuItem("Population: " + player.get().getPop() + "/" + player.get().getPopCap());
        playerMenu.add(popLabel);

        JMenuItem viewItem = new JMenuItem("Audio & Visuals");
        viewItem.addActionListener(_ -> {
            settingsScroller.setVisible(true);
            contentPanel.requestFocus();
        });
        settingsMenu.add(viewItem);
    }

    /**
     * Adds a {@code SettingsPanel} to the game window.
     */
    private void initializeSettingsPanel() {

        tintedGlassPanel.setOpaque(false);
        settingsScroller.addComponentListener(new ComponentAdapter() {
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

        settingsScroller.setVisible(false);
    }

    public void setCustomCursor() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension size = tk.getBestCursorSize(10, 10);
        Image img = new ImageIcon("src/img/Cursor.png").getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
        setCursor(tk.createCustomCursor(img, new Point(size.width / 2, size.height / 2), "customCursor"));
    }

    public void showMessageBox(String text) {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());
//                gr.setColor(new Color(200, 150, 0, 76));
                gr.setColor(new Color(200, 150, 0));
                gr.fillRoundRect(1, 1, (int)(2 * settings.getCellWidth()) - 2, getHeight() - 2, CustomBorder.RADIUS, CustomBorder.RADIUS);
                gr.dispose();
            }
        };

        panel.setPreferredSize(new Dimension((int)(2 * settings.getCellWidth()), (int)(2 * settings.getCellHeight())));
        panel.setBorder(new CustomBorder(Color.black));
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());

        JTextArea area = new JTextArea(text);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        area.setOpaque(false);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setFocusable(false);
        panel.add(area);

        getContentPane().add(panel, Integer.valueOf(1));
        SpringLayout layout = (SpringLayout) getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, panel, (int)(settings.getScreenWidth() - 2 * settings.getCellWidth() - 50), SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, panel, 50, SpringLayout.NORTH, getContentPane());

        player.bindSingle(_ -> getContentPane().remove(panel));
        area.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                getContentPane().remove(panel);
            }
        });
    }

    /**
     * Draws cells
     * @param gr Graphics object of the game panel
     */
    private void drawCells(Graphics2D gr) {

        /*
         * Draw all (visible) cells
         */

        for (int x = 0; x < NUMBER_OF_CELLS_IN_VIEW; x++) {
            for (int y = 0; y < NUMBER_OF_CELLS_IN_VIEW; y++) {
                Cell vp = player.get().getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));

                if (cell.isField()) {
                    gr.setColor(new Color(200, 120, 0, 100));
                    gr.fillRect((int)(x * settings.getCellWidth() - 1), (int)(y * settings.getCellHeight() - 1), (int)(settings.getCellWidth() + 2), (int)(settings.getCellHeight() + 2));
                }

                if (cell.isForest()) {
                    gr.setColor(new Color(20, 150, 20));
    //                        gr.fillArc(x * settings.getCellWidth() - Math.round(settings.getPoolSize() / 2f), y * settings.getCellHeight() - Math.round(settings.getPoolSize() / 2f), settings.getPoolSize(), settings.getPoolSize(), 270, 90);
                    gr.fillArc((int)(x * settings.getCellWidth() - settings.getPoolSize()), (int)((y + 1) * settings.getCellHeight() - settings.getPoolSize() - 1), (int)(2 * settings.getPoolSize()), (int)(2 * settings.getPoolSize()), 0, 90);
                }

                if (cell.isRiver()) {
                    gr.setColor(new Color(0, 100, 255));
                    if (cell.getX() < NUMBER_OF_CELLS - 2 && cell.getY() < NUMBER_OF_CELLS - 2 && cell.fetch(1, 0, 0).isRiver() && cell.fetch(0, 1, 0).isRiver() && cell.fetch(1, 1, 0).isRiver())
                        gr.fillRoundRect(Math.round((x + 0.5f) * settings.getCellWidth() - (settings.getPoolSize() / 2f)), Math.round((y + 0.5f) * settings.getCellHeight() - (settings.getPoolSize() / 2f)), (int)(settings.getCellWidth() + settings.getPoolSize()), (int)(settings.getCellHeight() + settings.getPoolSize()), (int)settings.getPoolSize(), (int)settings.getPoolSize());
                    else {
                        boolean singleFlag = true;
                        if (cell.getX() < NUMBER_OF_CELLS - 1 && cell.fetch(1, 0, 0).isRiver())
                            gr.fillRect(Math.round((x + 0.5f) * settings.getCellWidth()), Math.round((y + 0.5f) * settings.getCellHeight() - (settings.getPoolSize() / 2f)), Math.round(settings.getCellWidth() / 2f) + 1, (int)settings.getPoolSize());
                        if (cell.getX() > 0 && cell.fetch(-1, 0, 0).isRiver())
                            gr.fillRect((int)(x * settings.getCellWidth() - 1), Math.round((y + 0.5f) * settings.getCellHeight() - (settings.getPoolSize() / 2f)), Math.round(settings.getCellWidth() / 2f), (int)settings.getPoolSize());
                        if (cell.getY() < NUMBER_OF_CELLS - 1 && cell.fetch(0, 1, 0).isRiver()) {
                            gr.fillRect(Math.round((x + 0.5f) * settings.getCellWidth() - (settings.getPoolSize() / 2f)), Math.round((y + 0.5f) * settings.getCellHeight()), (int)settings.getPoolSize(), Math.round(settings.getCellHeight() / 2f));
                            singleFlag = false;
                        }
                        if (cell.getY() > 0 && cell.fetch(0, -1, 0).isRiver()) {
                            gr.fillRect(Math.round((x + 0.5f) * settings.getCellWidth() - (settings.getPoolSize() / 2f)), (int)(y * settings.getCellHeight()), (int)settings.getPoolSize(), Math.round(settings.getCellHeight() / 2f));
                            singleFlag = false;
                        }

                        if (singleFlag)
                            gr.fillOval(Math.round((x + 0.5f) * settings.getCellWidth() - settings.getPoolSize()), Math.round((y + 0.5f) * settings.getCellHeight() - (settings.getPoolSize() / 2f)), (int)(settings.getPoolSize() * 2), (int)settings.getPoolSize());
                        else
                            gr.fillOval(Math.round((x + 0.5f) * settings.getCellWidth() - (settings.getPoolSize() / 2f)), Math.round((y + 0.5f) * settings.getCellHeight() - (settings.getPoolSize() / 2f)), (int)settings.getPoolSize(), (int)settings.getPoolSize());
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
        hoverPath.ifPresent(path -> {
            Cell vp = player.get().getViewPoint();
            Cell current = cells.get(path[0]).fetch(vp.getLocation().negative());

            drawPath(gr, path, current);
        });
        motions.stream().filter(motion -> motion.getObject().getPlayer().equals(player.get())).forEach(motion -> {
            Cell vp = motion.getObject().getPlayer().getViewPoint();
            Cell current = cells.get(motion.current()).fetch(vp.getLocation().negative());
            drawPath(gr, motion.getRemainingRelativePath(), current);
        });
    }

    private void drawPath(Graphics2D gr, Location[] path, Cell current) {
        // Draws the initial marker
        gr.fillOval(Math.round((current.getX() + 0.5f) * settings.getCellWidth()) - 5, Math.round((current.getY() + 0.5f) * settings.getCellHeight()) - 5, 10, 10);

        Stroke oldStroke = gr.getStroke();
        gr.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{9}, 0));

        for (int i = 1; i < path.length - 1; i++) {
            current = current.fetch(path[i].x(), path[i].y(), path[i].z());
            Location previous = path[i];
            Location next = path[i + 1];

            if (previous.x() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()), (int)(current.getX() * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()));
            else if (previous.x() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()), (int)((current.getX() + 1) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()));
            else if (previous.y() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()), Math.round((current.getX() + 0.5f) * settings.getCellWidth()), (int)(current.getY() * settings.getCellHeight()));
            else if (previous.y() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()), Math.round((current.getX() + 0.5f) * settings.getCellWidth()), (int)((current.getY() + 1) * settings.getCellHeight()));

            if (next.x() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()), (int)((current.getX() + 1) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()));
            else if (next.x() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()), (int)(current.getX() * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()));
            else if (next.y() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()), Math.round((current.getX() + 0.5f) * settings.getCellWidth()), (int)((current.getY() + 1) * settings.getCellHeight()));
            else if (next.y() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * settings.getCellWidth()), Math.round((current.getY() + 0.5f) * settings.getCellHeight()), Math.round((current.getX() + 0.5f) * settings.getCellWidth()), (int)(current.getY() * settings.getCellHeight()));
        }

        Location finalMove = path[path.length - 1];
        current = current.fetch(finalMove.x(), finalMove.y(), finalMove.z());

        // Draws the destination circle with corresponding distance
        gr.setStroke(oldStroke);
        gr.drawOval(Math.round((current.getX() + 0.5f) * settings.getCellWidth()) - 10, Math.round((current.getY() + 0.5f) * settings.getCellHeight()) - 10, 20, 20);
        String distanceText = String.valueOf(travelDistance);
        gr.drawString(distanceText, (current.getX() + 0.5f) * settings.getCellWidth() - gr.getFontMetrics().stringWidth(distanceText) / 2f, (current.getY() + 0.5f) * settings.getCellHeight() + (float)(gr.getFont().createGlyphVector(gr.getFontRenderContext(), distanceText).getVisualBounds().getHeight() / 2));
    }

    /**
     * Draws currently visible/relevant game objects
     * @param gr Graphics object of the game panel
     */
    private void drawDetails(Graphics2D gr) {

        for(int x = 0; x < NUMBER_OF_CELLS_IN_VIEW; x++) {
            for(int y = 0; y < NUMBER_OF_CELLS_IN_VIEW; y++) {
                Cell vp = player.get().getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));

                gr.setColor(player.get().getColor());
                List<Player> playersInCell = new ArrayList<>(cell.getObjects().stream().map(GameObject::getPlayer).distinct().toList());
                if(playersInCell.contains(player.get())) {
                    gr.fillOval((int)(x * settings.getCellWidth() + 5), (int)(y * settings.getCellHeight() + 5), 10, 10);
                    playersInCell.remove(player.get());
                }

                if(cell.isEndOfMap()) {
                    gr.setColor(Color.black);
                    gr.fillRect((int)(x * settings.getCellWidth() - 1), (int)(y * settings.getCellHeight() - 1), (int)settings.getCellWidth() + 2, (int)settings.getCellHeight() + 2);
                } else if (!player.get().hasSpotted(cell)) {
                    gr.setColor(Color.lightGray);
                    gr.fillRect((int)(x * settings.getCellWidth() - 1), (int)(y * settings.getCellHeight() - 1), (int)settings.getCellWidth() + 2, (int)settings.getCellHeight() + 2);
                } else if (!player.get().hasDiscovered(cell)) {
                    drawWall(gr, cell, x, y);
                    gr.setColor(new Color(192, 192, 192, 200));
                    gr.fillRect((int)(x * settings.getCellWidth() - 1), (int)(y * settings.getCellHeight() - 1), (int)settings.getCellWidth() + 2, (int)settings.getCellHeight() + 2);
                } else {
                    drawWall(gr, cell, x, y);
                    if (cell.getHeatLevel() <= COLD_LEVEL)
                        gr.drawImage(CLOUD, (int)((x + 1) * settings.getCellWidth() - CLOUD.getWidth() - 5),
                                (int)(y * settings.getCellHeight() + 5), null);
                    else if (cell.getHeatLevel() >= HOT_LEVEL)
                        gr.drawImage(SUN, (int)((x + 1) * settings.getCellWidth() - SUN.getWidth() - 5),
                                (int)(y * settings.getCellHeight() + 5), null);

                    for(int i = 0; i < playersInCell.size(); i++) {
                        gr.setColor(playersInCell.get(i).getColor());
                        gr.drawOval((int)(x * settings.getCellWidth() + 5 + 15 * (i + 1)), (int)(y * settings.getCellHeight() + 5), 10, 10);
                    }
                }
            }
        }

        // In case GPS mode is enabled, the route to the nearest TownHall is indicated (minimalistic).
        // TODO remove grid overlay
        if(gps) {
            Cell vp = player.get().getViewPoint();
            Cell nearestTownHall =
                    player.get().getObjects().stream()
                            .filter(obj -> obj instanceof TownHall)
                            .min((obj1, obj2) -> Integer.min(obj1.getCell().getLocation().distanceTo(vp.getLocation()), obj2.getCell().getLocation().distanceTo(vp.getLocation())))
                            .map(GameObject::getCell)
                            .orElse(vp);

            gr.setColor(Color.black);
            Stroke oldStroke =  gr.getStroke();
            gr.setStroke(new BasicStroke(5));
            Location drawLoc = nearestTownHall.getLocation().add(vp.getLocation().negative());
            gr.drawRoundRect((int)(drawLoc.x() * settings.getCellWidth() - 10), (int)(drawLoc.y() * settings.getCellHeight() - 10), (int)settings.getCellWidth() + 20,
                    (int)settings.getCellHeight() + 20, 20, 20);
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

        gr.setColor(Color.lightGray);
        for (int i = 0; i < NUMBER_OF_CELLS_IN_VIEW - 1; i++) {
            gr.fillRect((int)((i + 1) * settings.getCellWidth() - 1), 0, 1, settings.getScreenHeight());
            gr.fillRect(0, (int)((i + 1) * settings.getCellHeight() - 1), settings.getScreenWidth(), 1);
        }
    }

    private void drawWall(Graphics2D gr, Cell cell, int x, int y) {
        gr.setColor(new Color(80, 40, 10));
        Stroke oldStroke = gr.getStroke();
        int strokeWidth = 10;
        gr.setStroke(new BasicStroke(strokeWidth));
        var walls = cell.getObjects().stream()
                .filter(Wall.class::isInstance)
                .map(obj -> ((Wall)obj).getDirection())
                .distinct()
                .toList();
        for(Direction d : walls) {
            switch(d) {
                case NORTH -> gr.drawLine((int)(x * settings.getCellWidth()) + strokeWidth / 2, (int)(y * settings.getCellHeight()) + strokeWidth / 2, (int)((x + 1) * settings.getCellWidth()) - strokeWidth / 2, (int)(y * settings.getCellHeight()) + strokeWidth / 2);
                case SOUTH -> gr.drawLine((int)(x * settings.getCellWidth()) + strokeWidth / 2, (int)((y + 1) * settings.getCellHeight()) - strokeWidth / 2, (int)((x + 1) * settings.getCellWidth()) - strokeWidth / 2, (int)((y + 1) * settings.getCellHeight()) - strokeWidth / 2);
                case WEST -> gr.drawLine((int)(x * settings.getCellWidth()) + strokeWidth / 2, (int)(y * settings.getCellHeight()) + strokeWidth / 2, (int)(x * settings.getCellWidth()) + strokeWidth / 2, (int)((y + 1) * settings.getCellHeight()) - strokeWidth / 2);
                case EAST -> gr.drawLine((int)((x + 1) * settings.getCellWidth()) - strokeWidth / 2, (int)(y * settings.getCellHeight()) + strokeWidth / 2, (int)((x + 1) * settings.getCellWidth()) - strokeWidth / 2, (int)((y + 1) * settings.getCellHeight()) - strokeWidth / 2);
            }
        }
        gr.setStroke(oldStroke);
    }

    /**
     * Refreshes the game window components that are not drawn (menus, ...)
     */
    public void refreshMenubar() {
        playerMenu.setText("Player: " + player.get().getName());
        cycleLabel.setText("Cycle: " + cycle.get());

        popLabel.setText("Population: " + player.get().getPop() + "/" + player.get().getPopCap());

        String[] resources = player.get().getResources().keySet().toArray(String[]::new);
        for(int i = 0; i < resources.length; i++)
            playerLabels[i].setText(resources[i] + ": " + player.get().getResource(resources[i]));

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
        settings.setScreenWidth(getContentPane().getWidth());
        settings.setScreenHeight(getContentPane().getHeight());
        Sprite.setSpriteSize(settings.getSpriteSize());
        Sprite.resizeSprites(settings.getSpriteSize());
    }

    private void refresh() {
        resetScales();

        tintedGlassPanel.setPreferredSize(new Dimension(settings.getScreenWidth(), settings.getScreenHeight()));
        layout.putConstraint(SpringLayout.WEST, tintedGlassPanel, 0, SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, tintedGlassPanel, 0, SpringLayout.NORTH, contentPanel);

        choicePanel.resizePanel((int)settings.getCellWidth(), (int)settings.getCellHeight());
        layout.putConstraint(SpringLayout.WEST, choicePanel, Math.round((settings.getScreenWidth() - 3 * settings.getCellWidth()) / 2f), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, choicePanel, Math.round(settings.getScreenHeight() - 3 * settings.getCellHeight()), SpringLayout.NORTH, contentPanel);

        operationsPanel.resizePanel((int)settings.getCellWidth(), (int)settings.getCellHeight());
        layout.putConstraint(SpringLayout.WEST, operationsPanel, Math.round((settings.getScreenWidth() - 3 * settings.getCellWidth()) / 2f), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, operationsPanel, Math.round(settings.getScreenHeight() - 3 * settings.getCellHeight()), SpringLayout.NORTH, contentPanel);

        cellPanel.setPreferredSize(new Dimension((int)((NUMBER_OF_CELLS_IN_VIEW - 2) * settings.getCellWidth() + 40), ( int)((NUMBER_OF_CELLS_IN_VIEW - 2) * settings.getCellHeight() + 40)));
        layout.putConstraint(SpringLayout.WEST, cellPanel, (int)(settings.getCellWidth() - 20), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, cellPanel, (int)(settings.getCellHeight() - 20), SpringLayout.NORTH, contentPanel);

        infoPanel.resizePanel((int)settings.getCellWidth(), (int)settings.getCellHeight());
        layout.putConstraint(SpringLayout.WEST, infoPanel,
                (clickPos.x() - player.get().getViewPoint().getX()) >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f) ? 10 : (int)(settings.getScreenWidth() - 2 * settings.getCellWidth() - 30), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, infoPanel, 10, SpringLayout.NORTH, contentPanel);

        settingsScroller.setPreferredSize(new Dimension((int)(4 * settings.getCellWidth()), (int)(5 * settings.getCellHeight())));
        layout.putConstraint(SpringLayout.WEST, settingsScroller, (int)(2 * settings.getCellWidth()), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, settingsScroller, (int)settings.getCellHeight(), SpringLayout.NORTH, contentPanel);

        missionScroller.setPreferredSize(new Dimension((int)(4 * settings.getCellWidth()), (int)(5 * settings.getCellHeight())));
        layout.putConstraint(SpringLayout.WEST, missionScroller, (int)(2 * settings.getCellWidth()), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, missionScroller, (int)settings.getCellHeight(), SpringLayout.NORTH, contentPanel);

        contentPanel.revalidate();
    }

    public void hidePanels(boolean alsoCellPanel) {
        infoPanel.setVisible(false);
        operationsPanel.setVisible(false);
        choicePanel.setVisible(false);
        settingsScroller.setVisible(false);
        missionScroller.setVisible(false);
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
        return cells.get(new Location((int)(x / settings.getCellWidth()), (int)(y / settings.getCellHeight()), h));
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
                refreshMenubar();
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
