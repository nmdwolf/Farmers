package core;

import core.contracts.LaborContract;
import general.Main;
import general.Motion;
import items.GameObject;
import items.buildings.MainBuilding;
import items.units.Villager;
import items.units.Worker;

import java.awt.*;
import java.util.*;
import java.util.List;

import static core.GameConstants.WORKING_STATUS;
import static core.GameConstants.rand;
import static core.Resource.*;
import static core.Option.*;

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

        if(getObjects().stream().filter(obj -> obj.getToken().equals("v")).count() < 5) {
            Villager v = new Villager(this, base.getCell().fetch(1, 0, 0));
            if(v.checkStatus(CONSTRUCT) && main.addObject(v))
                v.perform(CONSTRUCT);
        }

        for(Iterator<GameObject> it = getObjects().stream().filter(obj -> obj instanceof Worker &&
                obj.getValue(STATUS) == GameConstants.IDLE_STATUS && !obj.checkStatus(CONTRACT)).iterator(); it.hasNext();) {
            Worker obj = (Worker) it.next();
            Cell newLoc = obj.getCell();

            boolean needsToMove = true;
            if(harvested.containsKey(newLoc)) {
                List<Resource> resources = Arrays.asList(Resource.values());
                Collections.shuffle(resources, rand);
                for(Resource res : resources) {
                    if(!harvested.get(newLoc).contains(res) && obj.getYield(res) > 0) {
                        harvested.get(newLoc).add(res);
                        LaborContract contract = new LaborContract(obj, res, newLoc, 1);
                        obj.addContract(contract);
                        obj.changeValue(STATUS, WORKING_STATUS);
                        needsToMove = false;
                        break;
                    }
                }
            }

            if(needsToMove) {
                newLoc = obj.getCell().fetch(rand.nextInt(3) - 1, rand.nextInt(3) - 1, 0);
                Motion motion = main.getShortestAdmissiblePath(obj, newLoc);

                int counter = 0;
                while (newLoc.getUnitSpace() - newLoc.getUnitOccupied() < obj.getValue(SIZE)
                        || (motion == null || (harvested.containsKey(newLoc) && counter++ <= SEARCH_LIMIT))) {
                    newLoc = obj.getCell().fetch(rand.nextInt(3) - 1, rand.nextInt(3) - 1, 0);
                    motion = main.getShortestAdmissiblePath(obj, newLoc);
                }

                obj.changeValue(ENERGY, -motion.getSize());
                main.motionToThread(motion);

                harvested.put(newLoc, new ArrayList<>());
                harvested.get(newLoc).add(FOOD);
                LaborContract contract = new LaborContract(obj, FOOD, newLoc, 1);
                obj.addContract(contract);
                obj.changeValue(OLD_STATUS, WORKING_STATUS);
            }
        }

//        main.cyclePlayers();
    }
}
