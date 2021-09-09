package core;

public class Location {

    public final int x, y, z;

    public Location(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int distanceTo(Location loc) {
        return Math.abs(x - loc.x) + Math.abs(y - loc.y) + Math.abs(z - loc.z);
    }

    public Location add(Location loc) {
        return new Location(x + loc.x, y + loc.y, z + loc.z);
    }

    public Location subtract(Location loc) {
        return new Location(x - loc.x, y - loc.y, z - loc.z);
    }

    public Location add(int xIncr, int yIncr, int zIncr) {
        return new Location(x + xIncr, y + yIncr, z + zIncr);
    }

    @Override
    public int hashCode() {
        return z * 1000_000 + x * 1000 + y;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Location) {
            Location l = (Location)obj;
            return (x == l.x) && (y == l.y) && (z == l.z);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Location(" + x + "," + y + "," + z + ")";
    }
}
