//
// $Id: SoundManager.java,v 1.5 2002/08/02 01:45:19 shaper Exp $

package com.threerings.media;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;

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
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.StreamUtils;

import com.samskivert.util.Queue;
import com.samskivert.util.Tuple;

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
                while (amRunning()) {
                    // wait for a sound
                    _clips.waitForItem();

                    // play the sound
                    Object o = _clips.get();
                    if (o instanceof String) {
                        playSound((String) o);
                    }
                }
                Log.debug("SoundManager exit.");
            }
        };
        _player.setDaemon(true);
        _player.start();
    }

    /**
     * Shut the damn thing off.
     */
    public synchronized void shutdown ()
    {
        _player = null;
        _clips.append(this); // signal death
    }

    /**
     * Used by the sound playing thread to determine whether or not to
     * shut down.
     */
    protected synchronized boolean amRunning ()
    {
        return (_player == Thread.currentThread());
    }

    /**
     * Queues up the sound file with the given pathname to be played when
     * the sound manager deems the time appropriate.
     */
    public void play (String path)
    {
        if (_player != null) {
            // Log.debug("Queueing sound [path=" + path + "].");
            _clips.append(path);
        }
    }

    /**
     * Plays the sound file with the given pathname.
     */
    protected void playSound (String path)
    {
        // Log.debug("Playing sound [path=" + path + "].");

        try {
            // get the audio input stream
            Tuple tup = getAudioInfo(path);
            AudioFormat format = (AudioFormat)tup.left;
            byte[] data = (byte[])tup.right;

            // get the audio output line
            Clip clip = (Clip)getLine(Clip.class, format, data.length);
            if (clip != null) {
                // listen to our audio antics
                clip.addLineListener(this);

                // and start the clip playing
                clip.open(format, data, 0, data.length);
                clip.start();
            }

        } catch (LineUnavailableException lue) {
            Log.warning("Line unavailable, shutting down [lue=" + lue + "].");
            shutdown();

        } catch (Exception e) {
            Log.warning("Failed to play sound [path=" + path +
                        ", e=" + e + "].");
            Log.logStackTrace(e);
        }
    }

    /**
     * Returns a tuple detailing the audio file format and raw audio data
     * for the given sound file, retrieving the relevant information from
     * the cache if already present, or loading it into the cache if not.
     */
    protected Tuple getAudioInfo (String path)
        throws IOException, UnsupportedAudioFileException
    {
        // try to retrieve the audio information from the cache
        Tuple tup = (Tuple)_data.get(path);
        if (tup != null) {
            return tup;
        }

        // get the audio input stream
        InputStream in = _rmgr.getResource(path);
        // AudioInputStream ais = AudioSystem.getAudioInputStream(in);
        // ais = getPCMAudioStream(ais);

        // AudioFormat format = ais.getFormat();

        // note that we have to explicitly specify the file format here
        // because using AudioSystem.getAudioInputStream() or any of the
        // various other similar methods to determine the audio format
        // from the file data requires that the supplied input stream
        // support mark()/reset(), and though this works happily in the
        // local client, it works not at all when running after launching
        // from Java Web Start.  we'll doubtless revisit this later; at
        // the least, to update the audio format to whatever we eventually
        // decide upon, and at best, to fix this so that we can properly
        // determine the file format and support automagically playing
        // different formats.
        AudioFormat format = new AudioFormat(8000, 8, 1, false, false);
//         Log.debug("Obtained stream [format=" + format +
//                   ", encoding=" + format.getEncoding() +
//                   ", rate=" + format.getSampleRate() +
//                   ", frameSize=" + format.getFrameSize() +
//                   ", bits=" + format.getSampleSizeInBits() +
//                   ", channels=" + format.getChannels() +
//                   ", isBigEndian=" + format.isBigEndian() + "].");

        // read in all of the data
        byte[] data = StreamUtils.streamAsBytes(in, BUFFER_SIZE);

        // cache the data
        tup = new Tuple(format, data);
        _data.put(path, tup);
        return tup;
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

            // Log.debug("Finished playing sound [clip=" + clip + "].");
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
    protected Line getLine (Class clazz, AudioFormat format, int bufferSize)
    {
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

    /** The default mixer. */
    protected Mixer _mixer;

    /** The thread that plays sounds. */
    protected Thread _player;

    /** The queue of sound clips to be played. */
    protected Queue _clips = new Queue();

    /** The cached audio file data. */
    protected HashMap _data = new HashMap();

    /** The buffer size in bytes used when reading audio file data. */
    protected static final int BUFFER_SIZE = 2048;
}
