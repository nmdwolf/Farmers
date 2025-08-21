package objects.units;

import core.*;
import core.contracts.Contract;
import core.contracts.LaborContract;
import UI.OperationsList;
import objects.resources.Resource;
import objects.resources.ResourceContainer;
import objects.Booster;
import objects.GameObject;
import objects.resources.Source;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Worker extends Unit {

    private final ArrayList<Contract> contracts;
    private HashSet<Booster> boosters;
    private ResourceContainer production;

    public Worker(Player p, Cell cell, int cycle, int animationDelay, int space, int sight, int health,
                  int degradeTime, int degradeAmount, int cycleLength, int energy,
                  ResourceContainer cost, ResourceContainer production) {
        super(p, cell, cycle, animationDelay, space, sight, health,
                degradeTime, degradeAmount, cycleLength, energy, cost);

        contracts = new ArrayList<>();
        boosters = new HashSet<>();
        this.production = production;
    }

    @Override
    public void cycle(int cycle) {
        super.cycle(cycle);
        if(getStatus() != Status.WALKING)
            work();
    }

    /**
     * Handles contract removal on move, fight, ...
     */
    private void seizeActions() {
        List<Contract> removals = contracts.stream().filter(obj -> obj instanceof LaborContract).toList();
        removals.forEach(Contract::abandon);
        contracts.removeAll(removals);
        setStatus(Status.IDLE);
    }

    @Override
    public void setCell(Cell cell) {
        super.setCell(cell);

        if(getOldStatus() != Status.WALKING)
            seizeActions();

        boosters = getPlayer().getObjects().stream().filter(
                obj -> obj instanceof Booster &&
                        getCell().distanceTo(obj.getCell()) <= ((Booster)obj).getBoostRadius()
        ).map(Booster.class::cast).collect(Collectors.toCollection(HashSet::new));
    }

    public void work() {
        if(!contracts.isEmpty()) {
            setStatus(Status.WORKING);

            for (Iterator<Contract> iterator = contracts.iterator(); iterator.hasNext(); ) {
                Contract c = iterator.next();
                if (getEnergy() >= c.getEnergyCost()) {
                    changeEnergy(-c.getEnergyCost());
                    if (c.work())
                        iterator.remove();
                }
            }
        } else
            setStatus(Status.IDLE);
    }

    public void addContract(Contract c) {
        if(c instanceof LaborContract)
            contracts.removeIf(obj -> obj instanceof LaborContract); // Removes current LabourContract
        contracts.add(c);
        c.initialize(); // If this fails (e.g. insufficient resources), it will be called again in work() until it succeeds.
        setStatus(Status.WORKING);
    }

    public int getYield(Resource resource) {
        int gain = 0;
        if(production.get(resource) > 0) {
            gain = 1;
            for(Booster booster : boosters)
                gain += booster.getBoostAmount(this, resource);

        }
        return gain;
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        OperationsList operations = new OperationsList();
        if(code == OperationCode.RESOURCE) {
            for (Resource res : Resource.values()) {
                for (GameObject obj : getCell().getContent()) {
                    if (obj instanceof Source && ((Source) obj).getResourceType() == res && production.get(res) > 0) {
                        operations.put(res.name, () -> {
                            LaborContract contract = new LaborContract(Worker.this, res, getCell(), 1);
                            addContract(contract);
                            //changeValue(Option.OLD_STATUS, GameConstants.WORKING_STATUS);
                        });
                    }
                }
            }
        }
        return operations;
    }
}
