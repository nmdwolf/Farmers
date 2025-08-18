package UI;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DJ implements Runnable{

    private static final int BUFFER_SIZE = 4096;

    private String src;
    private SourceDataLine line;
    private AudioInputStream stream;
    private boolean running;

    public DJ(String folder) {
        src = folder;
    }

    public void run() {
        running = true;
        try {
            File dir = new File(src);
            if(dir.isDirectory()) {
                for (File file : Arrays.stream(dir.listFiles()).filter(obj -> obj.getName().endsWith("wav")).toList()){
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

                    if(running) {
                        line.drain();
                        line.close();
                        stream.close();
                    }
                }
            }
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
