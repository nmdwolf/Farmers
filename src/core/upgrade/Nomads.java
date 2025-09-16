package core.upgrade;

import core.player.Civilization;
import objects.GameObject;

import java.util.ArrayList;
import java.util.List;

public class Nomads implements Civilization {
    @Override
    public String getName() {
        return "Nomads";
    }

    @Override
    public List<Upgrade> getUpgrades() {
        return new ArrayList<>();
    }

    @Override
    public GameObject<?> initObject(GameObject<?> obj) {
        return obj;
    }
}
