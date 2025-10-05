package core;

import UI.Sprite;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public interface GameConstants {

    /**
     * Number of cells along each axis.
     */
    int NUMBER_OF_CELLS = 100;

    /**
     * Number of cells along each axis in the visible part of the map.
     */
    int NUMBER_OF_CELLS_IN_VIEW = 8;

    /**
     * Initial size of the game screen during startup.
     * Should be nonzero to avoid errors.
     */
    int INITIAL_SCREEN_SIZE = 500;

    /**
     * Initial cycle in the game loop.
     */
    int START_CYCLE = 1;

    /**
     * Unit type identifier.
     */
    int UNIT_TYPE = 0;

    /**
     * Building type identifier.
     */
    int BUILDING_TYPE = 1;

    /**
     * Identifier for alternative object types.
     */
    int OTHER_TYPE = 2;

    /**
     * Initial unit slots for cells.
     */
    int INITIAL_CELL_UNIT_SPACE = 5;

    /**
     * Initial building slots for cells.
     */
    int INITIAL_CELL_BUILDING_SPACE = 5;

    /**
     * Initial travel cost for empty cells.
     */
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
    int ROAD_THRESHOLD = 5;

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
    int FPS = 30;
    String MUSIC_FOLDER = "src/music/FAVA - Lifetracks/";
    int SECONDS_PER_ANIMATION = 4;

    Color GRAY = new Color(210, 210, 210);
    String[] PLAYER_COLORS = new String[]{"Blue", "Green", "Yellow"};

    BufferedImage CLOUD = Sprite.loadSprite("src/img/cloud.png", GameConstants.SPRITE_SIZE * 2, GameConstants.SPRITE_SIZE).orElseThrow();
    BufferedImage SUN = Sprite.loadSprite("src/img/sun.png", GameConstants.SPRITE_SIZE,
            GameConstants.SPRITE_SIZE).orElseThrow();
    BufferedImage ARROWHEAD = Sprite.loadSprite("src/img/Arrowhead.png",
            GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE).orElseThrow();
    BufferedImage COBBLESTONE = Sprite.loadSprite("src/img/Cobblestone.png", 40, 20).orElseThrow();
    BufferedImage RIVER = Sprite.loadSprite("src/img/WaterTexture.png", 90, 90).orElseThrow();
    BufferedImage CHECK = Sprite.loadSprite("src/img/Check.png", 150, 150).orElseThrow();
}
