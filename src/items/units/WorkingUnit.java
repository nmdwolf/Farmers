package items.units;

import core.*;
import items.Worker;

import java.util.ArrayList;
import java.util.Map;

public abstract class WorkingUnit extends Unit implements Worker {

    private final ArrayList<Contract> contracts;

    public WorkingUnit(Player p, Location loc, Map<Resource, Integer> res, Map<Options, Integer> params) {
        super(p, loc, res, params);
        updateDescriptions(GameConstants.WORKER_TYPE);
        contracts = new ArrayList<>();
    }

    /**
     * Handles contract removal on move, fight, ...
     */
    private void quitJob() {
        contracts.removeIf(c -> c instanceof LaborContract);
    }

    @Override
    public void setLocation(Location loc) {
        super.setLocation(loc);
        quitJob();
    }

    @Override
    public void work() {
        for(Contract c : contracts) {
            if(getEnergy() >= c.getEnergyCost()) {
                changeEnergy(-c.getEnergyCost());
                c.work();
            }
        }
    }

    @Override
    public void addContract(Contract c) {
        contracts.add(c);
    }

    @Override
    public boolean hasContract() {
        return contracts.size() != 0;
    }
}
