package UI;

import core.*;
import core.contracts.AttackContract;
import core.contracts.ConstructContract;
import core.player.Player;
import objects.GameObject;
import core.Status;
import objects.buildings.Building;
import objects.buildings.Foundation;
import objects.buildings.Wall;
import objects.units.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Set;

import static UI.CustomMethods.cellCoordinateTransform;
import static core.GameConstants.*;

public class CellPanel extends JPanel {

    private Pair<Integer, Integer> selection;
    private int poolSize, unitRow, buildingRow, enemyRow;

    private final Property<GameObject> selected;
    private final Property<Pair<GameObject, Boolean>> target;
    private final Property<Boolean> cellArrowProperty;
    private Player player;
    private BiMap objectMap;
    private Cell cell;

    public CellPanel(Property<GameObject> selected, Property<Pair<GameObject, Boolean>> target, Property<Boolean> cellArrowProperty) {
        selection = new Pair<>(-1, -1);
        objectMap = new BiMap();
        buildingRow = 1;
        this.selected = selected;
        this.target = target;
        this.cellArrowProperty = cellArrowProperty;

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseClicked(e);
                GameObject obj = objectMap.get(selection);

                if(target.get().map(Pair::value).orElse(false) && SwingUtilities.isRightMouseButton(e))
                    target.set(new Pair<>(obj, false));
                else if(SwingUtilities.isLeftMouseButton(e))
                    selected.set(obj);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);

                selection = cellCoordinateTransform(e.getX(), e.getY());
                if(objectMap.get(selection) == null)
                    selection = new Pair<>(-1, -1);

                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                selection = new Pair<>(-1, -1);
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        setOpaque(false);
    }

    public void update() {
        update(cell, player);
    }
    
    public void update(Cell cell, Player player) {
        this.cell = cell;
        this.player = player;
        poolSize = Math.min(Math.round(getWidth() / 4f), Math.round(getHeight() / 4f)); // TODO Might move to a resize method

        unitRow = 0;
        buildingRow = CustomMethods.cellCoordinateTransform(0, getHeight() - CELL_Y_MARGIN - SPRITE_SIZE_MAX).value();
        enemyRow = (buildingRow / 2) + 1;


        if(cell != null) {
            int unitCounter = 0;
            int buildingCounter = 0;
            int enemyCounter = 0;
            objectMap = new BiMap();
            for (GameObject object : cell.getContent()) {
                if(object.getPlayer().equals(player)) {
                    if (object instanceof Unit)
                        objectMap.put(new Pair<>(unitCounter++, unitRow), object);
                    else if (object instanceof Building || object instanceof Foundation)
                        objectMap.put(new Pair<>(buildingCounter++, buildingRow), object);
                } else
                    objectMap.put(new Pair<>(enemyCounter++, enemyRow), object);
            }
        }
    }

    public Cell getCurrentCell() {
        return cell;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(cell != null) {
            Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D) g.create());

            gr.setColor(Color.white);
            gr.fill(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 30, 30));
            gr.setColor(Color.black);

            gr.setStroke(new BasicStroke(2));
            gr.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 30, 30));

            drawField(gr);
            drawForest(gr);
            drawRiver(gr);
            drawObjects(gr);

            if(target.getUnsafe().value()) {
                Area cover = new Area(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 30, 30));
                cover.subtract(new Area(new RoundRectangle2D.Double(CELL_X_MARGIN / 2f, CELL_Y_MARGIN + enemyRow * (CELL_Y_MARGIN + SPRITE_SIZE_MAX) - SPRITE_SIZE_MAX / 2f, getWidth() - CELL_X_MARGIN, 2 * SPRITE_SIZE_MAX, 30, 30)));

                gr.setColor(new Color(0, 0, 0, 128));
                gr.fill(cover);
            }

            gr.dispose();
        }
    }

    public void drawField(Graphics2D gr) {
        if (cell.isField()) {
            gr.setColor(new Color(200, 120, 0, 100));
            gr.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        }
    }

    public void drawRiver(Graphics2D gr) {
        
        if (cell.isRiver()) {
            gr.setColor(new Color(0, 100, 255));
            if (cell.getX() < NUMBER_OF_CELLS - 2 && cell.getY() < NUMBER_OF_CELLS - 2 && cell.fetch(1, 0, 0).isRiver() && cell.fetch(0, 1, 0).isRiver() && cell.fetch(1, 1, 0).isRiver())
                gr.fillRoundRect(Math.round(0.5f * getWidth() - (poolSize / 2f)), Math.round(0.5f * getHeight() - (poolSize / 2f)), getWidth() + poolSize, getHeight() + poolSize, poolSize, poolSize);
            else {
                boolean singleFlag = true;
                if (cell.getX() < NUMBER_OF_CELLS - 1 && cell.fetch(1, 0, 0).isRiver())
                    gr.fillRect(Math.round(0.5f * getWidth()), Math.round(0.5f * getHeight() - (poolSize / 2f)), Math.round(getWidth() / 2f) + 1, poolSize);
                if (cell.getX() > 0 && cell.fetch(-1, 0, 0).isRiver())
                    gr.fillRect(- 1, Math.round(0.5f * getHeight() - (poolSize / 2f)), Math.round(getWidth() / 2f), poolSize);
                if (cell.getY() < NUMBER_OF_CELLS - 1 && cell.fetch(0, 1, 0).isRiver()) {
                    gr.fillRect(Math.round(0.5f * getWidth() - (poolSize / 2f)), Math.round(0.5f * getHeight()), poolSize, Math.round(getHeight() / 2f));
                    singleFlag = false;
                }
                if (cell.getY() > 0 && cell.fetch(0, -1, 0).isRiver()) {
                    gr.fillRect(Math.round(0.5f * getWidth() - (poolSize / 2f)), 0, poolSize, Math.round(getHeight() / 2f));
                    singleFlag = false;
                }

                if (singleFlag)
                    gr.fillOval(Math.round(0.5f * getWidth() - poolSize), Math.round(0.5f * getHeight() - (poolSize / 2f)), poolSize * 2, poolSize);
                else
                    gr.fillOval(Math.round(0.5f * getWidth() - (poolSize / 2f)), Math.round(0.5f * getHeight() - (poolSize / 2f)), poolSize, poolSize);
            }
        }
    }

    /**
     * Draws forests if the current cell is a forest.
     * TODO Fix rounded corners for arcs!
     * @param gr Graphics object from panel
     */
    public void drawForest(Graphics2D gr) {
        if (cell.isForest()) {
            gr.setColor(new Color(20, 150, 20));

            gr.fillArc(- poolSize, getHeight() - poolSize, 2 * poolSize, 2 * poolSize, 0, 90);
            if(cell.fetch(1, 0, 0).isForest())
                gr.fillArc(getWidth() - poolSize, getHeight() - poolSize, 2 * poolSize, 2 * poolSize, 90, 90);
            if(cell.fetch(0, -1, 0).isForest())
                gr.fillArc(- poolSize, - poolSize, 2 * poolSize, 2 * poolSize, 270, 90);
        }
    }

    /**
     * Draws GameObjects such as Units and Buildings.
     * @param gr Graphics object from panel
     */
    public void drawObjects(Graphics2D gr) {
        /*
         * Draw all (visible/relevant) game objects taking into account their state
         */
        for(Pair<Integer, Integer> pair : objectMap.posSet()) {
            GameObject object = objectMap.get(pair);
            selected.get().ifPresentOrElse(
                    obj -> gr.setColor(object.equals(obj) ? object.getPlayer().getAlternativeColor() : object.getPlayer().getColor()),
                    () -> gr.setColor(object.getPlayer().getColor()));

            // Draws animation around working Units
            if(object instanceof Unit<?> u && u.getStatus() == Status.WORKING) {
                if(cellArrowProperty.getUnsafe()) {
                    u.getContracts().stream().filter(c -> c instanceof ConstructContract<?>).map(c -> (ConstructContract<?>)c).forEach(c -> {
                        Pair<Integer, Integer> targetPair = objectMap.get(c.getFoundation());
                        if(targetPair != null)
                            drawArrow(gr, (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), CELL_Y_MARGIN,targetPair.key() * (CELL_X_MARGIN + SPRITE_SIZE_MAX),getHeight() - CELL_X_MARGIN - SPRITE_SIZE_MAX);
                    });

                    u.getContracts().stream().filter(c -> c instanceof AttackContract).map(c -> (AttackContract)c).forEach(c -> {
                        Pair<Integer, Integer> targetPair = objectMap.get(c.getTarget());
                        if(targetPair != null)
                            drawWavyArrow(gr, (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), pair.value() * (CELL_Y_MARGIN + SPRITE_SIZE_MAX), (CELL_X_MARGIN + SPRITE_SIZE_MAX) * targetPair.key(),targetPair.value() * (CELL_Y_MARGIN + SPRITE_SIZE_MAX));
                    });
                }

                double pieces = u.getCycleLength() / 4f;
                int currentLength = u.getCurrentStep() + 1;
                int part = (int)(currentLength / pieces);
                double remainder = (currentLength / pieces) - part;

                if(part > 0) {
                    gr.drawLine(CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), CELL_Y_MARGIN, CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + SPRITE_SIZE_MAX, CELL_Y_MARGIN);
                    if(part > 1) {
                        gr.drawLine(CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + SPRITE_SIZE_MAX, CELL_Y_MARGIN, CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + SPRITE_SIZE_MAX, CELL_Y_MARGIN + SPRITE_SIZE_MAX);
                        if(part > 2) {
                            gr.drawLine(CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), CELL_Y_MARGIN + SPRITE_SIZE_MAX, CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + SPRITE_SIZE_MAX, CELL_Y_MARGIN + SPRITE_SIZE_MAX);
                            if(part > 3)
                                gr.drawLine(CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), CELL_Y_MARGIN, CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), CELL_Y_MARGIN + SPRITE_SIZE_MAX);
                            else
                                gr.drawLine(CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), CELL_Y_MARGIN + (int)((1 - remainder) * SPRITE_SIZE_MAX), CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), CELL_Y_MARGIN + SPRITE_SIZE_MAX);
                        } else
                            gr.drawLine(CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + (int)((1 - remainder) * SPRITE_SIZE_MAX), CELL_Y_MARGIN + SPRITE_SIZE_MAX, CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + SPRITE_SIZE_MAX, CELL_Y_MARGIN + SPRITE_SIZE_MAX);
                    } else
                        gr.drawLine(CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + SPRITE_SIZE_MAX, CELL_Y_MARGIN, CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + SPRITE_SIZE_MAX, CELL_Y_MARGIN + (int)(SPRITE_SIZE_MAX * remainder));
                } else
                    gr.drawLine(CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), CELL_Y_MARGIN, CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + (int)(SPRITE_SIZE_MAX * remainder), CELL_Y_MARGIN);
            }

            object.getSprite(true).ifPresentOrElse(
                    sprite -> gr.drawImage(selected.get().map(
                                    obj -> object.equals(obj) ? CustomMethods.selectedSprite(sprite, gr.getColor()) : sprite).orElse(sprite),
                            (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key() + CELL_X_MARGIN,
                            (CELL_Y_MARGIN + SPRITE_SIZE_MAX) * pair.value() + CELL_Y_MARGIN, null),
                    () -> gr.drawString(object.getToken(), CELL_X_MARGIN + (CELL_X_MARGIN + SPRITE_SIZE_MAX) * pair.key(), (CELL_Y_MARGIN + SPRITE_SIZE_MAX) * pair.value() + CELL_Y_MARGIN + gr.getFontMetrics().getHeight()));

            // Draws a coloured box around objects of other players to indicate the corresponding Player
            if(!object.getPlayer().equals(player) && objectMap.get(object) != objectMap.get(selected.getUnsafe()))
                drawBox(gr, object.getPlayer().getColor(), objectMap.get(object));
        }

        if(objectMap.objSet().stream().anyMatch(obj -> player.equals(obj.getPlayer()) && obj instanceof Wall)) {
            gr.setColor(new Color(80, 40, 10));
            Stroke oldStroke = gr.getStroke();
            gr.setStroke(new BasicStroke(20));
            gr.drawOval(getWidth() / 6, getHeight() / 6, 2 * getWidth() / 3, 2 * getHeight() / 3);
            gr.setStroke(oldStroke);
        }

        // Draws the selection box for Unit/Building/Foundation objects
        // TODO Fix bounding box for nonstandard sprite sizes (cf. CustomMethods.selectedSprite)
        if(selection.key() !=-1 && selection.value() != -1)
            drawBox(gr, player.getAlternativeColor(), selection);
    }

    private void drawBox(Graphics2D gr, Color c, Pair<Integer, Integer> pos) {
        Color oldColor = gr.getColor();
        gr.setColor(c);
        gr.drawRect(pos.key() * (SPRITE_SIZE_MAX + CELL_X_MARGIN) + CELL_X_MARGIN, (CELL_Y_MARGIN + SPRITE_SIZE_MAX) * pos.value() + CELL_Y_MARGIN,  SPRITE_SIZE_MAX, SPRITE_SIZE_MAX);
        gr.setColor(oldColor);
    }

    private void drawArrow(Graphics2D gr, int x1, int y1, int x2, int y2) {

        Stroke oldStroke = gr.getStroke();
        gr.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));

        int lineX = x2 - x1;
        int lineY = y2 - y1;
        double angle = -Math.atan2(lineY, lineX) - Math.PI / 2;

        BufferedImage rotated = CustomMethods.rotateImage(ARROWHEAD, angle);
        gr.drawLine(x1 + CELL_X_MARGIN + (SPRITE_SIZE_MAX / 2),
                y1 + (SPRITE_SIZE_MAX / 2), x2 + CELL_X_MARGIN + (SPRITE_SIZE_MAX / 2), y2);
        gr.setStroke(oldStroke);
        gr.drawImage(rotated, x2 + (SPRITE_SIZE_MAX / 2) + CELL_X_MARGIN - rotated.getWidth() / 2,
                y2 - rotated.getHeight() / 2,
                null);
    }

    private void drawWavyArrow(Graphics2D gr, int x1, int y1, int x2, int y2) {

        double dx = x2 - x1;
        double dy = y2 - y1;

        double angle = -Math.atan2(dy, dx) - Math.PI / 2;
        BufferedImage rotated = CustomMethods.rotateImage(ARROWHEAD, angle);

        x1 = x1 + CELL_X_MARGIN + (SPRITE_SIZE_MAX / 2);
        y1 = y1 + CELL_Y_MARGIN + (SPRITE_SIZE_MAX / 2);
        x2 = x2 + CELL_X_MARGIN + (SPRITE_SIZE_MAX / 2);
        y2 = y2 - rotated.getHeight() / 2;

        dx = x2 - x1;
        dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        // Normalize direction
        double ux = dx / length;
        double uy = dy / length;

        // Perpendicular vector (for offsetting)
        double px = -uy;
        double py = ux;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x1, y1);

        // Step along the line in small increments
        for (double t = 0; t <= length; t += 2) { // step of 2 pixels
            double baseX = x1 + ux * t;
            double baseY = y1 + uy * t;
            double offset = 5 * Math.sin((2 * Math.PI / (length / 3)) * t);
            double wx = baseX + px * offset;
            double wy = baseY + py * offset;
            path.lineTo(wx, wy);
        }

        gr.draw(path);
        gr.drawImage(rotated, x2  - rotated.getWidth() / 2, y2 - rotated.getHeight() / 2,null);
    }

    public static class BiMap {
        private final HashMap<Pair<Integer, Integer>, GameObject> posToObj;
        private final HashMap<GameObject, Pair<Integer, Integer>> objToPos;

        public BiMap() {
            posToObj = new HashMap<>();
            objToPos = new HashMap<>();
        }

        public GameObject get(Pair<Integer, Integer> pos) {
            return posToObj.get(pos);
        }

        public Pair<Integer, Integer> get(GameObject obj) {
            return objToPos.get(obj);
        }

        public void put(Pair<Integer, Integer> pos, GameObject obj) {
            posToObj.put(pos, obj);
            objToPos.put(obj, pos);
        }

        public Set<Pair<Integer, Integer>> posSet() {
            return posToObj.keySet();
        }

        public Set<GameObject> objSet() {
            return objToPos.keySet();
        }
    }
}