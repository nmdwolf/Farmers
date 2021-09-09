package items;

import core.Location;

import java.util.ArrayList;

public class Motion {

    private int step;

    private final int length;
    private final Movable object;
    private final ArrayList<Location> locations;

    public Motion(Movable obj, ArrayList<Location> path, int effectiveLength) {
        length = effectiveLength;
        locations = path;
        object = obj;
    }

    public Location next() {
        return locations.get(step++);
    }

    public boolean isDone() {
        return step == locations.size();
    }

    public int getSize() { return length; }

    public Movable getObject() { return object; }
}
