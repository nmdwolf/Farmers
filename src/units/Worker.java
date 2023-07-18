package units;

import core.*;
import contracts.Contract;
import contracts.LaborContract;
import general.OperationsList;
import resources.Resource;
import resources.ResourceContainer;
import items.Booster;
import items.GameObject;
import resources.Source;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Worker extends Unit {

    private final ArrayList<Contract> contracts;
    private HashSet<Booster> boosters;
    private ResourceContainer production;

    public Worker(Player p, Cell cell, int cycle, int animationDelay, int space, int sight, int health,
                  int degradeTime, int degradeAmount, int energy,
                  ResourceContainer cost, ResourceContainer production) {
        super(p, cell, cycle, animationDelay, space, sight, health, degradeTime, degradeAmount, energy, cost);

        contracts = new ArrayList<>();
        boosters = new HashSet<>();
        this.production = production;
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
        if(contracts.size() > 0)
            setStatus(Status.WORKING);

        for (Iterator<Contract> iterator = contracts.iterator(); iterator.hasNext(); ) {
            Contract c = iterator.next();
            if (getEnergy() >= c.getEnergyCost()) {
                changeEnergy(-c.getEnergyCost());
                if(c.work())
                    iterator.remove();
            }
        }

        if(contracts.size() == 0)
            setStatus(Status.IDLE);
    }

    public ResourceContainer getProduction() { return production; }

    public int getYield(Resource resource) {
        int gain = 0;
        if(production.get(resource) > 0) {
            gain = 1;
            for(Booster booster : boosters)
                gain += booster.getBoostAmount(this, resource);

        }
        return gain;
    }

    public void addContract(Contract c) {
        contracts.add(c);
    }

    public void removeContract(Contract c) { contracts.remove(c); }

    @Override
    public OperationsList getOperations(int cycle) {
        OperationsList operations = new OperationsList();
        for (Resource res : Resource.values()) {
            for(GameObject obj : getCell().getContent()) {
                if(obj instanceof Source && ((Source)obj).getResourceType() == res && production.get(res) > 0) {
                    operations.put(res.name, () -> {
                        LaborContract contract = new LaborContract(Worker.this, res, getCell(), 1);
                        addContract(contract);
                        setStatus(Status.WORKING);
                        //changeValue(Option.OLD_STATUS, GameConstants.WORKING_STATUS);
                    });
                }
            }
        }
        return operations;
    }
}
