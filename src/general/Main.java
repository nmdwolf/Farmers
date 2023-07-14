package general;

import core.*;

import items.*;
import items.buildings.Building;
import items.buildings.Lumberjack;
import items.buildings.MainBuilding;
import items.units.Hero;
import items.units.Unit;
import items.units.Villager;
import items.units.Worker;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.swing.MenuSelectionManager.defaultManager;

import static core.GameConstants.*;
import static core.Option.*;

public class Main extends JFrame{

    private int mouseX, mouseY, travelDistance;
    private Location clickPos;
    private Location[] hoverPath;
    private int cellWidth, cellHeight, poolSize, screenWidth, screenHeight;
    private int current, cycle, actionPage, workPage;
    private boolean clicked;

    /**
     * Map of all game cells.
     */
    private final HashMap<Location, Cell> cells;

    /**
     * List of players.
     */
    private final ArrayList<Player> players;

    private OperationsList operations, actions;
    private GameObject selected;

    private JMenu playerMenu, viewMenu, cellMenu;
    private JMenuItem cycleLabel, popLabel;
    private JMenuItem[] playerLabels, resourceLabels;
    private JPanel workPanel, infoPanel, resourcePanel, actionPanel;
    private final Timer gc;

    public static void main(String[] args) {
        Main mainframe = new Main();
        mainframe.initialize();
    }

    public Main() {
        cells = new HashMap<>();
        players = new ArrayList<>();
        cycle = 1;
        clickPos = new Location(0, 0, 0);

        for (int i = 0; i < NUMBER_OF_CELLS; i++) {
            for (int j = 0; j < NUMBER_OF_CELLS; j++) {
                Cell cell = new Cell(i, j, 0, INITIAL_CELL_UNIT_SPACE, INITIAL_CELL_BUILDING_SPACE);
                cells.put(new Location(i, j, 0), cell);
            }
        }

        for (int i = 1; i < NUMBER_OF_CELLS; i++) {
            for (int j = 1; j < NUMBER_OF_CELLS; j++) {
                cells.get(new Location(i, j, 0)).link(cells.get(new Location(i - 1, j, 0)), -1, 0, 0);
                cells.get(new Location(i, j, 0)).link(cells.get(new Location(i, j - 1, 0)), 0, -1, 0);
            }
        }

        String[] colors = new String[]{"Blue", "Green", "Yellow"};
        int playerCount = Integer.parseInt(JOptionPane.showInputDialog("How many (human) players?"));

        for (int i = 0; i < playerCount; i++) {
            String name = JOptionPane.showInputDialog("What is the name of the Hero?");
            String col = (String)JOptionPane.showInputDialog(null, "Choose the player color.", "Choose the player color.", JOptionPane.QUESTION_MESSAGE, null, colors, "Blue");
            Color color = switch(col) {
                case "Green" -> Color.green;
                case "Yellow" -> Color.yellow;
                default -> Color.blue;
            };
            int x = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 9);
            int y = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 9);
            addPlayer(new Player(name, color, Color.magenta, cells.get(new Location(x, y, 0))));
        }

        int x = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 9);
        int y = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 9);
        addPlayer(new AI("Thor", Color.red, Color.yellow, cells.get(new Location(x, y, 0)), this));

        gc = new Timer(500, e -> {
            for(Player p : players) {
                for(GameObject obj : p.getNewObjects())
                    addObject(obj);

                for (GameObject obj : p.getObjects())
                    if (obj.checkStatus(DESTROY)) {
                        obj.perform(DESTROY);
                        removeObject(obj);
                    }
            }
            refreshWindow();
        });
    }

    /**
     * Adds a player to the current game. Initializes resources and game objects.
     * @param p new Player
     */
    public void addPlayer(Player p) {
        players.add(p);

        GameObject base = new FreeDecorator<>(new MainBuilding(p, p.getViewPoint().fetch(2, 2, 0)));
        GameObject lumber = new FreeDecorator<>(new Lumberjack(p, p.getViewPoint().fetch(2, 5, 0)));
        GameObject v1 = new FreeDecorator<>(new Villager(p, p.getViewPoint().fetch(2, 1, 0)));
        GameObject v2 = new FreeDecorator<>(new Villager(p, p.getViewPoint().fetch(2, 1, 0)));
        GameObject hero = new Hero(p, p.getViewPoint(), p.getName());

        base.perform(CONSTRUCT);
        lumber.perform(CONSTRUCT);
        v1.perform(CONSTRUCT);
        v2.perform(CONSTRUCT);
        hero.perform(CONSTRUCT);
    }

    /**
     * Changes the current player.
     */
    public void cyclePlayers(){

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
            if(object instanceof Unit) {
                Unit u = (Unit)object;
                u.changeValue(HEALTH, Math.min(0, cells.get(u.getCell().getLocation()).getHeatLevel() - COLD_LEVEL));
                if(u.getValue(STATUS) == HEALING_STATUS)
                    u.changeValue(HEALTH, u.getValue(HEAL));
            }
            object.getCell().changeResources(object.getResources(Option.SOURCE));
        }

        selected = null;
        hoverPath = null;
        actions = null;
        operations = null;
        actionPage = 0;

        hidePanels();
        refreshWindow();

        if(players.get(current) instanceof AI)
            ((AI) players.get(current)).makeMove(cycle);
    }

    /**
     * Adds a new GameObject to the game.
     * @param obj new object
     */
    public boolean addObject(GameObject obj) {

        boolean added = false;

        if((obj instanceof Unit) && (obj.getCell().getUnitAvailable() >= obj.getSize())) {
            obj.getCell().changeUnitOccupied(obj.getSize());
            added = true;
        }
        if((obj instanceof Building) && (obj.getCell().getBuildingAvailable() >= obj.getSize())) {
            obj.getCell().changeBuildingOccupied(obj.getSize());
            added = true;
        }

        if(added) {
            if(obj instanceof Spacer)
                obj.getCell().changeUnitSpace(((Spacer)obj).getSpace());
            if(obj instanceof Obstruction)
                obj.getCell().changeTravelCost(100);
        }

        return added;
    }

    /**
     * Removes a GameObject from the game.
     * @param obj object to remove
     */
    public void removeObject(GameObject obj) {
        if(obj instanceof Unit)
            obj.getCell().changeUnitOccupied(-obj.getSize());
        if(obj instanceof Building)
            obj.getCell().changeBuildingOccupied(-obj.getSize());
        if(obj instanceof Spacer)
            obj.getCell().changeUnitSpace(-((Spacer)obj).getSpace());
        if(obj instanceof Obstruction)
            obj.getCell().changeTravelCost(-100);
    }

    /**
     * Moves an existing object
     * @param obj object to move
     * @param loc new Location of object
     */
    public void moveObject(GameObject obj, Location loc) {
        Cell target = cells.get(loc);
        if (!obj.getCell().equals(target)) {
            if(obj instanceof Unit) {
                obj.getCell().changeUnitOccupied(-obj.getSize());
                target.changeUnitOccupied(obj.getSize());
            }
            if(obj instanceof Building) {
                obj.getCell().changeBuildingOccupied(-obj.getSize());
                target.changeBuildingOccupied(obj.getSize());
            }
            if(obj instanceof Spacer) {
                obj.getCell().changeUnitSpace(-obj.getSize());
                target.changeUnitSpace(obj.getSize());
                // NEED TO CHECK IF SPACE REDUCTION PROHIBITS MOVE
            }

            obj.getPlayer().discover(target);
            int sight = obj.getValue(SIGHT);
            for(int x = -sight; x < sight + 1; x++)
                for(int y = -sight; y < sight + 1; y++)
                    for(int z = -sight; y < sight + 1; z++)
                        if(Math.abs(x) + Math.abs(y) + Math.abs(z) <= sight)
                            obj.getPlayer().spot(target.fetch(x, y, z));
            obj.setCell(target);
        }
    }

    /**
     * Constructs a game frame
     */
    private void initialize() {
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
                drawObjects(gr);

                gr.setColor(players.get(current).getAlternativeColor());
                gr.setStroke(new BasicStroke(2));
                Cell p = posToCell(mouseX, mouseY, 0);
                gr.drawRect(p.getX() * cellWidth, p.getY() * cellHeight, cellWidth, cellHeight);
                if (hoverPath != null) {
                    gr.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                            0, new float[]{9}, 0));
                    Cell vp = players.get(current).getViewPoint();
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
                    gr.drawString(travelDistance + "", Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight));
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
                cyclePlayers();
            }
        });
        contentPanel.getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player player = players.get(current);
                if(player.getViewPoint().getX() > 0) {
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().fetch(-1, 0, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player player = players.get(current);
                if(player.getViewPoint().getX() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().fetch(1, 0, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player player = players.get(current);
                if(player.getViewPoint().getY() > 0) {
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().fetch(0, -1, 0));
                    refreshWindow();
                }
            }
        });
        contentPanel.getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player player = players.get(current);
                if(player.getViewPoint().getY() < NUMBER_OF_CELLS - NUMBER_OF_CELLS_IN_VIEW) {
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().fetch(0, 1, 0));
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
                    Cell mousePos = posToCell(mouseX, mouseY, 0);
                    layout.putConstraint(SpringLayout.WEST, infoPanel, (mousePos.getX() >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f)) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                }

                Cell loc = posToCell(e.getX(), e.getY(), 0);
                if(selected != null && (selected instanceof Unit) && (hoverPath == null || !loc.equals(hoverPath[hoverPath.length - 1]))) {
                    Motion motion = getShortestAdmissiblePath(selected, loc.fetch(players.get(current).getViewPoint()));
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
                    players.get(current).changeViewpoint(players.get(current).getViewPoint().fetch(0, 0, e.getWheelRotation()));
                    viewMenu.setText("View: " + (players.get(current).getViewPoint().getZ()));
                    selected = null;

                    hidePanels();
                    refreshWindow();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                clickPos = players.get(current).getViewPoint().fetch(posToCell(e.getX(), e.getY(), players.get(current).getViewPoint().getZ())).getLocation();
                hoverPath = null;
                hidePanels();

                /*
                 * left-click events
                 */
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Any existing info panels should be removed on click
                    if (clicked)
                        selected = null;

                    // If clicked on cell with objects, show info panel
                    if (players.get(current).getObjects().stream().anyMatch(obj -> obj.getCell().equals(clickPos))) {
                        infoPanel = createInfoPanel();
                        contentPanel.add(infoPanel);
                        layout.putConstraint(SpringLayout.WEST, infoPanel, (clickPos.x() - players.get(current).getViewPoint().getX()) >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                        layout.putConstraint(SpringLayout.NORTH, infoPanel, 10, SpringLayout.NORTH, contentPanel);

                        clicked = true;
                    } else
                        clicked = false;

                    // right-click events
                } else if (SwingUtilities.isRightMouseButton(e) && (selected instanceof Unit)) {
                    Motion motion = getShortestAdmissiblePath(selected, cells.get(clickPos));
                    if(motion != null) {
                        hoverPath = null;
                        if (cells.get(clickPos).getUnitSpace() - cells.get(clickPos).getUnitOccupied() >= selected.getValue(SIZE)) {
                            if(motion.getSize() != 0) {
                                selected.changeValue(ENERGY, -motion.getSize());
                                motionToThread(motion);
                            }
                            if(selected instanceof Worker) {
                                createResourcePanel(cells.get(clickPos));
                                operations = selected.getOperations(CONSTRUCT);
                                workPanel.setVisible(true);
                            }
                        }
                    }
                }

                // Redraw game panel
                refreshWindow();
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
                resetScales();
                constructActionPanel();
                constructWorkPanel();
                hidePanels();
                refreshWindow();
            }
        });

        // Create menus
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        playerMenu = new JMenu("Player: " + players.get(current).getName());
        viewMenu = new JMenu("View: " + players.get(current).getViewPoint().getZ());
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
        constructActionPanel();
        constructWorkPanel();

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        contentPanel.requestFocus();

        gc.start();
    }

    private void constructActionPanel() {

        if(actionPanel != null)
            getContentPane().remove(actionPanel);

        JButton[] buttons = new JButton[8];

        actionPanel = new JPanel() {
            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(aFlag);
                if(aFlag) {
                    if (actions == null || actions.size() == 0 || actions.size() < actionPage * 7) {
                        super.setVisible(false);
                        actions = null;
                    } else if (buttons[0] != null) {
                        for (int i = 0; i < 7; i++) {
                            if(actionPage * 7 + i < actions.size()) {
                                buttons[i].setVisible(true);
                                buttons[i].setText(actions.getDescription(actionPage * 7 + i));
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
        actionPanel.addMouseListener(new MouseAdapter() {});

        actionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        actionPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Dimension buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);
        for(int i = 0; i < 8; i++) {
            final int step = i;

            RoundedButton button = new RoundedButton(step == 7 ? "Switch" : "N/A", buttonSize, players.get(current).getAlternativeColor());
            buttons[step] = button;

            if(step < 7) {
                button.addActionListener(actionEvent -> actions.get(actionPage * 7 + step).perform());
            } else {
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(SwingUtilities.isLeftMouseButton(e)) {
                            actionPage++;
                            actionPanel.setVisible(true);
                        } else if(SwingUtilities.isRightMouseButton(e)) {
                            actionPage = Math.max(actionPage - 1, 0);
                            actionPanel.setVisible(true);
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
            actionPanel.add(button, c);
        }

        actionPanel.setVisible(false);
        actionPanel.setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        actionPanel.setOpaque(false);

        getContentPane().add(actionPanel);
        SpringLayout layout = (SpringLayout)getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, actionPanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, actionPanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, getContentPane());
    }

    private void constructWorkPanel() {

        if(workPanel != null)
            getContentPane().remove(workPanel);

        JButton[] buttons = new JButton[8];

        workPanel = new JPanel() {
            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(aFlag);
                if(aFlag) {
                    if (operations == null || operations.size() == 0 || operations.size() < workPage * 7) {
                        super.setVisible(false);
                        operations = null;
                    }
                    else if (buttons[0] != null) {
                        for (int i = 0; i < 7; i++) {
                            if(workPage * 7 + i < operations.size()) {
                                buttons[i].setVisible(true);
                                buttons[i].setText(operations.getDescription(workPage * 7 + i));
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
        workPanel.addMouseListener(new MouseAdapter() {});

        workPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        workPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Dimension buttonSize = new Dimension(Math.round(cellWidth / 1.5f) + 2, Math.round(cellHeight / 2f) + 2);

        for(int i = 0; i < 8; i++) {
            final int step = i;

            RoundedButton button = new RoundedButton(step == 7 ? "Switch" : "N/A", buttonSize, players.get(current).getAlternativeColor());
            buttons[step] = button;

            if(step < 7)
                button.addActionListener(actionEvent -> {
                    operations.get(workPage * 7 + step).perform();
                    refreshWindow();
                    hidePanels();
                });
            else {
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(SwingUtilities.isLeftMouseButton(e)) {
                            workPage++;
                            workPanel.setVisible(true);
                        } else if(SwingUtilities.isRightMouseButton(e)) {
                            workPage = Math.max(workPage - 1, 0);
                            workPanel.setVisible(true);
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
            workPanel.add(button, c);
        }

        workPanel.setVisible(false);
        workPanel.setPreferredSize(new Dimension(2 * cellWidth, 5 * cellHeight));
        workPanel.setOpaque(false);

        getContentPane().add(workPanel);
        SpringLayout layout = (SpringLayout)getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, workPanel, 10, SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, workPanel, 10, SpringLayout.NORTH, getContentPane());
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
        for (Iterator<GameObject> it = players.get(current).getObjects().stream().filter(obj -> obj.getCell().equals(clickPos)).iterator(); it.hasNext() && selected == null; ) {
            GameObject object = it.next();
            RoundedButton objectButton = new RoundedButton(object.getToken(), object.getSprite(), buttonSize, players.get(current).getAlternativeColor());

            objectButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if(object instanceof Worker) {
                            actions = selected.getOperations(CONSTRUCT);
                            actionPanel.setVisible(true);
                        }
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        if(object instanceof Evolvable) {
                            Evolvable selected = (Evolvable)object;
                            actions = selected.getEvolutions();
                            actionPanel.setVisible(true);
                        }
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
                resourceContracts.get(step).perform();
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
    private void drawObjects(Graphics2D gr) {

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
                Cell vp = players.get(current).getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));
                if (players.get(current).hasDiscovered(cell) || players.get(current).hasSpotted(cell)) {

                    if (cell.isField()) {
                        gr.setColor(new Color(200, 120, 0, 100));
                        gr.fillRect(x * cellWidth, y * cellHeight, cellWidth - 1, cellHeight - 1);
                    }

                    if (cell.isForest()) {
                        gr.setColor(new Color(20, 150, 20));
                        gr.fillArc(x * cellWidth - Math.round(poolSize / 2f), y * cellHeight - Math.round(poolSize / 2f), poolSize, poolSize, 270, 90);
                        gr.fillArc(x * cellWidth - poolSize, (y + 1) * cellHeight - poolSize - 1, 2 * poolSize, 2 * poolSize, 0, 90);
                    }
                }

                /*
                 * Draw all (visible/relevant) game objects
                 * taking into account their state
                 */
                final AtomicInteger unitCounter = new AtomicInteger();
                final AtomicInteger buildingCounter = new AtomicInteger();

                for(Player player : players) {
                    for (Iterator<GameObject> it = player.getObjects().stream().filter(obj -> obj.getCell().equals(cell)).iterator(); it.hasNext(); ) {;
                        GameObject object = it.next();
                        gr.setColor(object == selected ? object.getPlayer().getAlternativeColor() : object.getPlayer().getColor());
                        BufferedImage sprite = object.getSprite();
                        if(object instanceof Unit) {
                            Unit u = (Unit)object;
                            if(sprite != null) {
                                if(object == selected)
                                    gr.drawImage(CustomMethods.selectedSprite(sprite, gr.getColor()), 5 + x * cellWidth + UNIT_SPRITE_SIZE * unitCounter.get(), y * cellHeight + 10, null);
                                else
                                    gr.drawImage(sprite, 5 + x * cellWidth + UNIT_SPRITE_SIZE * unitCounter.get(), y * cellHeight + 10, null);
                            }
                            else {
                                gr.drawString(u.getToken(), 5 + x * cellWidth + 10 * unitCounter.get(), y * cellHeight + 15);
                                if(u instanceof Worker)
                                    if (u.checkStatus(CONTRACT))
                                        gr.drawLine(5 + x * cellWidth + 10 * unitCounter.get(), y * cellHeight + 15 + 2, x * cellWidth + 10 * unitCounter.get() + gr.getFontMetrics().stringWidth(u.getToken()), y * cellHeight + 15 + 2);
                            }
                            unitCounter.incrementAndGet();
                        }

                        if(object instanceof Building) {
                            Building b = (Building)object;
                            if(sprite != null) {
                                if(object == selected)
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
        }

        for(int x = -1; x < NUMBER_OF_CELLS_IN_VIEW + 1; x++) {
            for(int y = -1; y < NUMBER_OF_CELLS_IN_VIEW + 1; y++) {
                Cell vp = players.get(current).getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));

                if(players.get(current).hasDiscovered(cell) || players.get(current).hasSpotted(cell)){
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

                    if (cell.getHeatLevel() <= COLD_LEVEL) {
                        gr.setColor(Color.blue);
                        gr.fillOval((x + 1) * cellWidth - 15, y * cellHeight + 10, 5, 5);
                    } else if (cell.getHeatLevel() >= HOT_LEVEL) {
                        gr.setColor(Color.red);
                        gr.fillOval((x + 1) * cellWidth - 15, y * cellHeight + 10, 5, 5);
                    }
                }

                if (players.get(current).hasSpotted(cell)) {
                    gr.setColor(new Color(192, 192, 192, 200));
                    gr.fillRect(x * cellWidth - 1, y * cellHeight - 1, cellWidth + 1, cellHeight + 1);
                } else if (!players.get(current).hasDiscovered(cell)) {
                    gr.setColor(Color.lightGray);
                    gr.fillRect(x * cellWidth - 1, y * cellHeight - 1, cellWidth + 1, cellHeight + 1);
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
        actionPanel.setVisible(false);
        workPanel.setVisible(false);
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
    private Cell posToCell(int x, int y, int l) {
        return cells.get(new Location(Math.floorDiv(x, cellWidth), Math.floorDiv(y, cellHeight), l));
    }

    public Motion getShortestAdmissiblePath(GameObject object, Cell target) {
        if(target == null || target.distanceTo(object.getCell()) > object.getValue(MAX_ENERGY)
                || !object.getPlayer().hasDiscovered(target))
            return null;

        int locX = object.getValue(MAX_ENERGY);
        int locY = object.getValue(MAX_ENERGY);
        int locLevel = object.getValue(MAX_ENERGY);

        ArrayList<Location> toDo = new ArrayList<>();
        ArrayList<Location> done = new ArrayList<>();
        done.add(new Location(target.getX(), target.getY(), target.getZ()));

        if(object.getCell().getZ() == target.getZ()) {
            int[][] grid = new int[2 * object.getValue(MAX_ENERGY) + 1][2 * object.getValue(MAX_ENERGY) + 1];
            for (int x = -locX; x < locX + 1; x++) {
                for (int y = -locY; y < locY + 1; y++) {
                    grid[locX + x][locY + y] = ((x == y) && (x == 0)) ? cells.get(target).getTravelCost() : object.getValue(MAX_ENERGY) * 2;
                    if (Math.abs(x + y) == 1 && x * y == 0)
                        toDo.add(new Location(target.getX() + x, target.getY() + y, target.getZ()));
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
                    for(int x = (target.getX() - loc.x() == locX ? 0 : -1); x < (loc.x() - target.getX() == locX ? 1 : 2); x++)
                        for(int y = (target.getY() - loc.y() == locY ? 0 : -1); y < (loc.y() - target.getY() == locY ? 1 : 2); y++)
                            if(x + y != 0 && x * y == 0)
                                min = Math.min(grid[locX + (loc.x() - target.getX()) + x][locY + (loc.y() - target.getY()) + y], min);

                    grid[locX + (loc.x() - target.getX())][locY + (loc.y() - target.getY())] = min + cells.get(loc).getTravelCost();

                    done.add(loc);
                    for (int x = -1; x < 2; x++) {
                        for (int y = -1; y < 2; y++) {
                            if(x * y == 0) {
                                Location next = new Location(loc.x() + x, loc.y() + y, loc.z());
                                if (target.getLocation().distanceTo(next) <= object.getValue(MAX_ENERGY) && !done.contains(next) && !tempList.contains(next))
                                    tempList.add(next);
                            }
                        }
                    }

                    it.remove();
                }
                toDo = tempList;
            }

            int deltaX = object.getCell().getX() - target.getX();
            int deltaY = object.getCell().getY() - target.getY();
            int cost = grid[locX + deltaX][locY + deltaY] - object.getCell().getTravelCost();

            if(cost > object.getValue(ENERGY))
                return null;

            ArrayList<Location> path = new ArrayList<>();
            Location current = object.getCell().getLocation();

            while(!current.equals(target)) {
                int min = object.getValue(MAX_ENERGY) * 2;
                Location temp = null;
                for(int x = (target.getX() - current.x() == locX ? 0 : -1); x < (current.x() - target.getX() == locX ? 1 : 2); x++) {
                    for (int y = (target.getY() - current.y() == locY ? 0 : -1); y < (current.y() - target.getY() == locY ? 1 : 2); y++) {
                        if (x + y != 0 && x * y == 0) {
                            if (grid[locX + (current.x() - target.getX()) + x][locY + (current.y() - target.getY()) + y] < min
                                    && grid[locX + (current.x() - target.getX()) + x][locY + (current.y() - target.getY()) + y] < grid[locX + (current.x() - target.getX())][locY + (current.y() - target.getY())]) {
                                min = Math.min(grid[locX + (current.x() - target.getX()) + x][locY + (current.y() - target.getY()) + y], min);
                                temp = new Location(x, y, 0);
                            }
                        }
                    }
                }
                current = new Location(current.x() + temp.x(), current.y() + temp.y(), current.z());
                path.add(temp);
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

        int delay = motion.getObject().getValue(ANIMATION);

        motion.getObject().setValue(STATUS, WALKING_STATUS);
        ActionListener taskPerformer = evt -> {
            if(!motion.isDone()) {
                moveObject(motion.getObject(), motion.next());
                refreshWindow();
            }

            if(motion.isDone()) {
                motion.getObject().setValue(STATUS, IDLE_STATUS);
                ((Timer)evt.getSource()).stop();
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
    private int calculateTravelCost(GameObject object, Cell target) {
        int locX = object.getValue(MAX_ENERGY);
        int locY = object.getValue(MAX_ENERGY);
        int locLevel = object.getValue(MAX_ENERGY);

        ArrayList<Cell> toDo = new ArrayList<>();
        ArrayList<Cell> done = new ArrayList<>();
        done.add(target);

        if(target.distanceTo(object.getCell()) > object.getValue(MAX_ENERGY))
            return NUMBER_OF_CELLS;

        if(object.getCell().getZ() == target.getZ()) {
            int[][] grid = new int[2 * object.getValue(MAX_ENERGY) + 1][2 * object.getValue(MAX_ENERGY) + 1];
            for (int x = -locX; x < locX + 1; x++) {
                for (int y = -locY; y < locY + 1; y++) {
                    grid[locX + x][locY + y] = ((x == y) && (x == 0)) ? target.getTravelCost() : 9;
                    if (Math.abs(x + y) == 1 && x * y == 0)
                        toDo.add(cells.get(new Location(target.getX() + x, target.getY() + y, target.getZ())));
                }
            }

            while(toDo.size() != 0) {
                done.addAll(toDo);
                ArrayList<Cell> tempList = new ArrayList<>();
                for(Iterator<Cell> it = toDo.iterator(); it.hasNext();) {
                    Cell loc = it.next();

                    if(!cells.containsKey(loc))
                        continue;

                    int min = NUMBER_OF_CELLS;
                    for(int x = (target.getX() - loc.getX() == locX ? 0 : -1); x < (loc.getX() - target.getX() == locX ? 1 : 2); x++)
                        for(int y = (target.getY() - loc.getY() == locY ? 0 : -1); y < (loc.getY() - target.getY() == locY ? 1 : 2); y++)
                            if(x + y != 0 && x * y == 0)
                                min = Math.min(grid[locX + (loc.getX() - target.getX()) + x][locY + (loc.getY() - target.getY()) + y], min);

                    grid[locX + (loc.getX() - target.getX())][locY + (loc.getY() - target.getY())] = min + cells.get(loc).getTravelCost();

                    done.add(loc);
                    for (int x = -1; x < 2; x++) {
                        for (int y = -1; y < 2; y++) {
                            if(x * y == 0) {
                                Cell next = cells.get(new Location(loc.getX() + x, loc.getY() + y, loc.getZ()));
                                if (target.distanceTo(next) <= object.getValue(MAX_ENERGY) && !done.contains(next) && !tempList.contains(next))
                                    tempList.add(next);
                            }
                        }
                    }

                    it.remove();
                }
                toDo = tempList;
            }

            int deltaX = object.getCell().getX() - target.getX();
            int deltaY = object.getCell().getY() - target.getY();

//            printTranspose(grid);

            return grid[locX + deltaX][locY + deltaY] - cells.get(object.getCell()).getTravelCost();
        } else
            return NUMBER_OF_CELLS;
    }
}
