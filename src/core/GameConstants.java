package core;

import UI.CustomMethods;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public interface GameConstants {

    int NUMBER_OF_CELLS = 100;
    int NUMBER_OF_CELLS_IN_VIEW = 8;
    int INITIAL_SCREEN_SIZE = 500;

    int UNIT_TYPE = 0;
    int BUILDING_TYPE = 1;
    int OTHER_TYPE = 2;

    int INITIAL_CELL_UNIT_SPACE = 5;
    int INITIAL_CELL_BUILDING_SPACE = 5;
    int INITIAL_TRAVEL_COST = 1;
    int BOOSTER_DISTANCE = 2;
    int SEASON_LENGTH = 10;
    int INITIAL_HEAT_LEVEL = 5;
    int COLD_LEVEL = 2;
    int HOT_LEVEL = 10;

    int SPRITE_SIZE = 15;
    int SPRITE_SIZE_MAX = 50;
    int CELL_X_MARGIN = 5;
    int CELL_Y_MARGIN = 10;
    int STROKE_WIDTH = 2;

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
    boolean SHUFFLE_MUSIC = true;
    boolean PLAY_MUSIC = false;
    boolean SHOW_CELL_ARROWS = true;
    boolean CUSTOM_CURSOR = true;

    Color GRAY = new Color(210, 210, 210);
    String[] PLAYER_COLORS = new String[]{"Blue", "Green", "Yellow"};

    BufferedImage CLOUD = CustomMethods.loadSprite("src/img/cloud.png", GameConstants.SPRITE_SIZE * 2, GameConstants.SPRITE_SIZE).get();
    BufferedImage SUN = CustomMethods.loadSprite("src/img/sun.png", GameConstants.SPRITE_SIZE,
            GameConstants.SPRITE_SIZE).get();
    BufferedImage ARROWHEAD = CustomMethods.loadSprite("src/img/Arrowhead.png",
            GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE).get();
}
