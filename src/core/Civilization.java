package core;

import objects.GameObject;
import core.upgrade.Upgrade;

import java.util.List;

public interface Civilization {

    public String getName();

    public List<Upgrade> getUpgrades();

    /**
     * Takes a basic instance of a game object and adapts it to the civilization.
     * @param obj basic instance
     * @return civilization-adapted instance
     */
    public GameObject initObject(GameObject obj);
}
