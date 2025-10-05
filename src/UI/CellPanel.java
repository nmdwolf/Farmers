package UI;

import core.*;
import core.contracts.AttackContract;
import core.contracts.ConstructContract;
import core.player.Player;
import objects.Animated;
import objects.GameObject;
import core.Status;
import objects.Obstruction;
import objects.buildings.*;
import objects.loadouts.Fighter;
import objects.units.Unit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

import static core.GameConstants.*;

public class CellPanel extends JPanel {

    private int poolSize, unitRow, buildingRow, enemyRow, enemyCounter, cycle;
    private BufferedImage drawing, mask;
    private Pair<BufferedImage, String> currentAnimationFrame;
    private ArrayDeque<Animation> animations;
    private Rectangle selectionBox;

    private final UnsafeProperty<GameObject<?>> selected;
    private final UnsafeProperty<Pair<GameObject<?>, Boolean>> target;
    private final Property<Main.GameState> gameState;
    private final Settings settings;
    private Player player;
    private HashMap<GameObject<?>, Rectangle> objectMap;
    private Cell cell;
    private boolean reload;

    public CellPanel(@NotNull Cell initialCell, @NotNull Player initialPlayer, @NotNull UnsafeProperty<GameObject<?>> selected, @NotNull UnsafeProperty<Pair<GameObject<?>, Boolean>> target, @NotNull Property<Main.GameState> gameState, @NotNull Settings settings) {
        cell = initialCell;
        player = initialPlayer;
        objectMap = new HashMap<>();
        animations = new ArrayDeque<>();
        buildingRow = 1;
        enemyCounter = 0;
        cycle = 0;
        reload = false;
        this.selected = selected;
        this.target = target;
        this.settings = settings;
        this.gameState = gameState;

        // Ranged mode
        selected.bind(opt -> {
            opt.ifPresentOrElse(
                    obj -> obj.getLoadout(Fighter.class).ifPresent(loadout -> settings.setRangedMode(loadout.getRange() > 0)),
                    () -> settings.setRangedMode(false)
            );
            updateContent(true);
        });

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(gameState.get() == Main.GameState.PLAYING) {
                    GameObject<?> obj = null;
                    for(GameObject<?> object : objectMap.keySet())
                        if(objectMap.get(object).contains(e.getPoint())) {
                            obj = object;
                            break; // early stopping
                        }

                    if (target.get().map(Pair::value).orElse(false) && SwingUtilities.isRightMouseButton(e))
                        target.setOptional(new Pair<>(obj, false));
                    else if (SwingUtilities.isLeftMouseButton(e)) {
                        selected.setOptional(obj);
                        target.setOptional(new Pair<>(null, false));
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                getParent().dispatchEvent(SwingUtilities.convertMouseEvent(CellPanel.this, e, getParent()));

                if(gameState.get() == Main.GameState.PLAYING) {
                    selectionBox = null;
                    for(GameObject<?> object : objectMap.keySet()) {
                        if (objectMap.get(object).contains(e.getPoint())) {
                            selectionBox = objectMap.get(object);
                            break; // early stopping
                        }
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                if(gameState.get() == Main.GameState.PLAYING)
                    selectionBox = null;
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                mask = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D gMask = CustomMethods.optimizeGraphics(mask.createGraphics());
                gMask.setColor(Color.WHITE);
                gMask.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
                gMask.dispose();
            }
        });

        setOpaque(false);
    }

    public void updateContent(boolean forceReload) {
        reload = forceReload;
        updateContent(cell, player);
    }
    
    public void updateContent(@NotNull Cell cell, @NotNull Player player) {
        Cell oldCell = this.cell;
        Player oldPlayer = this.player;
        this.cell = cell;
        this.player = player;

        poolSize = Math.min(Math.round(getWidth() / 4f), Math.round(getHeight() / 4f)); // TODO Might move to a resize method
        unitRow = 0;
        buildingRow = cellCoordinateTransform(0, getHeight() - CELL_Y_MARGIN - settings.getSpriteSize()).value();
        enemyRow = (buildingRow / 2) + 1;

        if(reload || !player.equals(oldPlayer) || !cell.equals(oldCell)) {
            int unitCounter = 0;
            int buildingCounter = 0;
            enemyCounter = 0;
            objectMap = new HashMap<>();

            // Objects in current cell
            int enemyCount = (int)cell.getObjects().stream().filter(obj -> !player.equals(obj.getPlayer())).count();
            for (GameObject<?> object : cell.getObjects()) {
                if (object.getPlayer().equals(player)) {
                    if (object instanceof Unit)
                        objectMap.put(object, constructBoundingBox(object, new Pair<>(unitCounter++, unitRow), 0, 0));
                    else if (object instanceof Building || object instanceof Foundation)
                        objectMap.put(object, constructBoundingBox(object, new Pair<>(buildingCounter++, buildingRow), 0, 0));
                } else
                    objectMap.put(object, constructBoundingBox(object, new Pair<>(enemyCounter++, enemyRow), (int)((getWidth() - enemyCount * (CELL_X_MARGIN + settings.getSpriteSize())) / 2f), 0));
            }

            if(enemyCount != enemyCounter)
                throw new IllegalStateException("A mismatch in the number of enemies has been detected: " + enemyCount + " vs. " + enemyCounter);

            // Objects in neighbouring cells when ranged mode is active (i.e. selected object has range > 0)
            if(settings.getRangedMode()) {
                for(Direction direction : Direction.values()) {
                    Cell neighbour = cell.getNeighbour(direction);
                    enemyCount = (int)neighbour.getObjects().stream()
                            .filter(obj -> !player.equals(obj.getPlayer())).count();
                    enemyCounter = 0;

                    int row = switch(direction) {
                        case NORTH -> enemyRow - 2;
                        case EAST, WEST -> enemyRow;
                        case SOUTH -> enemyRow + 2;
                    };

                    if(!neighbour.isEndOfMap()) {
                        for (GameObject<?> object : neighbour.getObjects())
                            if (!object.getPlayer().equals(player)) {
                                objectMap.put(object, constructBoundingBox(
                                        object,
                                        new Pair<>((direction == Direction.EAST ? -1 : 1) * (enemyCounter++ + (direction == Direction.EAST ? 1 : 0)), row),
                                        (direction == Direction.NORTH || direction == Direction.SOUTH) ? (int) ((getWidth() - enemyCount * (CELL_X_MARGIN + settings.getSpriteSize())) / 2f) : 0,
                                        0)
                                );
                            }

                        if(enemyCount != enemyCounter)
                            throw new IllegalStateException("A mismatch in the number of enemies has been detected: " + enemyCount + " vs. " + enemyCounter);
                    }
                }
            }

            doubleBuffer();
        }
    }

    /**
     * Gives the {@code Cell} currently in focus.
     * @return current cell
     */
    public Cell getCurrentCell() {
        return cell;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = CustomMethods.optimizeGraphics((Graphics2D) g.create());

        if(gameState.get() == Main.GameState.ANIMATING) {
            gr.drawImage(drawing, null, 0, 0);
            gr.drawImage(currentAnimationFrame.key(), null, (getWidth() - currentAnimationFrame.key().getWidth()) / 2, (getHeight() - currentAnimationFrame.key().getHeight()) / 2);

            String description = currentAnimationFrame.value();
            CustomMethods.drawString(gr, description, (getWidth() - CustomMethods.maxLineWidth(gr, description)) / 2, (getHeight() - currentAnimationFrame.key().getHeight()) / 2 + currentAnimationFrame.key().getHeight() + settings.getSpriteSize());
        } else {
            BufferedImage finalImg = new BufferedImage(drawing.getWidth(), drawing.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = CustomMethods.optimizeGraphics(finalImg.createGraphics());
            g2.drawImage(drawing, null, 0, 0);

            // Paint dynamic details
            drawRiver(g2);
            drawDetails(g2);
            drawSelection(g2);

            // Clipping/masking
            g2.setComposite(AlphaComposite.DstIn);
            g2.drawImage(mask, 0, 0, null);

            g2.dispose();

            gr.drawImage(finalImg, 0, 0, null);

            // TODO improve
            Shape shape = new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
            gr.setColor(Color.black);
            gr.setStroke(new BasicStroke(5));
            gr.draw(shape);

            // when ranged
            if (target.get().map(Pair::value).orElse(false)) {
                Area cover = new Area(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 30, 30));
                cover.subtract(new Area(new RoundRectangle2D.Double(CELL_X_MARGIN / 2f, CELL_Y_MARGIN + enemyRow * (CELL_Y_MARGIN + settings.getSpriteSize()) - settings.getSpriteSize() / 2f, getWidth() - CELL_X_MARGIN, 2 * settings.getSpriteSize(), 30, 30)));

                gr.setColor(new Color(0, 0, 0, 128));
                gr.fill(cover);
            }
        }

        gr.dispose();
    }

    public void doubleBuffer() {
        drawing = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = CustomMethods.optimizeGraphics(drawing.createGraphics());
        Shape shape = new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);

        // Paint background panel
        gr.setColor(Color.white);
        gr.fill(shape);
        gr.setColor(Color.black);
        gr.setStroke(new BasicStroke(STROKE_WIDTH));

        // Paint static components
        drawField(gr);
        drawForest(gr);
        drawRoad(gr);
//        drawRiver(gr);
        drawObjects(gr);

        gr.dispose();
    }

    //TODO Should "animations" be a deque or can it be a list?
    public void cycleAnimation() {
        if(gameState.get() == Main.GameState.ANIMATING) {
            if(!animations.isEmpty()) {
                if(animations.peek().isEmpty())
                    animations.pop();
                if(!animations.isEmpty())
                    currentAnimationFrame = animations.peek().pop();
            }
            else {
                setVisible(false);
                gameState.set(Main.GameState.IDLE);
            }
        }
    }

    /**
     * Generates an animation set based on the {@code GameObject}s presents in this {@code Cell}.
     */
    public void generateCycleAnimation() {
        if(isVisible()) {
            if(drawing == null)
                doubleBuffer();

            java.util.List<GameObject<?>> drawables = cell.getObjects().stream()
                    .filter(obj -> obj.getPlayer().equals(player))
                    .filter(Animated.class::isInstance)
                    .filter(obj -> ((Animated<?>) obj).getLogger().size() > 0)
                    .collect(Collectors.toCollection(ArrayList::new));

            animations = new ArrayDeque<>(drawables.size());
            currentAnimationFrame = new Pair<>(drawing, ""); // Start with current cell (without selection boxes)

            for(var obj : drawables)
                obj.getSprite().ifPresent(_ -> animations.addLast(new Animation(((Animated<?>) obj), SECONDS_PER_ANIMATION * FPS)));

            gameState.set(Main.GameState.ANIMATING);

        } else
            gameState.set(Main.GameState.IDLE);
    }

    private void drawField(Graphics2D gr) {
        if (cell.isField()) {
            gr.setColor(new Color(200, 120, 0, 100));
            gr.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
        }
    }

    private void drawRiver(Graphics2D gr) {
        if (cell.isRiver()) {
//            gr.setColor(new Color(0, 100, 255));
            Rectangle anchor = new Rectangle(0, 0, settings.getWaterTexture().getWidth(), settings.getWaterTexture().getHeight());
            TexturePaint tp = new TexturePaint(settings.getWaterTexture(), anchor);
            gr.setPaint(tp);

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

    private void drawRoad(Graphics2D gr) {
        if (cell.isRoad()) {
            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = CustomMethods.optimizeGraphics(img.createGraphics());

            Path2D curve = new Path2D.Double();
            curve.moveTo(getWidth() / 2., getHeight() * 1.5);
            curve.curveTo(getWidth() * 0.25, getHeight() * 0.2,
                    getWidth() * 0.75, getHeight() * 0.8,
                    getWidth(), 0);

            float roadWidth = Sprite.getSpriteSize();
            Shape roadShape = new BasicStroke(roadWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND).createStrokedShape(curve);
            Rectangle anchor = new Rectangle(0, 0, COBBLESTONE.getWidth(), COBBLESTONE.getHeight());
            TexturePaint tp = new TexturePaint(COBBLESTONE, anchor);

            g2.setPaint(tp);
            g2.fill(roadShape);
            g2.dispose();

            gr.drawImage(img, 0, 0, null);
        }
    }

    /**
     * Draws forests if the current cell is a forest.
     * TODO Fix rounded corners for arcs!
     * @param gr Graphics object from panel
     */
    private void drawForest(Graphics2D gr) {
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
    private void drawObjects(Graphics2D gr) {
        /*
         * Draw all (visible/relevant) game objects taking into account their state
         */
        for(GameObject<?> object : objectMap.keySet()) {
            Rectangle boundingBox = objectMap.get(object);
            int x = boundingBox.x;
            int y = boundingBox.y;

            object.getSprite().ifPresentOrElse(
                    sprite -> gr.drawImage(sprite, x, y, null),
                    () -> gr.drawString(object.getToken(), x, y + gr.getFontMetrics().getHeight()));

            // Draws a coloured box around objects of other players to indicate the corresponding Player
            if(!object.getPlayer().equals(player) && !object.equals(selected.get().orElse(null)))
                drawBox(gr, object.getPlayer().getColor(), boundingBox);
        }

        // Walls
        var walls = objectMap.keySet().stream()
                .filter(obj -> player.equals(obj.getPlayer()))
                .filter(Obstruction.class::isInstance)
                .filter(Directional.class::isInstance)
                .map(obj -> ((Directional)obj).getDirection())
                .distinct()
                .toList();
        if(!walls.isEmpty()) {
            gr.setColor(new Color(80, 40, 10));
            Stroke oldStroke = gr.getStroke();
            gr.setStroke(new BasicStroke(20));

            if(walls.size() == 4)
                gr.drawOval(getWidth() / 6, getHeight() / 6, 2 * getWidth() / 3, 2 * getHeight() / 3);
            else {
                for(Direction d : walls) {
                    switch(d) {
                        case NORTH -> gr.drawLine(1, 1, getWidth() - 3, 1);
                        case SOUTH -> gr.drawLine(1, getHeight() - 3, getWidth() - 3, getHeight() - 3);
                        case WEST -> gr.drawLine(1, 1, 1, getHeight() - 3);
                        case EAST -> gr.drawLine(getWidth() - 3, 1, getWidth() - 3, getHeight() - 3);
                    }
                }
            }

            gr.setStroke(oldStroke);
        }
    }

    // TODO Change size of working box based on sprite
    // TODO calibrate arrows
    private void drawDetails(Graphics2D gr) {
        cycle = (++cycle % FPS);
        gr.setStroke(new BasicStroke(STROKE_WIDTH));
        for(GameObject<?> object : objectMap.keySet()) {
            selected.get().ifPresentOrElse(
                    obj -> gr.setColor(object.equals(obj) ? object.getPlayer().getAlternativeColor() : object.getPlayer().getColor()),
                    () -> gr.setColor(object.getPlayer().getColor()));

            // Draws animation around working Units
            if(object.getPlayer().equals(player) && object instanceof Unit<?> u && u.getStatus() == Status.WORKING) {
                Rectangle sourceBox = objectMap.get(u);
                if(settings.showArrows()) {
                    u.getContracts().stream().filter(c -> c instanceof ConstructContract<?>).map(c -> (ConstructContract<?>)c).forEach(c -> {
                        Rectangle targetBox = objectMap.get(c.getFoundation());
                        if(targetBox != null)
                            drawArrow(gr, sourceBox.x, sourceBox.y, targetBox.x, targetBox.y);
                    });

                    u.getContracts().stream().filter(c -> c instanceof AttackContract).map(c -> (AttackContract<?>)c).forEach(c -> {
                        Rectangle targetBox = objectMap.get(c.getTarget());
                        if(targetBox != null)
                            drawWavyArrow(gr, sourceBox.x, sourceBox.y, targetBox.x, targetBox.y);
                    });
                }

                double pieces = FPS / 4f; // 4 seconds per cycle
                int part = (int)(cycle / pieces);
                double remainder = (cycle / pieces) - part;

                // Drawing of working box
                if(part > 0) {
                    gr.drawLine(sourceBox.x, CELL_Y_MARGIN, sourceBox.x + settings.getSpriteSize(), CELL_Y_MARGIN);
                    if(part > 1) {
                        gr.drawLine(sourceBox.x + settings.getSpriteSize(), CELL_Y_MARGIN, sourceBox.x + settings.getSpriteSize(), CELL_Y_MARGIN + settings.getSpriteSize());
                        if(part > 2) {
                            gr.drawLine(sourceBox.x, CELL_Y_MARGIN + settings.getSpriteSize(), sourceBox.x + settings.getSpriteSize(), CELL_Y_MARGIN + settings.getSpriteSize());
                            if(part > 3)
                                gr.drawLine(sourceBox.x, CELL_Y_MARGIN, sourceBox.x, CELL_Y_MARGIN + settings.getSpriteSize());
                            else
                                gr.drawLine(sourceBox.x, CELL_Y_MARGIN + (int)((1 - remainder) * settings.getSpriteSize()), sourceBox.x, CELL_Y_MARGIN + settings.getSpriteSize());
                        } else
                            gr.drawLine(sourceBox.x + (int)((1 - remainder) * settings.getSpriteSize()), CELL_Y_MARGIN + settings.getSpriteSize(), sourceBox.x + settings.getSpriteSize(), CELL_Y_MARGIN + settings.getSpriteSize());
                    } else
                        gr.drawLine(sourceBox.x + settings.getSpriteSize(), CELL_Y_MARGIN, sourceBox.x + settings.getSpriteSize(), CELL_Y_MARGIN + (int)(settings.getSpriteSize() * remainder));
                } else
                    gr.drawLine(sourceBox.x, CELL_Y_MARGIN, sourceBox.x + (int)(settings.getSpriteSize() * remainder), CELL_Y_MARGIN);
            }
        }
    }

    private void drawSelection(Graphics2D gr) {
        // Draw box around currently selected object
        selected.ifPresent(obj -> {
            if(objectMap.get(obj) != null) {
                gr.setColor(obj.getPlayer().getAlternativeColor());
                gr.setStroke(new BasicStroke(STROKE_WIDTH));
                drawBox(gr, obj.getPlayer().getColor(), objectMap.get(obj));
//                drawBox(gr, obj.getPlayer().getAlternativeColor(), constructBoundingBox(objectMap, objectMap.get(obj), player.equals(obj.getPlayer()) ? 0 : (int)((getWidth() - enemyCounter * (CELL_X_MARGIN + settings.getSpriteSize())) / 2f), 0));
            }
        });

        // Draws the selection box for Unit/Building/Foundation objects
        if(selectionBox != null)
            drawBox(gr, player.getAlternativeColor(), selectionBox);
    }

    private void drawBox(Graphics2D gr, Color c, Rectangle box) {
        Color oldColor = gr.getColor();
        gr.setColor(c);
        gr.draw(box);
        gr.setColor(oldColor);
    }

    private Rectangle constructBoundingBox(GameObject<?> obj, Pair<Integer, Integer> selection, int xShift, int yShift) {
        return obj.getSprite()
                .map(sprite -> new Rectangle(
                        selection.key() * (CELL_X_MARGIN + settings.getSpriteSize()) + xShift + ((selection.key() < 0) ? getWidth() : CELL_X_MARGIN),
                        selection.value() * (CELL_Y_MARGIN + settings.getSpriteSize()) + CELL_Y_MARGIN + yShift,
                        sprite.getWidth(), sprite.getHeight()))
                .orElse(new Rectangle(
                        selection.key() * (CELL_X_MARGIN + settings.getSpriteSize()) + CELL_X_MARGIN + xShift,
                        selection.value() * (CELL_Y_MARGIN + settings.getSpriteSize()) + CELL_Y_MARGIN + yShift,
                        settings.getSpriteSize(), settings.getSpriteSize())
                );
    }

    private void drawArrow(Graphics2D gr, int x1, int y1, int x2, int y2) {

        Stroke oldStroke = gr.getStroke();
        gr.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));

        int lineX = x2 - x1;
        int lineY = y2 - y1;
        double angle = -Math.atan2(lineY, lineX) - Math.PI / 2;

        BufferedImage rotated = CustomMethods.rotateImage(ARROWHEAD, angle);
        gr.drawLine(x1 + CELL_X_MARGIN + (settings.getSpriteSize() / 2),
                y1 + (settings.getSpriteSize() / 2), x2 + CELL_X_MARGIN + (settings.getSpriteSize() / 2), y2);
        gr.setStroke(oldStroke);
        gr.drawImage(rotated, x2 + (settings.getSpriteSize() / 2) + CELL_X_MARGIN - rotated.getWidth() / 2,
                y2 - rotated.getHeight() / 2,
                null);
    }

    private void drawWavyArrow(Graphics2D gr, int x1, int y1, int x2, int y2) {

        double dx = x2 - x1;
        double dy = y2 - y1;

        double angle = -Math.atan2(dy, dx) - Math.PI / 2;
        BufferedImage rotated = CustomMethods.rotateImage(ARROWHEAD, angle);

        x1 = x1 + CELL_X_MARGIN + (settings.getSpriteSize() / 2);
        y1 = y1 + CELL_Y_MARGIN + (settings.getSpriteSize() / 2);
        x2 = x2 + (settings.getSpriteSize() / 2);
        y2 = y2 + CELL_Y_MARGIN - rotated.getHeight() / 2;

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

    public static Pair<Integer, Integer> cellCoordinateTransform(int x, int y) {
        int selectionX = (int)Math.floor((x - CELL_X_MARGIN) / (float)(Sprite.getSpriteSize() + CELL_X_MARGIN));
        int selectionY = (int)Math.floor((y - CELL_Y_MARGIN) / (float)(Sprite.getSpriteSize() + CELL_Y_MARGIN));
        return new Pair<>(selectionX, selectionY);
    }

}