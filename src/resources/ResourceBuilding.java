package resources;

import core.Cell;
import core.Player;
import buildings.IdleBuilding;

public abstract class ResourceBuilding extends IdleBuilding implements Source {

    private ResourceContainer gains;

    public ResourceBuilding(Player p, Cell cell, int cycle, int space, int sight, int health,
                            ResourceContainer cost, int difficulty, ResourceContainer gains,
                            int degradeTime, int degradeAmount) {
        super(p, cell, cycle, space, sight, health, degradeTime, degradeAmount, cost, difficulty);
        this.gains = gains;
    }

    @Override
    public ResourceContainer getYield() {
        return gains;
    }

}
