package core;

import items.Productive;

public class LaborContract extends Contract {

    private int resource;
    private int cost, amount;
    private Cell station;

    public LaborContract(Productive p, int r, int c, int a, Cell s) {
        super(p, GameConstants.LABOR);
        amount = a;
        cost = c;
        resource = r;
        station = s;
    }

    public int getResource() { return resource; }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public int getType() {
        return GameConstants.LABOR;
    }

    @Override
    public boolean complete() {
        getParty().getPlayer().changeResource(resource, Math.min(station.getResource(resource), amount));
        station.changeResource(resource, -Math.min(station.getResource(resource), amount));
        return station.getResource(resource) == 0;
    }
}
