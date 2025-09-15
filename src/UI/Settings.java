package UI;

import core.Property;

import java.awt.*;

import static core.GameConstants.*;

public class Settings {

    private final Property<Boolean> cursorFlag, playMusic, shuffleMusic, showArrows;
    private final Property<String> audioSource;
    private GameFrame frame;

    private Thread audioThread;
    private DJ dj;

    public Settings() {

        audioSource = new Property<>(MUSIC_FOLDER);
        showArrows = new Property<>(SHOW_CELL_ARROWS);
        cursorFlag = new Property<>(CUSTOM_CURSOR);
        playMusic = new Property<>(PLAY_MUSIC);
        shuffleMusic = new Property<>(SHUFFLE_MUSIC);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(dj != null)
                dj.closeStream();
        }));
        setAudioSource(MUSIC_FOLDER);
    }

    public void setGameFrame(GameFrame frame) {
        this.frame = frame;
    }

    public void toggleCursor(boolean flag) {
        cursorFlag.set(flag);

        if(flag)
            frame.setCustomCursor();
        else
            frame.setCursor(Cursor.getDefaultCursor());
    }

    public boolean showCursor() {
        return cursorFlag.getUnsafe();
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
        audioSource.set(src);

        if (audioThread != null) {
            audioThread.interrupt();
            dj.closeStream();
        }

        if(playMusic()) {
            dj = new DJ(src, isShuffled());
            audioThread = new Thread(dj);
            audioThread.start();
        }
    }

    public String getAudioSource() {
        return audioSource.getUnsafe();
    }
}
