package objects.units;

import UI.Sprite;
import core.*;
import core.contracts.LaborContract;
import core.OperationsList;
import objects.buildings.Foundation;
import objects.loadouts.Booster;
import objects.loadouts.Gatherer;
import objects.templates.UnitTemplate;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Worker extends Unit<Worker> implements objects.Gatherer {

    private List<Booster> boosters;

    public Worker(UnitTemplate temp) {
        super(temp);
    }

    @Override
    public void setCell(@NotNull Cell cell) {
        super.setCell(cell);
        boosters = getPlayer().getObjects().stream().flatMap(obj -> obj.getLoadout(objects.loadouts.Booster.class).stream().filter(booster -> cell.distanceTo(obj.getCell()) <= booster.getBoostRadius())).toList();
    }

    @Override
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
                    operations.put(res, _ -> addContract(new LaborContract(this, res, getCell())));
            }
        } else if(code == OperationCode.CONSTRUCTION) {
            getCell().getObjects().stream().filter(obj -> obj instanceof Foundation<?> f && f.getContract().isIdle()).map(obj -> new Pair<>(obj.getClassLabel(), ((Foundation<?>) obj).getContract())).forEach(pair -> operations.put("Continue " + pair.key(), _ -> this.transferContract(pair.value())));
        }
        return operations;
    }

    @Override
    public int getGatherCost() {
        return getLoadout(Gatherer.class).map(Gatherer::getGatherCost).orElse(0);
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite() {
        if(getStatus() != Status.WORKING)
            return super.getSprite();
        else
            return Sprite.getSprite(getTemplate().type + "_working");
    }
}
