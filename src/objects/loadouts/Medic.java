package objects.loadouts;

import objects.GameObject;
import objects.Healer;
import objects.templates.HealerTemplate;

public class Medic extends Loadout<HealerTemplate> implements Healer {

    private int heal;

    public Medic(HealerTemplate temp) {
        super(temp);
        this.heal = temp.heal;
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
}
