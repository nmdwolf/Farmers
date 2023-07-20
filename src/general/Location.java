package general;

public record Location(int x, int y, int z) {

    public int distanceTo(Location location) {
        return Math.abs(x - location.x()) + Math.abs(y - location.y()) + Math.abs(z - location.z());
    }

    public Location negative() {
        return new Location(-x, -y, -z);
    }

    public Location add(int x, int y, int z) {
        return new Location(this.x + x, this.y + y, this.z + z);
    }

    public Location add(Location loc) {
        return new Location(this.x + loc.x, this.y + loc.y, this.z + loc.z);
    }
}
