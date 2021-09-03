package items;

import java.util.HashMap;
import java.util.Random;

public class Cell {

    public final static int FOOD = 0;
    public final static int WOOD = 1;
    public final static int WATER = 2;
    public final static int STONE = 3;
    public final static int IRON = 4;
    public final static int COAL = 5;

    public final static int WATER_THRESHOLD = 150;

    public final static Random rand = new Random(123);

    private int space, occupied;
    private HashMap<Integer, Integer> resources;

    public Cell(int s) {
        occupied = 0;
        space = s;
        resources = generateResources();
    }

    public int getOccupied() {
        return occupied;
    }

    public int getSpace() {
        return space;
    }

    public int getResource(int type) {
        return resources.get(type);
    }

    public void changeResource(int type, int amount) {
        resources.put(type, resources.get(type) + amount);
    }

    public static  HashMap<Integer, Integer> generateResources() {
        HashMap<Integer,Integer> resources = new HashMap<>();
        resources.put(FOOD, rand.nextInt(100));
        resources.put(WOOD, rand.nextInt(100));
        resources.put(STONE, rand.nextInt(100));
        resources.put(IRON, rand.nextInt(50));
        resources.put(COAL, rand.nextInt(50));

        resources.put(WATER, rand.nextInt(200));
        if(resources.get(WATER) > WATER_THRESHOLD) {
            resources.put(WATER, 50000);
        }
        return resources;
    }
}
