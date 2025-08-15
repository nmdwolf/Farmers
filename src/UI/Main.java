package UI;

import core.*;

import objects.*;
import objects.buildings.Building;
import objects.buildings.Lumberjack;
import objects.buildings.MainBuilding;
import objects.units.Hero;
import objects.units.Unit;
import objects.units.Villager;
import objects.resources.Resource;

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

public class Main extends JFrame{

    private int mouseX, mouseY, travelDistance;
    private Location clickPos;
    private Location[] hoverPath;
    private Location destination;
    private int cellWidth, cellHeight, poolSize, screenWidth, screenHeight;
    private int current, cycle;
    private boolean clicked;

    /**
     * Map of all game cells.
     */
    private final HashMap<Location, Cell> cells;

    /**
     * List of players.
     */
    private final ArrayList<Player> players, allPlayers;
    private final ArrayList<AI> ais;

    // Currently selected GameObject
    private GameObject selected;

    // UI elements
    private JMenu playerMenu, viewMenu, cellMenu;
    private JMenuItem cycleLabel, popLabel;
    private JMenuItem[] playerLabels, resourceLabels;
    private JPanel infoPanel;
    private final ChoicePanel choicePanel;
    private final OperationPanel operationsPanel;
    private final CellPanel cellPanel;
    private final Timer garbageCollector;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main mainframe = new Main();
            mainframe.initialize();
        });
    }

    public Main() {
        cells = new Grid(NUMBER_OF_CELLS);
        players = new ArrayList<>();
        ais = new ArrayList<>();
        cycle = 1;
        clickPos = new Location(0, 0, 0);

        String[] colors = new String[]{"Blue", "Green", "Yellow"};

        boolean inputFlag = true;
        int playerCount = 1;
        do {
            String input = "";
            try {
                input = JOptionPane.showInputDialog("How many (human) players?");
                if(input == null) {
                    JOptionPane.showMessageDialog(null, "Startup was cancelled by a player.");
                    System.exit(0);
                }

                playerCount = Integer.parseInt(input);
                inputFlag = false;
            } catch(NumberFormatException e) {
                JOptionPane.showMessageDialog(null,"A nonnumerical value was entered: " + input + ". Please try again.");
            }
        } while(inputFlag);

        for (int i = 0; i < playerCount; i++) {
            String name = JOptionPane.showInputDialog("What is the name of the Hero?");
            String col = (String)JOptionPane.showInputDialog(null, "Choose the player color.", "Choose the player color.", JOptionPane.QUESTION_MESSAGE, null, colors, "Blue");
            Color color = switch(col) {
                case "Green" -> Color.green;
                case "Yellow" -> Color.yellow;
                default -> Color.blue;
            };
            int x = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 10);
            int y = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 10);
            addPlayer(new Player(name, color, Color.magenta, cells.get(new Location(x, y, 0))));
        }

        int x = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 10);
        int y = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 10);
        addPlayer(new AI("Thor", Color.red, Color.yellow, cells.get(new Location(x, y, 0)), this));

        allPlayers = new ArrayList<>(players.size() + ais.size());
        allPlayers.addAll(players);
        allPlayers.addAll(ais);

        garbageCollector = new Timer(500, e -> {
            for(Player p : allPlayers) {
                for(GameObject obj : p.getNewObjects())
                    addObject(obj);
                p.clearNewObjects();

                for (GameObject obj : p.getObjects())
                    if ((obj.getHealth() <= 0) && !(obj instanceof Revivable)) {
                        p.removeObject(obj);
                        removeObject(obj);
                    }
            }
            refreshWindow();
        });

        operationsPanel = new OperationPanel(cellWidth, cellHeight);
        choicePanel = new ChoicePanel(operationsPanel, cellWidth, cellHeight);
        cellPanel = new CellPanel(this);
    }

    /**
     * Adds a player to the current game.
     * Initializes resources and objects.
     * @param p new Player
     */
    public void addPlayer(Player p) {

//        if(p instanceof AI)
//            ais.add((AI)p);
//        else
            players.add(p);

        GameObject base = new MainBuilding(p, p.getViewPoint().fetch(2, 2, 0), cycle);
        GameObject lumber = new Lumberjack(p, p.getViewPoint().fetch(2, 5, 0), cycle);
        GameObject v1 = new Villager(p, p.getViewPoint().fetch(2, 1, 0), cycle);
        GameObject v2 = new Villager(p, p.getViewPoint().fetch(2, 1, 0), cycle);
        GameObject hero = new Hero(p, p.getViewPoint(), cycle, p.getName());

        p.addObject(base);
        p.addObject(lumber);
        p.addObject(v1);
        p.addObject(v2);
        p.addObject(hero);
    }

    /**
     * Changes the current player.
     */
    public void cyclePlayers(){

        selected = null;
        hoverPath = null;
        destination = null;

        if(current == players.size() - 1) { // Let AIs play at end of cycle
            for(AI ai : ais)
                ai.makeMove(cycle);

            cycle++;

            for(Cell cell : cells.values())
                cell.cycle(cycle);
        }

        current = (++current) % players.size();
        Player player = players.get(current);

        for (String text : player.getMessages())
            showMessagePanel(text);

        for (GameObject object : player.getObjects()) {
            object.cycle(cycle);
//            if(object instanceof Unit) {
//                for(GameObject obj2 : object.getCell().getContent())
//                    if(obj2 instanceof Healer)
//                        ((Healer)obj2).heal(object);
//            }
        }

        hidePanels();
        refreshWindow();
    }

    /**
     * Adds a new GameObject to the game.
     * @param obj new object
     * @return whether the object has been added or not
     */
    public boolean addObject(GameObject obj) {

        boolean added = false;

        if((obj instanceof Unit) && (obj.getCell().getUnitAvailable() >= obj.getSpace())) {
            obj.getCell().changeUnitOccupied(obj.getSpace());
            added = true;
        }
        if((obj instanceof Building) && (obj.getCell().getBuildingAvailable() >= obj.getSpace())) {
            obj.getCell().changeBuildingOccupied(obj.getSpace());
            added = true;
        }

        if(added) {
            obj.getPlayer().addObject(obj);
            if(obj instanceof Spacer)
                obj.getCell().changeUnitSpace(((Spacer)obj).getSpaceBoost());
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
            obj.getCell().changeUnitOccupied(-obj.getSpace());
        if(obj instanceof Building)
            obj.getCell().changeBuildingOccupied(-obj.getSpace());
        if(obj instanceof Spacer)
            obj.getCell().changeUnitSpace(-((Spacer)obj).getSpaceBoost());
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
                obj.getCell().changeUnitOccupied(-obj.getSpace());
                target.changeUnitOccupied(obj.getSpace());
            }
            if(obj instanceof Building) {
                obj.getCell().changeBuildingOccupied(-obj.getSpace());
                target.changeBuildingOccupied(obj.getSpace());
            }
            if(obj instanceof Spacer) {
                obj.getCell().changeUnitSpace(-obj.getSpace());
                target.changeUnitSpace(obj.getSpace());
                // NEED TO CHECK IF SPACE REDUCTION PROHIBITS MOVE
            }

            obj.getPlayer().discover(target);
            int sight = obj.getSight();
            for(int x = -sight; x < sight + 1; x++)
                for(int y = -sight; y < sight + 1; y++)
                    for(int z = -sight; z < sight + 1; z++)
                        if(Math.abs(x) + Math.abs(y) + Math.abs(z) <= sight)
                            obj.getPlayer().spot(target.fetch(x, y, z));
            obj.setCell(target);
        }
    }

    /**
     * Constructs the game frame.
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
                    gr.drawString(String.valueOf(travelDistance), Math.round((current.getX() + 0.5f) * cellWidth), Math.round((current.getY() + 0.5f) * cellHeight));
                }
            }
        };

        contentPanel.setOpaque(false);
        super.setContentPane(contentPanel);
        SpringLayout layout = new SpringLayout();
        contentPanel.setLayout(layout);

        // Cycle players on "n" stroke
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('n'), "cycle");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "left");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "right");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "up");
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "down");
        contentPanel.getActionMap().put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected == null)
                    hidePanels();
                else
                    cellPanel.setVisible(false);
            }
        });
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
                Cell mousePos = posToCell(mouseX, mouseY, 0);

                // Moves info panel to make sure that the underlying cells are reachable.
                if(infoPanel != null && selected != null) {
                    layout.putConstraint(SpringLayout.WEST, infoPanel, (mousePos.getX() >= 7) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                    refreshWindow();
                }

                if(selected != null && (selected instanceof Unit) && (hoverPath == null || !mousePos.getLocation().equals(destination))) {
                    Pair<Motion, Location> motion = getShortestAdmissiblePath(selected, mousePos.fetch(players.get(current).getViewPoint()));
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
                    if (e.getWheelRotation() != 0 && !players.get(current).isViewLocked()) {
                        players.get(current).changeViewpoint(players.get(current).getViewPoint().fetch(0, 0, e.getWheelRotation()));
                        viewMenu.setText("View: " + (players.get(current).getViewPoint().getZ()));
                        selected = null;

                        hidePanels();
                        refreshWindow();
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                clickPos = players.get(current).getViewPoint().fetch(posToCell(e.getX(), e.getY(), players.get(current).getViewPoint().getZ())).getLocation();
                hoverPath = null;
                destination = null;

                // Any existing panels should be hidden on click
                hidePanels();

                /*
                 * left-click events
                 */
                if (SwingUtilities.isLeftMouseButton(e)) {

                    // Unselect GameObject on left click
                    if (clicked)
                        selected = null;

                    // If clicked on cell with objects, show info panel
                    if (players.get(current).getObjects().stream().anyMatch(obj -> obj.getCell().getLocation().equals(clickPos))) {

                        cellPanel.update(cells.get(clickPos), players.get(current), selected);
                        cellPanel.setVisible(true);
                        if(infoPanel != null)
                            contentPanel.remove(infoPanel);
                        infoPanel = createInfoPanel();
                        contentPanel.add(infoPanel);
                        layout.putConstraint(SpringLayout.WEST, infoPanel, (clickPos.x() - players.get(current).getViewPoint().getX()) >= Math.round(NUMBER_OF_CELLS_IN_VIEW / 2f) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                        layout.putConstraint(SpringLayout.NORTH, infoPanel, 10, SpringLayout.NORTH, contentPanel);

                        clicked = true;
                    } else
                        clicked = false;

                    // right-click events
                } else if (SwingUtilities.isRightMouseButton(e) && (selected instanceof Unit)) {
                    Motion motion = getShortestAdmissiblePath(selected, cells.get(clickPos)).key();

                    hoverPath = null;
                    destination = null;
                    if (cells.get(clickPos).getUnitSpace() - cells.get(clickPos).getUnitOccupied() >= selected.getSpace()
                            && motion.length() != 0) {
                        ((Unit) selected).changeEnergy(-motion.length());
                        motionToThread(motion);
                    }
                }

                // Redraw game panel
                refreshWindow();
            }
        };
        contentPanel.addMouseListener(mouseAdapter);
        contentPanel.addMouseMotionListener(mouseAdapter);
        contentPanel.addMouseWheelListener(mouseAdapter);

        getContentPane().add(choicePanel);
        getContentPane().add(operationsPanel);
        getContentPane().add(cellPanel);

        // Rescale game elements on screen resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                resetScales();

                choicePanel.resize(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, choicePanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, getContentPane());
                layout.putConstraint(SpringLayout.NORTH, choicePanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, getContentPane());
                operationsPanel.resize(cellWidth, cellHeight);
                layout.putConstraint(SpringLayout.WEST, operationsPanel, Math.round((screenWidth - 3 * cellWidth) / 2f), SpringLayout.WEST, getContentPane());
                layout.putConstraint(SpringLayout.NORTH, operationsPanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, getContentPane());
                cellPanel.setPreferredSize(new Dimension((NUMBER_OF_CELLS_IN_VIEW - 2) * cellWidth + 40, (NUMBER_OF_CELLS_IN_VIEW - 2) * cellHeight + 40));
                layout.putConstraint(SpringLayout.WEST, cellPanel, cellWidth - 20, SpringLayout.WEST, getContentPane());
                layout.putConstraint(SpringLayout.NORTH, cellPanel, cellHeight - 20, SpringLayout.NORTH, getContentPane());

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

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        contentPanel.requestFocus();
        garbageCollector.start();
    }

    private JPanel createInfoPanel() {

        JPanel objectPanel = new JPanel() {
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

        objectPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        objectPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // To intercept mouse motion
        objectPanel.addMouseListener(new MouseAdapter() {});


        objectPanel.setPreferredSize(new Dimension(2 * cellWidth, 5 * cellHeight));
        objectPanel.setOpaque(false);
        return objectPanel;
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
    private void drawObjects(Graphics2D gr) {

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
                Cell vp = players.get(current).getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));
                if (players.get(current).hasSpotted(cell)) {
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
                    gr.setColor(object == selected ? object.getPlayer().getAlternativeColor() : object.getPlayer().getColor());
                    BufferedImage sprite = object.getSprite(false);
                    if(object instanceof Unit u) {
                        if(sprite != null) {
                            if(object == selected)
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

        for(int x = -1; x < NUMBER_OF_CELLS_IN_VIEW + 1; x++) {
            for(int y = -1; y < NUMBER_OF_CELLS_IN_VIEW + 1; y++) {
                Cell vp = players.get(current).getViewPoint();
                Cell cell = cells.get(vp.getLocation().add(x, y, 0));

                if(players.get(current).hasSpotted(cell)){
                    if (cell.getHeatLevel() <= COLD_LEVEL) {
                        gr.setColor(Color.blue);
                        gr.fillOval((x + 1) * cellWidth - 15, y * cellHeight + 10, 5, 5);
                    } else if (cell.getHeatLevel() >= HOT_LEVEL) {
                        gr.setColor(Color.red);
                        gr.fillOval((x + 1) * cellWidth - 15, y * cellHeight + 10, 5, 5);
                    }
                }

                if (!players.get(current).hasSpotted(cell)) {
                    gr.setColor(Color.lightGray);
                    gr.fillRect(x * cellWidth - 1, y * cellHeight - 1, cellWidth + 1, cellHeight + 1);
                } else if (!players.get(current).hasDiscovered(cell)) {
                    gr.setColor(new Color(192, 192, 192, 200));
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
        if(infoPanel != null)
            getContentPane().remove(infoPanel); // TODO replace infoPanel by fixed panel (cf. operationsPanel and choicePanel)
        operationsPanel.setVisible(false);
        choicePanel.setVisible(false);
        cellPanel.setVisible(false);
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

    public Pair<Motion, Location> getShortestAdmissiblePath(GameObject obj, Cell target) {
        if(target == null)
            throw new IllegalArgumentException("Target should not be null.");
        if(!(obj instanceof Unit unit))
            throw new IllegalArgumentException("GameObject should be of type Unit.");

        if(target == obj.getCell())
            return new Pair<>(new Motion(unit, new ArrayList<>(), 0), target.getLocation());

        if(target.distanceTo(unit.getCell()) > unit.getEnergy()
                || !unit.getPlayer().hasSpotted(target))
            return null;

        int maxDist = unit.getEnergy();

        ArrayList<Location> toDo = new ArrayList<>();
        ArrayList<Location> done = new ArrayList<>();
        done.add(new Location(target.getX(), target.getY(), target.getZ()));

        if (unit.getCell().getZ() == target.getZ()) {
            int[][] grid = new int[2 * maxDist + 1][2 * maxDist + 1];
            for (int x = -maxDist; x < maxDist + 1; x++) {
                for (int y = -maxDist; y < maxDist + 1; y++) {
                    grid[maxDist + x][maxDist + y] = ((x == y) && (x == 0)) ? target.getTravelCost() : unit.getEnergy() * 2;
                    if (Math.abs(x + y) == 1 && x * y == 0)
                        toDo.add(new Location(target.getX() + x, target.getY() + y, target.getZ()));
                }
            }

            while (!toDo.isEmpty()) {
                done.addAll(toDo);
                ArrayList<Location> tempList = new ArrayList<>();
                for (Iterator<Location> it = toDo.iterator(); it.hasNext(); ) {
                    Location loc = it.next();

                    if (!cells.containsKey(loc))
                        continue;

                    int min = maxDist * 2;
                    for (int x = (target.getX() - loc.x() == maxDist ? 0 : -1); x < (loc.x() - target.getX() == maxDist ? 1 : 2); x++)
                        for (int y = (target.getY() - loc.y() == maxDist ? 0 : -1); y < (loc.y() - target.getY() == maxDist ? 1 : 2); y++)
                            if (x + y != 0 && x * y == 0)
                                min = Math.min(grid[maxDist + (loc.x() - target.getX()) + x][maxDist + (loc.y() - target.getY()) + y], min);

                    grid[maxDist + (loc.x() - target.getX())][maxDist + (loc.y() - target.getY())] = min + cells.get(loc).getTravelCost() + (players.get(current).hasSpotted(cells.get(loc)) ? 0 : maxDist);
                    done.add(loc);

                    for (int x = -1; x < 2; x++) {
                        for (int y = -1; y < 2; y++) {
                            if (x * y == 0) {
                                Location next = new Location(loc.x() + x, loc.y() + y, loc.z());
                                if (target.getLocation().distanceTo(next) <= unit.getEnergy() && !done.contains(next) && !tempList.contains(next))
                                    tempList.add(next);
                            }
                        }
                    }

                    it.remove();
                }
                toDo = tempList;
            }

            int deltaX = unit.getCell().getX() - target.getX();
            int deltaY = unit.getCell().getY() - target.getY();
            int cost = grid[maxDist + deltaX][maxDist + deltaY] - unit.getCell().getTravelCost();

            if (cost > unit.getEnergy())
                return null;

            ArrayList<Location> path = new ArrayList<>();
            Location current = unit.getCell().getLocation();

            while (!current.equals(target.getLocation())) {
                int min = unit.getEnergy() * 2;
                Location temp = null;
                for (int x = (target.getX() - current.x() == maxDist ? 0 : -1); x < (current.x() - target.getX() == maxDist ? 1 : 2); x++) {
                    for (int y = (target.getY() - current.y() == maxDist ? 0 : -1); y < (current.y() - target.getY() == maxDist ? 1 : 2); y++) {
                        if (x + y != 0 && x * y == 0) {
                            if (grid[maxDist + (current.x() - target.getX()) + x][maxDist + (current.y() - target.getY()) + y] < min
                                    && grid[maxDist + (current.x() - target.getX()) + x][maxDist + (current.y() - target.getY()) + y] < grid[maxDist + (current.x() - target.getX())][maxDist + (current.y() - target.getY())]) {
                                min = Math.min(grid[maxDist + (current.x() - target.getX()) + x][maxDist + (current.y() - target.getY()) + y], min);
                                temp = new Location(x, y, 0);
                            }
                        }
                    }
                }
                current = current.add(temp.x(), temp.y(), 0);
                path.add(temp);
            }

            return new Pair<>(new Motion(unit, path, cost), target.getLocation());
        } else
            return null;
    }

    /**
     * Turns a given Motion object into a thread that moves the object along the given path.
     * If the thread is interrupted, the path is completed instantly.
     * @param motion path to complete
     */
    public void motionToThread(Motion motion) {

        motion.getObject().setStatus(Status.WALKING);
        motion.next(); // To skip the starting position.
        ActionListener taskPerformer = evt -> {
            if(!motion.isDone()) {
                Location next = motion.next();
                moveObject(motion.getObject(), motion.getObject().getCell().getLocation().add(next.x(), next.y(), next.z()));
                refreshWindow();
            }

            if(motion.isDone()) {
                motion.getObject().setStatus(Status.IDLE);
                ((Timer)evt.getSource()).stop();
            }
        };
        new Timer(motion.getObject().getAnimationDelay(), taskPerformer).start();
    }

    public void alert(GameObject object) {
        selected = object;
        refreshWindow();
    }

    /**
     * Calculates the required energy to travel from the current Location to the target Location
     * based on Dijkstra's algorithm
     * @param obj travelling object
     * @param target target Location
     * @return required energy cost
     */
    @Deprecated
    private int calculateTravelCost(GameObject obj, Cell target) {
        if(obj instanceof Unit object) {
            int locX = object.getMaxEnergy();
            int locY = object.getMaxEnergy();
            int locLevel = object.getMaxEnergy();

            ArrayList<Cell> toDo = new ArrayList<>();
            ArrayList<Cell> done = new ArrayList<>();
            done.add(target);

            if (target.distanceTo(object.getCell()) > object.getMaxEnergy())
                return NUMBER_OF_CELLS;

            if (object.getCell().getZ() == target.getZ()) {
                int[][] grid = new int[2 * object.getMaxEnergy() + 1][2 * object.getMaxEnergy() + 1];
                for (int x = -locX; x < locX + 1; x++) {
                    for (int y = -locY; y < locY + 1; y++) {
                        grid[locX + x][locY + y] = ((x == y) && (x == 0)) ? target.getTravelCost() : 9;
                        if (Math.abs(x + y) == 1 && x * y == 0)
                            toDo.add(cells.get(target.getLocation().add(x, y, 0)));
                    }
                }

                while (!toDo.isEmpty()) {
                    done.addAll(toDo);
                    ArrayList<Cell> tempList = new ArrayList<>();
                    for (Iterator<Cell> it = toDo.iterator(); it.hasNext(); ) {
                        Cell loc = it.next();

                        if (!cells.containsKey(loc.getLocation()))
                            continue;

                        int min = NUMBER_OF_CELLS;
                        for (int x = (target.getX() - loc.getX() == locX ? 0 : -1); x < (loc.getX() - target.getX() == locX ? 1 : 2); x++)
                            for (int y = (target.getY() - loc.getY() == locY ? 0 : -1); y < (loc.getY() - target.getY() == locY ? 1 : 2); y++)
                                if (x + y != 0 && x * y == 0)
                                    min = Math.min(grid[locX + (loc.getX() - target.getX()) + x][locY + (loc.getY() - target.getY()) + y], min);

                        grid[locX + (loc.getX() - target.getX())][locY + (loc.getY() - target.getY())] = min + loc.getTravelCost();

                        done.add(loc);
                        for (int x = -1; x < 2; x++) {
                            for (int y = -1; y < 2; y++) {
                                if (x * y == 0) {
                                    Cell next = cells.get(loc.getLocation().add(x, y, 0));
                                    if (target.distanceTo(next) <= object.getMaxEnergy() && !done.contains(next) && !tempList.contains(next))
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

                return grid[locX + deltaX][locY + deltaY] - object.getCell().getTravelCost();
            }
        }
        return NUMBER_OF_CELLS;
    }
}
