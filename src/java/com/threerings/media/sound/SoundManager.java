//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.media.sound;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.IOUtils;

import com.samskivert.util.Config;
import com.samskivert.util.ConfigUtil;
import com.samskivert.util.Interval;
import com.samskivert.util.LRUHashMap;
import com.samskivert.util.Queue;
import com.samskivert.util.RuntimeAdjust;
import com.samskivert.util.StringUtil;

import com.threerings.resource.ResourceManager;
import com.threerings.util.RandomUtil;

import com.threerings.media.Log;
import com.threerings.media.MediaPrefs;

/**
 * Manages the playing of audio files.
 */
public class SoundManager
{
    /**
     * Create instances of this for your application to differentiate
     * between different types of sounds.
     */
    public static class SoundType
    {
        /**
         * Construct a new SoundType.
         * Which should be a static variable stashed somewhere for the
         * entire application to share.
         * 
         * @param strname a short string identifier, preferably without spaces.
         */
        public SoundType (String strname)
        {
            _strname = strname;
        }

        public String toString ()
        {
            return _strname;
        }

        protected String _strname;
    }

    /**
     * A control for sounds.
     */
    public static interface Frob
    {
        /**
         * Stop playing or looping the sound.
         * At present, the granularity of this command is limited to the
         * buffer size of the line spooler, or about 8k of data. Thus,
         * if playing an 11khz sample, it could take 8/11ths of a second
         * for the sound to actually stop playing.
         */
        public void stop ();

        /**
         * Set the volume of the sound.
         */
        public void setVolume (float vol);

        /**
         * Get the volume of this sound.
         */
        public float getVolume ();
    }

    /** The default sound type. */
    public static final SoundType DEFAULT = new SoundType("default");

    /**
     * Constructs a sound manager.
     */
    public SoundManager (ResourceManager rmgr)
    {
        this(rmgr, null, null);
    }

    /**
     * Constructs a sound manager.
     *
     * @param defaultClipBundle
     * @param defaultClipPath The pathname of a sound clip to use as a
     * fallback if another sound clip cannot be located.
     */
    public SoundManager (
        ResourceManager rmgr, String defaultClipBundle, String defaultClipPath)
    {
        // save things off
        _rmgr = rmgr;
        _defaultClipBundle = defaultClipBundle;
        _defaultClipPath = defaultClipPath;
    }

    /**
     * Shut the damn thing off.
     */
    public void shutdown ()
    {
        synchronized (_queue) {
            _queue.clear();
            if (_spoolerCount > 0) {
                _queue.append(new SoundKey(DIE)); // signal death
            }
        }
        synchronized (_clipCache) {
            _lockedClips.clear();
        }
    }

    /**
     * Returns a string summarizing our volume settings and disabled sound
     * types.
     */
    public String summarizeState ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(", clipVol=").append(_clipVol);
        buf.append(", disabled=[");
        int ii = 0;
        for (Iterator iter = _disabledTypes.iterator(); iter.hasNext(); ) {
            if (ii++ > 0) {
                buf.append(", ");
            }
            buf.append(iter.next());
        }
        return buf.append("]").toString();
    }

    /**
     * Is the specified soundtype enabled?
     */
    public boolean isEnabled (SoundType type)
    {
        // by default, types are enabled..
        return (!_disabledTypes.contains(type));
    }

    /**
     * Turns on or off the specified sound type.
     */
    public void setEnabled (SoundType type, boolean enabled)
    {
        if (enabled) {
            _disabledTypes.remove(type);
        } else {
            _disabledTypes.add(type);
        }
    }

    /**
     * Sets the volume for all sound clips.
     *
     * @param vol a volume parameter between 0f and 1f, inclusive.
     */
    public void setClipVolume (float vol)
    {
        _clipVol = Math.max(0f, Math.min(1f, vol));
    }

    /**
     * Get the volume for all sound clips.
     */
    public float getClipVolume ()
    {
        return _clipVol;
    }

    /**
     * Optionally lock the sound data prior to playing, to guarantee
     * that it will be quickly available for playing.
     */
    public void lock (String pkgPath, String key)
    {
        enqueue(new SoundKey(LOCK, pkgPath, key), true);
    }

    /**
     * Unlock the specified sound so that its resources can be freed.
     */
    public void unlock (String pkgPath, String key)
    {
        enqueue(new SoundKey(UNLOCK, pkgPath, key), true);
    }

    /**
     * Batch lock a list of sounds.
     */
    public void lock (String pkgPath, String[] keys)
    {
        for (int ii=0; ii < keys.length; ii++) {
            enqueue(new SoundKey(LOCK, pkgPath, keys[ii]), (ii == 0));
        }
    }

    /**
     * Batch unlock a list of sounds.
     */
    public void unlock (String pkgPath, String[] keys)
    {
        for (int ii=0; ii < keys.length; ii++) {
            enqueue(new SoundKey(UNLOCK, pkgPath, keys[ii]), (ii == 0));
        }
    }

    /**
     * Play the specified sound of as the specified type of sound, immediately.
     * Note that a sound need not be locked prior to playing.
     */
    public void play (SoundType type, String pkgPath, String key)
    {
        play(type, pkgPath, key, 0);
    }

    /**
     * Play the specified sound after the specified delay.
     * @param delay the delay in milliseconds.
     */
    public void play (SoundType type, String pkgPath, String key, int delay)
    {
        if (type == null) {
            type = DEFAULT; // let the lazy kids play too
        }

        if ((_clipVol != 0f) && isEnabled(type)) {
            final SoundKey skey = new SoundKey(PLAY, pkgPath, key, delay,
                _clipVol);
            if (delay > 0) {
                new Interval() {
                    public void expired () {
                        addToPlayQueue(skey);
                    }
                }.schedule(delay);
            } else {
                addToPlayQueue(skey);
            }
        }
    }

    /**
     * Loop the specified sound.
     */
    public Frob loop (SoundType type, String pkgPath, String key)
    {
        SoundKey skey = new SoundKey(LOOP, pkgPath, key, 0, _clipVol);
        addToPlayQueue(skey);
        return skey; // it is a frob
    }

    // ==== End of public methods ====

    /**
     * Add the sound clip key to the queue to be played.
     */
    protected void addToPlayQueue (SoundKey skey)
    {
        boolean queued = enqueue(skey, true);
        if (queued) {
            if (_verbose.getValue()) {
                Log.info("Sound request [key=" + skey.key + "].");
            }

        } else /* if (_verbose.getValue()) */ {
            Log.warning("SoundManager not playing sound because " +
                        "too many sounds in queue [key=" + skey + "].");
        }
    }

    /**
     * Enqueue a new SoundKey.
     */
    protected boolean enqueue (SoundKey key, boolean okToStartNew)
    {
        boolean add;
        boolean queued;
        synchronized (_queue) {
            if (key.cmd == PLAY && _queue.size() > MAX_QUEUE_SIZE) {
                queued = add = false;
            } else {
                _queue.appendLoud(key);
                queued = true;
                add = okToStartNew && (_freeSpoolers == 0) &&
                        (_spoolerCount < MAX_SPOOLERS);
                if (add) {
                    _spoolerCount++;
                }
            }
        }

        // and if we need a new thread, add it
        if (add) {
            Thread spooler = new Thread("narya SoundManager line spooler") {
                public void run () {
                    spoolerRun();
                }
            };
            spooler.setDaemon(true);
            spooler.start();
        }

        return queued;
    }

    /**
     * This is the primary run method of the sound-playing threads.
     */
    protected void spoolerRun ()
    {
        while (true) {
            try {
                SoundKey key;
                synchronized (_queue) {
                    _freeSpoolers++;
                    key = (SoundKey) _queue.get(MAX_WAIT_TIME);
                    _freeSpoolers--;

                    if (key == null || key.cmd == DIE) {
                        _spoolerCount--;
                        // if dieing and there are others to kill, do so
                        if (key != null && _spoolerCount > 0) {
                            _queue.appendLoud(key);
                        }
                        return;
                    }
                }

                // process the command
                processKey(key);

            } catch (Exception e) {
                Log.logStackTrace(e);
            }
        }
    }

    /**
     * Process the requested command in the specified SoundKey.
     */
    protected void processKey (SoundKey key)
        throws Exception
    {
        switch (key.cmd) {
        case PLAY:
        case LOOP:
            playSound(key);
            break;

        case LOCK:
            if (!isTesting()) {
                synchronized (_clipCache) {
                    try {
                        getClipData(key); // preload
                        // copy cached to lock map
                        _lockedClips.put(key, _clipCache.get(key));
                    } catch (Exception e) {
                        // don't whine about LOCK failures unless
                        // we are verbosely logging
                        if (_verbose.getValue()) {
                            throw e;
                        }
                    }
                }
            }
            break;

        case UNLOCK:
            synchronized (_clipCache) {
                _lockedClips.remove(key);
            }
            break;
        }
    }

    /**
     * On a spooling thread, 
     */
    protected void playSound (SoundKey key)
    {
        if (!key.running) {
            return;
        }
        key.thread = Thread.currentThread();
        SourceDataLine line = null;
        try {
            // get the sound data from our LRU cache
            byte[] data = getClipData(key);
            if (data == null) {
                return; // borked!

            } else if (key.isExpired()) {
                if (_verbose.getValue()) {
                    Log.info("Sound expired [key=" + key.key + "].");
                }
                return;

            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(
                new ByteArrayInputStream(data));
            AudioFormat format = stream.getFormat();

            // open the sound line
            line = (SourceDataLine) AudioSystem.getLine(
                new DataLine.Info(SourceDataLine.class, format));
            line.open(format, LINEBUF_SIZE);
            float setVolume = 1;
            line.start();
            _soundSeemsToWork = true;

            do {
                // play the sound
                byte[] buffer = new byte[LINEBUF_SIZE];
                int count = 0;
                while (key.running && count != -1) {
                    float vol = key.volume;
                    if (vol != setVolume) {
                        adjustVolume(line, vol);
                        setVolume = vol;
                    }
                    try {
                        count = stream.read(buffer, 0, buffer.length);
                    } catch (IOException e) {
                        // this shouldn't ever ever happen because the stream
                        // we're given is from a reliable source
                        Log.warning("Error reading clip data! [e=" + e + "].");
                        return;
                    }

                    if (count >= 0) {
                        line.write(buffer, 0, count);
                    }
                }

                // if we're going to loop, reset the stream to the beginning
                if (key.cmd == LOOP) {
                    stream.reset();
                }
            } while (key.cmd == LOOP && key.running);

            // sleep the drain time. We never trust line.drain() because
            // it is buggy and locks up on natively multithreaded systems
            // (linux, winXP with HT).
            float sampleRate = format.getSampleRate();
            if (sampleRate == AudioSystem.NOT_SPECIFIED) {
                sampleRate = 11025; // most of our sounds are
            }
            int sampleSize = format.getSampleSizeInBits();
            if (sampleSize == AudioSystem.NOT_SPECIFIED) {
                sampleSize = 16;
            }
            int drainTime = (int) Math.ceil(
                (LINEBUF_SIZE * 8 * 1000) / (sampleRate * sampleSize));

            // add in a fudge factor of half a second 
            drainTime += 500;

            try {
                Thread.sleep(drainTime);
            } catch (InterruptedException ie) {
            }

        } catch (IOException ioe) {
            Log.warning("Error loading sound file [key=" + key +
                        ", e=" + ioe + "].");

        } catch (UnsupportedAudioFileException uafe) {
            Log.warning("Unsupported sound format [key=" + key +
                        ", e=" + uafe + "].");

        } catch (LineUnavailableException lue) {
            String err = "Line not available to play sound [key=" + key.key +
                      ", e=" + lue + "].";
            if (_soundSeemsToWork) {
                Log.warning(err);
            } else {
                // this error comes every goddamned time we play a sound on
                // someone with a misconfigured sound card, so let's just keep
                // it to ourselves
                Log.debug(err);
            }

        } finally {
            if (line != null) {
                line.close();
            }
            key.thread = null;
        }
    }

    /**
     * @return true if we're using a test sound directory.
     */
    protected boolean isTesting ()
    {
        return !StringUtil.blank(_testDir.getValue());
    }

    /**
     * Called by spooling threads, loads clip data from the resource
     * manager or the cache.
     */
    protected byte[] getClipData (SoundKey key)
        throws IOException, UnsupportedAudioFileException
    {
        byte[][] data;
        boolean verbose = _verbose.getValue();
        synchronized (_clipCache) {
            // if we're testing, clear all non-locked sounds every time
            if (isTesting()) {
                _clipCache.clear();
            }

            data = (byte[][]) _clipCache.get(key);

            // see if it's in the locked cache (we first look in the regular
            // clip cache so that locked clips that are still cached continue
            // to be moved to the head of the LRU queue)
            if (data == null) {
                data = (byte[][]) _lockedClips.get(key);
            }

            if (data == null) {
                // if there is a test sound, JUST use the test sound.
                InputStream stream = getTestClip(key);
                if (stream != null) {
                    data = new byte[1][];
                    data[0] = IOUtils.toByteArray(stream);

                } else { 
                    // otherwise, randomize between all available sounds
                    Config c = getConfig(key);
                    String[] names = c.getValue(key.key, (String[])null);
                    if (names == null) {
                        Log.warning("No such sound [key=" + key + "].");
                        return null;
                    }

                    data = new byte[names.length][];
                    String bundle = c.getValue("bundle", (String)null);
                    for (int ii=0; ii < names.length; ii++) {
                        data[ii] = loadClipData(bundle, names[ii]);
                    }
                }

                _clipCache.put(key, data);
            }
        }

        return (data.length > 0) ? data[RandomUtil.getInt(data.length)] : null;
    }

    protected InputStream getTestClip (SoundKey key)
    {
        String testDirectory = _testDir.getValue();
        if (StringUtil.blank(testDirectory)) {
            return null;
        }

        final String namePrefix = key.key;
        File f = new File(testDirectory);
        File[] list = f.listFiles(new FilenameFilter() {
            public boolean accept (File f, String name)
            {
                if (name.startsWith(namePrefix)) {
                    String backhalf = name.substring(namePrefix.length());
                    int dot = backhalf.indexOf('.');
                    if (dot == -1) {
                        dot = backhalf.length();
                    }

                    // allow the file if the portion of the name
                    // after the prefix but before the extension is blank
                    // or a parsable integer
                    String extra = backhalf.substring(0, dot);
                    if ("".equals(extra)) {
                        return true;
                    } else {
                        try {
                            Integer.parseInt(extra);
                            // success!
                            return true;
                        } catch (NumberFormatException nfe) {
                            // not a number, we fall through...
                        }
                    }
                    // else fall through
                }
                return false;
            }
        });
        int size = (list == null) ? 0 : list.length;
        if (size > 0) {
            File pick = list[RandomUtil.getInt(size)];
            try {
                return new FileInputStream(pick);
            } catch (Exception e) {
                Log.warning("Error reading test sound [e=" + e + ", file=" +
                    pick + "].");
            }
        }
        return null;
    }

    /**
     * Read the data from the resource manager.
     */
    protected byte[] loadClipData (String bundle, String path)
        throws IOException
    {
        InputStream clipin = null;
        try {
            clipin = _rmgr.getResource(bundle, path);
        } catch (FileNotFoundException fnfe) {
            // try from the classpath
            try {
                clipin = _rmgr.getResource(path);
            } catch (FileNotFoundException fnfe2) {
                // only play the default sound if we have verbose sound
                // debuggin turned on.
                if (_verbose.getValue()) {
                    Log.warning("Could not locate sound data [bundle=" +
                        bundle + ", path=" + path + "].");
                    if (_defaultClipPath != null) {
                        try {
                            clipin = _rmgr.getResource(
                                _defaultClipBundle, _defaultClipPath);
                        } catch (FileNotFoundException fnfe3) {
                            try {
                                clipin = _rmgr.getResource(_defaultClipPath);
                            } catch (FileNotFoundException fnfe4) {
                                Log.warning("Additionally, the default " +
                                    "fallback sound could not be located " +
                                    "[bundle=" + _defaultClipBundle +
                                    ", path=" + _defaultClipPath + "].");
                            }
                        }
                    } else {
                        Log.warning("No fallback default sound specified!");
                    }
                }
                // if we couldn't load the default, rethrow
                if (clipin == null) {
                    throw fnfe2;
                }
            }
        }

        return IOUtils.toByteArray(clipin);
    }

    /**
     * Get the cached Config.
     */
    protected Config getConfig (SoundKey key)
    {
        Config c = (Config) _configs.get(key.pkgPath);
        if (c == null) {
            String propPath = key.pkgPath + Sounds.PROP_NAME;
            Properties props = new Properties();
            try {
                props = ConfigUtil.loadInheritedProperties(
                    propPath + ".properties", _rmgr.getClassLoader());
            } catch (IOException ioe) {
                Log.warning("Failed to load sound properties " +
                            "[path=" + propPath + ", error=" + ioe + "].");
            }
            c = new Config(propPath, props);
            _configs.put(key.pkgPath, c);
        }
        return c;
    }

//    /**
//     * Adjust the volume of this clip.
//     */
//    protected static void adjustVolumeIdeally (Line line, float volume)
//    {
//        if (line.isControlSupported(FloatControl.Type.VOLUME)) {
//            FloatControl vol = (FloatControl) 
//                line.getControl(FloatControl.Type.VOLUME);
//
//            float min = vol.getMinimum();
//            float max = vol.getMaximum();
//
//            float ourval = (volume * (max - min)) + min;
//            Log.debug("adjust vol: [min=" + min + ", ourval=" + ourval +
//                ", max=" + max + "].");
//            vol.setValue(ourval);
//
//        } else {
//            // fall back
//            adjustVolume(line, volume);
//        }
//    }

    /**
     * Use the gain control to implement volume.
     */
    protected static void adjustVolume (Line line, float vol)
    {
        FloatControl control = (FloatControl) 
            line.getControl(FloatControl.Type.MASTER_GAIN);

        // the only problem is that gain is specified in decibals,
        // which is a logarithmic scale.
        // Since we want max volume to leave the sample unchanged, our
        // maximum volume translates into a 0db gain.
        float gain;
        if (vol == 0f) {
            gain = control.getMinimum();
        } else {
            gain = (float) ((Math.log(vol) / Math.log(10.0)) * 20.0);
        }

        control.setValue(gain);
        //Log.info("Set gain: " + gain);
    }

    /**
     * A key for tracking sounds.
     */
    protected static class SoundKey
        implements Frob
    {
        public byte cmd;

        public String pkgPath;

        public String key;

        public long stamp;

        /** Should we still be running? */
        public volatile boolean running = true;

        public volatile float volume;

        /** The player thread, if it's playing us. */
        public Thread thread;

        /**
         * Create a SoundKey that just contains the specified command.
         * DIE.
         */
        public SoundKey (byte cmd)
        {
            this.cmd = cmd;
        }

        /**
         * Quicky constructor for music keys and lock operations.
         */
        public SoundKey (byte cmd, String pkgPath, String key)
        {
            this(cmd);
            this.pkgPath = pkgPath;
            this.key = key;
        }

        /**
         * Constructor for a sound effect soundkey.
         */
        public SoundKey (byte cmd, String pkgPath, String key, int delay,
                         float volume)
        {
            this(cmd, pkgPath, key);

            stamp = System.currentTimeMillis() + delay;
            this.volume = volume;
        }

        // documentation inherited from interface Frob
        public void stop ()
        {
            running = false;
            Thread t = thread;
            if (t != null) {
                // doesn't actually ever seem to do much
                t.interrupt();
            }
        }

        // documentation inherited from interface Frob
        public void setVolume (float vol)
        {
            volume = Math.max(0f, Math.min(1f, vol));
        }

        // documentation inherited from interface Frob
        public float getVolume ()
        {
            return volume;
        }

        /**
         * Has this sound key expired.
         */
        public boolean isExpired ()
        {
            return (stamp + MAX_SOUND_DELAY < System.currentTimeMillis());
        }

        // documentation inherited
        public String toString ()
        {
            return "SoundKey{cmd=" + cmd + ", pkgPath=" + pkgPath +
                ", key=" + key + "}";
        }

        // documentation inherited
        public int hashCode ()
        {
            return pkgPath.hashCode() ^ key.hashCode();
        }

        // documentation inherited
        public boolean equals (Object o)
        {
            if (o instanceof SoundKey) {
                SoundKey that = (SoundKey) o;
                return this.pkgPath.equals(that.pkgPath) &&
                       this.key.equals(that.key);
            }
            return false;
        }
    }

    /** The path of the default sound to use for missing sounds. */
    protected String _defaultClipBundle, _defaultClipPath;

    /** The resource manager from which we obtain audio files. */
    protected ResourceManager _rmgr;

    /** The queue of sound clips to be played. */
    protected Queue _queue = new Queue();

    /** The number of currently active LineSpoolers. */
    protected int _spoolerCount, _freeSpoolers;

    /** If we every play a sound successfully, this is set to true. */
    protected boolean _soundSeemsToWork = false;

    /** Volume level for sound clips. */
    protected float _clipVol = 1f;

    /** The cache of recent audio clips . */
    protected LRUHashMap _clipCache = new LRUHashMap(10);

    /** The set of locked audio clips; this is separate from the LRU so
     * that locking clips doesn't booch up an otherwise normal caching
     * agenda. */
    protected HashMap _lockedClips = new HashMap();

    /** A set of soundTypes for which sound is enabled. */
    protected HashSet _disabledTypes = new HashSet();

    /** A cache of config objects we've created. */
    protected HashMap _configs = new HashMap();

    /** Soundkey command constants. */
    protected static final byte PLAY = 0;
    protected static final byte LOCK = 1;
    protected static final byte UNLOCK = 2;
    protected static final byte DIE = 3;
    protected static final byte LOOP = 4;

    /** A pref that specifies a directory for us to get test sounds from. */
    protected static RuntimeAdjust.FileAdjust _testDir =
        new RuntimeAdjust.FileAdjust(
            "Test sound directory", "narya.media.sound.test_dir",
            MediaPrefs.config, true, "");

    protected static RuntimeAdjust.BooleanAdjust _verbose =
        new RuntimeAdjust.BooleanAdjust(
            "Verbose sound event logging", "narya.media.sound.verbose",
            MediaPrefs.config, false);

    /** The queue size at which we start to ignore requests to play sounds. */
    protected static final int MAX_QUEUE_SIZE = 25;

    /** The maximum time after which we throw away a sound rather
     * than play it. */
    protected static final long MAX_SOUND_DELAY = 400L;

    /** The size of the line's buffer. */
    protected static final int LINEBUF_SIZE = 8 * 1024;

    /** The maximum time a spooler will wait for a stream before
     * deciding to shut down. */
    protected static final long MAX_WAIT_TIME = 30000L;

    /** The maximum number of spoolers we'll allow. This is a lot. */
    protected static final int MAX_SPOOLERS = 12;
}
