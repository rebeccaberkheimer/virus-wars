package edu.commonwealthu.viruswars;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * Provides game related sound effects
 *
 * @author Rebecca Berkheimer
 */
public class SoundManager {
    private SoundPool soundPool;
    private boolean soundEnabled = true;
    private final int startSound;  // start a new game
    private final int moveSound; // valid move
    private final int errorSound;   // invalid move
    private final int undoSound;   // take back a move
    private final int winSound;    // puzzle solved

    /**
     * Initializes a new sound manager for a given context.
     */
    public SoundManager(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(3)
                .setAudioAttributes(audioAttributes).build();
        startSound = soundPool.load(context, R.raw.start, 1);
        moveSound = soundPool.load(context, R.raw.move, 1);
        errorSound = soundPool.load(context, R.raw.error, 1);
        winSound = soundPool.load(context, R.raw.win, 1);
        undoSound = soundPool.load(context, R.raw.betterundo, 1);
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void toggleSoundEnabled() {
        soundEnabled = !soundEnabled;
    }

    /**
     * Releases all memory and resources used by the SoundPool.
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    public void playStartSound() {
        play(startSound);
    }

    public void playMoveSound() {
        play(moveSound);
    }

    public void playWinSound() {
        play(winSound);
    }

    public void playErrorSound() {
        play(errorSound);
    }

    public void playUndoSound() {
        play(undoSound);
    }

    /**
     * Plays a sound specified by its resource ID.
     */
    private void play(int id) {
        if (soundEnabled && soundPool != null) {
            soundPool.play(id, 1, 1, 0, 0, 1);
        }
    }
}


