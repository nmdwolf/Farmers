package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import objects.GameObject;
import objects.Healer;

public class Medic extends Loadout implements Healer {

    private int heal;

    @JsonCreator
    public Medic(int heal) {
        super("medic");
        this.heal = heal;
    }

    public int getHeal() {
        return heal;
    }

    public void setHeal(int heal) {
        this.heal = heal;
    }

    @Override
    public void heal(GameObject obj) {
        obj.changeHealth(heal);
    }

    @Override
    public String toString() {
        return "Heal: " + heal;
    }
}
