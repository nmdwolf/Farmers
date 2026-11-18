package UI;

import core.*;
import core.contracts.AttackContract;
import core.player.Player;
import objects.GameObject;
import core.Status;
import objects.Operational;
import objects.buildings.TownHall;
import objects.buildings.Wall;
import objects.loadouts.Fighter;
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

import static core.InternalSettings.HOVER_SHIFT;
import static javax.swing.MenuSelectionManager.defaultManager;

import static core.InternalSettings.NUMBER_OF_CELLS_IN_VIEW;

public class GameFrame extends JFrame {

    // UI elements
    private JMenu playerMenu, viewMenu, cellMenu;
    private JMenuItem cycleLabel, popLabel;
    private JMenuItem[] playerLabels, resourceLabels;
//    private final InfoPanel infoPanel;
    private final JLayer<InfoPanel> infoPanel;
    private final JLayer<ChoicePanel> choicePanel;
    private final JLayer<OperationsPanel> operationsPanel;
    private final CellPanel cellPanel;
    private final SpringLayout layout;
    private final JLayeredPane contentPanel;
    private final JScrollPane settingsScroller, missionScroller;
    private final JLayer<JScrollPane> settingsPanel, missionsPanel;
    private final JPanel tintedGlassPanel;
    private Font font;

    @NotNull private final Property<Player> player;
    private int mouseX, mouseY;

    @NotNull private final Main parent;
    @NotNull private final Settings settings;
    @NotNull private final InternalSettings internalSettings;
    @NotNull private final Property<Main.GameState> gameState;
    @NotNull private final Property<Integer> cycle, playerCounter;
    @NotNull private final Property<Boolean> clicked;
    @NotNull private final Property<InfoPanel.Mode> showResources;
    @NotNull private final UnsafeProperty<GameObject> selected;
    @NotNull private final UnsafeProperty<Pair<GameObject, Boolean>> target;
    private int travelDistance;
    private Location clickLocation, destination;
    @NotNull private final UnsafeProperty<Location[]> hoverPath;
    @NotNull private final Grid cells;
    private boolean gps;
    private Timer resizeTimer;
    private final HashSet<Motion> motions;

    public GameFrame(@NotNull Main main, @NotNull Grid cells, @NotNull Property<Integer> cycle, @NotNull Property<Integer> playerCounter, @NotNull Property<Player> player, @NotNull Property<Main.GameState> gameState, @NotNull Settings settings, @NotNull InternalSettings internalSettings) {
        parent = main;
        this.settings = settings;
        this.internalSettings = internalSettings;
        this.gameState = gameState;
        this.cells = cells;
        this.cycle = cycle;
        this.playerCounter = playerCounter;
        this.player = player;
        settings.initialize(this);

        clicked = new Property<>(false);
        selected = new UnsafeProperty<>();
        target = new UnsafeProperty<>(new Pair<>(null, false));
        clickLocation = new Location(0, 0, 0);
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

        cellPanel = new CellPanel(cells.get(new Location(0, 0, 0)), player.get(), selected, target, gameState, settings, internalSettings);

        TranslucentLayerUI<OperationsPanel> operationsProxy =
                new TranslucentLayerUI<>(new OperationsPanel(internalSettings.getCellWidth(), internalSettings.getCellHeight()));
        operationsPanel = new JLayer<>(operationsProxy.getPanel(), operationsProxy);

        TranslucentLayerUI<ChoicePanel> choiceProxy = new TranslucentLayerUI<>(new ChoicePanel(operationsPanel.getView(),
                internalSettings.getCellWidth(), internalSettings.getCellHeight(), _ -> hidePanels(true), showResources, target));
        choicePanel = new JLayer<>(choiceProxy.getPanel(), choiceProxy);

        TranslucentLayerUI<InfoPanel> infoProxy = new TranslucentLayerUI<>(new InfoPanel(selected));
        infoPanel = new JLayer<>(infoProxy.getPanel(), infoProxy);

        settingsScroller = CustomMethods.wrapPanel(new SettingsPanel(settings));
        TranslucentLayerUI<JScrollPane> settingsProxy = new TranslucentLayerUI<>(settingsScroller);
        settingsPanel = new JLayer<>(settingsProxy.getPanel(), settingsProxy);

        MissionPanel mp = new MissionPanel(settings, internalSettings);
        mp.update(player.get().getMissionArchive());
        missionScroller = CustomMethods.wrapPanel(mp);
        TranslucentLayerUI<JScrollPane> missionsProxy = new TranslucentLayerUI<>(missionScroller);
        missionsPanel = new JLayer<>(missionsProxy.getPanel(), missionsProxy);

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

        showResources.bind(prop -> selected.ifPresent(_ -> infoPanel.getView().update(prop)));
        selected.bind(_ -> {
            hidePanels(false);
            selected.ifPresent(obj -> {
                if(obj instanceof Operational en) // Calculate path finding costs
                    cells.populateDistanceMatrix(cells.get(clickLocation), player.get(), en.getEnergy());
                showResources.set(InfoPanel.Mode.OBJECT);
                infoPanel.getView().setVisible(true);
                if(obj.getPlayer().equals(player.get())) {
                    choicePanel.getView().setVisible(choicePanel.getView().update(obj, cycle.get()));
                }
            });
            refreshMenubar();
        });
        target.bindIfPresent(pair -> {
            if (pair.key() != null && !pair.value()) {
                selected.ifPresent(obj -> {
                    if(obj instanceof Operational fighter && obj.hasLoadout(Fighter.class))
                        if(Grid.objectDistance(obj, pair.key()) <= obj.getLoadout(Fighter.class)
                                                                        .orElse(new Fighter()).getRange()) //
                            // TODO Fix this
                            fighter.addContract(new AttackContract(fighter, pair.key()));
                });
                selected.unset();
            }
        });
        player.bind(p -> mp.update(p.getMissionArchive()));
    }

    /**
     * Constructs the game frame.
     */
    public void initialize() {
        super.setTitle("The Game of Ages");
        setUndecorated(true);

        addResizeListener();
        setSize(new Dimension(InternalSettings.INITIAL_SCREEN_SIZE, InternalSettings.INITIAL_SCREEN_SIZE));

        contentPanel.add(choicePanel, Integer.valueOf(1));
        contentPanel.add(operationsPanel, Integer.valueOf(1));
        contentPanel.add(cellPanel, Integer.valueOf(0));
        contentPanel.add(infoPanel, Integer.valueOf(1));
        contentPanel.add(tintedGlassPanel, Integer.valueOf(2));
        contentPanel.add(settingsPanel, Integer.valueOf(3));
        contentPanel.add(missionsPanel, Integer.valueOf(3));

        setVisible(true);
        setExtendedState(MAXIMIZED_BOTH);
        refresh();

        settings.toggleCursor(InternalSettings.CUSTOM_CURSOR);
        addMouseListener();
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
                if(internalSettings.getCellWidth() > 0 && internalSettings.getCellHeight() > 0) {
                    Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D) g.create());

                    // draw gray background
                    gr.setColor(Color.lightGray);
                    gr.fillRect(0, 0, internalSettings.getScreenWidth(), internalSettings.getScreenHeight());

                    drawCells(gr);

                    // In case GPS mode is enabled, the route to the nearest TownHall is indicated (minimalistic).
                    // TODO remove grid overlay
                    if(gps) {
                        Cell viewpoint = player.get().getViewPoint();

                        // find the nearest townhall
                        Cell nearestTownHall =
                                player.get().getObjects().stream()
                                        .filter(obj -> obj instanceof TownHall)
                                        .min((obj1, obj2) -> Integer.min(obj1.getCell().getLocation().distanceTo(viewpoint.getLocation())
                                                , obj2.getCell().getLocation().distanceTo(viewpoint.getLocation())))
                                        .map(GameObject::getCell)
                                        .orElse(viewpoint);

                        gr.setColor(Color.black);
                        Stroke oldStroke =  gr.getStroke();
                        gr.setStroke(new BasicStroke(5));
                        Location drawLoc = nearestTownHall.getLocation().add(viewpoint.getLocation().negative());
                        gr.drawRoundRect((int)(drawLoc.x() * internalSettings.getCellWidth() - 10), (int)(drawLoc.y() * internalSettings.getCellHeight() - 10), (int)internalSettings.getCellWidth() + 20,
                                (int)internalSettings.getCellHeight() + 20, 20, 20);
                        gr.setStroke(oldStroke);

                        if (drawLoc.x() >= NUMBER_OF_CELLS_IN_VIEW)
                            gr.fillOval(this.getWidth() - 60, this.getHeight() / 2 - 10, 20, 20);
                        else if(drawLoc.x() < 0)
                            gr.fillOval(40, this.getHeight() / 2 - 10, 20, 20);

                        if (drawLoc.y() >= NUMBER_OF_CELLS_IN_VIEW)
                            gr.fillOval(this.getWidth() / 2 - 10, this.getHeight() - 60, 20, 20);
                        else if(drawLoc.y() < 0)
                            gr.fillOval(this.getWidth() / 2 - 10, 40, 20, 20);
                    }

                    // indicate cell over which the active player is hovering the mouse
                    // but only if the player has already spotted that cell
                    Cell p = posToCellCoord(mouseX, mouseY, 0);
                    if(player.get().hasSpotted(player.get().getViewPoint().fetch(p.getLocation()))) {

                        int leftEdge = (int) (p.getX() * internalSettings.getCellWidth());
                        int rightEdge = (int) ((p.getX() + 1) * internalSettings.getCellWidth());
                        int topEdge = (int) (p.getY() * internalSettings.getCellHeight());
                        int bottomEdge = (int) ((p.getY() + 1) * internalSettings.getCellHeight());


                        Color altColor = player.get().getAlternativeColor();
                        altColor = new Color(altColor.getRed(), altColor.getGreen(), altColor.getBlue(), 20);
                        gr.setColor(altColor);
                        gr.fillRect(leftEdge + HOVER_SHIFT, topEdge + HOVER_SHIFT, (int) internalSettings.getCellWidth(),
                                (int) internalSettings.getCellHeight());

                        // draw small shadows around hovered cell to imitate raising

                        int[] Xs = {leftEdge + HOVER_SHIFT, leftEdge, rightEdge, rightEdge};      // bottom shadow
                        int[] Ys = {bottomEdge + HOVER_SHIFT, bottomEdge, bottomEdge, bottomEdge + HOVER_SHIFT};
                        gr.setColor(new Color(50, 50, 50));
                        gr.fillPolygon(Xs, Ys, 4);

                        Xs = new int[]{rightEdge + HOVER_SHIFT, rightEdge, rightEdge, rightEdge + HOVER_SHIFT};     // right shadow
                        Ys = new int[]{bottomEdge, bottomEdge, topEdge, topEdge + HOVER_SHIFT};
                        gr.fillPolygon(Xs, Ys, 4);
                    }

                    // draw all relevant paths
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
                        case KeyEvent.VK_M -> missionsPanel.getView().setVisible(true);
                        case KeyEvent.VK_S -> settingsPanel.getView().setVisible(true);
                        default -> done = false;
                    }
                } else switch(event.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        selected.get().ifPresentOrElse(_ -> hidePanels(true), () -> cellPanel.setVisible(false));
                        break;
                    case KeyEvent.VK_N:
                        if (gameState.get() == Main.GameState.PLAYING) {
                            hoverPath.unset();
                            selected.unset();
                            playerCounter.set(playerCounter.get() + 1);
                            cellPanel.generateCycleAnimation();
                        }
                        break;
                    case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN:
                        dispatchArrowKeys(event);
                        break;
                    default:
                        if(choicePanel.isVisible())
                            return choicePanel.getView().dispatchKeyEvent(event);
                        else
                            return false;
                }
            } else
                return false;

            return done;
        });
    }

    private void dispatchArrowKeys(KeyEvent e) {
        Cell targetCell = null;
        Cell targetViewpoint = null;

        switch(e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if(player.get().getViewPoint().getX() > 0) {
                    targetCell = cellPanel.getCurrentCell().fetch(-1, 0, 0);
                    targetViewpoint = player.get().getViewPoint().fetch(-1, 0, 0);
                }
                break;
            case KeyEvent.VK_RIGHT:
                if(player.get().getViewPoint().getX() < InternalSettings.NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    targetCell = cellPanel.getCurrentCell().fetch(1, 0, 0);
                    targetViewpoint = player.get().getViewPoint().fetch(1, 0, 0);
                }
                break;
            case KeyEvent.VK_UP:
                if(player.get().getViewPoint().getY() > 0) {
                    targetCell = cellPanel.getCurrentCell().fetch(0, -1, 0);
                    targetViewpoint = player.get().getViewPoint().fetch(0, -1, 0);
                }
                break;
            case KeyEvent.VK_DOWN:
                if(player.get().getViewPoint().getY() < InternalSettings.NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    targetCell = cellPanel.getCurrentCell().fetch(0, 1, 0);
                    targetViewpoint = player.get().getViewPoint().fetch(0, 1, 0);
                }
        }

        // Check if target cell lies within map boundary and whether player has visited it before
        if (targetCell != null && player.get().hasVisited(targetCell)) {
            player.get().changeViewpoint(targetViewpoint);
            if(cellPanel.isVisible())
                cellPanel.updateContent(targetCell, player.get());
        }
    }

    /**
     * Adds {@code MouseListener}s to the game window.
     */
    private void addMouseListener() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                mouseX = e.getX(); // TODO Merge clickPos and mousePos variables!
                mouseY = e.getY();

                Cell mousePos = posToCellCoord(mouseX, mouseY, player.get().getViewPoint().getZ());
                Cell absoluteMousePos = mousePos.fetch(player.get().getViewPoint().getLocation());

                // only change cell menu content when player is hovering over game frame
                if(!cellPanel.isVisible()) {
                    if (player.get().hasVisited(absoluteMousePos))
                        setCellMenuContent(absoluteMousePos);
                    else
                        setCellMenuContent(null);
                }

                // Moves info panel to make sure that the underlying cells are reachable.
                selected.ifPresent( obj -> {
                    if(infoPanel != null)
                        layout.putConstraint(SpringLayout.WEST, infoPanel, (mousePos.getX() > (NUMBER_OF_CELLS_IN_VIEW - 4)) ? 10 : (int)(internalSettings.getScreenWidth() - 2 * internalSettings.getCellWidth() - 30), SpringLayout.WEST, contentPanel);

                    if(obj.getPlayer().equals(player.get()) && (obj instanceof Unit unit) && (hoverPath.get().isEmpty() || !mousePos.getLocation().equals(destination)) && !absoluteMousePos.isEndOfMap()) {
                        ArrayList<Location> path = cells.getShortestAdmissiblePath(absoluteMousePos);

                        if (path != null) {
                            Motion motion = new Motion(unit, path, cells.getPathDistance(absoluteMousePos));
                            if(motion.length() > 0) {
                                hoverPath.setOptional(motion.getRelativePath());
                                travelDistance = cells.getPathDistance(absoluteMousePos);
                            } else
                                hoverPath.unset();
                        }
                    }

                    destination = mousePos.getLocation();
                });
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                if(InternalSettings.GAME_3D) {
                    if (e.getWheelRotation() != 0 && !player.get().isViewLocked()) {
                        player.get().changeViewpoint(player.get().getViewPoint().fetch(0, 0,
                                e.getWheelRotation()));
                        viewMenu.setText("View: " + (player.get().getViewPoint().getZ()));
                        selected.unset();

                        hidePanels(true);
                        refreshMenubar();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Location oldPos = clickLocation;
                clickLocation = player.get().getViewPoint().fetch(posToCellCoord(e.getX(), e.getY(),
                        player.get().getViewPoint().getZ()).getLocation()).getLocation();

                // In case the selected cell lies within the map boundaries
                if(!cells.get(clickLocation).isEndOfMap()) {

                    // reset the current routing indicator on click
                    hoverPath.unset();

                    // Any existing panels should be hidden on click
                    hidePanels(true);

                    //left-click events
                    if (SwingUtilities.isLeftMouseButton(e)) {

                        // Unselect GameObjects on left click
                        if (clicked.get()) {
                            selected.unset();
                            target.unset();
                        }

                        // If clicked on cell with objects, show info panel
                        // TODO Change this check to cells within view distance
                        if (player.get().getObjects().stream().anyMatch(obj -> obj.getCell().getLocation().equals(clickLocation))) {
                            cellPanel.updateContent(cells.get(clickLocation), player.get());
                            cellPanel.setVisible(true);
                            clicked.set(true);
                        } else
                            clicked.set(false);

                        // right-click events
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        selected.ifPresent(obj -> {

                            if(obj instanceof Unit unit) {
                                ArrayList<Location> path = cells.getShortestAdmissiblePath(cells.get(clickLocation));

                                if (path != null) {
                                    Motion motion = new Motion(unit, path, cells.getPathDistance(cells.get(clickLocation)));
                                    hoverPath.unset();
                                    if (cells.get(clickLocation).getUnitSpace() - cells.get(clickLocation).getUnitOccupied() >= unit.getSize()
                                            && motion.length() != 0) {
                                        unit.increaseEnergy(-travelDistance);
                                        motionToThread(motion);
                                    }
                                }
                            }
                        });
                        selected.unset();
                    }
                } else
                    clickLocation = oldPos;
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
            settingsPanel.getView().setVisible(true);
            contentPanel.requestFocus();
        });
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(_ -> System.exit(0));
        JCheckBoxMenuItem saveBox = new JCheckBoxMenuItem("Save enabled");
        saveBox.setState(settings.save());
        saveBox.addActionListener(_ -> settings.save(saveBox.getState()));
        settingsMenu.add(viewItem);
        settingsMenu.add(saveBox);
        settingsMenu.add(closeItem);
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
        settingsPanel.getView().setVisible(false);
    }

    public JPanel showMessageBox(String text) {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());
//                gr.setColor(new Color(200, 150, 0, 76));
                gr.setColor(new Color(200, 150, 0));
                gr.fillRoundRect(1, 1, (int)(2 * internalSettings.getCellWidth()) - 2, getHeight() - 2, CustomBorder.RADIUS, CustomBorder.RADIUS);
                gr.dispose();
            }
        };

        panel.setPreferredSize(new Dimension((int)(2 * internalSettings.getCellWidth()), (int)(2 * internalSettings.getCellHeight())));
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
        layout.putConstraint(SpringLayout.WEST, panel, (int)(internalSettings.getScreenWidth() - 2 * internalSettings.getCellWidth() - 50), SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, panel, 50, SpringLayout.NORTH, getContentPane());

        player.bindSingle(_ -> getContentPane().remove(panel));
        area.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                getContentPane().remove(panel);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);

                // Dispatch to parent
                panel.dispatchEvent(new MouseEvent(area, e.getID(), e.getWhen(),
                        e.getModifiersEx(), e.getPoint().x, e.getPoint().y, e.getClickCount(), e.isPopupTrigger()));
            }
        });

        return panel;
    }

    /**
     * Draws the base layer of all (visible) cells.
     * @param gr Graphics object of the game panel
     */
    private void drawCells(Graphics2D gr) {

        Cell mousePos = posToCellCoord(mouseX, mouseY, 0);
        Cell viewPoint = player.get().getViewPoint();

        /*
         * Draw all (visible) cells
         */
        for (int x = 0; x < NUMBER_OF_CELLS_IN_VIEW; x++) {
            for (int y = 0; y < NUMBER_OF_CELLS_IN_VIEW; y++) {
                Cell currentCell = cells.get(viewPoint.getLocation().add(x, y, 0));

                // do not draw cells that have not been visited
                if(!player.get().hasSpotted(currentCell))
                    continue;

                int cellShift = 0;
                if (mousePos.getX() == x && mousePos.getY() == y)
                    cellShift = HOVER_SHIFT;

                // draw field effect, else draw white background
                if (currentCell.isField())
                    gr.setColor(new Color(200, 120, 0, 100));
                else
                    gr.setColor(new Color(255, 255, 255));

                gr.fillRect((int)(x * internalSettings.getCellWidth()) + cellShift,
                        (int)(y * internalSettings.getCellHeight()) + cellShift,
                        (int)(internalSettings.getCellWidth()), (int)(internalSettings.getCellHeight()));

                // draw forest effect
                if (currentCell.isForest()) {
                    gr.setColor(new Color(20, 150, 20));
                    gr.fillArc((int)(x * internalSettings.getCellWidth() - internalSettings.getPoolSize()) + cellShift,
                            (int)((y + 1) * internalSettings.getCellHeight() - internalSettings.getPoolSize()) + cellShift,
                            (int)(2 * internalSettings.getPoolSize()), (int)(2 * internalSettings.getPoolSize()), 0, 90);
                }

                //draw river effect
                if (currentCell.isRiver()) {
                    gr.setColor(new Color(0, 100, 255));
                    if (currentCell.getX() < InternalSettings.NUMBER_OF_CELLS - 2 && currentCell.getY() < InternalSettings.NUMBER_OF_CELLS - 2 && currentCell.fetch(1, 0, 0).isRiver() && currentCell.fetch(0, 1, 0).isRiver() && currentCell.fetch(1, 1, 0).isRiver())
                        gr.fillRoundRect(Math.round((x + 0.5f) * internalSettings.getCellWidth() - (internalSettings.getPoolSize() / 2f)) + cellShift,
                                Math.round((y + 0.5f) * internalSettings.getCellHeight() - (internalSettings.getPoolSize() / 2f)) + cellShift,
                                (int)(internalSettings.getCellWidth() + internalSettings.getPoolSize()),
                                (int)(internalSettings.getCellHeight() + internalSettings.getPoolSize()),
                                (int)internalSettings.getPoolSize(),
                                (int)internalSettings.getPoolSize());
                    else {
                        boolean singleFlag = true;
                        if (currentCell.getX() < InternalSettings.NUMBER_OF_CELLS - 1 && currentCell.fetch(1, 0, 0).isRiver())
                            gr.fillRect(Math.round((x + 0.5f) * internalSettings.getCellWidth()) + cellShift,
                                    Math.round((y + 0.5f) * internalSettings.getCellHeight() - (internalSettings.getPoolSize() / 2f)) + cellShift,
                                    Math.round(internalSettings.getCellWidth() / 2f) + 1,
                                    (int)internalSettings.getPoolSize());
                        if (currentCell.getX() > 0 && currentCell.fetch(-1, 0, 0).isRiver())
                            gr.fillRect((int)(x * internalSettings.getCellWidth() - 1) + cellShift,
                                    Math.round((y + 0.5f) * internalSettings.getCellHeight() - (internalSettings.getPoolSize() / 2f)) + cellShift,
                                    Math.round(internalSettings.getCellWidth() / 2f),
                                    (int)internalSettings.getPoolSize());
                        if (currentCell.getY() < InternalSettings.NUMBER_OF_CELLS - 1 && currentCell.fetch(0, 1, 0).isRiver()) {
                            gr.fillRect(Math.round((x + 0.5f) * internalSettings.getCellWidth() - (internalSettings.getPoolSize() / 2f)) + cellShift,
                                    Math.round((y + 0.5f) * internalSettings.getCellHeight()) + cellShift,
                                    (int)internalSettings.getPoolSize(),
                                    Math.round(internalSettings.getCellHeight() / 2f));
                            singleFlag = false;
                        }
                        if (currentCell.getY() > 0 && currentCell.fetch(0, -1, 0).isRiver()) {
                            gr.fillRect(Math.round((x + 0.5f) * internalSettings.getCellWidth() - (internalSettings.getPoolSize() / 2f)) + cellShift,
                                    (int)(y * internalSettings.getCellHeight()) + cellShift,
                                    (int)internalSettings.getPoolSize(),
                                    Math.round(internalSettings.getCellHeight() / 2f));
                            singleFlag = false;
                        }

                        if (singleFlag)
                            gr.fillOval(Math.round((x + 0.5f) * internalSettings.getCellWidth() - internalSettings.getPoolSize()) + cellShift,
                                    Math.round((y + 0.5f) * internalSettings.getCellHeight() - (internalSettings.getPoolSize() / 2f)) + cellShift,
                                    (int)(internalSettings.getPoolSize() * 2),
                                    (int)internalSettings.getPoolSize());
                        else
                            gr.fillOval(Math.round((x + 0.5f) * internalSettings.getCellWidth() - (internalSettings.getPoolSize() / 2f)) + cellShift,
                                    Math.round((y + 0.5f) * internalSettings.getCellHeight() - (internalSettings.getPoolSize() / 2f)) + cellShift,
                                    (int)internalSettings.getPoolSize(),
                                    (int)internalSettings.getPoolSize());
                    }
                }

                // draw all elements of the cell
                drawDetails(gr, currentCell, x, y);
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
        gr.fillOval(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()) - 5, Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()) - 5, 10, 10);

        Stroke oldStroke = gr.getStroke();
        gr.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{9}, 0));

        for (int i = 1; i < path.length - 1; i++) {
            current = current.fetch(path[i].x(), path[i].y(), path[i].z());
            Location previous = path[i];
            Location next = path[i + 1];

            if (previous.x() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()), (int)(current.getX() * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()));
            else if (previous.x() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()), (int)((current.getX() + 1) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()));
            else if (previous.y() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()), Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), (int)(current.getY() * internalSettings.getCellHeight()));
            else if (previous.y() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()), Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), (int)((current.getY() + 1) * internalSettings.getCellHeight()));

            if (next.x() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()), (int)((current.getX() + 1) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()));
            else if (next.x() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()), (int)(current.getX() * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()));
            else if (next.y() == 1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()), Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), (int)((current.getY() + 1) * internalSettings.getCellHeight()));
            else if (next.y() == -1)
                gr.drawLine(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()), Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()), (int)(current.getY() * internalSettings.getCellHeight()));
        }

        Location finalMove = path[path.length - 1];
        current = current.fetch(finalMove.x(), finalMove.y(), finalMove.z());

        // Draws the destination circle with corresponding distance
        gr.setStroke(oldStroke);
        gr.drawOval(Math.round((current.getX() + 0.5f) * internalSettings.getCellWidth()) - 10, Math.round((current.getY() + 0.5f) * internalSettings.getCellHeight()) - 10, 20, 20);
        String distanceText = String.valueOf(travelDistance);
        gr.drawString(distanceText, (current.getX() + 0.5f) * internalSettings.getCellWidth() - gr.getFontMetrics().stringWidth(distanceText) / 2f, (current.getY() + 0.5f) * internalSettings.getCellHeight() + (float)(gr.getFont().createGlyphVector(gr.getFontRenderContext(), distanceText).getVisualBounds().getHeight() / 2));
    }

    /**
     * Draws currently visible/relevant game objects in the provided cell
     * @param gr Graphics object of the game panel
     */
    private void drawDetails(Graphics2D gr, Cell currentCell, int x, int y) {
        Cell viewpoint = player.get().getViewPoint();
        Cell mousePos = posToCellCoord(mouseX, mouseY, 0);

        // shift to indicate the hovered cell
        int cellShift = 0;
        if (mousePos.getX() == x && mousePos.getY() == y)
            cellShift = HOVER_SHIFT;

        // set player color as brush color
        gr.setColor(player.get().getColor());

        // fetch a list of all players that have an object in the current cell
        List<Player> playersInCell = new ArrayList<>(currentCell.getObjects().stream().map(GameObject::getPlayer).distinct().toList());

        // if the active player has an object in the current cell, indicate the cell
        if(playersInCell.contains(player.get())) {
            gr.fillOval((int)(x * internalSettings.getCellWidth() + 5) + cellShift,
                    (int)(y * internalSettings.getCellHeight() + 5) + cellShift,
                    10, 10);
            playersInCell.remove(player.get());
        }

        // fill all cells outside the map boundary in black
        if(currentCell.isEndOfMap()) {
            gr.setColor(Color.black);
            gr.fillRect((int)(x * internalSettings.getCellWidth() - 1) + cellShift,
                    (int)(y * internalSettings.getCellHeight() - 1) + cellShift,
                    (int)internalSettings.getCellWidth() + 2,
                    (int)internalSettings.getCellHeight() + 2);
        }
//                //  hide all cells that the active player has not visited nor spotted
//                else if (!player.get().hasSpotted(currentCell)) {
//                    gr.setColor(Color.lightGray);
//                    gr.fillRect((int)(x * internalSettings.getCellWidth() - 1) + cellShift,
//                            (int)(y * internalSettings.getCellHeight() - 1) + cellShift,
//                            (int)internalSettings.getCellWidth() + 2,
//                            (int)internalSettings.getCellHeight() + 2);
//                }
        // blur all cells that the active player has only spotted
        else if (!player.get().hasVisited(currentCell)) {
            drawWalls(gr, currentCell, x, y);
            gr.setColor(new Color(192, 192, 192, 200));
            gr.fillRect((int)(x * internalSettings.getCellWidth() - 1) + cellShift,
                    (int)(y * internalSettings.getCellHeight() - 1) + cellShift,
                    (int)internalSettings.getCellWidth() + 2,
                    (int)internalSettings.getCellHeight() + 2);
        } else {

            // draw walls (if any)
            drawWalls(gr, currentCell, x, y);

            // indicate if the temperature of this cell is below 'freezing point'
            if (currentCell.getHeatLevel() <= InternalSettings.COLD_LEVEL)
                gr.drawImage(InternalSettings.CLOUD, (int)((x + 1) * internalSettings.getCellWidth() - InternalSettings.CLOUD.getWidth() - 5) + cellShift,
                        (int)(y * internalSettings.getCellHeight() + 5) + cellShift, null);
            // indicate if the temperature of this cell is above the 'boiling point'
            else if (currentCell.getHeatLevel() >= InternalSettings.HOT_LEVEL)
                gr.drawImage(InternalSettings.SUN, (int)((x + 1) * internalSettings.getCellWidth() - InternalSettings.SUN.getWidth() - 5) + cellShift,
                        (int)(y * internalSettings.getCellHeight() + 5) + cellShift, null);

            // indicate all other players that have a game object in the current cell (which should only
            // happen when the player can see this cell)
            for(int i = 0; i < playersInCell.size(); i++) {
                gr.setColor(playersInCell.get(i).getColor());
                gr.drawOval((int)(x * internalSettings.getCellWidth() + 5 + 15 * (i + 1)) + cellShift,
                        (int)(y * internalSettings.getCellHeight() + 5) + cellShift,
                        10,
                        10);
            }
        }

    }

    /**
     * Draws all walls in the provided cell (if any).
     * @param gr graphics object
     * @param cell current cell
     * @param x x-coordinate of cell on screen
     * @param y y-coordinate of cell on screen
     */
    private void drawWalls(Graphics2D gr, Cell cell, int x, int y) {
        // set wall color
        gr.setColor(new Color(80, 40, 10));

        // shift to indicate the hovered cell
        Cell mousePos = posToCellCoord(mouseX, mouseY, 0);
        int cellShift = 0;
        if (mousePos.getX() == x && mousePos.getY() == y)
            cellShift = HOVER_SHIFT;

        // store old stroke style
        Stroke oldStroke = gr.getStroke();

        //set stroke style of wall
        int strokeWidth = 10;
        gr.setStroke(new BasicStroke(strokeWidth));

        // fetch all walls in current cell
        var walls = cell.getObjects().stream()
                .filter(Wall.class::isInstance)
                .map(obj -> ((Wall)obj).getDirection())
                .distinct()
                .toList();

        // draw all walls
        for(Direction d : walls) {
            switch(d) {
                case NORTH -> gr.drawLine((int)(x * internalSettings.getCellWidth()) + strokeWidth / 2 + cellShift,
                        (int)(y * internalSettings.getCellHeight()) + strokeWidth / 2 + cellShift,
                        (int)((x + 1) * internalSettings.getCellWidth()) - strokeWidth / 2 + cellShift,
                        (int)(y * internalSettings.getCellHeight()) + strokeWidth / 2 + cellShift);
                case SOUTH -> gr.drawLine((int)(x * internalSettings.getCellWidth()) + strokeWidth / 2 + cellShift,
                        (int)((y + 1) * internalSettings.getCellHeight()) - strokeWidth / 2 + cellShift,
                        (int)((x + 1) * internalSettings.getCellWidth()) - strokeWidth / 2 + cellShift,
                        (int)((y + 1) * internalSettings.getCellHeight()) - strokeWidth / 2 + cellShift);
                case WEST -> gr.drawLine((int)(x * internalSettings.getCellWidth()) + strokeWidth / 2 + cellShift,
                        (int)(y * internalSettings.getCellHeight()) + strokeWidth / 2 + cellShift,
                        (int)(x * internalSettings.getCellWidth()) + strokeWidth / 2 + cellShift,
                        (int)((y + 1) * internalSettings.getCellHeight()) - strokeWidth / 2 + cellShift);
                case EAST -> gr.drawLine((int)((x + 1) * internalSettings.getCellWidth()) - strokeWidth / 2 + cellShift,
                        (int)(y * internalSettings.getCellHeight()) + strokeWidth / 2 + cellShift,
                        (int)((x + 1) * internalSettings.getCellWidth()) - strokeWidth / 2 + cellShift,
                        (int)((y + 1) * internalSettings.getCellHeight()) - strokeWidth / 2 + cellShift);
            }
        }

        // reset stroke style
        gr.setStroke(oldStroke);
    }

    public void cycleAnimation() {
        cellPanel.cycleAnimation();
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

        Cell cell = cells.get(clickLocation);
        for(int i = 0; i < resources.length; i++)
            resourceLabels[i].setText(resources[i] + ": " + cell.getResource(resources[i]));
    }

    /**
     * Recalculates screen size and rescales derived dimensions.
     */
    public void resetScales() {
        internalSettings.setScreenWidth(getContentPane().getWidth());
        internalSettings.setScreenHeight(getContentPane().getHeight());
        Sprite.setSpriteSize(internalSettings.getSpriteSize());
        Sprite.resizeSprites(internalSettings.getSpriteSize());
    }

    private void refresh() {
        resetScales();

        tintedGlassPanel.setPreferredSize(new Dimension(internalSettings.getScreenWidth(), internalSettings.getScreenHeight()));
        layout.putConstraint(SpringLayout.WEST, tintedGlassPanel, 0, SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, tintedGlassPanel, 0, SpringLayout.NORTH, contentPanel);

        choicePanel.getView().resizePanel((int)internalSettings.getCellWidth(), (int)internalSettings.getCellHeight());
        layout.putConstraint(SpringLayout.WEST, choicePanel, Math.round((internalSettings.getScreenWidth() - 3 * internalSettings.getCellWidth()) / 2f), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, choicePanel, Math.round(internalSettings.getScreenHeight() - 3 * internalSettings.getCellHeight()), SpringLayout.NORTH, contentPanel);

        operationsPanel.getView().resizePanel((int)internalSettings.getCellWidth(), (int)internalSettings.getCellHeight());
        layout.putConstraint(SpringLayout.WEST, operationsPanel, Math.round((internalSettings.getScreenWidth() - 3 * internalSettings.getCellWidth()) / 2f), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, operationsPanel, Math.round(internalSettings.getScreenHeight() - 3 * internalSettings.getCellHeight()), SpringLayout.NORTH, contentPanel);

        cellPanel.setPreferredSize(new Dimension((int)((NUMBER_OF_CELLS_IN_VIEW - 2) * internalSettings.getCellWidth() + 40), ( int)((NUMBER_OF_CELLS_IN_VIEW - 2) * internalSettings.getCellHeight() + 40)));
        layout.putConstraint(SpringLayout.WEST, cellPanel, (int)(internalSettings.getCellWidth() - 20), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, cellPanel, (int)(internalSettings.getCellHeight() - 20), SpringLayout.NORTH, contentPanel);

//        infoPanel.resizePanel((int)internalSettings.getCellWidth(), (int)internalSettings.getCellHeight());
        infoPanel.getView().resizePanel((int)internalSettings.getCellWidth(), (int)internalSettings.getCellHeight());
        layout.putConstraint(SpringLayout.WEST, infoPanel,
                (clickLocation.x() - player.get().getViewPoint().getX()) >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f) ? 10 : (int)(internalSettings.getScreenWidth() - 2 * internalSettings.getCellWidth() - 30), SpringLayout.WEST, contentPanel);
        layout.putConstraint(SpringLayout.NORTH, infoPanel, 10, SpringLayout.NORTH, contentPanel);

        settingsScroller.setPreferredSize(new Dimension((int)(4 * internalSettings.getCellWidth()), (int)(5 * internalSettings.getCellHeight())));
        layout.putConstraint(SpringLayout.WEST, settingsPanel, (int)(2 * internalSettings.getCellWidth()), SpringLayout.WEST,
                contentPanel);
        layout.putConstraint(SpringLayout.NORTH, settingsPanel, (int)internalSettings.getCellHeight(), SpringLayout.NORTH,
                contentPanel);

        missionScroller.setPreferredSize(new Dimension((int)(4 * internalSettings.getCellWidth()), (int)(5 * internalSettings.getCellHeight())));
        layout.putConstraint(SpringLayout.WEST, missionsPanel, (int)(2 * internalSettings.getCellWidth()), SpringLayout.WEST,
                contentPanel);
        layout.putConstraint(SpringLayout.NORTH, missionsPanel, (int)internalSettings.getCellHeight(), SpringLayout.NORTH,
                contentPanel);

        contentPanel.revalidate();
    }

    public void hidePanels(boolean alsoCellPanel) {
        infoPanel.getView().setVisible(false);
        operationsPanel.getView().setVisible(false);
        choicePanel.getView().setVisible(false);
        settingsPanel.getView().setVisible(false);
        missionsPanel.getView().setVisible(false);
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
        return cells.get(new Location((int)(x / internalSettings.getCellWidth()), (int)(y / internalSettings.getCellHeight()), h));
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

    public void setCellMenuContent(Cell cell) {
        if (cell != null) {
            cellMenu.setText("Space: " + cell.getUnitOccupied() + "/" + cell.getUnitSpace() +
                    " | " + cell.getBuildingOccupied() + "/" + cell.getBuildingSpace());
            cellMenu.setEnabled(true);
        }
        else {
            cellMenu.setText("Cell is unknown");
            cellMenu.setEnabled(false);
        }
    }
}
