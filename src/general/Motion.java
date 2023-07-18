package general;

import units.Unit;

import java.util.ArrayList;

public class Motion {

    private int step;

    private final int length;
    private final Unit object;
    private final ArrayList<Location> locations;

    public Motion(Unit obj, ArrayList<Location> path, int effectiveLength) {
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

    public Unit getObject() { return object; }

    public Location[] getPath() {
        Location[] path = locations.toArray(new Location[0]);
        Location[] totalPath = new Location[path.length + 1];
        totalPath[0] = object.getCell().getLocation();
        System.arraycopy(path, 0, totalPath, 1, path.length);
        return totalPath;
    }

}
