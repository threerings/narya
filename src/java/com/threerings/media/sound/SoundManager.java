//
// $Id: SoundManager.java,v 1.1 2002/07/24 21:28:09 shaper Exp $

package com.threerings.media;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import com.samskivert.util.Queue;

import com.threerings.resource.ResourceManager;

import com.threerings.media.Log;

/**
 * Manages the playing of audio files.
 */
public class SoundManager
    implements LineListener
{
    /**
     * Constructs a sound manager.
     */
    public SoundManager (ResourceManager rmgr)
    {
        // save things off
        _rmgr = rmgr;

        // obtain the mixer
        _mixer = AudioSystem.getMixer(null);
        if (_mixer == null) {
            Log.warning("Can't get default mixer, aborting audio " +
                        "initialization.");
            return;
        }

        // create a thread to manage the sound queue
        _player = new Thread() {
            public void run () {
                while (true) {
                    // wait for a sound
                    _clips.waitForItem();

                    // play the sound
                    playSound((String)_clips.get());
                }
            }
        };
        _player.setDaemon(true);
        _player.start();
    }

    /**
     * Queues up the sound file with the given pathname to be played when
     * the sound manager deems the time appropriate.
     */
    public void play (String path)
    {
        Log.debug("Queueing sound [path=" + path + "].");
        _clips.append(path);
    }

    /**
     * Plays the sound file with the given pathname.
     */
    protected void playSound (String path)
    {
        Log.debug("Playing sound [path=" + path + "].");

        // get the resource input stream
        InputStream in = null;
        try {
            in = _rmgr.getResource(path);
        } catch (IOException ioe) {
            Log.warning("Failed to obtain resource input stream " +
                        "[path=" + path + ", ioe=" + ioe + "].");
            return;
        }
        
        try {
            // get the audio input stream and format
            AudioInputStream ais = AudioSystem.getAudioInputStream(in);
            // ais = getPCMAudioStream(ais);
            AudioFormat format = ais.getFormat();
            Log.debug("Obtained stream [format=" + format + "].");

            // get the audio output line
            Clip clip = (Clip)getLine(Clip.class, ais);
            if (clip != null) {
                // listen to our audio antics
                clip.addLineListener(this);

                // and start the clip playing
                clip.open(ais);
                clip.start();
            }

        } catch (Exception e) {
            Log.warning("Failed to play sound [path=" + path +
                        ", e=" + e + "].");
            Log.logStackTrace(e);
        }
    }

    // documentation inherited
    public void update (LineEvent event)
    {
        // Log.info("LineListener update [event=" + event + "].");
        Clip clip = (Clip)event.getLine();
        if (event.getType() == LineEvent.Type.STOP) {
            // shut the clip down
//             clip.stop();
//             clip.close();

            Log.debug("Finished playing sound [clip=" + clip + "].");
        }
    }

    /**
     * Returns an audio stream from the given audio stream with the sound
     * data converted to a <code>PCM_SIGNED</code> format playable in JDK
     * 1.4-beta3.
     */
    protected AudioInputStream getPCMAudioStream (AudioInputStream ais)
    {
        AudioFormat format = ais.getFormat();
        AudioFormat pcmFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            format.getSampleRate(),
            format.getSampleSizeInBits() * 2,
            format.getChannels(),
            format.getFrameSize() * 2,
            format.getFrameRate(),
            true);
        return AudioSystem.getAudioInputStream(pcmFormat, ais);
    }

    /**
     * Returns a line suitable for playing the audio in the given stream,
     * or <code>null</code> if an error occurred.
     */
    protected Line getLine (Class clazz, AudioInputStream ais)
    {
        AudioFormat format = ais.getFormat();
        int bufferSize = (int)(ais.getFrameLength() * format.getFrameSize());
        DataLine.Info info = new DataLine.Info(clazz, format, bufferSize);
        if (!AudioSystem.isLineSupported(info)) {
            Log.warning("Audio line for clip playback not supported " +
                        "[format=" + format + "].");
            return null;
        }

        // Log.info("Checking available lines " +
        // "[num=" + _mixer.getMaxLines(info) + "].");

        // obtain a reference to the line
        Line line;
        try {
            line = AudioSystem.getLine(info);
            // line.open(format);
        } catch (LineUnavailableException lue) {
            Log.warning("Failed to open audio line [lue=" + lue + "].");
            return null;
        }
        return line;
    }

    /** The resource manager from which we obtain audio files. */
    protected ResourceManager _rmgr;

    /** The thread that plays sounds. */
    protected Thread _player;

    /** The queue of sound clips to be played. */
    protected Queue _clips = new Queue();

    /** The default mixer. */
    protected Mixer _mixer;
}
