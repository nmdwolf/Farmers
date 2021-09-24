package items.units;

import core.*;
import core.contracts.Contract;
import core.contracts.LaborContract;
import core.contracts.PrimeContract;
import general.OperationsList;
import general.ResourceContainer;
import general.TypeException;
import general.TypedConsumer;
import items.GameObject;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Worker extends Unit {

    private final ArrayList<Contract> contracts;
    private final HashSet<Option> gains;

    private HashSet<GameObject> boosters;

    public Worker(Player p, Location loc, ResourceContainer cost, Map<Option, Integer> params) {
        super(p, loc, cost, params);
        updateTypes(Type.CONSTRUCTOR, Type.WORKER);

        setValue(Option.CONSTRUCT_X, 0);
        setValue(Option.CONSTRUCT_Y, 0);

        contracts = new ArrayList<>();
        boosters = new HashSet<>();

        gains = new HashSet<>();
        for(Resource r : Resource.values()) {
            if(params.containsKey(r.operation))
                gains.add(r.operation);
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
        if(gains != null && gains.contains(option)) {
            int gain = super.getValue(option);
            for(GameObject booster : boosters)
                gain += booster.getValue(option);
            return gain;
        }
        else
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
    public OperationsList getOperations() {
        OperationsList operations = new OperationsList(super.getOperations());
        for (Iterator<GameObject> it = getPlayer().getObjects().stream().filter(obj -> obj.getLocation().equals(getLocation()) && obj.getTypes().contains(Type.SOURCE)).iterator(); it.hasNext(); ) {
            GameObject object = it.next().castAs(Type.SOURCE);
            operations.put("Prime " + object.getToken(), new TypedConsumer() {
                @Override
                public void accept(GameObject obj) throws TypeException {
                    addContract(new PrimeContract(Worker.this, object));
                }
            });
        }
        return operations;
    }
}
