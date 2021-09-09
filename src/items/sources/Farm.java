package items.sources;

import core.GameConstants;
import core.Location;
import core.Player;
import core.Resource;
import general.CustomMethods;
import items.GameObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Farm implements Source{

    public final static int FARM_SIZE = 1;
    public final static int FARM_SIGHT = 1;

    private final int id;
    private final HashSet<Integer> descriptions;
    private final HashMap<Resource, Integer> gain;

    private Player player;
    private Location location;

    public Farm(Player p, Location loc) {
        player = p;
        location = loc;

        descriptions = new HashSet<>(GameConstants.SOURCE_TYPE);

        gain = new HashMap<>();
        gain.put(Resource.FOOD, 10);

        id = CustomMethods.getNewIdentifier();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public String getType() {
        return "Farm";
    }

    @Override
    public String getToken() {
        return "F";
    }

    @Override
    public int getObjectIdentifier() {
        return id;
    }

    @Override
    public int getSize() {
        return FARM_SIZE;
    }

    @Override
    public int getLineOfSight() {
        return FARM_SIGHT;
    }

    @Override
    public void changeLineOfSight(int amount) {

    }

    @Override
    public void cycle(int cycle) {

    }

    @Override
    public Set<Integer> getDescriptions() {
        return descriptions;
    }

    @Override
    public void updateDescriptions(int... descriptions) {

    }

    @Override
    public GameObject getObject(int description) {
        return this;
    }

    @Override
    public Map<Resource, Integer> getResources() {
        return gain;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Farm)
            return id == ((Farm) obj).getObjectIdentifier();
        return false;
    }
}
