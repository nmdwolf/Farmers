package core;

import java.util.Random;

public interface GameConstants {

    int NUMBER_OF_CELLS = 100;
    int NUMBER_OF_CELLS_IN_VIEW = 10;
    int INITIAL_CELL_UNIT_SPACE = 2;
    int INITIAL_CELL_BUILDING_SPACE = 5;
    int INITIAL_TRAVEL_COST = 1;
    int BOOSTER_DISTANCE = 2;
    int SEASON_LENGTH = 10;
    int INITIAL_HEAT_LEVEL = 5;
    int COLD_LEVEL = 2;
    int HOT_LEVEL = 10;

    int UNIT_SPRITE_SIZE = 15;
    int BUILDING_SPRITE_SIZE = 15;

    int WATER_THRESHOLD = 150;
    int WOOD_THRESHOLD = 150;
    int FOOD_THRESHOLD = 150;

    int START_POP_CAP = 100;
    int START_FOOD = 300;
    int START_WOOD = 100;
    int START_WATER = 200;
    int START_STONE = 50;
    int START_IRON = 0;
    int START_COAL = 10;

    //Random rand = new Random(19970605);
    Random rand = new Random(19960808);

    boolean GAME_3D = false;
}
