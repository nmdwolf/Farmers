package UI;

import core.Property;
import java.awt.*;

import static core.GameConstants.*;

public class Settings {

    private final Property<Boolean> playMusic, shuffleMusic, showArrows;
    private String audioSource;
    private boolean customCursor, rangedMode;
    private GameFrame frame;
    private int screenWidth;
    private int screenHeight;
    private float cellWidth;
    private float cellHeight;
    private int spriteSize;

    private Thread audioThread;
    private DJ dj;

    public Settings() {
        showArrows = new Property<>(SHOW_CELL_ARROWS);
        playMusic = new Property<>(PLAY_MUSIC);
        shuffleMusic = new Property<>(SHUFFLE_MUSIC);
        audioSource = MUSIC_FOLDER;
        customCursor = CUSTOM_CURSOR;
        screenWidth = INITIAL_SCREEN_SIZE;
        screenHeight = INITIAL_SCREEN_SIZE;
        cellWidth = Math.round(screenWidth / (float)NUMBER_OF_CELLS_IN_VIEW);
        cellHeight = Math.round(screenHeight / (float)NUMBER_OF_CELLS_IN_VIEW);

        rangedMode = false;
    }

    public void initialize(GameFrame frame) {
        this.frame = frame;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(dj != null)
                dj.closeStream();
        }));

        toggleCursor(customCursor);
        setAudioSource(audioSource);
    }

    public void toggleCursor(boolean flag) {
        customCursor = flag;

        if(customCursor)
            frame.setCustomCursor();
        else
            frame.setCursor(Cursor.getDefaultCursor());
    }

    public boolean customCursor() {
        return customCursor;
    }

    public void toggleMusic(boolean flag) {
        playMusic.set(flag);
    }

    public boolean playMusic() {
        return playMusic.get();
    }

    public void toggleShuffle(boolean flag) {
        shuffleMusic.set(flag);
    }

    public boolean isShuffled() {
        return shuffleMusic.get();
    }

    public void toggleArrows(boolean flag) {
        showArrows.set(flag);
    }

    public boolean showArrows() {
        return showArrows.get();
    }

    public void setAudioSource(String src) {
        audioSource = src;

        if (audioThread != null) {
            audioThread.interrupt();
            dj.closeStream();
        }

        if(playMusic()) {
            dj = new DJ(audioSource, isShuffled());
            audioThread = new Thread(dj);
            audioThread.start();
        }
    }

    public String getAudioSource() {
        return audioSource;
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

    public boolean getRangedMode() {
        return rangedMode;
    }

    public void setRangedMode(boolean mode) {
        rangedMode = mode;
    }
}
