package core.upgrade;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.GameConstants;
import core.player.Player;
import core.resources.ResourceContainer;
import objects.Aggressive;
import objects.Energetic;
import objects.GameObject;

public class BasicUpgrade extends Upgrade {

    @JsonProperty(defaultValue = "0")
    private int health;

    @JsonProperty(defaultValue = "0")
    private int energy;

    @JsonProperty(defaultValue = "0")
    private int attack;

    @JsonProperty(defaultValue = "0")
    private int range;

    @JsonProperty(defaultValue = "0")
    private int sight;

    @JsonProperty(defaultValue = "0")
    private int space;

    @JsonProperty(defaultValue = "")
    private String target;

    @JsonCreator
    public BasicUpgrade(@JsonProperty(required = true) ResourceContainer cost, @JsonProperty(defaultValue = "0") int cycle, @JsonProperty(required = true) String description, @JsonProperty(defaultValue = "false") boolean global) {
        super(cost, cycle, description, global);
    }

    @Override
    public void apply(GameObject<?> object) {

        if(validateType(this, object)) {

            object.changeMaxHealth(health);
            object.changeSight(sight);
            object.changeSpace(space);

            if (object instanceof Energetic<?> e)
                e.changeMaxEnergy(energy);

            if (object instanceof Aggressive<?> a) {
                a.changeAttack(attack);
                a.changeRange(range);
            }
        }
    }

    public static boolean validateType(BasicUpgrade upgrade, GameObject<?> obj) {
        return switch(upgrade.target) {
            case "unit" -> obj.getType() == GameConstants.UNIT_TYPE;
            case "building" -> obj.getType() == GameConstants.BUILDING_TYPE;
            case "" -> true;
            default -> obj.getClassLabel().equals(upgrade.target);
        };
    }
}
