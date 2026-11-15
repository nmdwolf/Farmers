package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import objects.GameObject;

public class Medic extends Loadout {

    private int heal;

    @JsonCreator
    public Medic(@JsonProperty(required = true, value = "medic") int heal) {
        super("medic");
        this.heal = heal;
    }

    public int getHeal() {
        return heal;
    }

    public void setHeal(int heal) {
        this.heal = heal;
    }

    public void heal(GameObject obj) {
        obj.increaseHealth(heal);
    }

    @Override
    public String toString() {
        return "Heal: " + heal;
    }
}
