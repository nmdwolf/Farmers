package items;

import core.Location;

import java.util.List;

public interface Constructor extends Worker{

    List<Constructable> getProducts();

    Location getConstructionLocation();
    void setConstructionLocation(Location loc);
}
