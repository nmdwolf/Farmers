package core;

public interface GameConstants {

    int NUMBER_OF_CELLS = 100;
    int NUMBER_OF_CELLS_IN_VIEW = 10;
    int INITIAL_CELL_UNIT_SPACE = 2;
    int INITIAL_CELL_BUILDING_SPACE = 5;
    int WATER_THRESHOLD = 150;
    int WOOD_THRESHOLD = 150;
    int INITIAL_TRAVEL_COST = 1;

    int START_POP_CAP = 100;
    int START_FOOD = 300;
    int START_WOOD = 100;
    int START_WATER = 200;
    int START_STONE = 50;
    int START_IRON = 0;
    int START_COAL = 10;

    int FOUNDATION_KEY = 1000;
    int CONSTRUCTION_KEY = 1001;
    int BUILT_KEY = 1002;

    int ACTIVE_KEY = 1000;
    int PASSIVE_KEY = 1001;
    int DEFENSIVE_KEY = 1002;

    int LABOR_KEY = 0;

    int UNIT_TYPE = 5000;
    int BUILDING_TYPE = 5001;
    int SOURCE_TYPE = 5003;
    int DEPLETABLE_TYPE = 5004;
    int CONSTRUCTABLE_TYPE = 5005;
    int DESTROYABLE_TYPE = 5006;
    int CONSTRUCTOR_TYPE = 5007;
    int WORKER_TYPE = 5008;
    int MOVABLE_TYPE = 5009;
    int UPGRADER_TYPE = 5010;
    int OBSTRUCTION_TYPE = 5011;
}
