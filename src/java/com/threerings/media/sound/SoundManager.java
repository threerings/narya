//
// $Id: SoundManager.java,v 1.27 2002/11/23 03:24:26 ray Exp $

package com.threerings.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayInputStream;
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
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javax.swing.Timer;

import org.apache.commons.io.StreamUtils;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.LockableLRUHashMap;
import com.samskivert.util.LRUHashMap;
import com.samskivert.util.Queue;

import com.threerings.resource.ResourceManager;

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

        // create a thread to plays sounds and load sound
        // data from the resource manager
        _player = new Thread("narya SoundManager") {
            public void run () {
                Object command = null;
                SoundKey key = null;
                MusicInfo musicInfo = null;

                while (amRunning()) {
                    try {

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
                            // TODO: clean up more stuff.
                            shutdownMusic();
                        }
                    } catch (Exception e) {
                        Log.warning("Captured exception in SoundManager loop.");
                        Log.logStackTrace(e);
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
    public void lock (String set, String path)
    {
        synchronized (_queue) {
            _queue.append(LOCK);
            _queue.append(new SoundKey(set, path));
        }
    }

    /**
     * Unlock the specified sound so that its resources can be freed.
     */
    public void unlock (String set, String path)
    {
        synchronized (_queue) {
            _queue.append(UNLOCK);
            _queue.append(new SoundKey(set, path));
        }
    }

    /**
     * Play the specified sound of as the specified type of sound.
     * Note that a sound need not be locked prior to playing.
     */
    public void play (SoundType type, String set, String path)
    {
        if (type == null) {
            // let the lazy kids play too
            type = DEFAULT;
        }

        if (_player != null && (_clipVol != 0f) && isEnabled(type)) {
            synchronized (_queue) {
                if (_queue.size() < MAX_QUEUE_SIZE) {
                    _queue.append(PLAY);
                    _queue.append(new SoundKey(set, path));

                } else {
                    Log.warning("SoundManager not playing sound because " +
                        "too many sounds in queue [path=" + path +
                        ", type=" + type + "].");
                }
            }
        }
    }

    /**
     * Start playing the specified music repeatedly.
     */
    public void pushMusic (String set, String path)
    {
        pushMusic(set, path, -1);
    }

    /**
     * Start playing music for the specified number of loops.
     */
    public void pushMusic (String set, String path, int numloops)
    {
        synchronized (_queue) {
            _queue.append(PLAYMUSIC);
            _queue.append(new MusicInfo(set, path, numloops));
        }
    }

    /**
     * Remove the specified music from the playlist. If it is currently
     * playing, it will be stopped and the previous song will be started.
     */
    public void removeMusic (String set, String path)
    {
        synchronized (_queue) {
            _queue.append(STOPMUSIC);
            _queue.append(new SoundKey(set, path));
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

            LineSpooler.play(stream, _clipVol);

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
        Class playerClass = getMusicPlayerClass(info.path);

        // shutdown the old player if we're switching music types
        if (! playerClass.isInstance(_musicPlayer)) {
            if (_musicPlayer != null) {
                _musicPlayer.shutdown();
            }

            // set up the new player
            try {
                _musicPlayer = (MusicPlayer) playerClass.newInstance();
                _musicPlayer.init(_rmgr, this);

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
        try {
            _musicPlayer.start(info.set, info.path);
        } catch (Exception e) {
            Log.warning("Error playing music, skipping [e=" + e +
                ", set=" + info.set + ", path=" + info.path + "].");
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
                _musicPlayer.stop();

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
            shutdownMusic();
            break;
        }
    }

    // documentation inherited from interface MusicPlayer.MusicEventListener
    public void musicStopped ()
    {
        handleMusicStopped();
        playTopMusic();
    }

    protected void shutdownMusic ()
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
        byte[] data = (byte[]) _clipCache.get(key);
        if (data == null) {
            data = StreamUtils.streamAsBytes(
                _rmgr.getResource(key.set, key.path), BUFFER_SIZE);
            _clipCache.put(key, data);
        }

        return data;
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
        public static void play (AudioInputStream stream, float volume)
            throws LineUnavailableException
        {
            AudioFormat format = stream.getFormat();
            LineSpooler spooler;

            for (int ii=0, nn=_available.size(); ii < nn; ii++) {
                spooler = (LineSpooler) _available.get(ii);

                // we have this thread remove the spooler if it's dead
                // so that we avoid deadlock conditions
                if (spooler.isDead()) {
                    _available.remove(ii--);
                    nn--;

                } else if (spooler.checkPlay(format, stream, volume)) {
                    return;
                }
            }

            if (_available.size() >= MAX_SPOOLERS) {
                throw new LineUnavailableException("Exceeded maximum number " +
                    "of narya sound spoolers.");
            }

            spooler = new LineSpooler(format);
            _available.add(spooler);
            spooler.checkPlay(format, stream, volume);
            spooler.start();
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
            AudioFormat format, AudioInputStream stream, float volume)
        {
            if (_valid && (_stream == null) && _format.matches(format)) {
                _stream = stream;
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
                    _valid = false;
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

            while (count != -1) {
                try {
                    count = _stream.read(data, 0, data.length);
                } catch (IOException e) {
                    // this shouldn't ever ever happen
                    Log.warning("Error reading clip data! [e=" + e + "].");
                    _stream = null;
                    return;
                }

                if (count >= 0) {
                    _line.write(data, 0, count);
                }
            }

            // SO, I used to just always drain, but I found what appears
            // to be a bug in linux's native implementation of drain
            // that sometimes resulted in the internals of drain
            // going into an infinite loop. Checking to see if the line
            // isActive (engaging in I/O) before calling drain seems
            // to have stopped the problem from happening.
            if (_line.isActive()) {
//                Log.info("Waiting for drain (" + hashCode() + ", active=" +
//                    _line.isActive() + ", running=" + _line.isRunning()+
//                    ") : " + incDrainers());

                // wait for it to play all the way
                _line.drain();

//                Log.info("drained: (" + hashCode() + ") :" + decDrainers());
            }

            // clear it out so that we can wait for more.
            _stream = null;
        }

//        protected static final synchronized int incDrainers ()
//        {
//            return ++drainers;
//        }
//
//        protected static final synchronized int decDrainers ()
//        {
//            return --drainers;
//        }
//        static int drainers = 0;

        /** The format that our line was opened with. */
        protected AudioFormat _format;

        /** The stream we're currently spooling out. */
        protected AudioInputStream _stream;

        /** The line that we spool to. */
        protected SourceDataLine _line;

        /** Are we still active and usable for spooling sounds, or should
         * we be removed. */
        protected boolean _valid = true;

        /** The list of all the currently instantiated spoolers. */
        protected static ArrayList _available = new ArrayList();

        /** The maximum time a spooler will wait for a stream before
         * deciding to shut down. */
        protected static final long MAX_WAIT_TIME = 30000L;

        /** The maximum number of spoolers we'll allow. This is a lot. */
        protected static final int MAX_SPOOLERS = 24;
    }

    /**
     * A key for tracking sounds.
     */
    protected static class SoundKey
    {
        public String path;

        public String set;

        public SoundKey (String set, String path)
        {
            this.set = set;
            this.path = path;
        }

        // documentation inherited
        public String toString ()
        {
            return "SoundKey{set=" + set + ", path=" + path + "}";
        }

        // documentation inherited
        public int hashCode ()
        {
            return path.hashCode() ^ set.hashCode();
        }

        // documentation inherited
        public boolean equals (Object o)
        {
            if (o instanceof SoundKey) {
                SoundKey that = (SoundKey) o;
                return this.path.equals(that.path) &&
                       this.set.equals(that.set);
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

        public MusicInfo (String set, String path, int loops)
        {
            super(set, path);
            this.loops = loops;
        }
    }

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

    /** Signals to the queue to do different things. */
    protected Object PLAY = new Object();
    protected Object PLAYMUSIC = new Object();
    protected Object STOPMUSIC = new Object();
    protected Object UPDATE_MUSIC_VOL = new Object();
    protected Object LOCK = new Object();
    protected Object UNLOCK = new Object();
    protected Object DIE = new Object();

    /** Music action constants. */
    protected static final int NONE = 0;
    protected static final int START = 1;
    protected static final int STOP = 2;

    /** The queue size at which we start to ignore requests to play sounds. */
    protected static final int MAX_QUEUE_SIZE = 25;

    /** The buffer size in bytes used when reading audio file data. */
    protected static final int BUFFER_SIZE = 8192;
}
