package items;

import core.Location;

public interface Movable extends Depletable {
    void setLocation(Location loc);
}
