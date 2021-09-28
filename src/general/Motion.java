package general;

import core.Location;
import items.GameObject;

import java.util.ArrayList;

public class Motion {

    private int step;

    private final int length;
    private final GameObject object;
    private final ArrayList<Location> locations;

    public Motion(GameObject obj, ArrayList<Location> path, int effectiveLength) {
        length = effectiveLength;
        locations = path;
        object = obj;
    }

    public Location next() {
        return locations.get(step++);
    }

    public boolean isDone() {
        return step >= locations.size();
    }

    public int getSize() { return length; }

    public GameObject getObject() { return object; }

    public Location[] getPath() {
        Location[] path = locations.toArray(new Location[0]);
        Location[] totalPath = new Location[path.length + 1];
        totalPath[0] = object.getLocation();
        System.arraycopy(path, 0, totalPath, 1, path.length);
        return totalPath;
    }

}
