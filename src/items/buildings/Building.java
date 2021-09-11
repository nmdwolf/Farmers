package items.buildings;

import static core.Options.*;

import core.*;

import items.GameObject;

import java.util.Map;

public abstract class Building extends GameObject {

    private int maxHealth, health, degradationCycle, degradationAmount;
    private int status, cycle, space, maxFoundation, level;
    private final ResourceContainer cost;

    public Building(Player p, Location loc, ResourceContainer res, Map<Options, Integer> params) {
        super(p, loc, params.get(SIZE_KEY), params.get(SIGHT_KEY));

        maxHealth = params.get(HEALTH_KEY);
        health = params.get(HEALTH_KEY);
        status = params.get(STATUS_KEY);
        space = params.get(SPACE_KEY);
        degradationAmount = params.get(DEGRADATION_AMOUNT_KEY);
        degradationCycle = params.get(DEGRADATION_CYCLE_KEY);
        cost = res;

        updateDescriptions(Type.BUILDING_TYPE);
    }

    @Override
    public int getValue(Options option) {
        return switch(option) {
            case LEVEL_KEY: yield level;
            case STATUS_KEY: yield status;
            case SPACE_KEY: yield space;
            case HEALTH_KEY: yield health;
            case MAX_HEALTH_KEY: yield maxHealth;
            case DEGRADATION_CYCLE_KEY: yield degradationCycle;
            case DEGRADATION_AMOUNT_KEY: yield degradationAmount;
            default: yield super.getValue(option);
        };
    }

    @Override
    public void changeValue(Options option, int amount) {
        switch (option) {
            case LEVEL_KEY: level += amount;
            case HEALTH_KEY: health += amount;
            case MAX_HEALTH_KEY: maxHealth += amount;
            case DEGRADATION_CYCLE_KEY: degradationCycle += amount;
        }
    }

    @Override
    public ResourceContainer getResources(Options option) {
        return switch(option) {
            case CONSTRUCT_KEY: yield cost;
            default: yield ResourceContainer.EMPTY_CONTAINER;
        };
    }

    @Override
    public void perform(Options option) {
        switch(option) {
            case CONSTRUCT_KEY:
                getPlayer().changeResources(cost);
                getPlayer().addObject(this);
                break;
            case CYCLE_KEY:
                super.perform(CYCLE_KEY);
                if(getValue(CYCLE_KEY) >= degradationCycle)
                    health -= degradationAmount;
                break;
            case DEGRADE_KEY:
                health -= degradationAmount;
                break;
            case DESTROY_KEY:
                player.removeObject(this);
                break;
        }
    }

    @Override
    public boolean checkStatus(Options option) {
        return switch(option) {
            case CONSTRUCT_KEY: yield player.hasResources(cost);
            default: yield false;
        };
    }

    @Override
    public String toString() {
        return "Type: " + getType() + "\ncore.Player: " + player.getName() +
                "\nHealth: " + health + "/" + maxHealth;
    }
}
