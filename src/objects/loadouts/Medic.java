package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import objects.GameObject;
import objects.Healer;

public class Medic extends Loadout implements Healer {

    private int heal;

    @JsonCreator
    public Medic(@JsonProperty(required = true, value = "medic") int heal) {
        super("medic");
        this.heal = heal;
    }

    @Override
    public int getHeal() {
        return heal;
    }

    @Override
    public void setHeal(int heal) {
        this.heal = heal;
    }

    @Override
    public void heal(GameObject<?> obj) {
        obj.changeHealth(heal);
    }

    @Override
    public String toString() {
        return "Heal: " + heal;
    }
}
