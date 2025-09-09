package objects.units;

import core.*;
import core.contracts.LaborContract;
import core.OperationsList;
import core.resources.ResourceContainer;
import objects.Booster;
import objects.loadouts.Gatherer;
import objects.templates.UnitTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class Worker extends Unit {

    private HashSet<Booster> boosters;

    public Worker(UnitTemplate temp) {
        super(temp);

        boosters = new HashSet<>();
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
    public int getYield(String resource) {
        final AtomicInteger yield = new AtomicInteger(0);
        getLoadout(Gatherer.class).ifPresent(loadout -> {
            yield.set(loadout.getYield(resource));
            if(yield.get() > 0)
                for(Booster booster : boosters)
                    yield.addAndGet(booster.getBoostAmount(this, resource));
        });

        return yield.get();
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        OperationsList operations = new OperationsList();
        if(code == OperationCode.RESOURCE) {
            for (String res : getPlayer().getResources().keySet()) {
                if(getYield(res) > 0)
                    operations.put(res, _ -> addContract(new LaborContract(Worker.this, res, getCell(), 1)));
            }
        }
        return operations;
    }
}
