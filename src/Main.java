import items.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends JFrame{

    public final static int NUMBER_OF_CELLS = 10;

    private int mouseX, mouseY, viewLevel;
    private int cellWidth, cellHeight, poolSize, screenWidth, screenHeight;
    private int current;
    private boolean clicked;

    private HashMap<Pair, GameObject> objects;
    private HashMap<Pair, Cell> cells;
    private ArrayList<Player> players;

    private JMenu playerMenu, viewMenu, cellMenu;
    private JPanel infoPanel;

    public static void main(String[] args) {
        Main mainframe = new Main();
        mainframe.addPlayer(new Player("You", Color.blue));
        mainframe.construct();
    }

    public Main() {
        cells = new HashMap<>();
        objects = new HashMap<>();
        this.players = new ArrayList<>();
        current = 0;
        viewLevel = 0;

        for(int i = 0; i < NUMBER_OF_CELLS; i++) {
            for(int j = 0; j < NUMBER_OF_CELLS; j++) {
                cells.put(new Pair(i, j), new Cell(2));
            }
        }
    }

    public void addPlayer(Player p) {
        players.add(p);
        objects.put(new Pair(2, 2), new Base(p, 2, 2));
        objects.put(new Pair(2, 3), new Villager(p, 2, 3));
    }

    private void cyclePlayers() {
        current = (current + 1) % players.size();
        playerMenu.setText("Player: " + players.get(current).getName());
    }

    private void construct() {
        super.setTitle("Game of Ages");
        JFrame frame = this;

        screenWidth = frame.getWidth();
        screenHeight = frame.getHeight();
        cellWidth = Math.floorDiv(screenWidth, NUMBER_OF_CELLS);
        cellHeight = Math.floorDiv(screenHeight - frame.getInsets().top, NUMBER_OF_CELLS);
        poolSize = Math.min(Math.floorDiv(cellWidth, 2), Math.floorDiv(cellHeight, 2));
        clicked = false;

        JPanel pane = new JPanel() {
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

                gr.setColor(Color.red);
                gr.setStroke(new BasicStroke(2));
                gr.drawRect(Math.floorDiv(mouseX, cellWidth) * cellWidth, Math.floorDiv(mouseY, cellHeight) * cellHeight, cellWidth, cellHeight);

                for (Pair key : cells.keySet()) {
                    Cell cell = cells.get(key);
                    if(cell.getResource(Cell.WATER) >= Cell.WATER_THRESHOLD) {
                        gr.setColor(Color.blue);
                        gr.fillOval((int)Math.round((key.X + 0.5) * cellWidth) - Math.floorDiv(poolSize, 2), (int)Math.round((key.Y + 0.5) * cellHeight) - Math.floorDiv(poolSize, 2), poolSize, poolSize);
                    }
                    gr.setColor(Color.black);
                }

                for (Pair key : objects.keySet()) {
                    GameObject object = objects.get(key);
                    gr.setColor(object.getPlayer().getColor());
                    gr.drawString(object.getToken(), object.getX() * cellWidth + 10, object.getY() * cellHeight + 20);
                }
            }
        };
        super.setContentPane(pane);
        SpringLayout layout = new SpringLayout();
        pane.setLayout(layout);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if(e.getKeyChar() == 'n') {
                    cyclePlayers();
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                mouseX = e.getX();
                mouseY = e.getY();
                pane.repaint();
                pane.revalidate();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                viewLevel += e.getWheelRotation();
                viewMenu.setText("View: " + viewLevel);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int clickX = Math.floorDiv(mouseX, cellWidth);
                int clickY = Math.floorDiv(mouseY, cellHeight);
                Pair p = new Pair(clickX, clickY);
                cellMenu.setText("Space: " + cells.get(p).getOccupied() + "/" + cells.get(p).getSpace());

                if(clicked) {
                    pane.remove(infoPanel);
                }

                GameObject object = objects.get(p);
                if(object != null) {
                    infoPanel = createInfoPanel(object);
                    pane.add(infoPanel);
                    layout.putConstraint(SpringLayout.WEST, infoPanel, (clickX >= Math.floorDiv(NUMBER_OF_CELLS, 2)) ? 10 : (screenWidth - 2 * cellWidth - 30), SpringLayout.WEST, pane);
                    layout.putConstraint(SpringLayout.NORTH, infoPanel, 10, SpringLayout.NORTH, pane);

                    clicked = true;
                } else {clicked = false;}

                frame.repaint();
                frame.revalidate();
            }
        };

        pane.addMouseListener(mouseAdapter);
        pane.addMouseMotionListener(mouseAdapter);
        pane.addMouseWheelListener(mouseAdapter);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                screenWidth = frame.getWidth();
                screenHeight = frame.getHeight();
                cellWidth = Math.floorDiv(frame.getWidth(), NUMBER_OF_CELLS);
                cellHeight = Math.floorDiv(frame.getHeight() - frame.getInsets().top, NUMBER_OF_CELLS);
                poolSize = Math.min(Math.floorDiv(cellWidth, 2), Math.floorDiv(cellHeight, 2));
                pane.repaint();
                pane.revalidate();
            }
        });

        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        playerMenu = new JMenu("Player: " + players.get(current).getName());
        viewMenu = new JMenu("View: " + viewLevel);
        cellMenu = new JMenu("Nothing to show");
        menubar.add(playerMenu);
        menubar.add(viewMenu);
        menubar.add(cellMenu);

        super.pack();
        super.setVisible(true);
        super.setExtendedState(MAXIMIZED_BOTH);

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pane.requestFocus();
    }

    private JPanel createInfoPanel(GameObject object) {
        JPanel pane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D gr = (Graphics2D) g;
                gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                gr.setColor(Color.lightGray);
                gr.fillRoundRect(1, 1, 2 * cellWidth - 2, 5 * cellHeight - 3, 10, 10);
                gr.setColor(Color.black);
                gr.setStroke(new BasicStroke(2));
                gr.drawRoundRect(1, 1, 2 * cellWidth - 2, 5 * cellHeight - 3, 10, 10);

                gr.setColor(Color.black);
                customDrawString(gr, object.toString(), 5, 0);
            }
        };
        pane.setPreferredSize(new Dimension(2 * cellWidth, 5 * cellHeight));
        pane.setOpaque(false);
        return pane;
    }

    private static void customDrawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }
}
