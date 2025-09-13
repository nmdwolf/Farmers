package UI;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DJ implements Runnable{

    private static final int BUFFER_SIZE = 4096;

    private final String src;
    private final boolean shuffle;
    private SourceDataLine line;
    private AudioInputStream stream;
    private boolean running;

    public DJ(String folder, boolean shuffle) {
        src = folder;
        this.shuffle = shuffle;
    }

    public void run() {
        running = true;
        try {
            File dir = new File(src);
            if(dir.exists()) {
                if (dir.isDirectory()) {
                    List<File> files = Arrays.asList(Objects.requireNonNull(dir.listFiles()));
                    if (shuffle)
                        Collections.shuffle(files);
                    for (File file : files.stream().filter(obj -> obj.getName().endsWith("wav")).toList()) {
                        stream = AudioSystem.getAudioInputStream(file);
                        AudioFormat format = stream.getFormat();
                        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                        line = (SourceDataLine) AudioSystem.getLine(info);
                        line.open(format);
                        line.start();

                        byte[] bufferBytes = new byte[BUFFER_SIZE];
                        int readBytes = -1;
                        while (running && (readBytes = stream.read(bufferBytes)) != -1)
                            line.write(bufferBytes, 0, readBytes);

                        if (running) {
                            line.drain();
                            line.close();
                            stream.close();
                        }
                    }
                }
            } else
                throw new IllegalArgumentException("Specified folder does not exist: " + src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void closeStream() {
        try {
            running = false;
            if(line != null) {
                line.stop();
                line.flush();
                line.close();
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
