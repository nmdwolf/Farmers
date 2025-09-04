package core.player;

import core.Cell;
import core.Location;
import core.Pair;
import core.Status;
import core.contracts.LaborContract;
import UI.Main;
import UI.Motion;
import objects.GameObject;
import objects.buildings.TownHall;
import objects.units.Villager;
import objects.units.Worker;
import core.resources.Resource;

import java.awt.*;
import java.util.*;

import static core.GameConstants.rand;
import static core.resources.Resource.*;

public class AI extends Player {

    public final static int SEARCH_LIMIT = 5;

    private final Main main;

    private TownHall base;
    private final HashMap<Cell, ArrayList<Resource>> harvested;

    public AI(String name, Color color, Color alternativeColor, Cell start, Main game) {
        super(name, color, alternativeColor, start);
        harvested = new HashMap<>();
        main = game;
    }

    public void makeMove(int cycle) {
        // init step
        if(base == null)
            for(GameObject obj : getObjects())
                if (obj instanceof TownHall)
                    base = (TownHall) obj;

        if(getObjects().stream().filter(obj -> obj.getToken().equals("v")).count() < 5 && base != null) {
            Villager v = new Villager();
            v.initialize(this, base.getCell().fetch(1, 0, 0), cycle);
            if(main.addObject(v))
                v.construct();
        }

        for(Iterator<GameObject> it = getObjects().stream().filter(obj -> obj instanceof Worker).iterator(); it.hasNext();) {
            Worker obj = (Worker) it.next();
            Cell newLoc = obj.getCell();
            if(obj.getStatus() == Status.IDLE) {

                boolean needsToMove = true;
                if (harvested.containsKey(newLoc)) {
                    ArrayList<Resource> resources = new ArrayList<>(getResources().keySet());
                    Collections.shuffle(resources, rand);
                    for (Resource res : resources) {
                        if (!harvested.get(newLoc).contains(res) && obj.getYield(res) > 0) {
                            harvested.get(newLoc).add(res);
                            LaborContract contract = new LaborContract(obj, res, newLoc, 1);
                            obj.addContract(contract);
                            obj.setStatus(Status.WORKING);
                            needsToMove = false;
                            break;
                        }
                    }
                }

                if (needsToMove) {
                    int x = rand.nextInt(obj.getEnergy());
                    int y = rand.nextInt(obj.getEnergy() - x);
                    newLoc = obj.getCell().fetch(x, y, 0);
                    Pair<Motion, Location> motion = main.getShortestAdmissiblePath(obj, newLoc);

                    int counter = 0;
                    while ((newLoc.getUnitSpace() - newLoc.getUnitOccupied() < obj.getSpace()
                            || motion == null || harvested.containsKey(newLoc)) && counter++ <= SEARCH_LIMIT) {
                        x = rand.nextInt(obj.getEnergy());
                        y = rand.nextInt(obj.getEnergy() - x);
                        newLoc = obj.getCell().fetch(x, y, 0);
                        motion = main.getShortestAdmissiblePath(obj, newLoc);
                    }

                    if(motion != null) {
                        obj.changeEnergy(-motion.key().length());
//                        motionToThread(motion.key());
                        main.moveObject(motion.key().getObject(), motion.key().getPath()[motion.key().getPath().length - 1]);

                        harvested.put(newLoc, new ArrayList<>());
                        harvested.get(newLoc).add(FOOD);
                        LaborContract contract = new LaborContract(obj, FOOD, newLoc, 1);
                        obj.addContract(contract);
                        obj.setStatus(Status.WORKING);
                    }
                }
            }
        }

//        main.cyclePlayers();
    }
}
