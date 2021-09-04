import core.Cell;
import core.LaborContract;
import core.Player;
import items.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

import static javax.swing.MenuSelectionManager.defaultManager;
import static core.GameConstants.*;

public class Main extends JFrame{

    private int mouseX, mouseY;
    private Pair clickPos;
    private int cellWidth, cellHeight, poolSize, screenWidth, screenHeight;
    private int current, cycle;
    private boolean clicked;

    private HashMap<Pair, ArrayList<GameObject>> objects;
    private HashMap<Pair, Cell> cells;
    private ArrayList<Player> players;
    private GameObject selected;

    private JMenu playerMenu, viewMenu, cellMenu;
    private JMenuItem[] playerLabels;
    private JMenuItem[] resourceLabels;
    private JPanel infoPanel, resourcePanel;

    public static void main(String[] args) {
        Main mainframe = new Main();
        mainframe.addPlayer(new Player("You", Color.blue, Color.MAGENTA));
        mainframe.construct();
    }

    public Main() {
        cells = new HashMap<>();
        objects = new HashMap<>();
        this.players = new ArrayList<>();
        current = 0;
        cycle = 0;
        clickPos = new Pair(0, 0);

        for(int i = 0; i < NUMBER_OF_CELLS; i++) {
            for(int j = 0; j < NUMBER_OF_CELLS; j++) {
                cells.put(new Pair(i, j), new Cell(INITIAL_CELL_UNIT_SPACE, INITIAL_CELL_BUILDING_SPACE));
                objects.put(new Pair(i, j), new ArrayList<>(INITIAL_CELL_UNIT_SPACE + INITIAL_CELL_BUILDING_SPACE));
            }
        }
    }

    public void addPlayer(Player p) {
        players.add(p);
        addObject(new Base(p, 2, 2));
        addObject(new Villager(p, 2, 2));
        addObject(new Villager(p, 7, 3));
    }

    /**
     * Changes the current player
     */
    private void cyclePlayers() {
        if(current == (players.size() - 1)) {
            cycle++;
            current = 0;
        } else
            current++;
        playerMenu.setText("Player: " + players.get(current).getName());

        for(ArrayList<GameObject> list : objects.values()) {
            for(GameObject object : list) {
                object.reset();
                if(object.getPlayer() == players.get(current) && object instanceof Productive) {
                    Productive producer = (Productive)object;
                    producer.work();
                }
            }
        }

        for(int i = 0; i < RESOURCE_LABELS.length; i++) {
            playerLabels[i].setText(RESOURCE_LABELS[i] + ": " + players.get(current).getResource(i));
        }
    }

    /**
     * Adds a new object to the game
     * @param object
     */
    private void addObject(GameObject object) {
        Pair p = new Pair(object.getX(), object.getY());
        objects.get(p).add(object);
        if(object instanceof Unit) {
            Unit u = (Unit)object;
            cells.get(p).changeUnitOccupied(u.getSize());
        } else if(object instanceof Building) {
            Building b = (Building)object;
            cells.get(p).changeBuildingOccupied(b.getSize());
            cells.get(p).changeUnitSpace(b.getSpace());
        }
    }

    /**
     * Moves an existing object
     * @param object
     * @param coord
     */
    private void moveObject(Movable object, Pair coord) {
        Pair p = new Pair(object.getX(), object.getY());
        object.setX(coord.X);
        object.setY(coord.Y);
        object.setViewLevel(object.getPlayer().getViewLevel());
        objects.get(p).remove(object);
        objects.get(coord).add(object);
        if(object instanceof Unit) {
            Unit u = (Unit)object;
            cells.get(p).changeUnitOccupied(-u.getSize());
            cells.get(coord).changeUnitOccupied(u.getSize());
        } else if(object instanceof Building) {
            Building b = (Building)object;
            cells.get(p).changeBuildingOccupied(-b.getSize());
            cells.get(p).changeUnitSpace(-b.getSpace());
            cells.get(coord).changeBuildingOccupied(b.getSize());
            cells.get(coord).changeUnitSpace(b.getSpace());
        }
    }

    /**
     * Constructs a game frame
     */
    private void construct() {
        super.setTitle("Game of Ages");
        JFrame frame = this;

        screenWidth = 0;
        screenHeight = 0;
        cellWidth = 0;
        cellHeight = 0;
        poolSize = 0;
        clicked = false;

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                super.setBackground(Color.white);
                Graphics2D gr = (Graphics2D)g;
                gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                for(int i = 0; i < NUMBER_OF_CELLS-1; i++) {
                    gr.drawLine((i+1)*cellWidth, 0, (i+1)*cellWidth, screenHeight - frame.getInsets().top);
                    gr.drawLine( 0,  (i+1)*cellHeight, screenWidth, (i+1)*cellHeight);
                }

                gr.setColor(players.get(current).getAlternativeColor());
                gr.setStroke(new BasicStroke(2));
                Pair p = posToPair(mouseX, mouseY);
                gr.drawRect(p.X * cellWidth, p.Y * cellHeight, cellWidth, cellHeight);

                for (Pair key : cells.keySet()) {
                    Cell cell = cells.get(key);
                    if(cell.getResource(WATER) >= WATER_THRESHOLD) {
                        gr.setColor(Color.blue);
                        gr.fillOval((int)Math.round((key.X + 0.5) * cellWidth) - Math.floorDiv(poolSize, 2), (int)Math.round((key.Y + 0.5) * cellHeight) - Math.floorDiv(poolSize, 2), poolSize, poolSize);
                    }
                    gr.setColor(Color.black);
                }

                for (ArrayList<GameObject> list : objects.values()) {
                    int unitCounter = 0;
                    int buildingCounter = 0;
                    for(GameObject object : list) {
                        if(object.getViewLevel() == players.get(current).getViewLevel()) {
                            gr.setColor(object == selected ? object.getPlayer().getAlternativeColor() : object.getPlayer().getColor());
                            if(object instanceof Unit) {
                                unitCounter++;
                                gr.drawString(object.getToken(), object.getX() * cellWidth + 10 * unitCounter, object.getY() * cellHeight + 20);
                            } else {
                                buildingCounter++;
                                gr.drawString(object.getToken(), object.getX() * cellWidth + 10 * buildingCounter, (object.getY() + 1) * cellHeight - g.getFontMetrics().getHeight());
                            }
                        }
                    }
                }
            }
        };
        super.setContentPane(contentPanel);
        SpringLayout layout = new SpringLayout();
        contentPanel.setLayout(layout);

        // Cycle players on "n" stroke
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('n'), "cycle");
        contentPanel.getActionMap().put("cycle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cyclePlayers();
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                mouseX = e.getX();
                mouseY = e.getY();
                contentPanel.repaint();
                contentPanel.revalidate();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                players.get(current).changeViewLevel(e.getWheelRotation());
                viewMenu.setText("View: " + players.get(current).getViewLevel());
                frame.repaint();
                frame.revalidate();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                clickPos = posToPair(e.getX(), e.getY());
                Cell cell = cells.get(clickPos);

                // left-click events
                if(e.getButton() == MouseEvent.BUTTON1) {
                    cellMenu.setText("Space: " + cell.getUnitOccupied() + "/" + cell.getUnitSpace() +
                            " | " + cell.getBuildingOccupied() + "/" + cell.getBuildingSpace());
                    for(int i = 0; i < RESOURCE_LABELS.length; i++)
                        resourceLabels[i].setText(RESOURCE_LABELS[i] + ": " + cell.getResource(i));

                    // Any existing info panels should be removed on click
                    if(clicked) {
                        selected = null;
                        contentPanel.remove(infoPanel);
                    }

                    // If clicked on cell with objects, show info panel
                    if(objects.get(clickPos).size() > 0) {
                        infoPanel = createInfoPanel();
                        contentPanel.add(infoPanel);
                        layout.putConstraint(SpringLayout.WEST, infoPanel, (clickPos.X >= Math.floorDiv(NUMBER_OF_CELLS, 2)) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, contentPanel);
                        layout.putConstraint(SpringLayout.NORTH, infoPanel, 10, SpringLayout.NORTH, contentPanel);

                        clicked = true;
                    } else {clicked = false;}
                // right-click events
                } else if(e.getButton() == MouseEvent.BUTTON3) {
                    if(selected != null && selected instanceof Depletable) {
                        Depletable object = (Depletable)selected;
                        int distance = (Math.abs(object.getViewLevel() - players.get(current).getViewLevel()) + travelDistance(object, clickPos));
                        if(object instanceof Movable && object.getEnergy() >= distance) {
                            moveObject((Movable) object, clickPos);
                            object.changeEnergy(-distance);
                            cellMenu.setText("Space: " + cell.getUnitOccupied() + "/" + cell.getUnitSpace() +
                                    " | " + cell.getBuildingOccupied() + "/" + cell.getBuildingSpace());
                        }

                        if(object instanceof Productive)
                            resourcePanel.setVisible(true);
                    }
                }

                // Redraw game panel
                // ?? MIGHT NEED TO BE OPTIMIZED ??
                // ?? NOT DOING THIS EVERY TIME IF NOT NECESSARY ??
                frame.repaint();
                frame.revalidate();
            }
        };

        contentPanel.addMouseListener(mouseAdapter);
        contentPanel.addMouseMotionListener(mouseAdapter);
        contentPanel.addMouseWheelListener(mouseAdapter);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                resetScales();
                constructResourcePanel();
                contentPanel.repaint();
                contentPanel.revalidate();
            }
        });

        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        playerMenu = new JMenu("Player: " + players.get(current).getName());
        viewMenu = new JMenu("View: " + players.get(current).getViewLevel());
        cellMenu = new JMenu("Nothing to show");
        menubar.add(playerMenu);
        menubar.add(viewMenu);
        menubar.add(cellMenu);

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
        resourceLabels = new JMenuItem[RESOURCE_LABELS.length];
        for(int i = 0; i < RESOURCE_LABELS.length; i++) {
            JMenuItem label = new JMenuItem(RESOURCE_LABELS[i] + ": N/A");
            cellMenu.add(label);
            resourceLabels[i] = label;
        }

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
        playerLabels = new JMenuItem[RESOURCE_LABELS.length];
        for(int i = 0; i < RESOURCE_LABELS.length; i++) {
            JMenuItem label = new JMenuItem(RESOURCE_LABELS[i] + ": " + players.get(current).getResource(i));
            playerMenu.add(label);
            playerLabels[i] = label;
        }

        super.setVisible(true);
        super.setExtendedState(MAXIMIZED_BOTH);

        resetScales();
        constructResourcePanel();

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        contentPanel.requestFocus();
    }

    private void constructResourcePanel() {

        if(resourcePanel != null)
            getContentPane().remove(resourcePanel);

        resourcePanel = new JPanel();
        resourcePanel.addMouseListener(new MouseAdapter() {});

        resourcePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resourcePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Dimension buttonSize = new Dimension((int)Math.round(cellWidth / 1.5) + 2, Math.floorDiv(cellHeight, 2) + 2);
        for(int i = 0; i < RESOURCE_LABELS.length; i++) {
            final int step = i;

            RoundedButton button = new RoundedButton(RESOURCE_LABELS[step], buttonSize, players.get(current).getAlternativeColor());

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    Productive producer = (Productive)selected;
                    producer.addContract(new LaborContract(producer, step, 1, 5, cells.get(clickPos)));
                    resourcePanel.setVisible(false);
                }
            });

            button.setPreferredSize(buttonSize);
            c.gridx = step % 4;
            c.gridy = Math.floorDiv(step, 4);
            c.weightx = 0.5;
            c.weighty = 0.5;
            resourcePanel.add(button, c);
        }

        resourcePanel.setVisible(false);
        resourcePanel.setPreferredSize(new Dimension(3 * cellWidth, 2 * cellHeight));
        resourcePanel.setOpaque(false);

        getContentPane().add(resourcePanel);
        SpringLayout layout = (SpringLayout)getContentPane().getLayout();
        layout.putConstraint(SpringLayout.WEST, resourcePanel, Math.floorDiv(screenWidth - 3 * cellWidth, 2), SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, resourcePanel, screenHeight - 3 * cellHeight, SpringLayout.NORTH, getContentPane());
    }

    private JPanel createInfoPanel() {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = (Graphics2D) g;
                gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                gr.setColor(Color.lightGray);
                gr.fillRoundRect(1, 1, 2 * cellWidth - 3, 5 * cellHeight - 3, 10, 10);
                gr.setColor(Color.black);
                gr.setStroke(new BasicStroke(2));
                gr.drawRoundRect(1, 1, 2 * cellWidth - 3, 5 * cellHeight - 3, 10, 10);
            }
        };

        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();

        // To intercept mouse motion
        panel.addMouseListener(new MouseAdapter() {});

        Dimension buttonSize = new Dimension(Math.floorDiv(cellWidth, 2), Math.floorDiv(cellHeight, 2));
        int counter = 0;
        for(GameObject object : objects.get(clickPos)) {
            RoundedButton objectButton = new RoundedButton(object.getToken(), buttonSize, players.get(current).getAlternativeColor());

            objectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    selected = object;
                    Main.this.repaint();
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

    private void resetScales() {
        screenWidth = getWidth();
        screenHeight = getHeight();
        cellWidth = Math.floorDiv(getWidth(), NUMBER_OF_CELLS);
        cellHeight = Math.floorDiv(getHeight() - getInsets().top, NUMBER_OF_CELLS);
        poolSize = Math.min(Math.floorDiv(cellWidth, 2), Math.floorDiv(cellHeight, 2));
    }

    private Pair posToPair(int x, int y) {
        return new Pair (Math.floorDiv(x, cellWidth), Math.floorDiv(y, cellHeight));
    }

    private static void customDrawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }

    private static int travelDistance(GameObject object, Pair p) {
        return Math.abs(object.getX() - p.X) + Math.abs(object.getY() - p.Y);
    }
}
