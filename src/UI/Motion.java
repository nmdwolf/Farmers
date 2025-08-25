package UI;

import core.Location;
import objects.units.Unit;

import java.util.ArrayList;

public class Motion {

    private int step;

    private final int length;
    private final Unit object;
    private final ArrayList<Location> locations;
    private Location current;

    public Motion(Unit obj, ArrayList<Location> path, int effectiveLength) throws IllegalArgumentException {
        if(path.size() > 1 && path.get(0) == path.get(1))
            throw new IllegalArgumentException("Path should not contain starting location.");

        length = effectiveLength;
        object = obj;
        step = 0;

        locations = path;
        locations.addFirst(obj.getCell().getLocation());
        current = locations.getFirst();
    }

    public Location next() {
        current = current.add(locations.get(++step));
        return locations.get(step);
    }

    public Location current() { return current; }

    public boolean isDone() {
        return step >= locations.size()-1;
    }

    public int length() { return length; }

    public Unit getObject() { return object; }

    public Location[] getPath() {
        Location[] totalPath = new Location[locations.size()];
        totalPath[0] = locations.getFirst();
        for(int i = 1; i < locations.size(); i++)
            totalPath[i] = totalPath[i - 1].add(locations.get(i));
        return totalPath;
    }

    public Location[] getRelativePath() {
        return locations.toArray(new Location[0]);
    }

    public Location[] getRemainingRelativePath() {
        if(step == 0)
            return getRelativePath();

        Location[] totalPath = new Location[locations.size() - step];
        totalPath[0] = current;
        for(int i = 1; i < totalPath.length; i++)
            totalPath[i] = locations.get(i + step);

        return totalPath;
    }
}
