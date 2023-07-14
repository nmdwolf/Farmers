package items.units;

import core.*;
import core.contracts.Contract;
import core.contracts.LaborContract;
import general.OperationsList;
import general.ResourceContainer;
import items.Booster;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Worker extends Unit {

    private final ArrayList<Contract> contracts;
    private HashSet<Booster> boosters;

    public Worker(Player p, Cell cell, int size, ResourceContainer cost, Map<Option, Integer> params) {
        super(p, cell, size, cost, params);

        setValue(Option.CONSTRUCT_X, 0);
        setValue(Option.CONSTRUCT_Y, 0);

        contracts = new ArrayList<>();
        boosters = new HashSet<>();
    }

    /**
     * Handles contract removal on move, fight, ...
     */
    private void seizeActions() {
        List<Contract> removals = contracts.stream().filter(obj -> obj instanceof LaborContract).toList();
        removals.forEach(Contract::terminate);
        contracts.removeAll(removals);
        changeValue(Option.STATUS, GameConstants.IDLE_STATUS);
    }

    @Override
    public void setCell(Cell cell) {
        super.setCell(cell);

        if(getValue(Option.STATUS) != GameConstants.WALKING_STATUS)
            seizeActions();

        boosters = getPlayer().getObjects().stream().filter(
                obj -> obj instanceof Booster &&
                        getCell().distanceTo(obj.getCell()) <= ((Booster)obj).getBoostRadius()
        ).map(Booster.class::cast).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public void perform(Option option) {
        if(option == Option.WORK) {
            if(contracts.size() > 0)
                changeValue(Option.STATUS, GameConstants.WORKING_STATUS);
            for (Iterator<Contract> iterator = contracts.iterator(); iterator.hasNext(); ) {
                Contract c = iterator.next();
                if (getValue(Option.ENERGY) >= c.getEnergyCost()) {
                    changeValue(Option.ENERGY, -c.getEnergyCost());
                    boolean done = c.work();
                    if(done)
                        iterator.remove();
                }
            }

            if(contracts.size() == 0)
                changeValue(Option.STATUS, GameConstants.IDLE_STATUS);
        } else
            super.perform(option);
    }

    public abstract List<Resource> getResources();

    public int getYield(Resource resource) {
        int gain = 0;
        if(getResources().contains(resource)) {
            gain = 1;
            for(Booster booster : boosters)
                gain += booster.getBoostAmount(this, resource);

        }
        return gain;
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == Option.CONTRACT)
            return (contracts.size() != 0);
        else
            return super.checkStatus(option);
    }

    public void addContract(Contract c) {
        contracts.add(c);
    }

    @Override
    public OperationsList getOperations(Option... options) {
        OperationsList operations = super.getOperations(options);
        for(Option option : options) {

            // Add resource labor contracts
            if(option == Option.RESOURCE) {
                for (Resource res : Resource.values()) {
                    operations.put(res.name, () -> {
                        LaborContract contract = new LaborContract(Worker.this, res, getCell(), 1);
                        addContract(contract);
                        changeValue(Option.OLD_STATUS, GameConstants.WORKING_STATUS);
                    });
                }
            }
        }
        return operations;
    }
}
