package objects.resources;
public enum Resource {

    FOOD("Food"), WATER("Water"), WOOD("Wood"),
    STONE("Stone"), IRON("Iron"), COAL("Coal"),
    TIME("Time");

    public final String name;

    Resource(String n) {
        name = n;
    }

}
