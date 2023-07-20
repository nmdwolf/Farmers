package objects.units;

import core.*;
import general.OperationsList;
import objects.resources.ResourceContainer;
import core.upgrade.Upgrade;

import java.awt.image.BufferedImage;

public class Hero extends Unit{

    private final String name;

    public final static ResourceContainer HERO_COST = ResourceContainer.EMPTY_CONTAINER;

    public final static int HERO_HEALTH = 300;
    public final static int HERO_ENERGY = 5;
    public final static int HERO_SPACE = 1;
    public final static int HERO_SIGHT = 2;
    public final static int HERO_ANIMATION = 1000;

    public Hero(Player p, Cell cell, int cycle, String name) {
        super(p, cell, cycle, HERO_ANIMATION, HERO_SPACE, HERO_SIGHT, HERO_HEALTH,
                0, 0, 0, HERO_ENERGY, HERO_COST);
        this.name = name;
    }

    @Override
    public String getClassLabel() {
        return "Hero " + name;
    }

    @Override
    public String getToken() {
        return "H";
    }

    @Override
    public BufferedImage getSprite() {
        return null;
    }
    @Override
    public OperationsList getOperations(int cycle) {
        OperationsList operations = new OperationsList();
        for(Upgrade u : getPlayer().getCivilization().getUpgrades())
            operations.putUpgrade(u.toString(), u);
        return operations;
    }

    @Override
    public Award getConstructionAward() {
        return null;
    }

    @Override
    public OperationsList getEvolutions(int cycle) {
        return new OperationsList();
    }

    @Override
    public Award getEvolveAward() {
        return null;
    }
}
