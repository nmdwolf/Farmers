package core;

import UI.CustomMethods;
import UI.Sprite;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class InternalSettings {

    /**
     * Number of cells along each axis.
     */
    public static final int NUMBER_OF_CELLS = 100;
    /**
     * Number of cells along each axis in the visible part of the map.
     */
    public static final int NUMBER_OF_CELLS_IN_VIEW = 8;
    /**
     * Initial size of the game screen during startup.
     * Should be nonzero to avoid errors.
     */
    public static final int INITIAL_SCREEN_SIZE = 500;
    /**
     * Initial cycle in the game loop.
     */
    public static final int START_CYCLE = 1;
    /**
     * Unit type identifier.
     */
    public static final int UNIT_TYPE = 0;
    /**
     * Building type identifier.
     */
    public static final int BUILDING_TYPE = 1;
    /**
     * Identifier for alternative object types.
     */
    public static final int OTHER_TYPE = 2;
    /**
     * Initial unit slots for cells.
     */
    public static final int INITIAL_CELL_UNIT_SPACE = 5;
    /**
     * Initial building slots for cells.
     */
    public static final int INITIAL_CELL_BUILDING_SPACE = 5;
    /**
     * Initial travel cost for empty cells.
     */
    public static final int INITIAL_TRAVEL_COST = 1;
    public static final int BOOSTER_DISTANCE = 2;
    public static final int SEASON_LENGTH = 10;
    public static final int INITIAL_HEAT_LEVEL = 5;
    public static final int COLD_LEVEL = 2;
    public static final int HOT_LEVEL = 10;
    public static final int SPRITE_SIZE = 15;
    public static final int FORGET_CELL_THRESHOLD = 10;

    public static final int SPRITE_SIZE_MAX = 50;
    public static final int CELL_X_MARGIN = 5;
    public static final int CELL_Y_MARGIN = 10;
    public static final int STROKE_WIDTH = 2;
    public static final int WATER_THRESHOLD = 150;
    public static final int WOOD_THRESHOLD = 150;
    public static final int FOOD_THRESHOLD = 150;
    public static final int ROAD_THRESHOLD = 5;
    public static final int START_POP_CAP = 100;
    public static final int START_FOOD = 300;
    public static final int START_WOOD = 100;
    public static final int START_WATER = 200;
    public static final int START_STONE = 50;
    public static final int START_IRON = 0;
    public static final int START_COAL = 10;


    //Random rand = new Random(19970605);
    public static final Random rand = new Random(19960808);
    public static final boolean GAME_3D = false;
    public static final boolean SHUFFLE_MUSIC = true;
    public static final boolean PLAY_MUSIC = false;
    public static final boolean SHOW_CELL_ARROWS = true;
    public static final boolean CUSTOM_CURSOR = true;
    public static final int FPS = 30;
    public static final String MUSIC_FOLDER = "src/music/FAVA - Lifetracks/";
    public static final int SECONDS_PER_ANIMATION = 4;
    public static final Color GRAY = new Color(210, 210, 210);
    public static final String[] PLAYER_COLORS = new String[]{"Blue", "Green", "Yellow"};
    public static final BufferedImage COBBLESTONE = Sprite.loadSprite("src/img/Cobblestone.png", 40, 20).orElseThrow();
    public static final BufferedImage RIVER = Sprite.loadSprite("src/img/WaterTexture.png", 85, 120).orElseThrow();
    public static final BufferedImage CHECK = Sprite.loadSprite("src/img/Check.png", 150, 150).orElseThrow();
    public static final BufferedImage ARROWHEAD = Sprite.loadSprite("src/img/Arrowhead.png",
            SPRITE_SIZE, SPRITE_SIZE).orElseThrow();
    public static final BufferedImage SUN = Sprite.loadSprite("src/img/sun.png", SPRITE_SIZE,
            SPRITE_SIZE).orElseThrow();
    public static final BufferedImage CLOUD = Sprite.loadSprite("src/img/cloud.png", SPRITE_SIZE * 2, SPRITE_SIZE).orElseThrow();


    private int screenWidth;
    private int screenHeight;
    private float cellWidth;
    private float cellHeight;
    private int spriteSize, textureStep;

    private BufferedImage waterTexture;

    public InternalSettings() {
        screenWidth = INITIAL_SCREEN_SIZE;
        screenHeight = INITIAL_SCREEN_SIZE;
        cellWidth = Math.round(screenWidth / (float)NUMBER_OF_CELLS_IN_VIEW);
        cellHeight = Math.round(screenHeight / (float)NUMBER_OF_CELLS_IN_VIEW);

        waterTexture = RIVER;
    }

    public float getCellHeight() {
        return cellHeight;
    }

    public float getCellWidth() {
        return cellWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
        cellHeight = screenHeight / (float)NUMBER_OF_CELLS_IN_VIEW;
        setSpriteSize();
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
        cellWidth = screenWidth / (float)NUMBER_OF_CELLS_IN_VIEW;
        setSpriteSize();
    }

    public float getPoolSize() {
        return Math.min(Math.round(cellWidth / 2f), Math.round(cellHeight / 2f));
    }

    private void setSpriteSize() {
        spriteSize = (int)Math.min(cellWidth, cellHeight) / 2;
    }

    public int getSpriteSize() {
        return spriteSize;
    }

    public void cycleTextures() {
        int steps = 40;
        textureStep = (textureStep + 1) % steps;

        int min = RIVER.getWidth();
        int max = RIVER.getHeight();
        waterTexture = new BufferedImage(min, max / 3 * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = CustomMethods.optimizeGraphics(waterTexture.createGraphics());
        gr.drawImage(RIVER, 0, -textureStep * max / (3 * steps), null);
        gr.dispose();
    }

    public BufferedImage getWaterTexture(int cycle) {
        return CustomMethods.rotateImage(waterTexture, (cycle % 4) * Math.PI / 2);
    }
}
