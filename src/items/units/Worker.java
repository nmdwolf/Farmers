package items.units;

import core.*;
import core.contracts.Contract;
import core.contracts.LaborContract;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public abstract class Worker extends Unit {

    private final ArrayList<Contract> contracts;

    private int constructX, constructY;

    public Worker(Player p, Location loc, ResourceContainer res, Map<Options, Integer> params) {
        super(p, loc, res, params);
        contracts = new ArrayList<>();
        updateDescriptions(Type.CONSTRUCTOR_TYPE, Type.WORKER_TYPE);

        constructX = constructY = 0;
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
    public void perform(Options option) {
        if(option == Options.WORK_KEY) {
            for (Iterator<Contract> iterator = contracts.iterator(); iterator.hasNext(); ) {
                Contract c = iterator.next();
                if (getValue(Options.ENERGY_KEY) >= c.getEnergyCost()) {
                    changeValue(Options.ENERGY_KEY, -c.getEnergyCost());
                    boolean done = c.work();
                    if(done)
                        iterator.remove();
                }
            }
        } else
            super.perform(option);
    }

    @Override
    public boolean checkStatus(Options option) {
        if(option == Options.CONTRACT_KEY)
            return (contracts.size() != 0);
        else
            return super.checkStatus(option);
    }

    public void addContract(Contract c) {
        contracts.add(c);
    }

    @Override
    public int getValue(Options option) {
        return switch(option) {
            case CONSTRUCT_X_KEY: yield constructX;
            case CONSTRUCT_Y_KEY: yield constructY;
            default: yield super.getValue(option);
        };
    }

    @Override
    public void changeValue(Options option, int amount) {
        switch(option) {
            case CONSTRUCT_X_KEY:
                constructX += amount;
                break;
            case CONSTRUCT_Y_KEY:
                constructY += amount;
                break;
            default:
                super.changeValue(option, amount);
                break;
        }
    }
}
