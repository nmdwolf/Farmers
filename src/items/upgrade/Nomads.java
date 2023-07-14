package items.upgrade;

import core.Civilization;
import items.GameObject;

import java.util.List;

public class Nomads implements Civilization {
    @Override
    public String getName() {
        return "Nomads";
    }

    @Override
    public List<Upgrade> getUpgrades() {
        return null;
    }

    @Override
    public GameObject initObject(GameObject obj) {
        return obj;
    }
}
