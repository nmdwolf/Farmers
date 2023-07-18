package buildings;

import core.*;

import resources.ResourceContainer;
import items.Constructable;

public abstract class Building extends Constructable {

    public Building(Player p, Cell cell, int cycle, int space, int sight,
                    int health, int degradeTime, int degradeAmount, ResourceContainer cost, int difficulty) {
        super(p, cell, cycle, space, sight, health, degradeTime, degradeAmount, cost, difficulty, true);
    }

    @Override
    public String toString() {
        return "Type: " + getClassLabel() + "\nPlayer: " + getPlayer().getName() +
                "\nHealth: " + getHealth() + "/" + getMaxHealth();
    }
}
