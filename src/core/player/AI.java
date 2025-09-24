package core.player;

import core.*;
import core.contracts.LaborContract;
import UI.Main;
import UI.Motion;
import objects.GameObject;
import objects.buildings.TownHall;
import objects.units.Villager;
import objects.units.Worker;

import java.awt.*;
import java.util.*;

import static core.GameConstants.rand;

public class AI extends Player {

    public final static int SEARCH_LIMIT = 5;

    private final Main main;
    private final Grid grid;

    private TownHall base;
    private final HashMap<Cell, ArrayList<String>> harvested;

    public AI(String name, Color color, Color alternativeColor, Cell start, Main game, Grid grid) {
        super(name, color, alternativeColor, start);
        harvested = new HashMap<>();
        main = game;
        this.grid = grid;
    }

    public void makeMove(int cycle) {
        // init step
        if(base == null)
            for(GameObject<?> obj : getObjects())
                if (obj instanceof TownHall)
                    base = (TownHall) obj;

        if(getObjects().stream().filter(obj -> obj.getToken().equals("v")).count() < 5 && base != null) {
            Villager v = new Villager();
            addObject(v, base.getCell().fetch(1, 0, 0));
        }

        for(Iterator<GameObject<?>> it = getObjects().stream().filter(obj -> obj instanceof Worker).iterator(); it.hasNext();) {
            Worker obj = (Worker) it.next();
            Cell newLoc = obj.getCell();
            if(obj.getStatus() == Status.IDLE) {

                boolean needsToMove = true;
                if (harvested.containsKey(newLoc)) {
                    ArrayList<String> resources = new ArrayList<>(getResources().keySet());
                    Collections.shuffle(resources, rand);
                    for (String res : resources) {
                        if (!harvested.get(newLoc).contains(res) && obj.getYield(res) > 0) {
                            harvested.get(newLoc).add(res);
                            LaborContract contract = new LaborContract(obj, res, newLoc);
                            obj.addContract(contract);
                            obj.setStatus(Status.WORKING);
                            needsToMove = false;
                            break;
                        }
                    }
                }

                if (needsToMove) {
                    grid.populateDistanceMatrix(newLoc, this, obj.getEnergy());
                    int x = rand.nextInt(obj.getEnergy());
                    int y = rand.nextInt(obj.getEnergy() - x);
                    newLoc = obj.getCell().fetch(x, y, 0);
                    var path = grid.getShortestAdmissiblePath(newLoc);

                    int counter = 0;
                    while ((newLoc.getUnitSpace() - newLoc.getUnitOccupied() < obj.getSize()
                            || path == null || harvested.containsKey(newLoc)) && counter++ <= SEARCH_LIMIT) {
                        x = rand.nextInt(obj.getEnergy());
                        y = rand.nextInt(obj.getEnergy() - x);
                        newLoc = obj.getCell().fetch(x, y, 0);
                        path = grid.getShortestAdmissiblePath(newLoc);
                    }

                    if(path != null) {
                        Motion motion = new Motion(obj, path, grid.getPathDistance(newLoc));
                        obj.changeEnergy(-motion.length());
                        main.moveObject(motion.getObject(), motion.getPath()[motion.getPath().length - 1]);

                        harvested.put(newLoc, new ArrayList<>());
                        harvested.get(newLoc).add("Food");
                        LaborContract contract = new LaborContract(obj, "Food", newLoc);
                        obj.addContract(contract);
                        obj.setStatus(Status.WORKING);
                    }
                }
            }
        }
    }
}
