//
// $Id: SoundManager.java,v 1.41 2002/12/12 19:00:25 mdb Exp $

package com.threerings.media;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javax.swing.Timer;

import org.apache.commons.io.StreamUtils;
import org.apache.commons.lang.Constant;

import com.samskivert.util.Config;
import com.samskivert.util.LockableLRUHashMap;
import com.samskivert.util.Queue;

import com.threerings.resource.ResourceManager;
import com.threerings.util.RandomUtil;

import com.threerings.media.Log;

/**
 * Manages the playing of audio files.
 */
// TODO:
//   - fade music out when stopped?
public class SoundManager
    implements MusicPlayer.MusicEventListener
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

    /** The default sound type. */
    public static final SoundType DEFAULT = new SoundType("default");

    /**
     * Constructs a sound manager.
     */
    public SoundManager (ResourceManager rmgr)
    {
        // save things off
        _rmgr = rmgr;

        if (!SOUND_ENABLED) return;

        // create a thread to plays sounds and load sound
        // data from the resource manager
        _player = new Thread("narya SoundManager") {
            public void run () {
                Object command = null;
                SoundKey key = null;
                MusicInfo musicInfo = null;

                while (amRunning()) {
                    // Get the next command and arguments
                    synchronized (_queue) {
                        command = _queue.get();

                        // some commands have an additional argument.
                        if ((PLAY == command) ||
                            (STOPMUSIC == command) ||
                            (LOCK == command) ||
                            (UNLOCK == command)) {
                            key = (SoundKey) _queue.get();

                        } else if (PLAYMUSIC == command) {
                            musicInfo = (MusicInfo) _queue.get();
                        }
                    }

                    try {
                        // execute the command outside of the queue synch
                        if (PLAY == command) {
                            playSound(key);

                        } else if (PLAYMUSIC == command) {
                            playMusic(musicInfo);

                        } else if (STOPMUSIC == command) {
                            stopMusic(key);

                        } else if (LOCK == command) {
                            _clipCache.lock(key);
                            getClipData(key); // preload

                        } else if (UNLOCK == command) {
                            _clipCache.unlock(key);

                        } else if (UPDATE_MUSIC_VOL == command) {
                            updateMusicVolume();

                        } else if (DIE == command) {
                            LineSpooler.shutdown();
                            shutdownMusic();

                        } else {
                            Log.warning("Got unknown command [cmd=" + command +
                                        ", key=" + key +
                                        ", info=" + musicInfo + "].");
                        }

                    } catch (Exception e) {
                        Log.warning("SoundManager failure [cmd=" + command +
                                    ", key=" + key +
                                    ", info=" + musicInfo + "].");
                        Log.logStackTrace(e);
                    }
                }
                Log.debug("SoundManager exit.");
            }
        };

        _player.start();
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
     * Set the test sound clip directory.
     */
    public void setTestDir (String testy)
    {
        _testDir = testy;
    }

    /**
     * Sets the volume for all sound clips.
     *
     * @param val a volume parameter between 0f and 1f, inclusive.
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
     * Sets the volume for music.
     *
     * @param val a volume parameter between 0f and 1f, inclusive.
     */
    public void setMusicVolume (float vol)
    {
        float oldvol = _musicVol;
        _musicVol = Math.max(0f, Math.min(1f, vol));

        if ((oldvol == 0f) && (_musicVol != 0f)) {
            _musicAction = START;
        } else if ((oldvol != 0f) && (_musicVol == 0f)) {
            _musicAction = STOP;
        } else {
            _musicAction = NONE;
        }
        _queue.append(UPDATE_MUSIC_VOL);
    }

    /**
     * Get the music volume.
     */
    public float getMusicVolume ()
    {
        return _musicVol;
    }

    /**
     * Optionally lock the sound data prior to playing, to guarantee
     * that it will be quickly available for playing.
     */
    public void lock (String pkgPath, String key)
    {
        if (!SOUND_ENABLED) return;

        synchronized (_queue) {
            _queue.append(LOCK);
            _queue.append(new SoundKey(pkgPath, key));
        }
    }

    /**
     * Unlock the specified sound so that its resources can be freed.
     */
    public void unlock (String pkgPath, String key)
    {
        if (!SOUND_ENABLED) return;

        synchronized (_queue) {
            _queue.append(UNLOCK);
            _queue.append(new SoundKey(pkgPath, key));
        }
    }

    /**
     * Batch lock a list of sounds.
     */
    public void lock (String pkgPath, String[] keys)
    {
        for (int ii=0; ii < keys.length; ii++) {
            lock(pkgPath, keys[ii]);
        }
    }

    /**
     * Batch unlock a list of sounds.
     */
    public void unlock (String pkgPath, String[] keys)
    {
        for (int ii=0; ii < keys.length; ii++) {
            unlock(pkgPath, keys[ii]);
        }
    }

    /**
     * Play the specified sound of as the specified type of sound.
     * Note that a sound need not be locked prior to playing.
     */
    public void play (SoundType type, String pkgPath, String key)
    {
        if (!SOUND_ENABLED) return;

        if (type == null) {
            // let the lazy kids play too
            type = DEFAULT;
        }

        if (_player != null && (_clipVol != 0f) && isEnabled(type)) {
            synchronized (_queue) {
                if (_queue.size() < MAX_QUEUE_SIZE) {
                    Log.debug("play requested [key=" + key + "].");
                    _queue.append(PLAY);
                    _queue.append(new SoundKey(pkgPath, key));

                } else {
                    Log.warning("SoundManager not playing sound because " +
                        "too many sounds in queue [pkgPath=" + pkgPath +
                        ", key=" + key + ", type=" + type + "].");
                }
            }
        }
    }

    /**
     * Start playing the specified music repeatedly.
     */
    public void pushMusic (String pkgPath, String key)
    {
        pushMusic(pkgPath, key, -1);
    }

    /**
     * Start playing music for the specified number of loops.
     */
    public void pushMusic (String pkgPath, String key, int numloops)
    {
        if (!SOUND_ENABLED) {
            return;
        }

        synchronized (_queue) {
            _queue.append(PLAYMUSIC);
            _queue.append(new MusicInfo(pkgPath, key, numloops));
        }
    }

    /**
     * Remove the specified music from the playlist. If it is currently
     * playing, it will be stopped and the previous song will be started.
     */
    public void removeMusic (String pkgPath, String key)
    {
        synchronized (_queue) {
            _queue.append(STOPMUSIC);
            _queue.append(new SoundKey(pkgPath, key));
        }
    }

    /**
     * On the SoundManager thread,
     * plays the sound file with the given pathname.
     */
    protected void playSound (SoundKey key)
    {
        try {
            // get the sound data from our LRU cache
            byte[] data = getClipData(key);
            if (data == null) {
                return; // borked!
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(
                new ByteArrayInputStream(data));

            LineSpooler.play(stream, _clipVol, key);

        } catch (IOException ioe) {
            Log.warning("Error loading sound file [key=" + key +
                ", e=" + ioe + "].");

        } catch (UnsupportedAudioFileException uafe) {
            Log.warning("Unsupported sound format [key=" + key + ", e=" +
                uafe + "].");

        } catch (LineUnavailableException lue) {
            Log.warning("Line not available to play sound [key=" + key +
                ", e=" + lue + "].");
        }
    }

    /**
     * Play a song from the specified path.
     */
    protected void playMusic (MusicInfo info)
    {
        // stop whatever's currently playing
        if (_musicPlayer != null) {
            _musicPlayer.stop();
            handleMusicStopped();
        }

        // add the new song
        _musicStack.addFirst(info);

        // and play it
        playTopMusic();
    }

    /**
     * Start the specified sequence.
     */
    protected void playTopMusic ()
    {
        if (_musicStack.isEmpty()) {
            return;
        }

        // if the volume is off, we don't actually want to play anything
        // but we want to at least decrement any loopers by one
        // and keep them on the top of the queue
        if (_musicVol == 0f) {
            handleMusicStopped();
            return;
        }

        MusicInfo info = (MusicInfo) _musicStack.getFirst();

        Config c = getConfig(info);
        String[] names = c.getValue(info.key, (String[])null);
        if (names == null) {
            Log.warning("No such music [key=" + info + "].");
            _musicStack.removeFirst();
            playTopMusic();
            return;
        }
        String music = names[RandomUtil.getInt(names.length)];

        Class playerClass = getMusicPlayerClass(music);

        // shutdown the old player if we're switching music types
        if (! playerClass.isInstance(_musicPlayer)) {
            if (_musicPlayer != null) {
                _musicPlayer.shutdown();
            }

            // set up the new player
            try {
                _musicPlayer = (MusicPlayer) playerClass.newInstance();
                _musicPlayer.init(this);

            } catch (Exception e) {
                Log.warning("Unable to instantiate music player [class=" +
                        playerClass + ", e=" + e + "].");

                // scrap it, try again with the next song
                _musicPlayer = null;
                _musicStack.removeFirst();
                playTopMusic();
                return;
            }

            _musicPlayer.setVolume(_musicVol);
        }

        // play!
        String bundle = c.getValue("bundle", (String)null);
        try {
            // TODO: buffer for the music player?
            _musicPlayer.start(_rmgr.getResource(bundle, music));
        } catch (Exception e) {
            Log.warning("Error playing music, skipping [e=" + e +
                ", bundle=" + bundle + ", music=" + music + "].");
            _musicStack.removeFirst();
            playTopMusic();
            return;
        }
    }

    /**
     * Get the appropriate music player for the specified music file.
     */
    protected static Class getMusicPlayerClass (String path)
    {
        path = path.toLowerCase();

        if (path.endsWith(".mid") || path.endsWith(".rmf")) {
            return MidiPlayer.class;

        } else if (path.endsWith(".mod")) {
            return ModPlayer.class;

        } else if (path.endsWith(".mp3")) {
            return Mp3Player.class;

        } else {
            return null;
        }
    }

    /**
     * Stop whatever song is currently playing and deal with the
     * MusicInfo associated with it.
     */
    protected void handleMusicStopped ()
    {
        if (_musicStack.isEmpty()) {
            return;
        }

        // see what was playing
        MusicInfo current = (MusicInfo) _musicStack.getFirst();

        // see how many times the song was to loop and act accordingly
        switch (current.loops) {
        default:
            current.loops--;
            break;

        case 1:
            // sorry charlie
            _musicStack.removeFirst();
            break;
        }
    }

    /**
     * Stop the sequence at the specified path.
     */
    protected void stopMusic (SoundKey key)
    {
        if (! _musicStack.isEmpty()) {
            MusicInfo current = (MusicInfo) _musicStack.getFirst();

            // if we're currently playing this song..
            if (key.equals(current)) {
                // stop it
                if (_musicPlayer != null) {
                    _musicPlayer.stop();
                }

                // remove it from the stack
                _musicStack.removeFirst();
                // start playing the next..
                playTopMusic();
                return;

            } else {
                // we aren't currently playing this song. Simply remove.
                for (Iterator iter=_musicStack.iterator(); iter.hasNext(); ) {
                    if (key.equals(iter.next())) {
                        iter.remove();
                        return;
                    }
                }

            }
        }

        Log.debug("Sequence stopped that wasn't in the stack anymore " +
            "[key=" + key + "].");
    }

    /**
     * Attempt to modify the music volume for any playing tracks.
     *
     * @param start
     */
    protected void updateMusicVolume ()
    {
        if (_musicPlayer != null) {
            _musicPlayer.setVolume(_musicVol);
        }
        switch (_musicAction) {
        case START:
            playTopMusic();
            break;

        case STOP:
            stopMusicPlayer();
            break;
        }
    }

    // documentation inherited from interface MusicPlayer.MusicEventListener
    public void musicStopped ()
    {
        handleMusicStopped();
        playTopMusic();
    }

    /**
     * Shutdown the music subsystem.
     */
    protected void shutdownMusic ()
    {
        _musicStack.clear();
        stopMusicPlayer();
    }

    /**
     * Stop the current music player.
     */
    protected void stopMusicPlayer ()
    {
        if (_musicPlayer != null) {
            _musicPlayer.stop();
            _musicPlayer.shutdown();
            _musicPlayer = null;
        }
    }

    /**
     * Get the audio data for the specified path.
     */
    protected byte[] getClipData (SoundKey key)
        throws IOException, UnsupportedAudioFileException
    {
        // if we're testing, clear all non-locked sounds every time
        if (_testDir != null) {
            _clipCache.clear();
        }

        byte[][] data = (byte[][]) _clipCache.get(key);
        if (data == null) {

            // if there is a test sound, JUST use the test sound.
            InputStream stream = getTestClip(key);
            if (stream != null) {
                data = new byte[1][];
                data[0] = StreamUtils.streamAsBytes(stream, BUFFER_SIZE);

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
                    InputStream clipin = null;
                    try {
                        _rmgr.getResource(bundle, names[ii]);
                    } catch (FileNotFoundException fnfe) {
                        // try from the classpath
                        clipin = _rmgr.getResource(names[ii]);
                    }
                    data[ii] = StreamUtils.streamAsBytes(clipin, BUFFER_SIZE);
                }
            }

            _clipCache.put(key, data);
        }

        return data[RandomUtil.getInt(data.length)];
    }

    protected InputStream getTestClip (SoundKey key)
    {
        if (_testDir == null) {
            return null;
        }

        final String namePrefix = key.key + ".";
        File f = new File(_testDir);
        File[] list = f.listFiles(new FilenameFilter() {
            public boolean accept (File f, String name)
            {
                return name.startsWith(namePrefix);
            }
        });
        if ((list != null) && (list.length > 0)) {
            try {
                return new FileInputStream(list[0]);
            } catch (Exception e) {
                Log.warning("Error reading test sound [e=" + e + ", file=" +
                    list[0] + "].");
            }
        }
        return null;
    }

    protected Config getConfig (SoundKey key)
    {
        Config c = (Config) _configs.get(key.pkgPath);
        if (c == null) {
            String propPath = key.pkgPath + Sounds.PROP_NAME;
            c = new Config(propPath);
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
     * Handles the playing of sound clip data.
     */
    protected static class LineSpooler extends Thread
    {
        /**
         * Attempt to play the specified sound.
         */
        public static void play (
            AudioInputStream stream, float volume, SoundKey key)
            throws LineUnavailableException
        {
            AudioFormat format = stream.getFormat();
            LineSpooler spooler;

            for (int ii=0, nn=_openSpoolers.size(); ii < nn; ii++) {
                spooler = (LineSpooler) _openSpoolers.get(ii);

                // we have this thread remove the spooler if it's dead
                // so that we avoid deadlock conditions
                if (spooler.isDead()) {
                    _openSpoolers.remove(ii--);
                    nn--;

                } else if (spooler.checkPlay(format, stream, volume, key)) {
                    return;
                }
            }

            if (_openSpoolers.size() >= MAX_SPOOLERS) {
                throw new LineUnavailableException("Exceeded maximum number " +
                    "of narya sound spoolers.");
            }

            spooler = new LineSpooler(format);
            _openSpoolers.add(spooler);
            spooler.checkPlay(format, stream, volume, key);
            spooler.start();
        }

        /**
         * Shutdown the linespooler subsystem.
         */
        public static void shutdown ()
        {
            // this is all that is needed, after 30 seconds each spooler
            for (int ii=0, nn=_openSpoolers.size(); ii < nn; ii++) {
                // this will stop playback now
                ((LineSpooler) _openSpoolers.get(ii)).setDead();
            }
            // and this will remove all the spoolers. They'll still be
            // around while they drain their lines and then wait 30 seconds
            // to die...
            _openSpoolers.clear();
        }

        /**
         * Private constructor.
         */
        private LineSpooler (AudioFormat format)
            throws LineUnavailableException
        {
            super("narya SoundManager LineSpooler");
            _format = format;

            _line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(
                SourceDataLine.class, _format));
            _line.open(_format);
            _line.start();
        }

        /**
         * Set this line to dead.
         */
        protected void setDead ()
        {
            _valid = false;
        }

        /**
         * Has this line been closed?
         */
        protected boolean isDead ()
        {
            return !_valid;
        }

        /**
         * Check-and-set method to see if we can play and to start
         * doing so if we can.
         */
        protected synchronized boolean checkPlay (
            AudioFormat format, AudioInputStream stream, float volume,
            SoundKey key)
        {
            if (_valid && (_stream == null) && _format.matches(format)) {
                _stream = stream;
                _key = key;
                SoundManager.adjustVolume(_line, volume);
                notify();
                return true;
            }

            return false;
        }

        /**
         * @return true if we have sound data to play.
         */
        protected synchronized boolean waitForData ()
        {
            if (_stream == null) {
                try {
                    wait(MAX_WAIT_TIME);
                } catch (InterruptedException ie) {
                    // ignore.
                }
                if (_stream == null) {
                    setDead(); // we waited 30 seconds and never got a sound.
                    return false;
                }
            }

            return true;
        }

        /**
         * Main loop for the LineSpooler.
         */
        public void run ()
        {
            while (waitForData()) {
                playStream();
            }

            _line.close();
        }

        /**
         * Play the current stream.
         */
        protected void playStream ()
        {
            int count = 0;
            byte[] data = new byte[BUFFER_SIZE];

            Log.debug("play happening [key=" + _key.key + "].");
            while (_valid && count != -1) {
                try {
                    count = _stream.read(data, 0, data.length);
                } catch (IOException e) {
                    // this shouldn't ever ever happen because the stream
                    // we're given is from a reliable source
                    Log.warning("Error reading clip data! [e=" + e + "].");
                    _stream = null;
                    return;
                }

                if (count >= 0) {
                    _line.write(data, 0, count);
                }
            }

            // There is a major bug with using drain() under linux. It often
            // causes the thread to just loop forever inside the native
            // implementation of drain(), and we're screwed.
            if (_shouldDrain) {
                _line.drain();

            } else {
                // we instead attempt to sleep long enough such that
                // everything should be drained.
                try {
                    Thread.sleep(NO_DRAIN_SLEEP_TIME);
                } catch (InterruptedException ie) {
                }
            }

            // clear it out so that we can wait for more.
            _stream = null;
        }

        /** The format that our line was opened with. */
        protected AudioFormat _format;

        /** The stream we're currently spooling out. */
        protected AudioInputStream _stream;

        /** The line that we spool to. */
        protected SourceDataLine _line;

        /** Are we still active and usable for spooling sounds, or should
         * we be removed. */
        protected boolean _valid = true;

        /** Used for debugging. */
        protected SoundKey _key;

        /** The list of all the currently instantiated spoolers. */
        protected static ArrayList _openSpoolers = new ArrayList();

        /** Should we attempt to use line.drain()? */
        protected static boolean _shouldDrain = true;

        /** The maximum time a spooler will wait for a stream before
         * deciding to shut down. */
        protected static final long MAX_WAIT_TIME = 30000L;

        /** The maximum number of spoolers we'll allow. This is a lot. */
        protected static final int MAX_SPOOLERS = 24;

        /** The time we sleep if it's not safe to drain. */
        protected static final long NO_DRAIN_SLEEP_TIME = 3000L;

        // see if we should use drain.
        static {
            String os = System.getProperty("os.name");
            if (os == null || (os.indexOf("Linux") != -1)) {
                _shouldDrain = false;
                Log.info("Detected Linux, will not use drain() on lines.");
            }
        }
    }

    /**
     * A key for tracking sounds.
     */
    protected static class SoundKey
    {
        public String pkgPath;

        public String key;

        public SoundKey (String pkgPath, String key)
        {
            this.pkgPath = pkgPath;
            this.key = key;
        }

        // documentation inherited
        public String toString ()
        {
            return "SoundKey{pkgPath=" + pkgPath + ", key=" + key + "}";
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

    /**
     * A class that tracks the information about our playing music files.
     */
    protected static class MusicInfo extends SoundKey
    {
        /** How many times to loop, or -1 for forever. */
        public int loops;

        // TODO rename
        public MusicInfo (String set, String path, int loops)
        {
            super(set, path);
            this.loops = loops;
        }
    }

    /** Directory from which we load test sounds. */
    protected String _testDir;

    /** The resource manager from which we obtain audio files. */
    protected ResourceManager _rmgr;

    /** The thread that plays sounds. */
    protected Thread _player;

    /** The queue of sound clips to be played. */
    protected Queue _queue = new Queue();

    /** Volume levels for both sound clips and music. */
    protected float _clipVol = 1f, _musicVol = 1f;

    /** The action to take when adjusting music volume. */
    protected int _musicAction = NONE;

    /** The cache of recent audio clips . */
    protected LockableLRUHashMap _clipCache = new LockableLRUHashMap(10);

    /** The clips that are currently active. */
    protected ArrayList _activeClips = new ArrayList();

    /** The stack of songs that we're playing. */
    protected LinkedList _musicStack = new LinkedList();

    /** The current music player, if any. */
    protected MusicPlayer _musicPlayer;

    /** A set of soundTypes for which sound is enabled. */
    protected HashSet _disabledTypes = new HashSet();

    /** A cache of config objects we've created. */
    protected HashMap _configs = new HashMap();

    /** Signals to the queue to do different things. */
    protected Constant PLAY = new Constant("PLAY");
    protected Constant PLAYMUSIC = new Constant("PLAYMUSIC");
    protected Constant STOPMUSIC = new Constant("STOPMUSIC");
    protected Constant UPDATE_MUSIC_VOL = new Constant("UPDATE_MUSIC_VOL");
    protected Constant LOCK = new Constant("LOCK");
    protected Constant UNLOCK = new Constant("UNLOCK");
    protected Constant DIE = new Constant("DIE");

    /** Music action constants. */
    protected static final int NONE = 0;
    protected static final int START = 1;
    protected static final int STOP = 2;

    /** The queue size at which we start to ignore requests to play sounds. */
    protected static final int MAX_QUEUE_SIZE = 25;

    /** The buffer size in bytes used when reading audio file data. */
    protected static final int BUFFER_SIZE = 1024 * 24;

    /** Used to disable sound entirely. */
    protected static final boolean SOUND_ENABLED = false;
}
