package UI;

import core.*;

import core.player.AI;
import core.player.Player;
import objects.*;
import objects.buildings.BasicBuilding;
import objects.buildings.Building;
import objects.buildings.TownHall;
import objects.loadouts.*;
import objects.loadouts.Booster;
import objects.loadouts.Gatherer;
import objects.loadouts.Spacer;
import objects.templates.*;
import objects.units.Hero;
import objects.units.Unit;
import objects.units.Villager;
import objects.units.Warrior;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static core.GameConstants.*;

public class Main{

    @NotNull private final Property<Integer> cycle, playerCounter;
    @NotNull private final Property<Player> currentPlayer;
    @NotNull private final Property<GameState> gameState;

    /**
     * List of players.
     */
    private final ArrayList<Player> players, allPlayers;

    private final ArrayList<AI> ais;
    private final Thread gameLoop;
    private final GameFrame game;

    /**
     * Map of all game cells.
     */
    private final Grid cells;

    public static void main(String[] args) {

        LoadoutFactory.registerLoadout("fighter", Fighter.class);
        LoadoutFactory.registerLoadout("gatherer", Gatherer.class);
        LoadoutFactory.registerLoadout("heal", Medic.class);
        LoadoutFactory.registerLoadout("source", Source.class);
        LoadoutFactory.registerLoadout("space", Spacer.class);
        LoadoutFactory.registerLoadout("booster", Booster.class);

        TemplateFactory.loadTemplates(ConstructionTemplate.class);
        TemplateFactory.loadTemplates(UnitTemplate.class);

        SwingUtilities.invokeLater(Main::new);
    }

    public Main() {
        allPlayers = new ArrayList<>();
        players = new ArrayList<>();
        currentPlayer = new Property<>();
        ais = new ArrayList<>();
        cycle = new Property<>(START_CYCLE);
        playerCounter = new Property<>(0);
        cells = new Grid(NUMBER_OF_CELLS);
        gameState = new Property<>(GameState.PLAYING);

        Settings settings = new Settings();

        showPlayerInputDialog();
        currentPlayer.set(players.getFirst());

        game = new GameFrame(this, cells, cycle, playerCounter, currentPlayer, gameState, settings);
        game.initialize();

        playerCounter.bind(_ -> {
            // All objects should 'work' at the end of every cycle.
            // E.g. Operational objects should work on contracts, etc. These will use up all remaining energy. The less they worked during the cycle, the more energy remains for contracts.
            for (var object : currentPlayer.getUnsafe().getObjects())
                object.cycle(cycle.getUnsafe());
        });

        // Add player change logic
        gameState.bind(state -> {
           if(state == GameState.IDLE)
               nextPlayer();
        });

        // The game loop: checks for new/removable objects, fires repaint events, etc.
        gameLoop = new Thread(() -> {
            while (gameState.getUnsafe() != GameState.CLOSE) {
                boolean reload = false;
                for(Player p : allPlayers) {
                    for(GameObject<?> obj : p.getNewObjects()) {
                        reload = addObject(obj) || reload;
                        obj.setCell(obj.getCell());
                    }

                    for (GameObject<?> obj : p.getObjects()) {
                        if ((obj.getHealth() <= 0) && !(obj instanceof Revivable))
                            p.removeObject(obj);
                        if(obj instanceof Animated<?> a)
                            a.step();

                        // Checks whether the object has changed
                        reload = reload || obj.hasChanged();
                    }

                    for(GameObject<?> obj : p.getRemovableObjects())
                        reload  = removeObject(obj) || reload;
                }

                if(gameState.getUnsafe() == GameState.ANIMATING)
                    game.cycleAnimation();
                else {
                    for (String text : currentPlayer.getUnsafe().getMessages()) // Shows awards and others
                        game.showMessagePanel(text);
                }

                game.updateContent(reload);

                try { Thread.sleep(1000 / FPS); } catch (InterruptedException e) { break; }
            }
        });
        gameLoop.start();

        // In case of shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            gameState.set(GameState.CLOSE);
            gameLoop.interrupt();
        }));
    }

    /**
     * Switches to the next player.
     */
    private void nextPlayer() {
        for (GameObject<?> object : currentPlayer.getUnsafe().getObjects()) {
            if(object instanceof Energetic<?> op)
                op.initLogger();
        }
        currentPlayer.ifPresent(Player::cycle);

        if (playerCounter.getUnsafe() == players.size())
            playEndOfCycle();

        currentPlayer.set(players.get(playerCounter.getUnsafe()));
        currentPlayer.getUnsafe().validateMissions();

        SwingUtilities.invokeLater(() -> game.hidePanels(true));
        gameState.set(GameState.PLAYING);
    }

    /**
     * If the last player of the queue has finished his turn, the AIs are given control and a new cycle is started.
     */
    private void playEndOfCycle() {
        for(AI ai : ais) // Let AIs play at end of cycle
            ai.makeMove(cycle.getUnsafe());

        cycle.set(cycle.getUnsafe() + 1);
        cells.cycle(cycle.getUnsafe());
        playerCounter.set(0);
    }

    /**
     * Shows an input dialog for a new player's name, the name of its hero and its preferred colour.
     */
    public void showPlayerInputDialog() {
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
            if(name == null || name.isEmpty())
                name = "Player " + (i + 1);
            String col = (String)JOptionPane.showInputDialog(null, "Choose the player color.", "Choose the player color.", JOptionPane.QUESTION_MESSAGE, null, PLAYER_COLORS, "Blue");
            Color color = switch(col) {
                case "Green" -> Color.green;
                case "Yellow" -> Color.yellow;
                default -> Color.blue;
            };
            int x = 20;
            int y = 20;
//            int x = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 10);
//            int y = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 10);
            addPlayer(new Player(name, color, Color.magenta, cells.get(new Location(x, y, 0))));
        }

        int x = 21;
        int y = 20;
//        int x = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 10);
//        int y = ThreadLocalRandom.current().nextInt(10, NUMBER_OF_CELLS - 10);
        addPlayer(new AI("Thor", Color.red, Color.yellow, cells.get(new Location(x, y, 0)), this));

        allPlayers.addAll(players);
        allPlayers.addAll(ais);
    }

    /**
     * Adds a player to the current game.
     * Initializes resources and objects.
     * @param p new player
     */
    public void addPlayer(Player p) {

//        if(p instanceof AI)
//            ais.add((AI)p);
//        else
        players.add(p);

        GameObject<?> base = new TownHall();
        base.initialize(p, p.getViewPoint().fetch(2, 2, 0), cycle.getUnsafe());
        GameObject<?> building = BasicBuilding.createBuilding("House");
//        GameObject<?> building = BasicBuilding.createBuilding("Lumberjack");
        building.initialize(p, p.getViewPoint().fetch(2, 5, 0), cycle.getUnsafe());
        GameObject<?> v1 = new Villager();
        v1.initialize(p, p.getViewPoint().fetch(2, 1, 0), cycle.getUnsafe());
        GameObject<?> v2 = new Villager();
        v2.initialize(p, p.getViewPoint().fetch(2, 1, 0), cycle.getUnsafe());
        Hero hero = new Hero();
        hero.setName(p.getName());
        hero.initialize(p, p.getViewPoint(), cycle.getUnsafe());

        p.addObject(base);
        p.addObject(building);
        p.addObject(v1);
        p.addObject(v2);
        p.addObject(hero);

        Warrior w = Warrior.createWarrior("Archer");
        w.initialize(p, p.getViewPoint(), cycle.getUnsafe());
        p.addObject(w);
    }

    /**
     * Adds a new GameObject to the game.
     * @param obj new object
     * @return whether the object has been added or not
     */
    public boolean addObject(GameObject<?> obj) {

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
            obj.getLoadout(Spacer.class).ifPresent(spacer -> obj.getCell().changeUnitSpace(spacer.getSpaceBoost()));
            if(obj instanceof Obstruction)
                obj.getCell().changeTravelCost(100);
        }

        return added;
    }

    /**
     * Removes a GameObject from the game.
     * @param obj object to remove
     */
    public boolean removeObject(GameObject<?> obj) {

        boolean removed = false;

        if(obj instanceof Unit) {
            obj.getCell().changeUnitOccupied(-obj.getSize());
            removed = true;
        }
        if(obj instanceof Building) {
            obj.getCell().changeBuildingOccupied(-obj.getSize());
            removed = true;
        }

        if(removed) {
            obj.getLoadout(Spacer.class).ifPresent(spacer -> obj.getCell().changeUnitSpace(-spacer.getSpaceBoost()));
            if(obj instanceof Obstruction)
                obj.getCell().changeTravelCost(-100);
        }

        obj.getCell().removeContent(obj);
        return removed;
    }

    /**
     * Moves an existing object
     * @param obj object to move
     * @param loc new Location of object
     */
    public void moveObject(GameObject<?> obj, Location loc) {
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
            obj.getLoadout(Spacer.class).ifPresent(spacer -> {
                obj.getCell().changeUnitSpace(-spacer.getSpaceBoost());
                target.changeUnitSpace(spacer.getSpaceBoost());
            });

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
     * Calculates the shortest path from the given object's {@code Location} to the target {@code Location}
     * using Dijkstra's algorithm in L1-distance.
     * @param obj travelling object
     * @param target target location
     * @return shortest path (in L1-distance)
     * @throws IllegalArgumentException if target is {@code null} or object is not a Unit
     * @throws IllegalStateException if no valid path could be found
     */
    public Pair<Motion, Location> getShortestAdmissiblePath(GameObject<?> obj, Cell target) throws IllegalArgumentException, IllegalStateException {
        if(target == null)
            throw new IllegalArgumentException("Target should not be null.");

        // TODO Rephrase through getType()? Does lose type safety and pattern matching
        if(!(obj instanceof Unit<?> unit))
            throw new IllegalArgumentException("GameObject should be of type Unit.");

        if(target == obj.getCell())
            return null;
//            return new Pair<>(new Motion(unit, new ArrayList<>(), 0), target.getLocation());

        if(target.distanceTo(unit.getCell()) > unit.getEnergy() || !unit.getPlayer().hasSpotted(target))
            return null;

        // TODO Might need to use heuristics. (Complexity becomes cumbersome for units with high energy, noticeable lag in this case.
        int maxDist = unit.getEnergy();
        maxDist = NUMBER_OF_CELLS_IN_VIEW; // Temporary heuristic

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

                    grid[maxDist + (loc.x() - target.getX())][maxDist + (loc.y() - target.getY())] =
                            min + cells.get(loc).getTravelCost() + (currentPlayer.getUnsafe().hasSpotted(cells.get(loc))
                                    ? 0 : maxDist);
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

                if(temp == null)
                    throw new IllegalStateException("No valid path could be found.");

                current = current.add(temp.x(), temp.y(), 0);
                path.add(temp);
            }

            return new Pair<>(new Motion(unit, path, cost), target.getLocation());
        } else
            return null;
    }

    /**
     * Calculates the required energy to travel from the current {@code Location} to the target {@code Location}
     * based on Dijkstra's algorithm in L1-distance.
     * @param obj travelling object
     * @param target target location
     * @return required energy cost
     */
    @Deprecated
    private int calculateTravelCost(GameObject<?> obj, Cell target) {
        if(obj instanceof Unit<?> unit) {
            int locX = unit.getMaxEnergy();
            int locY = unit.getMaxEnergy();
//            int locLevel = object.getMaxEnergy();

            ArrayList<Cell> toDo = new ArrayList<>();
            ArrayList<Cell> done = new ArrayList<>();
            done.add(target);

            if (target.distanceTo(unit.getCell()) > unit.getMaxEnergy())
                return NUMBER_OF_CELLS;

            if (unit.getCell().getZ() == target.getZ()) {
                int[][] grid = new int[2 * unit.getMaxEnergy() + 1][2 * unit.getMaxEnergy() + 1];
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
                                    if (target.distanceTo(next) <= unit.getMaxEnergy() && !done.contains(next) && !tempList.contains(next))
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

//            printTranspose(grid);

                return grid[locX + deltaX][locY + deltaY] - unit.getCell().getTravelCost();
            }
        }
        return NUMBER_OF_CELLS;
    }

    /**
     * Identifier indicating the current state of the game engine.
     */
    public enum GameState {
        PLAYING, ANIMATING, IDLE, CLOSE
    }
}
