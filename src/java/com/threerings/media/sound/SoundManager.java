//
// $Id: SoundManager.java,v 1.7 2002/11/02 00:26:55 ray Exp $

package com.threerings.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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

import javax.swing.Timer;

import org.apache.commons.io.StreamUtils;

import com.samskivert.util.LRUHashMap;
import com.samskivert.util.Queue;

import com.threerings.resource.ResourceManager;

import com.threerings.media.Log;

/**
 * Manages the playing of audio files.
 */
// TODO:
// - volume for different sound types
//   Clip.getControl(FloatControl.Type.VOLUME);
// - either a limit to the queue length, or a way to put items
//   in the queue with a timestamp, and if they're processed after that
//   time, we cut them out.
// - looping a sound
public class SoundManager
{
    /**
     * Create instances of this for your application to differentiate
     * between different types of sounds.
     */
    public static class SoundType
    {
        public SoundType (String description)
        {
            _desc = description;
        }

        public String toString ()
        {
            return _desc;
        }

        protected String _desc;
    }

    /** The default sound type. */
    public static final SoundType DEFAULT = null;

    /**
     * Constructs a sound manager.
     */
    public SoundManager (ResourceManager rmgr)
    {
        // save things off
        _rmgr = rmgr;

        // enable default sounds
        setEnabled(DEFAULT, true);

//        // obtain the mixer
//        _mixer = AudioSystem.getMixer(null);
//        if (_mixer == null) {
//            Log.warning("Can't get default mixer, aborting audio " +
//                        "initialization.");
//            return;
//        }
//
        // create a thread to plays sounds and load sound
        // data from the resource manager
        _player = new Thread() {
            public void run () {
                while (amRunning()) {
//                    Log.info("Waiting for clip");

                    // wait until there is an item to get from the queue
                    Object o = _queue.get();

                    // play sounds
                    if (o instanceof String) {
                        playSound((String) o);

                    // see if we got the flush rsrcs signal
                    } else if (o == FLUSH) {
                        flushResources(false);
                        // and re-start the resource freer timer to do
                        // it again in 3 seconds
                        _resourceFreer.start();

                    // see if we got the die signal
                    } else if (o == DIE) {
                        _resourceFreer.stop();
                        flushResources(true);
                    }
                }
                Log.debug("SoundManager exit.");
            }
        };

        _player.setDaemon(true);
        _player.start();
        _resourceFreer.start();
    }

    /**
     * Shut the damn thing off.
     */
    public synchronized void shutdown ()
    {
        _player = null;
        synchronized (_queue) {
            _queue.clear();
            _queue.append(DIE); // signal death
        }
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
     * Sets whether sound is enabled.
     */
    public void setEnabled (SoundType type, boolean enabled)
    {
        if (enabled) {
            _enabled.add(type);
        } else {
            _enabled.remove(type);
        }
    }

    /**
     * Is the specified soundtype enabled?
     */
    public boolean isEnabled (SoundType type)
    {
        return _enabled.contains(type);
    }

    /**
     * Queues up the sound file with the given pathname to be played when
     * the sound manager deems the time appropriate.
     */
    public void play (SoundType type, String path)
    {
        if (_player != null && isEnabled(type)) {
//            Log.info("Queueing sound [path=" + path + "].");
            _queue.append(path);
        }
    }

    /**
     * Plays the sound file with the given pathname.
     */
    protected void playSound (String path)
    {
//        Log.info("Playing sound [path=" + path + "].");

        // see if we're playing the same sound that was just played.
        SoundRecord rec = (SoundRecord) _active.get(path);
        if (rec != null) {
            rec.restart();
            return;
        }

        // get the sound data from our LRU cache
        byte[] data = getAudioData(path);
        if (data == null) {
            return; // borked!
        }
//        Log.info("got data [length=" + data.length + "].");

        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(
                new ByteArrayInputStream(data));
            DataLine.Info info = new DataLine.Info(
                Clip.class, stream.getFormat());

            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);

            rec = new SoundRecord(clip);
            rec.start();
            _active.put(path, rec);
//            Log.info("clip played [path=" + path + "]");

        } catch (IOException ioe) {
            Log.warning("Error loading sound file [path=" + path +
                ", e=" + ioe + "].");

        } catch (UnsupportedAudioFileException uafe) {
            Log.warning("Unsupported sound format [path=" + path + ", e=" +
                uafe + "].");

        } catch (LineUnavailableException lue) {
            Log.warning("Line not available to play sound [path=" + path +
                ", e=" + lue + "].");
        }
    }

    /**
     * Called occaisionally to flush sound resources.
     *
     * @param force if true, shutdown and free all sounds, no matter what.
     * If false, frees sounds that haven't been used since EXPIRE_TIME
     * ago.
     */
    protected void flushResources (boolean force)
    {
        long then = System.currentTimeMillis() - EXPIRE_TIME;

        for (Iterator iter=_active.values().iterator(); iter.hasNext(); ) {
            SoundRecord rec = (SoundRecord) iter.next();
            if (force || rec.isStoppedSince(then)) {
                iter.remove();
                rec.close();
            }
        }
    }

    /**
     * Get the audio data for the specified path.
     */
    protected byte[] getAudioData (String path)
    {
        byte[] data = (byte[]) _dataCache.get(path);
        if (data != null) {
            return data;
        }

        try {
            data = StreamUtils.streamAsBytes(
                _rmgr.getResource(path), BUFFER_SIZE);
            _dataCache.put(path, data);

        } catch (IOException ioe) {
            Log.warning("Error loading sound file [path=" + path + ", e=" + 
                ioe + "].");
        }

        return data;
    }

    /**
     * A record to help us manage the use of sound resources.
     * We don't free the resources associated with a clip immediately, because
     * it may be played again shortly.
     */
    protected static class SoundRecord
        implements LineListener
    {

        /**
         * Construct a SoundRecord.
         */
        public SoundRecord (Clip clip)
        {
            _clip = clip;

            _clip.addLineListener(this);
        }

        /**
         * Start playing the sound.
         */
        public void start ()
        {
            _clip.start();
            didStart();
        }

        /**
         * Restart the sound from the beginning.
         */
        public void restart ()
        {
            if (_clip.isRunning()) {
                _clip.stop();
            }
            _clip.setFramePosition(0);
            _clip.start();
            didStart();
        }

        /**
         * Stop playing the sound.
         */
        public void stop ()
        {
            _clip.stop();
            didStop();
        }

        /**
         * Close down this SoundRecord and free the resources associated
         * with the clip.
         */
        public void close ()
        {
            _clip.removeLineListener(this);
            if (_clip.isRunning()) {
                _clip.stop();
            }
            _clip.close();
        }

        /**
         * Indicate that we've started playback of this sound.
         */
        protected void didStart ()
        {
            _stamp = Long.MAX_VALUE;
        }

        /**
         * Indicate that we've stopped playback.
         */
        protected void didStop ()
        {
            _stamp = System.currentTimeMillis();
        }

        /**
         * Has this SoundRecord been stopped since before the specified time?
         */
        public boolean isStoppedSince (long then)
        {
            return (_stamp < then);
        }

        // documentation inherited from interface LineListener
        public void update (LineEvent event)
        {
            if (event.getType() == LineEvent.Type.STOP) {
                didStop();
            }
        }

        /** The timestamp of the moment this clip last stopped playing. */
        protected long _stamp;

        /** The clip we're wrapping. */
        protected Clip _clip;
    }

    /**
     * Every 3 seconds we look for sounds that haven't been used for 4 and
     * free them up.
     */
    protected Timer _resourceFreer = new Timer(3000, 
        new ActionListener() {
            public void actionPerformed (ActionEvent e)
            {
                // stop the timer so we don't queue up loads of FLUSH
                // requests when the main thread is bogged down.
                _resourceFreer.stop();

                // and request a sweep by the main thread
                _queue.append(FLUSH);
            }
        });

    /** The resource manager from which we obtain audio files. */
    protected ResourceManager _rmgr;

//    /** The default mixer. */
//    protected Mixer _mixer;
//
    /** The thread that plays sounds. */
    protected Thread _player;

    /** The queue of sound clips to be played. */
    protected Queue _queue = new Queue();
    
    /** The cache of recent audio clips . */
    protected LRUHashMap _dataCache = new LRUHashMap(10);

    /** The clips that are currently active. */
    protected HashMap _active = new HashMap();

    /** A set of soundTypes for which sound is enabled. */
    protected HashSet _enabled = new HashSet();

    /** Signals to the queue to do different things. */
    protected Object FLUSH = new Object(), DIE = new Object();

    /** The buffer size in bytes used when reading audio file data. */
    protected static final int BUFFER_SIZE = 2048;

    /** How long a clip may linger after stopping before we clear
     * its resources. */
    protected static final long EXPIRE_TIME = 4000L;
}
