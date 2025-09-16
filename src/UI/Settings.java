package UI;

import core.Property;

import java.awt.*;

import static core.GameConstants.*;

public class Settings {

    private final Property<Boolean> playMusic, shuffleMusic, showArrows;
    private String audioSource;
    private boolean customCursor;
    private GameFrame frame;

    private Thread audioThread;
    private DJ dj;

    public Settings() {
        showArrows = new Property<>(SHOW_CELL_ARROWS);
        playMusic = new Property<>(PLAY_MUSIC);
        shuffleMusic = new Property<>(SHUFFLE_MUSIC);
        audioSource = MUSIC_FOLDER;
        customCursor = CUSTOM_CURSOR;
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
        return playMusic.getUnsafe();
    }

    public void toggleShuffle(boolean flag) {
        shuffleMusic.set(flag);
    }

    public boolean isShuffled() {
        return shuffleMusic.getUnsafe();
    }

    public void toggleArrows(boolean flag) {
        showArrows.set(flag);
    }

    public boolean showArrows() {
        return showArrows.getUnsafe();
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
}
