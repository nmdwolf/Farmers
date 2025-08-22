package UI;

import core.Location;
import objects.units.Unit;

import java.util.ArrayList;

public class Motion {

    private int step;

    private final int length;
    private final Unit object;
    private final ArrayList<Location> locations;

    public Motion(Unit obj, ArrayList<Location> path, int effectiveLength) {
        length = effectiveLength;
        locations = path;
        locations.addFirst(obj.getCell().getLocation());
        if(path.size() > 1 && path.get(0) == path.get(1))
            throw new IllegalArgumentException("Path should not contain starting location.");
        object = obj;
        step = 0;
    }

    public Location next() {
        return locations.get(step++);
    }

    public boolean isDone() {
        return step >= locations.size();
    }

    public int length() { return length; }

    public Unit getObject() { return object; }

    public Location[] getPath() {
        Location[] totalPath = new Location[locations.size()];
        totalPath[0] = locations.getFirst();
        for(int i = 1; i <= locations.size(); i++)
            totalPath[i] = totalPath[i - 1].add(locations.get(i));
        return totalPath;
    }

    // TODO Find out what this does or why it's useful
    public Location[] getRelativePath() {
        return locations.toArray(new Location[0]);
    }

}
