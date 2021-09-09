package core;

import items.Worker;

public class LaborContract extends Contract {

    private Resource resource;
    private int cost, amount;
    private Cell station;

    public LaborContract(Worker p, Resource r, int c, int a, Cell s) {
        super(p, GameConstants.LABOR_KEY);
        amount = a;
        cost = c;
        resource = r;
        station = s;
    }

    public Resource getResource() { return resource; }

    @Override
    public int getEnergyCost() {
        return cost;
    }

    @Override
    public int getType() {
        return GameConstants.LABOR_KEY;
    }

    @Override
    public boolean work() {
        getParty().getPlayer().changeResource(resource, Math.min(station.getResource(resource), amount));
        station.changeResource(resource, -Math.min(station.getResource(resource), amount));
        return station.getResource(resource) == 0;
    }
}
