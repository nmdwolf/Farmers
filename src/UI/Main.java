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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        LoadoutFactory.registerLoadout(Fighter.class);
        LoadoutFactory.registerLoadout(Gatherer.class);
        LoadoutFactory.registerLoadout(Medic.class);
        LoadoutFactory.registerLoadout(Source.class);
        LoadoutFactory.registerLoadout(Spacer.class);
        LoadoutFactory.registerLoadout(Booster.class);

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
            for (var object : currentPlayer.get().getObjects())
                object.cycle(cycle.get());
        });

        // Add player change logic
        gameState.bind(state -> {
           if(state == GameState.IDLE)
               nextPlayer();
        });

        // The game loop: checks for new/removable objects, fires repaint events, etc.
        gameLoop = new Thread(() -> {
            while (gameState.get() != GameState.CLOSE) {
                boolean reload = false;
                for(Player p : allPlayers) {
                    reload = !p.getNewObjects().isEmpty() || reload;

                    for (GameObject<?> obj : p.getObjects()) {
                        if ((obj.getHealth() <= 0) && !(obj instanceof Revivable))
                            p.removeObject(obj);
                        if(obj instanceof Animated<?> a)
                            a.step();

                        // Checks whether the object has changed
                        reload = reload || obj.hasChanged();
                    }

                    reload  = !p.getRemovableObjects().isEmpty() || reload;
                }

                if(gameState.get() == GameState.ANIMATING)
                    game.cycleAnimation();

                game.updateContent(reload);

                if(gameState.get() != GameState.ANIMATING) {
                    currentPlayer.get().getMissionArchive().validateMissions();
                    for (String text : currentPlayer.get().getAwardArchive().getNewAwards()) {
                        JPanel messageBox = game.showMessageBox(text);
                        messageBox.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                super.mouseReleased(e);
                                game.getContentPane().remove(messageBox);
                            }
                        });

                    }
//                    for (String text : currentPlayer.get().getMessages()) // Shows awards and others
//                        game.showMessageBox(text);
                }

                try { Thread.sleep(1000 / FPS); } catch (InterruptedException e) { break; }
            }
        });
        gameLoop.start();

        // In case of shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            gameState.set(GameState.CLOSE);
            gameLoop.interrupt();
        }));

        JOptionPane.showMessageDialog(game, "To get you started, you can first try to finish some 'missions'. These will help you get a hang of the game. To see the next mission, press CTRL + M.");
    }

    /**
     * Switches to the next player.
     */
    private void nextPlayer() {
        for (GameObject<?> object : currentPlayer.get().getObjects()) {
            if(object instanceof Energetic<?> op)
                op.initLogger();
        }
        currentPlayer.get().cycle();

        if (playerCounter.get() == players.size())
            playEndOfCycle();

        currentPlayer.set(players.get(playerCounter.get()));

        SwingUtilities.invokeLater(() -> game.hidePanels(true));
        gameState.set(GameState.PLAYING);

        if(cycle.get() == 1) {
            game.updateContent(true);
            JOptionPane.showMessageDialog(game, "To get you started, you can first try to finish some 'missions'. These will help you get a hang of the game. To see the next mission, press CTRL + M.");
        }
    }

    /**
     * If the last player of the queue has finished his turn, the AIs are given control and a new cycle is started.
     */
    private void playEndOfCycle() {
        for(AI ai : ais) // Let AIs play at end of cycle
            ai.makeMove(cycle.get());

        cycle.set(cycle.get() + 1);
        cells.cycle(cycle.get());
        playerCounter.set(0);
    }

    /**
     * Shows an input dialog for a new player's name, the name of its hero and its preferred colour.
     */
    public void showPlayerInputDialog() {
        int playerCount = -1;
        do {
            String input = "";
            try {
                input = JOptionPane.showInputDialog("How many (human) players?");
                if(input == null) {
                    JOptionPane.showMessageDialog(null, "Startup was cancelled by a player.");
                    System.exit(0);
                }
                playerCount = Integer.parseInt(input);
            } catch(NumberFormatException e) {
                JOptionPane.showMessageDialog(null,"A nonnumerical value was entered: " + input + ". Please try again.");
            }
        } while(playerCount < 1 || playerCount > 5);

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
        addPlayer(new AI("Thor", Color.red, Color.yellow, cells.get(new Location(x, y, 0)), this, cells));

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
//        GameObject<?> building = BasicBuilding.createBuilding("House");
        GameObject<?> building = BasicBuilding.createBuilding("Lumberjack");
        GameObject<?> v1 = new Villager();
        GameObject<?> v2 = new Villager();
        Hero hero = new Hero();
        hero.setName(p.getName());

        p.addObject(base, p.getViewPoint().fetch(2, 2, 0));
        p.addObject(building, p.getViewPoint().fetch(2, 5, 0));
        p.addObject(v1, p.getViewPoint().fetch(2, 1, 0));
        p.addObject(v2, p.getViewPoint().fetch(2, 1, 0));
        p.addObject(hero, p.getViewPoint());

        Warrior w = Warrior.createWarrior("Archer");
        p.addObject(w, p.getViewPoint());
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
     * Identifier indicating the current state of the game engine.
     */
    public enum GameState {
        PLAYING, ANIMATING, IDLE, CLOSE
    }
}
