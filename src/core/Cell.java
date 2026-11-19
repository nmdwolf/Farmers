package core;

import UI.CustomMethods;
import core.player.Player;
import core.resources.ResourceContainer;
import objects.GameObject;
import objects.Obstruction;
import objects.buildings.Building;
import objects.buildings.Directional;
import objects.buildings.Wall;
import objects.loadouts.Spacer;
import objects.units.Unit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static core.InternalSettings.HOVER_SHIFT;

// TODO Fix North/South (y coordinates go down on computers)
public class Cell {

    private final ResourceContainer resources;
    private final Location loc;
    private final HashSet<GameObject> content;
    private int unitSpace, unitOccupied, buildingSpace, buildingOccupied, travelCost, heatLevel, traversalCount;

    private Cell east, west, north, south, up, down;

    /**
     * Cells are the basic entities of the game map.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     * @param unitSpace initial unit slots
     * @param buildingSpace initial building slots
     */
    public Cell(int x, int y, int z, int unitSpace, int buildingSpace) {
        this.loc = new Location(x, y, z);
        this.content = new HashSet<>();
        this.unitSpace = unitSpace;
        this.buildingSpace = buildingSpace;
        travelCost = InternalSettings.INITIAL_TRAVEL_COST;
        traversalCount = 0;
        resources = generateResources();
        seasonalCycle(0); // Start in Winter
    }

    /**
     * Advances the current cycle of this cell.
     * @param cycle new cycle
     */
    public void cycle(int cycle) {
        if(cycle % InternalSettings.SEASON_LENGTH == 0)
            seasonalCycle((cycle % (4 * InternalSettings.SEASON_LENGTH)) / InternalSettings.SEASON_LENGTH);
        if(heatLevel >= InternalSettings.HOT_LEVEL)
            resources.put("Water", Math.max(resources.get("Water") - 2, 0));
    }

    /**
     * Gives the total amount of unit slots.
     * @return space for units
     */
    public int getUnitSpace() {
        return unitSpace;
    }

    /**
     * Gives the amount of occupied unit slots.
     * @return space occupied by units
     */
    public int getUnitOccupied() {
        return unitOccupied;
    }

    /**
     * Gives the amount of available unit slots.
     * @return space available by units
     */
    public int getUnitAvailable() {
        return unitSpace - unitOccupied;
    }

    /**
     * Changes the amount of unit slots by the specified amount.
     * @param amount additional unit slots
     */
    public void changeUnitSpace(int amount) {
        unitSpace += amount;
    }

    /**
     * Changes the amount of occupied unit slots by the specified amount.
     * @param amount additional occupied unit slots
     */
    public void changeUnitOccupied(int amount) {
        unitOccupied += amount;
    }

    /**
     * Gives the total amount of building slots.
     * @return space for buildings
     */
    public int getBuildingSpace() {
        return buildingSpace;
    }

    /**
     * Gives the amount of occupied building slots.
     * @return space occupied by buildings
     */
    public int getBuildingOccupied() {
        return buildingOccupied;
    }

    /**
     * Gives the amount of available building slots.
     * @return space available by buildings
     */
    public int getBuildingAvailable() {
        return buildingSpace - buildingOccupied;
    }

    /**
     * Changes the amount of building slots by the specified amount.
     * @param amount additional building slots
     */
    public void changeBuildingSpace(int amount) {
        buildingSpace += amount;
    }

    /**
     * Changes the amount of occupied building slots by the specified amount.
     * @param amount additional occupied building slots
     */
    public void changeBuildingOccupied(int amount) {
        buildingOccupied += amount;
    }

    /**
     * Calculates the energy cost for passing through this cell based on the inbound direction.
     * @param direction inbound direction
     * @return energy cost
     */
    public int getTravelCost(Direction direction) {
        // basic cost
        int cost = travelCost;

        // if "river"
        if(isRiver())
            cost += 2;

        // if "hot"
        cost += Math.max(0, heatLevel - InternalSettings.HOT_LEVEL);

        // Obstructions in this cell
        var obstructions = content.stream().filter(Obstruction.class::isInstance).map(Obstruction.class::cast).filter(Obstruction::isActive);
        for(Obstruction ob : obstructions.collect(Collectors.toSet())) {
            if(ob instanceof Directional dir)
                cost += dir.getDirection().equals(direction) ? ob.getObstructionCost() : 0;
            else
                cost += ob.getObstructionCost();
        }

        // Obstructions in source cell
        obstructions = fetch(Direction.toDisplacement(direction)).content.stream().filter(Obstruction.class::isInstance).map(Obstruction.class::cast).filter(Obstruction::isActive);
        for(Obstruction ob : obstructions.collect(Collectors.toSet())) {
            if(ob instanceof Directional dir)
                cost += dir.getDirection().equals(direction.opposite()) ? ob.getObstructionCost() : 0;
            else
                cost += ob.getObstructionCost();
        }

        return cost;
    }

    /**
     * Gives the available amount of the specified resource.
     * @param type resource type
     * @return available resources
     */
    public int getResource(String type) {
        return resources.get(type);
    }

    /**
     * Changes the amount of the specified resource held by this cell. The required amount can be changed in certain cases, e.g.:
     * <ul>
     *     <li>{@code amount} is negative and the currently held reserves are insufficient,</li>
     *     <li>{@code type} is WATER and the cell's temperature exceeds its limits</li>
     * </ul>
     * @param type resource type
     * @param amount amount of resource
     * @return actual amount changed
     */
    public int changeResource(String type, int amount) {

        if(amount < 0)
            amount = -Math.min(resources.get(type), -amount);

        if(type.equals("Water"))
            if(heatLevel >= 50 || heatLevel <= InternalSettings.COLD_LEVEL)
                amount = 0;

        resources.put(type, resources.get(type) + amount);
        return amount;
    }

    /**
     * Changes the amount of the specified resources held by this cell.
     * @param res container with required resources
     */
    public void changeResources(ResourceContainer res) {
        for(String resource : res.keySet())
            changeResource(resource, res.get(resource));
    }

    /**
     * Gives the current temperature of this cell.
     * @return temperature
     */
    public int getHeatLevel() { return heatLevel; }

    /**
     * Changes the temperature of this cell.
     * @param amount temperature change
     */
    public void changeHeatLevel(int amount) { heatLevel += amount; }

    /**
     * Indicates whether this cell contains a field, i.e. whether this cell holds enough water (based on the constant {@link InternalSettings#WATER_THRESHOLD}).
     * @return if this is a river
     */
    public boolean isRiver() { return resources.get("Water") >= InternalSettings.WATER_THRESHOLD; }

    /**
     * Indicates whether this cell contains a field, i.e. whether this cell holds enough wood (based on the constant {@link InternalSettings#WOOD_THRESHOLD}).
     * @return if this is a forest
     */
    public boolean isForest() { return resources.get("Wood") >= InternalSettings.WOOD_THRESHOLD; }

    /**
     * Indicates whether this cell contains a field, i.e. whether this cell holds enough food (based on the constant {@link InternalSettings#FOOD_THRESHOLD}).
     * @return if this is a field
     */
    public boolean isField() { return resources.get("Food") >= InternalSettings.FOOD_THRESHOLD; }

    /**
     * Indicates whether this cell contains a natural road, i.e. whether sufficiently many units have moved through this cell (based on the constant {@link InternalSettings#ROAD_THRESHOLD}).
     * @return if this is a road
     */
    public boolean isRoad() {
        return traversalCount >= InternalSettings.ROAD_THRESHOLD;
    }

    /**
     * Adds an object to this cell.
     * @param obj new object
     */
    public void addContent(GameObject obj) {
        if(!equals(obj.getCell())) {
            content.add(obj);
            if (obj instanceof Unit) {
                traversalCount++;
                changeUnitOccupied(obj.getSize());
            } else if (obj instanceof Building)
                changeBuildingOccupied(obj.getSize());

            obj.getLoadout(Spacer.class).ifPresent(spacer -> {
                this.changeUnitSpace(spacer.getSpaceBoost());
            });
        } else
            throw new IllegalArgumentException("Object is already located in this cell.");
    }

    /**
     * Removes an object from this cell.
     * @param obj object to be removed
     */
    public void removeContent(GameObject obj) {
        if(equals(obj.getCell())) {
            content.remove(obj);
            if (obj instanceof Unit)
                changeUnitOccupied(-obj.getSize());
            else if (obj instanceof Building)
                changeBuildingOccupied(-obj.getSize());

            obj.getLoadout(Spacer.class).ifPresent(spacer -> {
                this.changeUnitSpace(-spacer.getSpaceBoost());
            });
        } else
            throw new IllegalArgumentException("Object is not located in this cell.");
    }

    /**
     * Returns a set of objects contained in this cell.
     * @return set of objects
     */
    public HashSet<GameObject> getObjects() {
        return content; }

    /**
     * Changes parameters of the cell when the season switches
     * @param season 0: Winter, 1: Spring, 2: Summer, 3: Fall
     */
    private void seasonalCycle(int season) {
        heatLevel = InternalSettings.INITIAL_HEAT_LEVEL;
        if(season == 0)
            heatLevel -= InternalSettings.rand.nextInt(4) + 1;
        else if(season == 1) {
            heatLevel += InternalSettings.rand.nextInt(3) - 1;
            if (InternalSettings.rand.nextInt(5) >= 3)
                resources.put("Water", resources.get("Water") + InternalSettings.rand.nextInt(30));
        }
        else if(season == 2)
            heatLevel += InternalSettings.rand.nextInt(5) + 1;
        else if(season == 3) {
            heatLevel += InternalSettings.rand.nextInt(3) - 1;
            if(InternalSettings.rand.nextInt(5) >= 2)
                resources.put("Water", resources.get("Water") + InternalSettings.rand.nextInt(50));
        }
    }

    /**
     * Gives the x-coordinate of this cell.
     * @return x-coordinate
     */
    public int getX() {
        return loc.x();
    }

    /**
     * Gives the y-coordinate of this cell.
     * @return y-coordinate
     */
    public int getY() {
        return loc.y();
    }

    /**
     * Gives the z-coordinate of this cell.
     * @return z-coordinate
     */
    public int getZ() {
        return loc.z();
    }

    /**
     * Links a cell (neighbour) to this one.
     * @param cell new neighbour
     */
    public void link(Cell cell) {
        Location diff = cell.getLocation().add(loc.negative());
        int dx = diff.x();
        int dy = diff.y();
        int dz = diff.z();

        if(Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1)
            throw new IllegalArgumentException("Distance should be exactly 1!");

        if (dx == 1)
            east = cell;
        if (dx == -1)
            west = cell;
        if (dy == 1)
            north = cell;
        if (dy == -1)
            south = cell;
        if (dz == 1)
            up = cell;
        if (dz == -1)
            down = cell;
    }

    /**
     * Recursively fetches a cell at the specified relative coordinates.
     * @param x relative x-coordinate
     * @param y relative y-coordinate
     * @param z relative z-coordinate
     * @return cell
     * @throws NullPointerException if no cell can be found at the specified relative coordinate
     */
    @NotNull
    public Cell fetch(int x, int y, int z) throws NullPointerException {
        if(x == 0 && y == 0 && z == 0)
            return this;

        if(!InternalSettings.GAME_3D && z != 0)
            return this;

        if(Math.abs(x) + Math.abs(y) + Math.abs(z) == 1) {
            if(x == 1)
                return east;
            else if(x == -1)
                return west;
            else if(y == 1)
                return north;
            else if(y == -1)
                return south;
            else if(InternalSettings.GAME_3D && z == 1)
                return up;
            else if (InternalSettings.GAME_3D)
                return down;
        } else {
            if(Math.abs(y) >= Math.abs(x)) {
                if(Math.abs(y) >= Math.abs(z))
                    return (Integer.signum(y) == 1 ? north : south).fetch(x, y - Integer.signum(y), z);
                else if(InternalSettings.GAME_3D)
                    return (Integer.signum(z) == 1 ? up : down).fetch(x, y, z - Integer.signum(z));
            }
            else
                return (Integer.signum(x) == 1 ? east : west).fetch(x - Integer.signum(x), y, z);
        }

        throw new NullPointerException("Cell does not exist.");
    }

    /**
     * Fetches a cell at the specified relative coordinates.
     * @param location relative coordinate
     * @return cell
     */
    @NotNull
    public Cell fetch(@NotNull Location location) {
        return fetch(location.x(), location.y(), location.z());
    }

    /**
     * Calculates the (L1-)distance between this cell and the specified one.
     * @param cell target cell
     * @return (L1-)distance
     */
    public int distanceTo(Cell cell) {
        return Location.distance(cell.getLocation(), loc);
    }

    /**
     * Gives the location of this cell
     * @return location
     */
    public Location getLocation() {
        return loc;
    }

    /**
     * Indicates whether this cell lies on the boundary of the game map.
     * @return if this cell is a boundary cell
     */
    public boolean isEndOfMap() {
        return loc.x() == 0 || loc.x() == InternalSettings.NUMBER_OF_CELLS - 1 || loc.y() == 0 || loc.y() == InternalSettings.NUMBER_OF_CELLS - 1;
    }

    /**
     * Returns all neighbouring cells that are not map boundaries.
     * @return nonboundary neighbours
     */
    public Cell[] getNeighbours() {
        return Stream.of(north, east, south, west).filter(c -> !c.isEndOfMap()).toArray(Cell[]::new);
    }

    /**
     * Returns the neighbour of this cell in the specified direction.
     * @param direction direction
     * @return neighbouring cell
     */
    public Cell getNeighbour(Direction direction) {
        return switch(direction) {
            case NORTH -> south;
            case EAST -> east;
            case SOUTH -> north;
            case WEST -> west;
        };
    }

    @Override
    public String toString() {
        return "Location(" + loc.x() + ", " + loc.y() + ", " + loc.z() + ")";
    }

    /**
     * Generates a description of this cell based on its content and properties.
     * @return description
     */
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        for(String res : resources.keySet())
            if(!res.equals("Time"))
                description.append(res).append(": ").append(resources.get(res)).append("\n");

        description.append("\n\n").append("Heat level: ").append(heatLevel);

        return description.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Cell cell)
            return loc.equals(cell.loc);
        return false;
    }

    /**
     * Generates a random amount of {@code Resource}s to initialize a cell
     * @return HashMap with random amount of resources
     */
    private static ResourceContainer generateResources() {
        ResourceContainer resources = new ResourceContainer();
        resources.put("Food", InternalSettings.rand.nextInt(200));
        resources.put("Wood", InternalSettings.rand.nextInt(250));
        resources.put("Stone", InternalSettings.rand.nextInt(100));
        resources.put("Iron", InternalSettings.rand.nextInt(50));
        resources.put("Coal", InternalSettings.rand.nextInt(50));
        resources.put("Time", 0);

        resources.put("Water", InternalSettings.rand.nextInt(200));
        if(resources.get("Water") > InternalSettings.WATER_THRESHOLD)
            resources.put("Water", 300 + InternalSettings.rand.nextInt(100));

        return resources;
    }

    /**
     * Creates an image of this cell from the perspective of the specified player.
     * @param currentPlayer current player
     * @return image to draw on the game panel
     */
    public BufferedImage draw(InternalSettings internalSettings, Player currentPlayer) {
        float widthFloat = internalSettings.getCellWidth();
        float heightFloat = internalSettings.getCellHeight();
        int width = Math.round(widthFloat);
        int height = Math.round(heightFloat);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = CustomMethods.optimizeGraphics(img.createGraphics());

        // fill all cells outside the map boundary in black
        if(isEndOfMap()) {
            gr.setColor(Color.black);
            gr.fillRect(0, 0, width, height);
        } else {
            // draw field effect, else draw white background
            if (isField())
                gr.setColor(new Color(200, 120, 0, 100));
            else
                gr.setColor(new Color(255, 255, 255));

            gr.fillRect(0,0, width, height);

            // draw forest effect
            if (isForest()) {
                gr.setColor(new Color(20, 150, 20));
                gr.fillArc(width - Math.round(internalSettings.getPoolSize()) ,
                        height - Math.round(internalSettings.getCellHeight() - internalSettings.getPoolSize()),
                        Math.round(2 * internalSettings.getPoolSize()), Math.round(2 * internalSettings.getPoolSize()), 0, 90);
            }

            //draw river effect
            if (isRiver()) {
                gr.setColor(new Color(0, 100, 255));
                if (getX() < InternalSettings.NUMBER_OF_CELLS - 2 && getY() < InternalSettings.NUMBER_OF_CELLS - 2 && fetch(1, 0, 0).isRiver() && fetch(0, 1, 0).isRiver() && fetch(1, 1, 0).isRiver())
                    gr.fillRoundRect(Math.round(0.5f * (widthFloat - internalSettings.getPoolSize())),
                            Math.round(0.5f * (heightFloat - internalSettings.getPoolSize())),
                            Math.round(widthFloat + internalSettings.getPoolSize()),
                            Math.round(heightFloat + internalSettings.getPoolSize()),
                            Math.round(internalSettings.getPoolSize()),
                            Math.round(internalSettings.getPoolSize()));
                else {
                    boolean singleFlag = true;
                    if (getX() < InternalSettings.NUMBER_OF_CELLS - 1 && fetch(1, 0, 0).isRiver())
                        gr.fillRect(Math.round(0.5f * widthFloat),
                                Math.round(0.5f * (heightFloat - internalSettings.getPoolSize())),
                                Math.round(0.5f * widthFloat) + 1,
                                Math.round(internalSettings.getPoolSize()));
                    if (getX() > 0 && fetch(-1, 0, 0).isRiver())
                        gr.fillRect(Math.round(internalSettings.getCellWidth() - 1),
                                Math.round(0.5f * (heightFloat - internalSettings.getPoolSize())),
                                Math.round(0.5f * widthFloat),
                                Math.round(internalSettings.getPoolSize()));
                    if (getY() < InternalSettings.NUMBER_OF_CELLS - 1 && fetch(0, 1, 0).isRiver()) {
                        gr.fillRect(Math.round(0.5f * (widthFloat - internalSettings.getPoolSize())),
                                Math.round(0.5f * heightFloat),
                                Math.round(internalSettings.getPoolSize()),
                                Math.round(0.5f * heightFloat));
                        singleFlag = false;
                    }
                    if (getY() > 0 && fetch(0, -1, 0).isRiver()) {
                        gr.fillRect(Math.round(0.5f * (widthFloat - internalSettings.getPoolSize())),
                                height,
                                Math.round(internalSettings.getPoolSize()),
                                Math.round(0.5f * heightFloat));
                        singleFlag = false;
                    }

                    if (singleFlag)
                        gr.fillOval(Math.round(0.5f * widthFloat - internalSettings.getPoolSize()),
                                Math.round(0.5f * (heightFloat - internalSettings.getPoolSize())),
                                Math.round(2 * internalSettings.getPoolSize()),
                                Math.round(internalSettings.getPoolSize()));
                    else
                        gr.fillOval(Math.round(0.5f * (widthFloat - internalSettings.getPoolSize())),
                                Math.round(0.5f * (heightFloat - internalSettings.getPoolSize())),
                                Math.round(internalSettings.getPoolSize()),
                                Math.round(internalSettings.getPoolSize()));
                }
            }

            // set player color as brush color
            gr.setColor(currentPlayer.getColor());

            // fetch a list of all players that have an object in the current cell
            List<Player> playersInCell = new ArrayList<>(getObjects().stream().map(GameObject::getPlayer).distinct().toList());

            // if the active player has an object in the current cell, indicate the cell
            if(playersInCell.contains(currentPlayer)) {
                gr.fillOval(5, 5, 10, 10);
                playersInCell.remove(currentPlayer);
            }

            // draw walls (if any)
            drawWalls(gr, internalSettings);

            // blur all cells that the active player has only spotted
            if (!currentPlayer.hasVisited(this)) {
                // draw gray, transparent overlay
                gr.setColor(new Color(192, 192, 192, 200));
                gr.fillRect(0, 0, width, height);
            } else {

                // indicate if the temperature of this cell is below 'freezing point'
                if (getHeatLevel() <= InternalSettings.COLD_LEVEL)
                    gr.drawImage(InternalSettings.CLOUD,
                            Math.round(widthFloat - InternalSettings.CLOUD.getWidth() - 5), height + 5, null);
                    // indicate if the temperature of this cell is above the 'boiling point'
                else if (getHeatLevel() >= InternalSettings.HOT_LEVEL)
                    gr.drawImage(InternalSettings.SUN,
                            Math.round(widthFloat - InternalSettings.SUN.getWidth() - 5), height + 5, null);

                // indicate all other players that have a game object in the current cell (which should only
                // happen when the player can see this cell)
                for (int i = 0; i < playersInCell.size(); i++) {
                    gr.setColor(playersInCell.get(i).getColor());
                    gr.drawOval(5 + 15 * (i + 1), 5,10,10);
                }
            }
        }

        return img;
    }

    /**
     * Draws all walls in the provided cell (if any).
     * @param gr graphics object
     */
    private void drawWalls(Graphics2D gr, InternalSettings internalSettings) {
        float widthFloat = internalSettings.getCellWidth();
        float heightFloat = internalSettings.getCellHeight();
        int width = Math.round(widthFloat);
        int height = Math.round(heightFloat);

        // set wall color
        gr.setColor(new Color(80, 40, 10));

        // store old stroke style
        Stroke oldStroke = gr.getStroke();

        //set stroke style of wall
        int strokeWidth = 10;
        gr.setStroke(new BasicStroke(strokeWidth));

        // fetch all walls in current cell
        var walls = getObjects().stream()
                .filter(Wall.class::isInstance)
                .map(obj -> ((Wall)obj).getDirection())
                .distinct()
                .toList();

        // draw all walls
        for(Direction d : walls) {
            switch(d) {
                case NORTH -> gr.drawLine(Math.round(widthFloat + 0.5f * strokeWidth),
                        Math.round(heightFloat + 0.5f * strokeWidth),
                        Math.round(widthFloat - 0.5f * strokeWidth),
                        Math.round(heightFloat + 0.5f * strokeWidth));
                case SOUTH -> gr.drawLine(Math.round(widthFloat + 0.5f * strokeWidth),
                        Math.round(heightFloat - 0.5f * strokeWidth),
                        Math.round(widthFloat - 0.5f * strokeWidth),
                        Math.round(heightFloat - 0.5f * strokeWidth));
                case WEST -> gr.drawLine(Math.round(widthFloat + 0.5f * strokeWidth),
                        Math.round(heightFloat + 0.5f * strokeWidth),
                        Math.round(widthFloat + 0.5f * strokeWidth),
                        Math.round(heightFloat - 0.5f * strokeWidth));
                case EAST -> gr.drawLine(Math.round(widthFloat - 0.5f * strokeWidth),
                        Math.round(heightFloat + 0.5f * strokeWidth),
                        Math.round(widthFloat - 0.5f * strokeWidth),
                        Math.round(heightFloat - 0.5f * strokeWidth));
            }
        }

        // reset stroke style
        gr.setStroke(oldStroke);
    }
}
