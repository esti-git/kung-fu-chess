package audio;

import events.EventBus;
import events.GameEndedEvent;
import events.GameStartedEvent;
import events.MoveMadeEvent;
import events.PieceCapturedEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

/**
 * Subscribes to move/capture/game-start/game-end events and plays the matching sound effect.
 * Game logic never calls into this class directly - it only ever reacts to events on the bus.
 *
 * Sound files are optional: if assets/sounds/&lt;name&gt;.wav is missing, the clip is skipped and
 * a message is logged instead of failing, so the game runs fine before real audio assets exist.
 */
public class SoundManager {

    private static final String SOUNDS_DIR = "assets/sounds/";

    public SoundManager(EventBus eventBus) {
        eventBus.subscribe(MoveMadeEvent.TYPE, event -> play("move"));
        eventBus.subscribe(PieceCapturedEvent.TYPE, event -> play("capture"));
        eventBus.subscribe(GameStartedEvent.TYPE, event -> play("start"));
        eventBus.subscribe(GameEndedEvent.TYPE, event -> play("gameOver"));
    }

    private void play(String soundName) {
        File file = new File(SOUNDS_DIR + soundName + ".wav");
        if (!file.isFile()) {
            System.out.println("[SoundManager] No sound file at " + file.getPath() + ", skipping playback.");
            return;
        }

        try (AudioInputStream stream = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            clip.start();
        } catch (Exception e) {
            System.out.println("[SoundManager] Failed to play " + file.getPath() + ": " + e.getMessage());
        }
    }
}
