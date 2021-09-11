package items.units;

import static core.Options.*;

import core.*;
import items.GameObject;

import java.util.Map;

public abstract class Unit extends GameObject {

    private int status, maxEnergy, energy;
    private int maxHealth, health, degradationCycle, degradationAmount;
    private final ResourceContainer cost;

    public Unit(Player p, Location loc, ResourceContainer res, Map<Options, Integer> params) {
        super(p, loc, params.get(SIZE_KEY), params.get(SIGHT_KEY));

        health = maxHealth = params.get(HEALTH_KEY);
        energy = maxEnergy = params.get(ENERGY_KEY);
        status = params.get(STATUS_KEY);
        degradationAmount = params.get(DEGRADATION_AMOUNT_KEY);
        degradationCycle = params.get(DEGRADATION_CYCLE_KEY);

        cost = res;

        updateDescriptions(Type.UNIT_TYPE);
    }

    @Override
    public int getValue(Options option) {
        return switch(option) {
            case STATUS_KEY: yield status;
            case HEALTH_KEY: yield health;
            case MAX_HEALTH_KEY: yield maxHealth;
            case ENERGY_KEY: yield energy;
            case MAX_ENERGY_KEY: yield maxEnergy;
            case DEGRADATION_CYCLE_KEY: yield degradationCycle;
            case DEGRADATION_AMOUNT_KEY: yield degradationAmount;
            default: yield super.getValue(option);
        };
    }

    @Override
    public void changeValue(Options option, int amount) {
        switch (option) {
            case HEALTH_KEY:
                health += amount;
                break;
            case MAX_HEALTH_KEY:
                maxHealth += amount;
                break;
            case ENERGY_KEY:
                energy += amount;
                break;
            case MAX_ENERGY_KEY:
                maxEnergy += amount;
                break;
            case DEGRADATION_CYCLE_KEY:
                degradationCycle += amount;
                break;
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
                energy = maxEnergy;
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
                "\nHealth: " + health + "/" + maxHealth +
                "\nEnergy: " + energy + "/" + maxEnergy;
    }
}
