package general;

import core.*;

import items.*;
import items.buildings.Lumberjack;
import items.buildings.MainBuilding;
import items.units.Villager;
import items.upgrade.Upgrade;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.swing.MenuSelectionManager.defaultManager;

import static core.GameConstants.*;
import static core.Option.*;
import static core.Type.*;

public class Main extends JFrame{

    private int mouseX, mouseY, travelDistance;
    private Location clickPos;
    private Location[] hoverPath;
    private int cellWidth, cellHeight, poolSize, screenWidth, screenHeight;
    private int current, cycle, upgradePage, constructPage, operationsPage;
    private boolean clicked;

    private final HashMap<Location, Cell> cells;
    private final ArrayList<Player> players;
    private List<Upgrade> upgrades;
    private OperationsList products;
    private OperationsList operations;
    private GameObject selected;

    private JMenu playerMenu, viewMenu, cellMenu;
    private JMenuItem cycleLabel, popLabel;
    private JMenuItem[] playerLabels, resourceLabels;
    private JPanel operationPanel, infoPanel, resourcePanel, upgradePanel, constructPanel;
    private final Timer gc;

    public static void main(String[] args) {
        try {
            Main mainframe = new Main();
            mainframe.addPlayer(new Player("You", Color.blue, Color.magenta));
            mainframe.addPlayer(new AI("AI", Color.red, Color.yellow, mainframe));
            mainframe.construct();
        } catch (TypeException e) {
            e.printStackTrace();
        }
    }

    public Main() {
        cells = new HashMap<>();
        players = new ArrayList<>();
        upgrades = new ArrayList<>();
        cycle = 1;
        clickPos = new Location(0, 0, 0);

        for(int i = 0; i < NUMBER_OF_CELLS; i++)
            for(int j = 0; j < NUMBER_OF_CELLS; j++)
                cells.put(new Location(i, j, 0), new Cell(INITIAL_CELL_UNIT_SPACE, INITIAL_CELL_BUILDING_SPACE));

        gc = new Timer(500, e -> {
            for(Player p : players) {
                for(GameObject obj : p.getNewObjects())
                    addObject(obj);

                for (GameObject obj : p.getObjects())
                    if (obj.checkStatus(DESTROY))
                        removeObject(obj);
            }
            refreshWindow();
        });
        gc.start();
    }

    public Cell getCell(Location loc) { return cells.get(loc); }

    public void addPlayer(Player p) throws TypeException {
        players.add(p);

        GameObject base = new FreeDecorator<>(new MainBuilding(p, p.getViewPoint().add(new Location(2, 2, 0))));
        GameObject lumber = new FreeDecorator<>(new Lumberjack(p, p.getViewPoint().add(new Location(2, 5, 0))));
        GameObject v1 = new FreeDecorator<>(new Villager(p, p.getViewPoint().add(new Location(2, 1, 0))));
        GameObject v2 = new FreeDecorator<>(new Villager(p, p.getViewPoint().add(new Location(2, 1, 0))));

        base.perform(CONSTRUCT);
        lumber.perform(CONSTRUCT);
        v1.perform(CONSTRUCT);
        v2.perform(CONSTRUCT);
    }

    /**
     * Changes the current player
     */
    public void cyclePlayers() throws TypeException{

        for(GameObject object : players.get(current).getObjects())
            while (object.checkStatus(CONTRACT)
                    && object.getValue(Option.STATUS) != GameConstants.WALKING_STATUS
                    && object.getValue(ENERGY) > 0)
                object.perform(WORK);

        if(current == (players.size() - 1)) {
            cycle++;
            current = 0;
            for(Cell cell : cells.values())
                cell.cycle(cycle);
        } else
            current++;

        for(String text : players.get(current).getMessages())
            showMessagePanel(text);

        for(GameObject object : players.get(current).getObjects()) {
            object.perform(TOTAL_CYCLE);
            object.perform(DEGRADE);
            object.typedDo(UNIT, (u, params) -> {
                if(u.getValue(STATUS) == HEALING_STATUS)
                    u.changeValue(HEALTH, u.getValue(HEAL));
            });
            object.changeValue(HEALTH, Math.min(0, cells.get(object.getLocation()).getHeatLevel() - COLD_LEVEL));
            cells.get(object.getLocation()).changeResources(object.getResources(Option.SOURCE));
        }

        selected = null;
        hoverPath = null;
        upgradePage = 0;
        upgrades = null;

        hidePanels();
        refreshWindow();

        if(players.get(current) instanceof AI)
            ((AI) players.get(current)).makeMove(cycle);
    }

    /**
     * Adds a new game object to the game
     * @param object new object
     */
    public boolean addObject(GameObject object) {
        try {
            Location loc = object.getLocation();
            if((object.getTypes().contains(UNIT) && cells.get(loc).getUnitAvailable() >= object.getValue(SIZE)) ||
                    (object.getTypes().contains(BUILDING) && cells.get(loc).getBuildingAvailable() >= object.getValue(SIZE))) {

                object.typedDo(UNIT, (u, params) -> cells.get(loc).changeUnitOccupied(u.getValue(SIZE)));
                object.typedDo(BUILDING, (b, params) -> cells.get(loc).changeBuildingOccupied(b.getValue(SIZE)));
                object.typedDo(SPACER, (b, params) -> cells.get(loc).changeUnitSpace(b.getValue(SPACE)));
                object.typedDo(OBSTRUCTION, (o, params) -> cells.get(loc).changeTravelCost(100));

                return true;
            }
        } catch(TypeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Removes a game object from the game
     * @param object object to remove
     */
    public void removeObject(GameObject object) {
        try {
            Location loc = object.getLocation();
            object.typedDo(UNIT, (u, params) -> cells.get(loc).changeUnitOccupied(-u.getValue(SIZE)));
            object.typedDo(BUILDING, (b, params) -> cells.get(loc).changeBuildingOccupied(-b.getValue(SIZE)));
            object.typedDo(BUILDING, (b, params) -> cells.get(loc).changeUnitSpace(-b.getValue(SPACE)));
            object.typedDo(OBSTRUCTION, (o, params) -> cells.get(loc).changeTravelCost(-100));
        } catch(TypeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Moves an existing object
     * @param object object to move
     * @param target new Location of object
     */
    public void moveObject(GameObject object, Location target) {

        try {
            if (!object.getLocation().equals(target)) {
                object.typedDo(UNIT, (u, params) -> cells.get(object.getLocation()).changeUnitOccupied(-u.getValue(SIZE)));
                object.typedDo(UNIT, (u, params) -> cells.get(target).changeUnitOccupied(u.getValue(SIZE)));
                object.typedDo(BUILDING, (b, params) -> cells.get(object.getLocation()).changeBuildingOccupied(-b.getValue(SIZE)));
                object.typedDo(BUILDING, (b, params) -> cells.get(object.getLocation()).changeUnitSpace(-b.getValue(SPACE)));
                object.typedDo(BUILDING, (b, params) -> cells.get(target).changeBuildingOccupied(b.getValue(SIZE)));
                object.typedDo(BUILDING, (b, params) -> cells.get(target).changeUnitSpace(b.getValue(SPACE)));

                object.getPlayer().discover(target);
                int sight = object.getValue(SIGHT);
                for(int x = -sight; x < sight + 1; x++)
                    for(int y = -sight; y < sight + 1; y++)
                        if(Math.abs(x) + Math.abs(y) <= sight)
                            object.getPlayer().spot(target.add(x, y, 0));
                object.setLocation(target);
            }
        } catch(TypeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a game frame
     */
    private void construct() throws TypeException{
        super.setTitle("Game of Ages");

        screenWidth = 0;
        screenHeight = 0;
        cellWidth = 0;
        cellHeight = 0;
        poolSize = 0;
        clicked = false;

        // new content panel
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);

                try {
                    drawObjects(gr);
                } catch (TypeException e) {
                    e.printStackTrace();
                }

                gr.setColor(players.get(current).getAlternativeColor());
                gr.setStroke(new BasicStroke(2));
                Location p = posToPair(mouseX, mouseY, 0);
                gr.drawRect(p.x * cellWidth, p.y * cellHeight, cellWidth, cellHeight);
                if (hoverPath != null) {
                    gr.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                            0, new float[]{9}, 0));
                    for(int i = 1; i < hoverPath.length - 1; i++) {
                        Location loc = hoverPath[i].subtract(players.get(current).getViewPoint());
                        Location previous = hoverPath[i].subtract(hoverPath[i - 1]);
                        Location next = hoverPath[i + 1].subtract(hoverPath[i]);

                        if(previous.x == 1)
                            gr.drawLine(Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight), loc.x * cellWidth, Math.round((loc.y + 0.5f) * cellHeight));
                        else if(previous.x == -1)
                            gr.drawLine(Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight), (loc.x + 1) * cellWidth, Math.round((loc.y + 0.5f) * cellHeight));
                        else if(previous.y == 1)
                            gr.drawLine(Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight), Math.round((loc.x + 0.5f) * cellWidth), loc.y  * cellHeight);
                        else if(previous.y == -1)
                            gr.drawLine(Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight), Math.round((loc.x + 0.5f) * cellWidth), (loc.y + 1)  * cellHeight);

                        if(next.x == 1)
                            gr.drawLine(Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight), (loc.x + 1) * cellWidth, Math.round((loc.y + 0.5f) * cellHeight));
                        else if(next.x == -1)
                            gr.drawLine(Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight), loc.x * cellWidth, Math.round((loc.y + 0.5f) * cellHeight));
                        else if(next.y == 1)
                            gr.drawLine(Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight), Math.round((loc.x + 0.5f) * cellWidth), (loc.y + 1)  * cellHeight);
                        else if(next.y == -1)
                            gr.drawLine(Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight), Math.round((loc.x + 0.5f) * cellWidth), loc.y  * cellHeight);
                    }
                    Location loc = hoverPath[hoverPath.length - 1].subtract(players.get(current).getViewPoint());
                    gr.drawString(travelDistance + "", Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight));
                }
            }
        };
        contentPanel.setOpaque(false);
        super.setContentPane(contentPanel);
        SpringLayout layout = new SpringLayout();
        contentPanel.setLayout(layout);

        // Cycle players on "n" stroke
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('n'), "cycle");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "left");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "right");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "up");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "down");
        contentPanel.getActionMap().put("cycle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cyclePlayers();
                } catch (TypeException ex) {
                    ex.printStackTrace();
                }
            }
        });
        contentPanel.getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player player = players.get(current);
                if(player.getViewPoint().x > 0) {
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().add(-1, 0, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player player = players.get(current);
                if(player.getViewPoint().x < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().add(1, 0, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player player = players.get(current);
                if(player.getViewPoint().y > 0) {
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().add(0, -1, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player player = players.get(current);
                if(player.getViewPoint().y < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().add(0, 1, 0));
                    refreshWindow();
                }
            }
        });

        // Mouse input for game panel
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                mouseX = e.getX();
                mouseY = e.getY();

                if(infoPanel != null && selected != null) {
                    Location mousePos = posToPair(mouseX, mouseY, 0);
                    layout.putConstraint(SpringLayout.WEST, infoPanel, (mousePos.x >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f)) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                }

                Location loc = posToPair(e.getX(), e.getY(), 0);
                if(selected != null && (hoverPath == null || !loc.equals(hoverPath[hoverPath.length - 1]))) {
                    Motion motion = getShortestAdmissiblePath(selected, loc.add(players.get(current).getViewPoint()));
                    if (motion != null && motion.getSize() > 0) {
                        hoverPath = motion.getPath();
                        travelDistance = motion.getSize();
                    }
                    else
                        hoverPath = null;
                }

                refreshWindow();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                if(e.getWheelRotation() != 0 && !players.get(current).isViewLocked()) {
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().add(0, 0, e.getWheelRotation()));
                    viewMenu.setText("View: " + (players.get(current).getViewPoint().z));
                    selected = null;

                    hidePanels();
                    refreshWindow();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    clickPos = players.get(current).getViewPoint().add(posToPair(e.getX(), e.getY(), players.get(current).getViewPoint().z));
                    hidePanels();

                    /*
                     * left-click events
                     */
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        // Any existing info panels should be removed on click
                        if (clicked)
                            selected = null;

                        // If clicked on cell with objects, show info panel
                        if (players.get(current).getObjects().stream().anyMatch(obj -> obj.getLocation().equals(clickPos))) {
                            infoPanel = createInfoPanel();
                            contentPanel.add(infoPanel);
                            layout.putConstraint(SpringLayout.WEST, infoPanel, (clickPos.subtract(players.get(current).getViewPoint()).x >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f)) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                            layout.putConstraint(SpringLayout.NORTH, infoPanel, 10, SpringLayout.NORTH, contentPanel);

                            clicked = true;
                        } else
                            clicked = false;
                        // right-click events
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        if (selected != null) {
                            Motion motion = getShortestAdmissiblePath(selected, clickPos);
                            if(motion != null) {
                                hoverPath = null;
                                if (cells.get(clickPos).getUnitSpace() - cells.get(clickPos).getUnitOccupied() >= selected.getValue(SIZE)) {
                                    if(motion.getSize() == 0) {
                                        selected.changeValue(ENERGY, -motion.getSize());
                                        motionToThread(motion);
                                    }
                                    selected.typedDo(WORKER, (u, params) -> {
                                        createResourcePanel(cells.get(clickPos));
                                        operations = selected.getOperations();
                                        operationPanel.setVisible(true);
                                    });
                                }
                            }
                        }
                    }

                    // Redraw game panel
                    refreshWindow();
                } catch (TypeException te) {
                    te.printStackTrace();
                }
            }
        };
        contentPanel.addMouseListener(mouseAdapter);
        contentPanel.addMouseMotionListener(mouseAdapter);
        contentPanel.addMouseWheelListener(mouseAdapter);

        // Rescale game elements on screen resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                try {
                    resetScales();
                    constructUpgradePanel();
                    constructOperationPanel();
                    hidePanels();
                    refreshWindow();
                } catch (TypeException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Create menus
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        playerMenu = new JMenu("Player: " + players.get(current).getName());
        viewMenu = new JMenu("View: " + players.get(current).getViewPoint().z);
        cellMenu = new JMenu("Nothing to show");
        menubar.add(playerMenu);
        menubar.add(viewMenu);
        menubar.add(cellMenu);

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
            JMenuItem label = new JMenuItem(Resource.values()[i].name + ": " + players.get(current).getResource(Resource.values()[i]));
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
        popLabel = new JMenuItem("Population: " + players.get(current).getPop() + "/" + players.get(current).getPopCap());
        playerMenu.add(popLabel);

        super.setVisible(true);
        super.setExtendedState(MAXIMIZED_BOTH);

        resetScales();
        constructConstructPanel();
        constructUpgradePanel();
        constructOperationPanel();

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        contentPanel.requestFocus();
    }

    private void constructUpgradePanel() {

        if(upgradePanel != null)
            getContentPane().remove(upgradePanel);

        JButton[] buttons = new JButton[8];

        upgradePanel = new JPanel() {

            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(aFlag);
                if(aFlag) {
                    if (upgrades.size() == 0 || upgrades.size() < upgradePage * 7) {
                        super.setVisible(false);
                        upgrades = null;
                    }
                    else if (buttons[0] != null) {
                        for (int i = 0; i < 7; i++) {
                            if(upgradePage * 7 + i < upgrades.size()) {
                                buttons[i].setVisible(true);
                                buttons[i].setText(upgrades.get(upgradePage * 7 + i).toString());
                            } else
                                buttons[i].setVisible(false);
                        }
                    }
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);

                gr.setColor(new Color(255, 255, 255, 200));
                gr.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };

        // Intercepts mouse events
        upgradePanel.addMouseListener(new MouseAdapter() {});

        upgradePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        upgradePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Dimension buttonSize = new Dimension((int)Math.round(cellWidth / 1.5) + 2, Math.floorDiv(cellHeight, 2) + 2);
        for(int i = 0; i < 8; i++) {
            final int step = i;

            RoundedButton button = new RoundedButton(step == 7 ? "Switch" : "N/A", buttonSize, players.get(current).getAlternativeColor());
            buttons[step] = button;

            if(step < 7) {
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        Upgrade upgrade = upgrades.get(upgradePage * 7 + step);
                        if(upgrade.isPossible(cycle)) {
                            upgrade.upgrade();
                            upgrades.remove(upgradePage * 7 + step);
                            upgradePage = 0;
                            refreshWindow();
                        }
                        upgradePanel.setVisible(false);
                    }
                });
            } else {
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(SwingUtilities.isLeftMouseButton(e)) {
                            upgradePage++;
                            upgradePanel.setVisible(true);
                        } else if(SwingUtilities.isRightMouseButton(e)) {
                            upgradePage = Math.max(upgradePage - 1, 0);
                            upgradePanel.setVisible(true);
                        }
                    }
                });
            }

            button.setPreferredSize(buttonSize);
            c.gridx = step % 4;
            c.gridy = Math.floorDiv(step, 4);
            c.weightx = 0.5;
            c.weighty = 0.5;
            if(c.gridx < 2)
                c.anchor = GridBagConstraints.WEST;
            else
                c.anchor = GridBagConstraints.EAST;
            upgradePanel.add(button, c);
        }

        upgradePanel.setVisible(false);
        upgradePanel.setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        upgradePanel.setOpaque(false);

        getContentPane().add(upgradePanel);
        SpringLayout layout = (SpringLayout)getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, upgradePanel, Math.floorDiv(screenWidth - 3 * cellWidth, 2), SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, upgradePanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, getContentPane());
    }

    private void constructConstructPanel() {

        if(constructPanel != null)
            getContentPane().remove(constructPanel);

        JButton[] buttons = new JButton[8];

        constructPanel = new JPanel() {
            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(aFlag);
                if(aFlag) {
                    if (products == null || products.size() == 0 || products.size() < constructPage * 7) {
                        super.setVisible(false);
                        products = null;
                    } else if (buttons[0] != null) {
                        for (int i = 0; i < 7; i++) {
                            if(constructPage * 7 + i < products.size()) {
                                buttons[i].setVisible(true);
                                buttons[i].setText(products.getDescription(constructPage * 7 + i));
                            } else
                                buttons[i].setVisible(false);
                        }
                    }
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);

                gr.setColor(new Color(255, 255, 255, 200));
                gr.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };

        // Intercepts mouse events
        constructPanel.addMouseListener(new MouseAdapter() {});

        constructPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        constructPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Dimension buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        for(int i = 0; i < 8; i++) {
            final int step = i;

            RoundedButton button = new RoundedButton(step == 7 ? "Switch" : "N/A", buttonSize, players.get(current).getAlternativeColor());
            buttons[step] = button;

            if(step < 7) {
                button.addActionListener(actionEvent -> {
                    try {
                        TypedConsumer construction = products.get(constructPage * 7 + step);
                        selected.typedDo(WORKER, construction);
                    } catch (TypeException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(SwingUtilities.isLeftMouseButton(e)) {
                            constructPage++;
                            constructPanel.setVisible(true);
                        } else if(SwingUtilities.isRightMouseButton(e)) {
                            constructPage = Math.max(constructPage - 1, 0);
                            constructPanel.setVisible(true);
                        }
                    }
                });
            }

            button.setPreferredSize(buttonSize);
            c.gridx = step % 4;
            c.gridy = Math.floorDiv(step, 4);
            c.weightx = 0.5;
            c.weighty = 0.5;
            if(c.gridx < 2)
                c.anchor = GridBagConstraints.WEST;
            else
                c.anchor = GridBagConstraints.EAST;
            constructPanel.add(button, c);
        }

        constructPanel.setVisible(false);
        constructPanel.setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        constructPanel.setOpaque(false);

        getContentPane().add(constructPanel);
        SpringLayout layout = (SpringLayout)getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, constructPanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, constructPanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, getContentPane());
    }

    private void constructOperationPanel() throws TypeException{

        if(operationPanel != null)
            getContentPane().remove(operationPanel);

        JButton[] buttons = new JButton[8];

        operationPanel = new JPanel() {
            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(aFlag);
                if(aFlag) {
                    if (operations == null || operations.size() == 0 || operations.size() < operationsPage * 7) {
                        super.setVisible(false);
                        operations = null;
                    }
                    else if (buttons[0] != null) {
                        for (int i = 0; i < 7; i++) {
                            if(operationsPage * 7 + i < operations.size()) {
                                buttons[i].setVisible(true);
                                buttons[i].setText(operations.getDescription(operationsPage * 7 + i));
                            } else
                                buttons[i].setVisible(false);
                        }
                    }
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);

                gr.setColor(new Color(255, 255, 255, 200));
                gr.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };

        // Intercepts mouse events
        operationPanel.addMouseListener(new MouseAdapter() {});

        operationPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        operationPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Dimension buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);

        for(int i = 0; i < 8; i++) {
            final int step = i;

            RoundedButton button = new RoundedButton(step == 7 ? "Switch" : "N/A", buttonSize, players.get(current).getAlternativeColor());
            buttons[step] = button;

            if(step < 7) {
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        TypedConsumer product = operations.get(operationsPage * 7 + step);
                        try {
                            selected.typedDo(WORKER, product);
                            refreshWindow();
                            hidePanels();
                        } catch (TypeException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(SwingUtilities.isLeftMouseButton(e)) {
                            operationsPage++;
                            operationPanel.setVisible(true);
                        } else if(SwingUtilities.isRightMouseButton(e)) {
                            operationsPage = Math.max(operationsPage - 1, 0);
                            operationPanel.setVisible(true);
                        }
                    }
                });
            }

            button.setPreferredSize(buttonSize);
            c.gridx = step % 2;
            c.gridy = Math.floorDiv(step, 2);
            c.weightx = 0.5;
            c.weighty = 0.5;
            if(c.gridy < 2)
                c.anchor = GridBagConstraints.NORTH;
            else
                c.anchor = GridBagConstraints.SOUTH;
            operationPanel.add(button, c);
        }

        operationPanel.setVisible(false);
        operationPanel.setPreferredSize(new Dimension(2 * cellWidth, 5 * cellHeight));
        operationPanel.setOpaque(false);

        getContentPane().add(operationPanel);
        SpringLayout layout = (SpringLayout)getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, operationPanel, 10, SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, operationPanel, 10, SpringLayout.NORTH, getContentPane());
    }

    private JPanel createInfoPanel() {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);

                gr.setColor(Color.lightGray);
                gr.fillRoundRect(1, 1, 2 * cellWidth - 3, 5 * cellHeight - 3, 10, 10);
                gr.setColor(Color.black);
                gr.setStroke(new BasicStroke(2));
                gr.drawRoundRect(1, 1, 2 * cellWidth - 3, 5 * cellHeight - 3, 10, 10);

                if(selected != null)
                    CustomMethods.customDrawString(gr, selected.toString(), 20, 20);
            }
        };

        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();

        // To intercept mouse motion
        panel.addMouseListener(new MouseAdapter() {});

        Dimension buttonSize = new Dimension(Math.floorDiv(cellWidth, 2), Math.floorDiv(cellHeight, 2));
        int counter = 0;
        for (Iterator<GameObject> it = players.get(current).getObjects().stream().filter(obj -> obj.getLocation().equals(clickPos)).iterator(); it.hasNext() && selected == null; ) {
            GameObject object = it.next();
            RoundedButton objectButton = new RoundedButton(object.getToken(), object.getSprite(), buttonSize, players.get(current).getAlternativeColor());

            objectButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selected = object;
                    try {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            selected.typedDo(CONSTRUCTOR, (c, params) -> {
                                products = c.getOperations(CONSTRUCT);
                                upgradePanel.setVisible(false);
                                constructPanel.setVisible(true);
                            });
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            upgrades = new ArrayList<>();
                            selected.typedDo(UPGRADER, (up, params) -> upgrades.addAll(extractUpgrades(up.getUpgrades())));
                            selected.typedDo(EVOLVABLE, (ev, params) -> upgrades.addAll(extractUpgrades(ev.getEvolutions())));
                            constructPanel.setVisible(false);
                            upgradePanel.setVisible(true);
                        }
                    } catch(TypeException te) {
                        te.printStackTrace();
                    }
                    panel.removeAll();
                    refreshWindow();
                }
            });

            objectButton.setBorder(new CustomBorder(Color.black, buttonSize));
            objectButton.setContentAreaFilled(false);
            objectButton.setFocusPainted(false);
            objectButton.setPreferredSize(buttonSize);
            constr.gridx = Math.floorDiv(counter, 8);
            constr.gridy = counter % 8;
            constr.weighty = 0.01;
            constr.anchor = GridBagConstraints.FIRST_LINE_START;
            counter++;
            panel.add(objectButton, constr);
        }

        // Filler to align buttons
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        constr.gridy++;
        constr.gridx = 2;
        constr.weightx = 1;
        constr.weighty = 1;
        panel.add(filler, constr);

        panel.setPreferredSize(new Dimension(2 * cellWidth, 5 * cellHeight));
        panel.setOpaque(false);
        return panel;
    }

    private void createResourcePanel(Cell cell) {

        if(resourcePanel != null)
            getContentPane().remove(resourcePanel);

        resourcePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g);

                gr.setColor(new Color(255, 255, 255, 200));
                gr.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };

        // Intercepts mouse events
        resourcePanel.addMouseListener(new MouseAdapter() {});

        resourcePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resourcePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Dimension buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        OperationsList resourceContracts = selected.getOperations(RESOURCE);
        for(int i = 0; i < resourceContracts.size(); i++) {

            final int step = i;
            RoundedButton button = new RoundedButton(Resource.values()[step].name, buttonSize, players.get(current).getAlternativeColor());
            button.addActionListener(actionEvent -> {
                try {
                    resourceContracts.get(step).run(selected, cell);
                } catch (TypeException e) {
                    e.printStackTrace();
                }
                refreshWindow();
                hidePanels();
            });

            button.setPreferredSize(buttonSize);
            c.gridx = step % 4;
            c.gridy = Math.floorDiv(step, 4);
            c.weightx = 0.5;
            c.weighty = 0.5;
            resourcePanel.add(button, c);
        }

        resourcePanel.setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        resourcePanel.setOpaque(false);

        getContentPane().add(resourcePanel);
        SpringLayout layout = (SpringLayout)getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, resourcePanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, resourcePanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, getContentPane());
    }

    private void showMessagePanel(String text) {

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
    private void resetScales() {
        screenWidth = getWidth();
        screenHeight = getHeight();
        cellWidth = Math.round(getWidth() / (float)NUMBER_OF_CELLS_IN_VIEW);
        cellHeight = Math.round((getHeight() - getInsets().top) / (float)NUMBER_OF_CELLS_IN_VIEW);
        poolSize = Math.min(Math.round(cellWidth / 2f), Math.round(cellHeight / 2f));
    }

    /**
     * Draws currently visible/relevant game objects
     * @param gr Graphics object of the game panel
     */
    private void drawObjects(Graphics2D gr) throws TypeException {

        /*
         * Redraw background image
         */
        for(int i = 0; i < NUMBER_OF_CELLS_IN_VIEW -1; i++) {
            gr.fillRect((i+1)*cellWidth - 1, 0, 1, screenHeight - getInsets().top);
            gr.fillRect( 0,  (i+1)*cellHeight - 1, screenWidth, 1);
        }

        /*
         * Draw all (visible) cells
         */

        for(int x = -1; x < NUMBER_OF_CELLS_IN_VIEW + 1; x++) {
            for(int y = -1; y < NUMBER_OF_CELLS_IN_VIEW + 1; y++) {
                Location key = players.get(current).getViewPoint().add(x, y, 0);
                Location loc = key.subtract(players.get(current).getViewPoint());
                Cell cell = cells.get(key);
                if (cell != null && (players.get(current).hasDiscovered(key) || players.get(current).hasSpotted(key))) {

                    if (cell.isField()) {
                        gr.setColor(new Color(200, 120, 0, 100));
                        gr.fillRect(loc.x * cellWidth, loc.y * cellHeight, cellWidth - 1, cellHeight - 1);
                    }

                    if (cell.isForest()) {
                        gr.setColor(new Color(20, 150, 20));
                        gr.fillArc(loc.x * cellWidth - Math.round(poolSize / 2f), loc.y * cellHeight - Math.round(poolSize / 2f), poolSize, poolSize, 270, 90);
                        gr.fillArc(loc.x * cellWidth - poolSize, (loc.y + 1) * cellHeight - poolSize - 1, 2 * poolSize, 2 * poolSize, 0, 90);
                    }
                }

                /*
                 * Draw all (visible/relevant) game objects
                 * taking into account their state
                 */
                final AtomicInteger unitCounter = new AtomicInteger();
                final AtomicInteger buildingCounter = new AtomicInteger();

                for(Player player : players) {
                    for (Iterator<GameObject> it = player.getObjects().stream().filter(obj -> obj.getLocation().equals(key)).iterator(); it.hasNext(); ) {;
                        GameObject object = it.next();
                        gr.setColor(object == selected ? object.getPlayer().getAlternativeColor() : object.getPlayer().getColor());
                        BufferedImage sprite = object.getSprite();
                        object.typedDo(UNIT, (u, params) -> {
                            if(sprite != null) {
                                if(object == selected)
                                    gr.drawImage(CustomMethods.selectedSprite(sprite, gr.getColor()), 5 + loc.x * cellWidth + UNIT_SPRITE_SIZE * unitCounter.get(), loc.y * cellHeight + 10, null);
                                else
                                    gr.drawImage(sprite, 5 + loc.x * cellWidth + UNIT_SPRITE_SIZE * unitCounter.get(), loc.y * cellHeight + 10, null);
                            }
                            else {
                                gr.drawString(u.getToken(), 5 + loc.x * cellWidth + 10 * unitCounter.get(), loc.y * cellHeight + 15);
                                u.typedDo(WORKER, (w, params2) -> {
                                    if (w.checkStatus(CONTRACT))
                                        gr.drawLine(5 + loc.x * cellWidth + 10 * unitCounter.get(), loc.y * cellHeight + 15 + 2, loc.x * cellWidth + 10 * unitCounter.get() + gr.getFontMetrics().stringWidth(w.getToken()), loc.y * cellHeight + 15 + 2);
                                });
                            }
                            unitCounter.incrementAndGet();
                        });
                        object.typedDo(BUILDING, (b, params) -> {
                            if(sprite != null) {
                                if(object == selected)
                                    gr.drawImage(CustomMethods.selectedSprite(sprite, gr.getColor()), 5 + loc.x * cellWidth + BUILDING_SPRITE_SIZE * buildingCounter.get(), (loc.y + 1) * cellHeight - BUILDING_SPRITE_SIZE - 10, null);
                                else
                                    gr.drawImage(sprite, 5 + loc.x * cellWidth + BUILDING_SPRITE_SIZE * buildingCounter.get(), (loc.y + 1) * cellHeight - BUILDING_SPRITE_SIZE - 10, null);
                            }
                            else {
                                gr.drawString(object.getToken(), 5 + loc.x * cellWidth + 10 * buildingCounter.get(), (loc.y + 1) * cellHeight - gr.getFontMetrics().getHeight() + 5 + 2);
                                b.typedDo(WORKER, (w, params2) -> {
                                    if (w.checkStatus(CONTRACT))
                                        gr.drawLine(5 + loc.x * cellWidth + 10 * buildingCounter.get(), (loc.y + 1) * cellHeight - gr.getFontMetrics().getHeight() + 5 + 2, loc.x * cellWidth + 10 * buildingCounter.get() + gr.getFontMetrics().stringWidth(w.getToken()), (loc.y + 1) * cellHeight - gr.getFontMetrics().getHeight() + 5 + 2);
                                });
                            }
                            buildingCounter.incrementAndGet();
                        });
                    }
                }
            }
        }

        for(int x = -1; x < NUMBER_OF_CELLS_IN_VIEW + 1; x++) {
            for(int y = -1; y < NUMBER_OF_CELLS_IN_VIEW + 1; y++) {
                Location key = players.get(current).getViewPoint().add(x, y, 0);
                Location loc = key.subtract(players.get(current).getViewPoint());
                Cell cell = cells.get(key);
                if(cell != null) {
                    if(players.get(current).hasDiscovered(key) || players.get(current).hasSpotted(key)){
                        if (cell.isRiver()) {
                            gr.setColor(new Color(0, 100, 255));
                            if (key.x < NUMBER_OF_CELLS - 2 && key.y < NUMBER_OF_CELLS - 2 && cells.get(key.add(1, 0, 0)).isRiver() && cells.get(key.add(0, 1, 0)).isRiver() && cells.get(key.add(1, 1, 0)).isRiver())
                                gr.fillRoundRect(Math.round((loc.x + 0.5f) * cellWidth - (poolSize / 2f)), Math.round((loc.y + 0.5f) * cellHeight - (poolSize / 2f)), cellWidth + poolSize, cellHeight + poolSize, poolSize, poolSize);
                            else {
                                boolean singleFlag = true;
                                if (key.x < NUMBER_OF_CELLS - 1 && cells.get(key.add(1, 0, 0)).isRiver())
                                    gr.fillRect(Math.round((loc.x + 0.5f) * cellWidth), Math.round((loc.y + 0.5f) * cellHeight - (poolSize / 2f)), Math.round(cellWidth / 2f) + 1, poolSize);
                                if (key.x > 0 && cells.get(key.add(-1, 0, 0)).isRiver())
                                    gr.fillRect(loc.x * cellWidth - 1, Math.round((loc.y + 0.5f) * cellHeight - (poolSize / 2f)), Math.round(cellWidth / 2f), poolSize);
                                if (key.y < NUMBER_OF_CELLS - 1 && cells.get(key.add(0, 1, 0)).isRiver()) {
                                    gr.fillRect(Math.round((loc.x + 0.5f) * cellWidth - (poolSize / 2f)), Math.round((loc.y + 0.5f) * cellHeight), poolSize, Math.round(cellHeight / 2f));
                                    singleFlag = false;
                                }
                                if (key.y > 0 && cells.get(key.add(0, -1, 0)).isRiver()) {
                                    gr.fillRect(Math.round((loc.x + 0.5f) * cellWidth - (poolSize / 2f)), loc.y * cellHeight, poolSize, Math.round(cellHeight / 2f));
                                    singleFlag = false;
                                }

                                if (singleFlag)
                                    gr.fillOval(Math.round((loc.x + 0.5f) * cellWidth - poolSize), Math.round((loc.y + 0.5f) * cellHeight - (poolSize / 2f)), poolSize * 2, poolSize);
                                else
                                    gr.fillOval(Math.round((loc.x + 0.5f) * cellWidth - (poolSize / 2f)), Math.round((loc.y + 0.5f) * cellHeight - (poolSize / 2f)), poolSize, poolSize);
                            }
                        }

                        if (cell.getHeatLevel() <= COLD_LEVEL) {
                            gr.setColor(Color.blue);
                            gr.fillOval((loc.x + 1) * cellWidth - 15, loc.y * cellHeight + 10, 5, 5);
                        } else if (cell.getHeatLevel() >= HOT_LEVEL) {
                            gr.setColor(Color.red);
                            gr.fillOval((loc.x + 1) * cellWidth - 15, loc.y * cellHeight + 10, 5, 5);
                        }
                    }

                    if (players.get(current).hasSpotted(key)) {
                        gr.setColor(new Color(192, 192, 192, 200));
                        gr.fillRect(loc.x * cellWidth - 1, loc.y * cellHeight - 1, cellWidth + 1, cellHeight + 1);
                    } else if (!players.get(current).hasDiscovered(key)) {
                        gr.setColor(Color.lightGray);
                        gr.fillRect(loc.x * cellWidth - 1, loc.y * cellHeight - 1, cellWidth + 1, cellHeight + 1);
                    }
                }
            }
        }
    }

    /**
     * Refreshes the game window components that are not drawn (menus, ...)
     */
    private void refreshWindow() {
        playerMenu.setText("Player: " + players.get(current).getName());
        cycleLabel.setText("Cycle: " + cycle);

        popLabel.setText("Population: " + players.get(current).getPop() + "/" + players.get(current).getPopCap());

        for(int i = 0; i < Resource.values().length; i++)
            playerLabels[i].setText(Resource.values()[i].name + ": " + players.get(current).getResource(Resource.values()[i]));

        Cell cell = cells.get(clickPos);
        for(int i = 0; i < Resource.values().length; i++)
            resourceLabels[i].setText(Resource.values()[i].name + ": " + cell.getResource(Resource.values()[i]));

        cellMenu.setText("Space: " + cell.getUnitOccupied() + "/" + cell.getUnitSpace() +
                " | " + cell.getBuildingOccupied() + "/" + cell.getBuildingSpace());

        getContentPane().repaint();
        getContentPane().revalidate();
    }

    private void hidePanels() {
        upgradePanel.setVisible(false);
        constructPanel.setVisible(false);
        operationPanel.setVisible(false);
        if(infoPanel != null)
            getContentPane().remove(infoPanel);

        if(resourcePanel != null)
            getContentPane().remove(resourcePanel);
    }

    /**
     * Converts coordinates to a core.Location object
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param l elevation
     * @return core.Location representation of the coordinates
     */
    private Location posToPair(int x, int y, int l) {
        return new Location(Math.floorDiv(x, cellWidth), Math.floorDiv(y, cellHeight), l);
    }

    /**
     * Removes all upgrades that are already enabled.
     * @param list list of potential upgrades
     * @return filtered upgrades
     */
    private <T extends Upgrade> List<T> extractUpgrades(List<T> list) {
        list.removeIf(upgrade -> upgrade.getPlayer().hasUpgrade(upgrade) || !upgrade.isVisible());
        return list;
    }

    public Motion getShortestAdmissiblePath(GameObject object, Location target) {
        if(target.distanceTo(object.getLocation()) > object.getValue(MAX_ENERGY) || cells.get(target) == null)
            return null;

        if(!object.getPlayer().hasDiscovered(target))
            return null;

        int locX = object.getValue(MAX_ENERGY);
        int locY = object.getValue(MAX_ENERGY);
        int locLevel = object.getValue(MAX_ENERGY);

        ArrayList<Location> toDo = new ArrayList<>();
        ArrayList<Location> done = new ArrayList<>();
        done.add(target);

        if(object.getLocation().z == target.z) {
            int[][] grid = new int[2 * object.getValue(MAX_ENERGY) + 1][2 * object.getValue(MAX_ENERGY) + 1];
            for (int x = -locX; x < locX + 1; x++) {
                for (int y = -locY; y < locY + 1; y++) {
                    grid[locX + x][locY + y] = ((x == y) && (x == 0)) ? cells.get(target).getTravelCost() : object.getValue(MAX_ENERGY) * 2;
                    if (Math.abs(x + y) == 1 && x * y == 0)
                        toDo.add(new Location(target.x + x, target.y + y, target.z));
                }
            }

            while(toDo.size() != 0) {
                done.addAll(toDo);
                ArrayList<Location> tempList = new ArrayList<>();
                for(Iterator<Location> it = toDo.iterator(); it.hasNext();) {
                    Location loc = it.next();

                    if(!cells.containsKey(loc))
                        continue;

                    int min = object.getValue(MAX_ENERGY) * 2;
                    for(int x = (target.x - loc.x == locX ? 0 : -1); x < (loc.x - target.x == locX ? 1 : 2); x++)
                        for(int y = (target.y - loc.y == locY ? 0 : -1); y < (loc.y - target.y == locY ? 1 : 2); y++)
                            if(x + y != 0 && x * y == 0)
                                min = Math.min(grid[locX + (loc.x - target.x) + x][locY + (loc.y - target.y) + y], min);

                    grid[locX + (loc.x - target.x)][locY + (loc.y - target.y)] = min + cells.get(loc).getTravelCost();

                    done.add(loc);
                    for (int x = -1; x < 2; x++) {
                        for (int y = -1; y < 2; y++) {
                            if(x * y == 0) {
                                Location next = new Location(loc.x + x, loc.y + y, loc.z);
                                if (target.distanceTo(next) <= object.getValue(MAX_ENERGY) && !done.contains(next) && !tempList.contains(next))
                                    tempList.add(next);
                            }
                        }
                    }

                    it.remove();
                }
                toDo = tempList;
            }

            int deltaX = object.getLocation().x - target.x;
            int deltaY = object.getLocation().y - target.y;
            int cost = grid[locX + deltaX][locY + deltaY] - cells.get(object.getLocation()).getTravelCost();

            if(cost > object.getValue(ENERGY))
                return null;

            ArrayList<Location> path = new ArrayList<>();
            Location current = object.getLocation();

            while(!current.equals(target)) {
                int min = object.getValue(MAX_ENERGY) * 2;
                Location temp = null;
                for(int x = (target.x - current.x == locX ? 0 : -1); x < (current.x - target.x == locX ? 1 : 2); x++) {
                    for (int y = (target.y - current.y == locY ? 0 : -1); y < (current.y - target.y == locY ? 1 : 2); y++) {
                        if (x + y != 0 && x * y == 0) {
                            if (grid[locX + (current.x - target.x) + x][locY + (current.y - target.y) + y] < min && grid[locX + (current.x - target.x) + x][locY + (current.y - target.y) + y] < grid[locX + (current.x - target.x)][locY + (current.y - target.y)]) {
                                min = Math.min(grid[locX + (current.x - target.x) + x][locY + (current.y - target.y) + y], min);
                                temp = new Location(current.x + x, current.y + y, current.z);
                            }
                        }
                    }
                }
                current = temp;
                path.add(current);
            }

            return new Motion(object, path, cost);
        } else
            return null;
    }

    /**
     * Turns a given Motion object into a thread that moves the object along the given path.
     * If the thread is interrupted, the path is completed instantly.
     * @param motion path to complete
     */
    public void motionToThread(Motion motion) {

        int delay = 1000;

        motion.getObject().setValue(STATUS, WALKING_STATUS);
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(!motion.isDone()) {
                    moveObject(motion.getObject(), motion.next());
                    refreshWindow();
                }

                if(motion.isDone()) {
                    motion.getObject().setValue(STATUS, IDLE_STATUS);
                    ((Timer)evt.getSource()).stop();
                }
            }
        };
        new Timer(delay, taskPerformer).start();
    }

    /**
     * Calculates the required energy to travel from the current Location to the target Location
     * based on Dijkstra's algorithm
     * @param object travelling object
     * @param target target Location
     * @return required energy cost
     */
    @Deprecated
    private int calculateTravelCost(GameObject object, Location target) {
        int locX = object.getValue(MAX_ENERGY);
        int locY = object.getValue(MAX_ENERGY);
        int locLevel = object.getValue(MAX_ENERGY);

        ArrayList<Location> toDo = new ArrayList<>();
        ArrayList<Location> done = new ArrayList<>();
        done.add(target);

        if(target.distanceTo(object.getLocation()) > object.getValue(MAX_ENERGY))
            return NUMBER_OF_CELLS;

        if(object.getLocation().z == target.z) {
            int[][] grid = new int[2 * object.getValue(MAX_ENERGY) + 1][2 * object.getValue(MAX_ENERGY) + 1];
            for (int x = -locX; x < locX + 1; x++) {
                for (int y = -locY; y < locY + 1; y++) {
                    grid[locX + x][locY + y] = ((x == y) && (x == 0)) ? cells.get(target).getTravelCost() : 9;
                    if (Math.abs(x + y) == 1 && x * y == 0)
                        toDo.add(new Location(target.x + x, target.y + y, target.z));
                }
            }

            while(toDo.size() != 0) {
                done.addAll(toDo);
                ArrayList<Location> tempList = new ArrayList<>();
                for(Iterator<Location> it = toDo.iterator(); it.hasNext();) {
                    Location loc = it.next();

                    if(!cells.containsKey(loc))
                        continue;

                    int min = NUMBER_OF_CELLS;
                    for(int x = (target.x - loc.x == locX ? 0 : -1); x < (loc.x - target.x == locX ? 1 : 2); x++)
                        for(int y = (target.y - loc.y == locY ? 0 : -1); y < (loc.y - target.y == locY ? 1 : 2); y++)
                            if(x + y != 0 && x * y == 0)
                                min = Math.min(grid[locX + (loc.x - target.x) + x][locY + (loc.y - target.y) + y], min);

                    grid[locX + (loc.x - target.x)][locY + (loc.y - target.y)] = min + cells.get(loc).getTravelCost();

                    done.add(loc);
                    for (int x = -1; x < 2; x++) {
                        for (int y = -1; y < 2; y++) {
                            if(x * y == 0) {
                                Location next = new Location(loc.x + x, loc.y + y, loc.z);
                                if (target.distanceTo(next) <= object.getValue(MAX_ENERGY) && !done.contains(next) && !tempList.contains(next))
                                    tempList.add(next);
                            }
                        }
                    }

                    it.remove();
                }
                toDo = tempList;
            }

            int deltaX = object.getLocation().x - target.x;
            int deltaY = object.getLocation().y - target.y;

//            printTranspose(grid);

            return grid[locX + deltaX][locY + deltaY] - cells.get(object.getLocation()).getTravelCost();
        } else
            return NUMBER_OF_CELLS;
    }
}
