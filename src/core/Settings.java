package core;

import UI.DJ;
import UI.GameFrame;

import javax.swing.*;
import java.awt.*;

import static core.InternalSettings.SAVE;

public class Settings {

    private final Property<Boolean> playMusic, shuffleMusic, showArrows;
    private String audioSource;
    private boolean customCursor, rangedMode, save;
    private GameFrame frame;

    private Thread audioThread;
    private DJ dj;

    public Settings() {
        showArrows = new Property<>(InternalSettings.SHOW_CELL_ARROWS);
        playMusic = new Property<>(InternalSettings.PLAY_MUSIC);
        shuffleMusic = new Property<>(InternalSettings.SHUFFLE_MUSIC);
        audioSource = InternalSettings.MUSIC_FOLDER;
        customCursor = InternalSettings.CUSTOM_CURSOR;
        save = SAVE;
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

    public boolean save() {
        return save;
    }

    public void save(boolean save) {
        this.save = save;
    }

    public void toggleCursor(boolean flag) {
        customCursor = flag;

        if(customCursor) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension size = tk.getBestCursorSize(10, 10);
            Image img = new ImageIcon("src/img/Cursor.png").getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
            frame.setCursor(tk.createCustomCursor(img, new Point(size.width / 2, size.height / 2), "customCursor"));
        } else
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

    public boolean getRangedMode() {
        return rangedMode;
    }

    public void setRangedMode(boolean mode) {
        rangedMode = mode;
    }

}
