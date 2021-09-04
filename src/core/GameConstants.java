package core;

public interface GameConstants {
    int NUMBER_OF_CELLS = 10;
    int INITIAL_CELL_UNIT_SPACE = 2;
    int INITIAL_CELL_BUILDING_SPACE = 5;
    int WATER_THRESHOLD = 150;

    int VIEW_KEY = -1;
    int X_KEY = 0;
    int Y_KEY = 1;
    int HEALTH_KEY = 2;
    int STATUS_KEY = 3;
    int SPACE_KEY = 4;
    int SIZE_KEY = 5;
    int ENERGY_KEY = 6;

    int FOUNDATION = 1000;
    int CONSTRUCTION = 1001;
    int BUILT = 1002;

    int ACTIVE = 1000;
    int PASSIVE = 1001;
    int DEFENSIVE = 1002;

    int FOOD = 0;
    int WATER = 1;
    int WOOD = 2;
    int STONE = 3;
    int IRON = 4;
    int COAL = 5;

    String[] RESOURCE_LABELS = {"Food", "Water", "Wood", "Stone", "Iron", "Coal"};

    int LABOR = 0;
}
