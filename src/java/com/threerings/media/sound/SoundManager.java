//
// $Id: SoundManager.java,v 1.25 2002/11/22 19:21:12 ray Exp $

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

                        } else if (FLUSH == command) {
                            flushResources(false);
                            // and re-start the resource freer timer
                            // to do it again in 3 seconds
                            _resourceFreer.restart();

                        } else if (UPDATE_MUSIC_VOL == command) {
                            updateMusicVolume();

                        } else if (DIE == command) {
                            _resourceFreer.stop();
                            flushResources(true);
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

        _resourceFreer.setRepeats(false);
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
        _musicVol = Math.max(0f, Math.min(1f, vol));
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

        if (_player != null && isEnabled(type)) {
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
        // see if we can restart a previously used sound that's still
        // hanging out.
        if (restartSound(key)) {
            return;
        }

        try {
            // get the sound data from our LRU cache
            ClipInfo info = getClipData(key);
            if (info == null) {
                return; // borked!
            }

            Clip clip = (Clip) AudioSystem.getLine(info.info);
            clip.open(info.stream);

            SoundRecord rec = new SoundRecord(key, clip);
            rec.start(_clipVol);
            _activeClips.add(rec);

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
     * Attempt to reuse a clip that's already been loaded.
     */
    protected boolean restartSound (SoundKey key)
    {
        long now = System.currentTimeMillis();

        // we just go through all the sounds. There'll be 32 max, so fuckit.
        for (int ii=0, nn=_activeClips.size(); ii < nn; ii++) {
            SoundRecord rec = (SoundRecord) _activeClips.get(ii);
            if (rec.key.equals(key) && rec.isStoppedSince(now)) {
                rec.restart(_clipVol);
                return true;
            }
        }

        return false;
    }

    /**
     * Play a song from the specified path.
     */
    protected void playMusic (MusicInfo info)
    {
        Log.info("Playing: " + info);
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
        Log.info("Stopping: " + key);
        if (! _musicStack.isEmpty()) {
            MusicInfo current = (MusicInfo) _musicStack.getFirst();

            // if we're currently playing this song..
            if (key.equals(current)) {
                Log.info("is top song!");

                // stop it
                _musicPlayer.stop();

                // remove it from the stack
                _musicStack.removeFirst();
                // start playing the next..
                playTopMusic();

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
     */
    protected void updateMusicVolume ()
    {
        if (_musicPlayer != null) {
            _musicPlayer.setVolume(_musicVol);
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

        for (Iterator iter=_activeClips.iterator(); iter.hasNext(); ) {
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
    protected ClipInfo getClipData (SoundKey key)
        throws IOException, UnsupportedAudioFileException
    {
        ClipInfo info = (ClipInfo) _clipCache.get(key);
        if (info != null) {
            // we are re-using an old stream, make sure to rewind it.
            info.stream.reset();

        } else {
            // set it up and put it in the cache
            AudioInputStream stream = AudioSystem.getAudioInputStream(
                new ByteArrayInputStream(StreamUtils.streamAsBytes(
                _rmgr.getResource(key.set, key.path), BUFFER_SIZE)));
            DataLine.Info dinfo = new DataLine.Info(
                Clip.class, stream.getFormat());
            info = new ClipInfo(dinfo, stream);
            _clipCache.put(key, info);
        }

        return info;
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
     * A record to help us manage the use of sound resources.
     * We don't free the resources associated with a clip immediately, because
     * it may be played again shortly.
     */
    protected static class SoundRecord
        implements LineListener
    {
        public SoundKey key;

        /**
         * Construct a SoundRecord.
         */
        public SoundRecord (SoundKey key, Clip clip)
        {
            this.key = key;
            _clip = clip;

            // The mess:
            // If we restart a sample, we get two stop events, one immediately
            // and one later on.
            // We can't just ignore the first in that case, because once
            // a sample is restarted it seems to not return true
            // for isRunning() (maybe because it's already been reset?)
            // but will continue to generate stop events each time it
            // is started.
            //
            // So- we can't depend on capturing stop events to know
            // when a sound is done playing. Instead we just add the length
            // of the sound to the current time. We fall back on the
            // LineListener method if the clip length is unavailable.
            //
            // The good news is that just adding the length is in many
            // ways a better system. I don't feel confident in doing
            // that right now because I don't know if any sounds
            // will ever return NOT_SPECIFIED.

            long length = _clip.getMicrosecondLength();
            if (length == AudioSystem.NOT_SPECIFIED) {
                Log.info("Length of AudioClip not specified, falling back " +
                        "to listening for stop events.");
                _clip.addLineListener(this);
                _length = AudioSystem.NOT_SPECIFIED;

            } else {
                // convert microseconds to milliseconds, round up.
                _length = (length / 1000L) + 1;
            }
        }

        /**
         * Start playing the sound.
         */
        public void start (float volume)
        {
            adjustVolume(_clip, volume);
            _clip.start();
            didStart();
        }

        /**
         * Restart the sound from the beginning.
         */
        public void restart (float volume)
        {
            // this only gets called after the sound has stopped, so
            // simply rewind and replay.
            _clip.setFramePosition(0);
            start(volume);
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
            if (areListening()) {
                _clip.removeLineListener(this);
            }
            if (_clip.isRunning()) {
                _clip.stop();
            }
            _clip.close();

            // make sure nobody uses this SoundRecord again
            _clip = null;
        }

        /**
         * Indicate that we've started playback of this sound.
         */
        protected void didStart ()
        {
            if (areListening()) {
                // clear out the stamp so that we can't possibly be reaped
                // and since we're listening, we'll eventually call didStop()
                _stamp = Long.MAX_VALUE;

            } else {
                _stamp = System.currentTimeMillis() + _length;
            }
        }

        /**
         * Indicate that we've stopped playback.
         */
        protected void didStop ()
        {
            // no matter what, when we stop, we stop
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
            // this only gets run if we actually need to listen for the
            // stop events.
            if (event.getType() == LineEvent.Type.STOP) {
                didStop();
            }
        }

        /**
         * Are we registered as a LineListener on our clip?
         */
        private final boolean areListening ()
        {
            return _length == AudioSystem.NOT_SPECIFIED;
        }

        /** The timestamp of the moment this clip last stopped playing. */
        protected long _stamp;

        /** The length of the clip, in milliseconds, or
         * AudioSystem.NOT_SPECIFIED if unknown. */
        protected long _length;

        /** The clip we're wrapping. */
        protected Clip _clip;
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
     * A wee helper class that holds clip information in our cache.
     */
    protected static class ClipInfo
    {
        /** The information needed to construct a clip from the stream. */
        public DataLine.Info info;

        /** The data to be used to make the clip. */
        public AudioInputStream stream;

        public ClipInfo (DataLine.Info info, AudioInputStream stream)
        {
            this.info = info;
            this.stream = stream;
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

    /**
     * Every 3 seconds we look for sounds that haven't been used for 4 and
     * free them up.
     */
    protected Timer _resourceFreer = new Timer(3000, 
        new ActionListener() {
            public void actionPerformed (ActionEvent e)
            {
                // request a sweep by the main thread
                _queue.append(FLUSH);
            }
        });

    /** The resource manager from which we obtain audio files. */
    protected ResourceManager _rmgr;

    /** The thread that plays sounds. */
    protected Thread _player;

    /** The queue of sound clips to be played. */
    protected Queue _queue = new Queue();

    /** Volume levels for both sound clips and music. */
    protected float _clipVol = 1f, _musicVol = 1f;
    
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
    protected Object FLUSH = new Object();
    protected Object DIE = new Object();

    /** The queue size at which we start to ignore requests to play sounds. */
    protected static final int MAX_QUEUE_SIZE = 100;

    /** The buffer size in bytes used when reading audio file data. */
    protected static final int BUFFER_SIZE = 2048;

    /** How long a clip may linger after stopping before we clear
     * its resources. */
    protected static final long EXPIRE_TIME = 4000L;
}
