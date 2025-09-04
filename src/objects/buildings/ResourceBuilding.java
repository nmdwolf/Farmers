package objects.buildings;

import core.Cell;
import core.player.Player;
import core.resources.ResourceContainer;
import core.resources.Source;

public abstract class ResourceBuilding extends IdleBuilding implements Source {

    private ResourceContainer gains;

    public ResourceBuilding(int space, int sight, int health,
                            ResourceContainer cost, int difficulty, ResourceContainer gains,
                            int degradeTime, int degradeAmount) {
        super(space, sight, health, degradeTime, degradeAmount, cost, difficulty);
        this.gains = gains;
    }

    @Override
    public ResourceContainer getYield() {
        return gains;
    }

}
