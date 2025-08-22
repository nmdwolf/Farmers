package objects.buildings;

public interface Directional {

    enum Direction { NORTH, SOUTH, EAST, WEST; }

    Direction getDirection();

}
