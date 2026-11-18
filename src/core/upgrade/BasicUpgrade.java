package core.upgrade;

import core.InternalSettings;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.resources.ResourceContainer;
import objects.GameObject;
import objects.Operational;
import objects.loadouts.Fighter;

public class BasicUpgrade extends Upgrade {

    @JsonProperty
    private int health;

    @JsonProperty
    private int energy;

    @JsonProperty
    private int attack;

    @JsonProperty
    private int range;

    @JsonProperty
    private int sight;

    @JsonProperty
    private int space;

    @JsonProperty
    private String target = "";

    @JsonCreator
    public BasicUpgrade(@JsonProperty(value = "cost", required = true) ResourceContainer cost, @JsonProperty(value = "cycle") int cycle, @JsonProperty(required = true, value = "description") String description, @JsonProperty(value = "global") boolean global) {
        super(cost, cycle, description, global);
    }

    @Override
    public void apply(GameObject object) {

        if(validateType(this, object)) {

            object.increaseMaxHealth(health);
            object.increaseSight(sight);
            object.increaseSize(space);

            if (object instanceof Operational e)
                e.increaseMaxEnergy(energy);

            // If object has Fighter loadout, modify its properties
            object.dispatchToLoadout(Fighter.class, fighter -> {
                fighter.increaseAttack(attack);
                fighter.increaseRange(range);
            });
        }
    }

    public static boolean validateType(BasicUpgrade upgrade, GameObject obj) {
        return switch(upgrade.target) {
            case "unit" -> obj.getType() == InternalSettings.UNIT_TYPE;
            case "building" -> obj.getType() == InternalSettings.BUILDING_TYPE;
            case "" -> true;
            default -> obj.getClassLabel().equals(upgrade.target);
        };
    }
}
