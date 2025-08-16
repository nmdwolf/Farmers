package UI;

import core.*;

import objects.*;
import objects.buildings.Building;
import objects.buildings.Lumberjack;
import objects.buildings.MainBuilding;
import objects.units.Hero;
import objects.units.Unit;
import objects.units.Villager;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static core.GameConstants.*;

public class Main extends JFrame{

    private final Property<Integer> current, cycle;
    private final Property<Player> currentPlayer;

    /**
     * List of players.
     */
    private final ArrayList<Player> players, allPlayers;
    private final ArrayList<AI> ais;

    // Currently selected GameObject
    private final Timer garbageCollector;
    private final GamePanel game;

    /**
     * Map of all game cells.
     */
    private final Grid cells;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main mainframe = new Main();
        });
    }

    public Main() {
        players = new ArrayList<>();
        currentPlayer = new Property<>();
        ais = new ArrayList<>();
        current = new Property<>(0);
        cycle = new Property<>(1);
        cells = new Grid(NUMBER_OF_CELLS);

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
            String col = (String)JOptionPane.showInputDialog(null, "Choose the player color.", "Choose the player color.", JOptionPane.QUESTION_MESSAGE, null, PLAYER_COLORS, "Blue");
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

        currentPlayer.set(players.get(current.get()));
        game = new GamePanel(this, cells, current, cycle, currentPlayer);

        current.bind(() -> {
            // When last player of round has played
            if(current.get() == players.size()) {
                for(AI ai : ais) // Let AIs play at end of cycle
                    ai.makeMove(cycle.get());

                cycle.set(cycle.get() + 1);
                cells.cycle(cycle.get());

                current.setAsParent(0);
            }

            currentPlayer.set(players.get(current.get()));

            for (String text : currentPlayer.get().getMessages())
                game.showMessagePanel(text);

            for (GameObject object : currentPlayer.get().getObjects())
                object.cycle(cycle.get());

            game.hidePanels();
            game.refreshWindow();
        });

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
            game.refreshWindow();
        });

        game.initialize();
        garbageCollector.start();
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

        GameObject base = new MainBuilding(p, p.getViewPoint().fetch(2, 2, 0), cycle.get());
        GameObject lumber = new Lumberjack(p, p.getViewPoint().fetch(2, 5, 0), cycle.get());
        GameObject v1 = new Villager(p, p.getViewPoint().fetch(2, 1, 0), cycle.get());
        GameObject v2 = new Villager(p, p.getViewPoint().fetch(2, 1, 0), cycle.get());
        GameObject hero = new Hero(p, p.getViewPoint(), cycle.get(), p.getName());

        p.addObject(base);
        p.addObject(lumber);
        p.addObject(v1);
        p.addObject(v2);
        p.addObject(hero);
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

                    grid[maxDist + (loc.x() - target.getX())][maxDist + (loc.y() - target.getY())] = min + cells.get(loc).getTravelCost() + (currentPlayer.get().hasSpotted(cells.get(loc)) ? 0 : maxDist);
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
                game.refreshWindow();
            }

            if(motion.isDone()) {
                motion.getObject().setStatus(Status.IDLE);
                ((Timer)evt.getSource()).stop();
            }
        };
        new Timer(motion.getObject().getAnimationDelay(), taskPerformer).start();
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
