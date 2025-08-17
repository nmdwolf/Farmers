package UI;

import core.Cell;
import core.Player;
import core.Property;
import core.Status;
import objects.GameObject;
import objects.buildings.Building;
import objects.units.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import static core.GameConstants.*;
import static core.GameConstants.BUILDING_SPRITE_SIZE_MAX;

public class CellPanel extends JPanel {

    private static final int STROKE_WIDTH = 2;
    private int selectionX, selectionY;
    private int poolSize;

    private final Property<GameObject> selected;
    private Player player;
    private HashMap<GameObject, Integer> objectMap;
    private Cell cell;

    public CellPanel(Property<GameObject> selected) {
        selectionX = -1;
        selectionY = -1;
        objectMap = new HashMap<>();
        this.selected = selected;

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(selectionY == CELL_UNIT_Y_MARGIN)
                    for(GameObject object : objectMap.keySet()) {
                        if (objectMap.get(object) == selectionX)
                            selected.set(object);
                    }
                else
                    selected.set(null);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);

                if(e.getY() >= CELL_UNIT_Y_MARGIN && e.getY() <= (UNIT_SPRITE_SIZE_MAX + CELL_UNIT_Y_MARGIN)) {
                    selectionX = (int)Math.floor((e.getX() - CELL_UNIT_X_MARGIN) / (float)(UNIT_SPRITE_SIZE_MAX + CELL_UNIT_X_MARGIN));
                    selectionY = CELL_UNIT_Y_MARGIN;
                } else {
                    selectionX = -1;
                    selectionY = -1;
                }

                repaint();
                revalidate();
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);

        setOpaque(false);
    }
    
    public void update(Cell cell, Player player) {
        this.cell = cell;
        this.player = player;

        int unitCounter = 0;
        int buildingCounter = 0;
        objectMap = new HashMap<>();
        for(GameObject object : cell.getContent().stream().filter(obj -> obj.getPlayer().equals(player)).toList()) {
            if(object instanceof Unit) {
                objectMap.put(object, unitCounter);
                unitCounter++;
            } else if(object instanceof Building) {
                objectMap.put(object, buildingCounter);
                buildingCounter++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        poolSize = Math.min(Math.round(getWidth() / 4f), Math.round(getHeight() / 4f));
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D)g.create());

        gr.setColor(Color.white);
        gr.fill(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 30, 30));
        gr.setColor(Color.black);

        gr.setStroke(new BasicStroke(2));
        gr.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 30, 30));

        drawField(gr);
        drawForest(gr);
        drawRiver(gr);
        drawObjects(gr);
        gr.dispose();
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
     *
     * TODO fixing rounded corners for arcs!
     *
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

    public void drawObjects(Graphics2D gr) {
        /*
         * Draw all (visible/relevant) game objects
         * taking into account their state
         */

        for(GameObject object : objectMap.keySet()) {
            gr.setColor(object == selected.get() ? object.getPlayer().getAlternativeColor() : object.getPlayer().getColor());
            BufferedImage sprite = object.getSprite(true);
            if(object instanceof Unit u) {
                if(sprite != null)
                    gr.drawImage(object == selected.get() ? CustomMethods.selectedSprite(sprite, gr.getColor()) : sprite, (5 + UNIT_SPRITE_SIZE_MAX) * objectMap.get(object) + 5, 10, null);
                else {
                    gr.drawString(object.getToken(), 5 + 10 * objectMap.get(object), 15);
                    if(u.getStatus() == Status.WORKING) {
                        int pieces = u.getCycleLength() / 4;
                        int remainder = u.getCurrentStep() % pieces;
                        int part = u.getCurrentStep() / pieces;
                        int segmentWidth = (int)((float)getWidth() / pieces);
                        int segmentHeight = (int)((float)getHeight() / pieces);

                        if(part > 0) {
                            gr.drawLine(0, 15 + 2, getWidth(), 15 + 2);
                            if(part > 1) {
                                gr.drawLine(getWidth(), 15 + 2, getWidth(), getHeight() + 15 + 2);
                                if(part > 2) {
                                    gr.drawLine(0, getHeight() + 15 + 2, getWidth(), getHeight() + 15 + 2);
                                    if(part > 3)
                                        gr.drawLine(0, 15 + 2, 0, getHeight() + 15 + 2);
                                    else
                                        gr.drawLine(0, getHeight() + 15 + 2, 0, 15 + 2 - remainder * segmentHeight);
                                } else
                                    gr.drawLine(getWidth(), getHeight() + 15 + 2, getWidth() - remainder * segmentWidth, getHeight() + 15 + 2);
                            } else
                                gr.drawLine(getWidth(), 15 + 2, getWidth(), 15 + 2 + remainder * segmentHeight);
                        } else
                            gr.drawLine(0, 15 + 2,  + remainder * segmentWidth, 15 + 2);

                        //gr.drawLine(5 + x * getWidth( + 10 * unitCounter.get(), 15 + 2, x * getWidth( + 10 * unitCounter.get() + gr.getFontMetrics().stringWidth(object.getToken()), 15 + 2);
                        u.step();
                    }
                }
            }

            if(object instanceof Building) {
                if(sprite != null)
                    gr.drawImage(object == selected.get() ? CustomMethods.selectedSprite(sprite, gr.getColor()) : sprite, (5 + BUILDING_SPRITE_SIZE_MAX) * objectMap.get(object) + 5, getHeight() - BUILDING_SPRITE_SIZE_MAX - 10, null);
                else {
                    gr.drawString(object.getToken(), 5 + 10 * objectMap.get(object), getHeight() - gr.getFontMetrics().getHeight() + 5 + 2);
//                                if(b instanceof Worker)
//                                    if (b.checkStatus(CONTRACT))
//                                        gr.drawLine(5 + x * getWidth( + 10 * objectMap.get(object), getHeight() - gr.getFontMetrics().getHeight() + 5 + 2, x * getWidth( + 10 * objectMap.get(object) + gr.getFontMetrics().stringWidth(b.getToken()), getHeight() - gr.getFontMetrics().getHeight() + 5 + 2);
                }
            }
        }

        if(selectionX !=-1 && selectionY != -1) {
            gr.setColor(player.getAlternativeColor());
            gr.drawRect(selectionX * (UNIT_SPRITE_SIZE_MAX + 5) + 5, selectionY, UNIT_SPRITE_SIZE_MAX, UNIT_SPRITE_SIZE_MAX);
        }
    }
}