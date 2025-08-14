package core;

public class EndOfMap extends Cell {

    public EndOfMap(int x, int y, int z) {
        super(x, y, z, 0, 0);
    }

    public Cell fetch(int x, int y, int z) {
        return this;
    }

    public Cell fetch(Cell cell) {

        System.out.println("End of world:" + this.getLocation());
        return this;
    }

    public void link(Cell cell) {};
}
