package objects.units;

import core.*;
import core.contracts.ConstructContract;
import core.contracts.Contract;
import core.contracts.LaborContract;
import UI.OperationsList;
import core.player.Player;
import objects.resources.Resource;
import objects.resources.ResourceContainer;
import objects.Booster;
import objects.GameObject;
import objects.resources.Source;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Worker extends Unit {

    private ArrayList<Contract> contracts;
    private HashSet<Booster> boosters;
    private final ResourceContainer production;

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
     * Abandons {@code LaborContracts} and unsets employee of {@code ConstructContracts}.
     */
    private void seizeActions() {
        contracts.stream().filter(obj -> obj instanceof LaborContract).forEach(Contract::abandon);
        contracts.stream().filter(obj -> obj instanceof ConstructContract<?>).forEach(contract -> contract.setEmployee(null));
        contracts = new ArrayList<>();
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

    /**
     * Performs work on the list of active contracts if sufficient this Worker has sufficient energy.
     * TODO implement prioritization of contracts
     */
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
        }

        if(contracts.isEmpty())
            setStatus(Status.IDLE);
    }

    /**
     * Adds a contract to this Worker's active contracts.
     * This also abandons the current LaborContract(s) if there are any.
     * @param c new contract
     * @throws IllegalArgumentException If the given contract does not have this Worker as assigned employee, an exception is thrown. For existing contracts, the {@code transferContract(Contract c) } method should be used.
     */
    public void addContract(Contract c) throws IllegalArgumentException {

        if(!c.getEmployee().equals(this))
            throw new IllegalArgumentException("Contract is required to have this Worker as assigned employee.");

        // Removes current LaborContract(s), if any
        if(c instanceof LaborContract) {
            contracts.stream().filter(obj -> obj instanceof LaborContract).forEach(Contract::abandon);
            contracts.removeIf(obj -> obj instanceof LaborContract);
        }

        contracts.add(c);
        c.initialize(); // If this fails (e.g. insufficient resources), it will be called again in work() until it succeeds.
        setStatus(Status.WORKING);
    }

    /**
     * Intended to be used in the same way as {@code addContract(Contract c)} with the sole difference that this
     * method first sets the employee of the given contract to be this Worker.
     * @param c new contract
     */
    public void transferContract(Contract c) {
        c.setEmployee(this);
        addContract(c);
    }

    /**
     * Calculates the production yield of the provided {@code Resource}, taking into account current {@code Booster}s.
     * @param resource Resource for which the production yield is calculated.
     * @return production yield
     */
    public int getYield(Resource resource) {
        int yield = production.get(resource);
        if(yield > 0)
            for(Booster booster : boosters)
                yield += booster.getBoostAmount(this, resource);

        return yield;
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        OperationsList operations = new OperationsList();
        if(code == OperationCode.RESOURCE) {
            for (Resource res : Resource.values()) {
                if(production.get(res) > 0) {
//                    for (GameObject obj : getCell().getContent()) {
//                        if (obj instanceof Source source && source.getResourceType() == res) {
//                            operations.put(res.name, () -> {
//                                addContract(new LaborContract(Worker.this, res, getCell(), 1));
//                                //changeValue(Option.OLD_STATUS, GameConstants.WORKING_STATUS);
//                            });
//                        }
//                    }
                    operations.put(res.name, () -> addContract(new LaborContract(Worker.this, res, getCell(), 1)));
                }
            }
        }
        return operations;
    }
}
