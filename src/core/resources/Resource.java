package core.resources;

import java.util.Objects;

public class Resource {

    public final static Resource TIME = new Resource("Time");
    public final static Resource FOOD = new Resource("Food");
    public final static Resource WATER = new Resource("Water");
    public final static Resource WOOD = new Resource("Wood");
    public final static Resource STONE = new Resource("Stone");
    public final static Resource IRON = new Resource("Iron");
    public final static Resource COAL = new Resource("Coal");

    private static int ID_COUNTER = 0;

    private final int id;
    private final String name;

    public Resource(String name) {
        id = ID_COUNTER++;
        this.name = name;
    }

    public int getID() { return id; }

    public String getName() { return name; }

    @Override
    public String toString() { return getName(); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Resource res)
            return res.getID() == id;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
