package items.units;

import core.*;
import core.contracts.Contract;
import core.contracts.LaborContract;
import core.contracts.PrimeContract;
import general.OperationsList;
import general.ResourceContainer;
import items.GameObject;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Worker extends Unit {

    private final ArrayList<Contract> contracts;

    private HashSet<GameObject> boosters;

    public Worker(Player p, Location loc, ResourceContainer cost, Map<Option, Integer> params) {
        super(p, loc, cost, params);
        updateTypes(Type.CONSTRUCTOR, Type.WORKER);

        setValue(Option.CONSTRUCT_X, 0);
        setValue(Option.CONSTRUCT_Y, 0);

        contracts = new ArrayList<>();
        boosters = new HashSet<>();

        for(Resource resource : Resource.values()) {
            if(!params.containsKey(resource.operation))
                throw new IllegalArgumentException("Resource of type " + resource + " not found.");
        }
    }

    /**
     * Handles contract removal on move, fight, ...
     */
    private void seizeActions() {
        contracts.removeIf(c -> c instanceof LaborContract);
        changeValue(Option.STATUS, GameConstants.IDLE_STATUS);
    }

    @Override
    public void setLocation(Location loc) {
        super.setLocation(loc);

        if(getValue(Option.STATUS) != GameConstants.WALKING_STATUS)
            seizeActions();

        boosters = getPlayer().getObjects().stream().filter(
                obj -> obj.getTypes().contains(Type.BOOSTER) &&
                        getLocation().distanceTo(obj.getLocation()) <= GameConstants.BOOSTER_DISTANCE
        ).collect(Collectors.toCollection(HashSet::new));
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

    @Override
    public int getValue(Option option) {
        for(Resource resource : Resource.values()) {
            if(option == resource.operation && super.getValue(option) != 0) {
                int gain = super.getValue(option);
                for(GameObject booster : boosters)
                    gain += booster.getValue(option);
                return gain;
            }
        }
        return super.getValue(option);
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
        OperationsList operations = new OperationsList(super.getOperations(options));
        for(Option option : options) {

            // Add resource labor contracts
            if(option == Option.RESOURCE) {
                for (Resource res : Resource.values()) {
                    operations.put(res.name, (obj, params) -> {
                        LaborContract contract = new LaborContract(Worker.this, res, 1);
                        contract.setCell((Cell)params[0]);
                        addContract(contract);
                        changeValue(Option.OLD_STATUS, GameConstants.WORKING_STATUS);
                    });
                }
            } else {

                // Add source prime contracts
                for (Iterator<GameObject> it = getPlayer().getObjects().stream().filter(obj -> obj.getLocation().equals(getLocation())
                        && obj.getTypes().contains(Type.SOURCE)).iterator(); option == Option.SOURCE && it.hasNext(); ) {
                    GameObject object = it.next().castAs(Type.SOURCE);
                    operations.put("Prime " + object.getToken(), (obj, params) -> addContract(new PrimeContract(Worker.this, object)));
                }
            }
        }
        return operations;
    }
}
