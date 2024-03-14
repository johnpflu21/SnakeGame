import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SoundBoard  {
    Clip clip;

    void play() {
        try {
        File f = new File("SnakeGame/Sounds/click.wav");
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
        clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
        }
        catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    void die() {
        try {
            File d = new File("SnakeGame/Sounds/roblox-oof-gamespecifications.com_.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(d.toURI().toURL());
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        }
        catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
