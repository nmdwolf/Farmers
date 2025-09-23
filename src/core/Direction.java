package core;


public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    public Direction opposite() {
        return switch(this) {
            case NORTH -> SOUTH;
            case EAST -> WEST;
            case SOUTH -> NORTH;
            case WEST -> EAST;
        };
    }

    /**
     * Determines the direction of the vector going between two {@code Location}s.
     * @param l1 source location
     * @param l2 target location
     * @return direction
     */
    public static Direction toDirection(Location l1, Location l2) {
        Location diff = l2.add(l1.negative());
        if(Location.distance(l1, l2) != 1)
            throw new IllegalArgumentException("The provided cells are not direct neighbours.");
        else return toDirection(diff.x(), diff.y(), diff.z());
    }

    public static Direction toDirection(int x, int y, int z) {
        if(Math.abs(x) + Math.abs(y) + Math.abs(z) != 1)
            throw new IllegalArgumentException("The provided difference does not characterize direct neighbours.");
        else if(x == 0) {
            if(y == 1)
                return NORTH;
            else
                return SOUTH;
        } else {
            if(x == 1)
                return EAST;
            else
                return WEST;
        }
    }

    public static Location toDisplacement(Direction dir) {
        return switch(dir) {
            case NORTH -> new Location(0, 1, 0);
            case EAST -> new Location(1, 0, 0);
            case SOUTH -> new Location(0, -1, 0);
            case WEST -> new Location(-1, 0, 0);
        };
    }
}
