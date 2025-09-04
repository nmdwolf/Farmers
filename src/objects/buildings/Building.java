package objects.buildings;

import core.*;

import core.player.Player;
import core.resources.ResourceContainer;
import objects.Construction;

import static core.GameConstants.BUILDING_TYPE;

public abstract class Building extends Construction {

    public Building(int space, int sight,
                    int health, int degradeTime, int degradeAmount, ResourceContainer cost, int difficulty) {
        super(space, sight, health, degradeTime, degradeAmount, cost, difficulty, true);
    }

    @Override
    public String toString() {
        return "Type: " + getClassLabel() + "\nPlayer: " + getPlayer().getName() +
                "\nHealth: " + getHealth() + "/" + getMaxHealth();
    }

    @Override
    public int getType() {
        return BUILDING_TYPE;
    }
}
