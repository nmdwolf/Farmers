package core;

import core.contracts.Contract;
import core.contracts.LaborContract;
import general.Main;
import general.Motion;
import general.TypeException;
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
import static core.Type.*;
import static core.Option.*;

public class AI extends Player{

    public final static int SEARCH_LIMIT = 5;

    private final Main main;

    private MainBuilding base;
    private final HashMap<Location, ArrayList<Resource>> harvested;

    public AI(String name, Color color, Color alternativeColor, Main game) {
        super(name, color, alternativeColor);
        harvested = new HashMap<>();
        main = game;
    }

    public void makeMove(int cycle) throws TypeException {
        // init step
        if(base == null)
            for(GameObject obj : getObjects())
                if (obj.castAs(BUILDING) instanceof MainBuilding)
                    base = (MainBuilding) obj.castAs(BUILDING);

        if(getObjects().stream().filter(obj -> obj.getToken().equals("v")).count() < 5) {
            Villager v = new Villager(this, base.getLocation().add(1, 0, 0));
            if(v.checkStatus(CONSTRUCT) && main.addObject(v))
                v.perform(CONSTRUCT);
        }

        for(Iterator<GameObject> it = getObjects().stream().filter(obj -> obj.getTypes().contains(WORKER) &&
                obj.getValue(STATUS) == GameConstants.IDLE_STATUS && !obj.checkStatus(CONTRACT)).iterator(); it.hasNext();) {
            Worker obj = (Worker) it.next();
            Location newLoc = obj.getLocation();

            boolean needsToMove = true;
            if(harvested.containsKey(newLoc)) {
                List<Resource> resources = Arrays.asList(Resource.values());
                Collections.shuffle(resources, rand);
                for(Resource res : resources) {
                    if(!harvested.get(newLoc).contains(res) && obj.getValue(res.operation) > 0) {
                        harvested.get(newLoc).add(res);
                        LaborContract contract = new LaborContract(obj, res, 1);
                        contract.setCell(main.getCell(newLoc));
                        obj.addContract(contract);
                        obj.changeValue(STATUS, WORKING_STATUS);
                        needsToMove = false;
                        break;
                    }
                }
            }

            if(needsToMove) {
                newLoc = obj.getLocation().add(rand.nextInt(3) - 1, rand.nextInt(3) - 1, 0);
                Motion motion = main.getShortestAdmissiblePath(obj, newLoc);

                int counter = 0;
                while (main.getCell(newLoc).getUnitSpace() - main.getCell(newLoc).getUnitOccupied() < obj.getValue(SIZE)
                        || (motion == null || (harvested.containsKey(newLoc) && counter++ <= SEARCH_LIMIT))) {
                    newLoc = obj.getLocation().add(rand.nextInt(3) - 1, rand.nextInt(3) - 1, 0);
                    motion = main.getShortestAdmissiblePath(obj, newLoc);
                }

                obj.changeValue(ENERGY, -motion.getSize());
                main.motionToThread(motion);

                harvested.put(newLoc, new ArrayList<>());
                harvested.get(newLoc).add(FOOD);
                LaborContract contract = new LaborContract(obj, FOOD, 1);
                contract.setCell(main.getCell(newLoc));
                obj.addContract(contract);
                obj.changeValue(OLD_STATUS, WORKING_STATUS);
            }
        }

//        main.cyclePlayers();
    }
}
