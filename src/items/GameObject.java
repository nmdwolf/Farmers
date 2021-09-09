package items;

import core.Player;

import core.Location;

import java.util.Set;

public interface GameObject {

    Location getLocation();

    Player getPlayer();

    String getType();
    String getToken();
    int getObjectIdentifier();

    int getSize();

    int getLineOfSight();
    void changeLineOfSight(int amount);

    void cycle(int cycle);

    Set<Integer> getDescriptions();
    void updateDescriptions(int... descriptions);

    GameObject getObject(int description);
}
