package core.player;

import objects.GameObject;
import core.upgrade.Upgrade;

import java.util.List;

public interface Civilization {

    String getName();

    List<Upgrade> getUpgrades();

    /**
     * Takes a basic instance of a game object and adapts it to the civilization.
     * @param obj basic instance
     * @return civilization-adapted instance
     */
    GameObject<?> initObject(GameObject<?> obj);
}
