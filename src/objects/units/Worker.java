package objects.units;

import core.*;
import core.contracts.Contract;
import core.contracts.LaborContract;
import core.OperationsList;
import core.player.Player;
import core.Status;
import core.resources.Resource;
import core.resources.ResourceContainer;
import objects.Booster;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Worker extends Unit {

    private HashSet<Booster> boosters;
    private final ResourceContainer production;

    public Worker(Player p, Cell cell, int cycle, int animationDelay, int space, int sight, int health,
                  int degradeTime, int degradeAmount, int cycleLength, int energy,
                  ResourceContainer cost, ResourceContainer production) {
        super(p, cell, cycle, animationDelay, space, sight, health,
                degradeTime, degradeAmount, cycleLength, energy, cost);

        boosters = new HashSet<>();
        this.production = production;
    }

    @Override
    public void setCell(Cell cell) {
        super.setCell(cell);

        boosters = getPlayer().getObjects().stream().filter(
                obj -> obj instanceof Booster &&
                        getCell().distanceTo(obj.getCell()) <= ((Booster)obj).getBoostRadius()
        ).map(Booster.class::cast).collect(Collectors.toCollection(HashSet::new));
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
            for (Resource res : getPlayer().getResources().keySet()) {
                if(production.get(res) > 0)
                    operations.put(res.getName(), _ -> addContract(new LaborContract(Worker.this, res, getCell(), 1)));
            }
        }
        return operations;
    }
}
