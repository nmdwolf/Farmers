package core;

import contracts.LaborContract;
import general.Main;
import general.Motion;
import items.GameObject;
import buildings.MainBuilding;
import units.Villager;
import units.Worker;
import resources.Resource;

import java.awt.*;
import java.util.*;
import java.util.List;

import static core.GameConstants.rand;
import static resources.Resource.*;

public class AI extends Player{

    public final static int SEARCH_LIMIT = 5;

    private final Main main;

    private MainBuilding base;
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
                if (obj instanceof MainBuilding)
                    base = (MainBuilding) obj;

        if(getObjects().stream().filter(obj -> obj.getToken().equals("v")).count() < 5 && base != null) {
            Villager v = new Villager(this, base.getCell().fetch(1, 0, 0), cycle);
            if(main.addObject(v))
                v.construct();
        }

        for(Iterator<GameObject> it = getObjects().stream().filter(obj -> obj instanceof Worker).iterator(); it.hasNext();) {
            Worker obj = (Worker) it.next();
            Cell newLoc = obj.getCell();
            if(obj.getStatus() == Status.IDLE) {

                boolean needsToMove = true;
                if (harvested.containsKey(newLoc)) {
                    List<Resource> resources = Arrays.asList(Resource.values());
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
                    Motion motion = main.getShortestAdmissiblePath(obj, newLoc).key();

                    int counter = 0;
                    while (newLoc.getUnitSpace() - newLoc.getUnitOccupied() < obj.getSpace()
                            || (motion == null || (harvested.containsKey(newLoc) && counter++ <= SEARCH_LIMIT))) {
                        x = rand.nextInt(obj.getEnergy());
                        y = rand.nextInt(obj.getEnergy() - x);
                        newLoc = obj.getCell().fetch(x, y, 0);
                        motion = main.getShortestAdmissiblePath(obj, newLoc).key();
                    }

                    obj.changeEnergy(-motion.getSize());
                    main.motionToThread(motion);

                    harvested.put(newLoc, new ArrayList<>());
                    harvested.get(newLoc).add(FOOD);
                    LaborContract contract = new LaborContract(obj, FOOD, newLoc, 1);
                    obj.addContract(contract);
                    obj.setStatus(Status.WORKING);
                }
            }
        }

//        main.cyclePlayers();
    }
}
