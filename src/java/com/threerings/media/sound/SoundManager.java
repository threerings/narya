//
// $Id: SoundManager.java,v 1.13 2002/11/15 05:34:43 ray Exp $

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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

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

import com.samskivert.util.LockableLRUHashMap;
import com.samskivert.util.Queue;

import com.threerings.resource.ResourceManager;

import com.threerings.media.Log;

/**
 * Manages the playing of audio files.
 */
// TODO:
//   - fade music out when stopped?
//   - redo volume stuff so that there is SFX and music volume, and then
//   sounds can be turned on/off by SoundType
//   - sounds only seem to loop once. WTF?
public class SoundManager
    implements MetaEventListener
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
        _player = new Thread() {
            public void run () {
                Object command = null;
                SoundType type = null;
                String path = null;

                while (amRunning()) {
                    try {

                        // Get the next command and arguments
                        synchronized (_queue) {
                            command = _queue.get();

                            if (PLAY == command) {
                                type = (SoundType) _queue.get();
                                path = (String) _queue.get();

                            } else if ((PLAYMUSIC == command) ||
                                       (STOPMUSIC == command) ||
                                       (LOCK == command) ||
                                       (UNLOCK == command)) {
                                path = (String) _queue.get();
                            }
                        }

                        // execute the command outside of the queue synch
                        if (PLAY == command) {
                            playSound(type, path);

                        } else if (PLAYMUSIC == command) {
                            playSequence(path);

                        } else if (STOPMUSIC == command) {
                            stopSequence(path);

                        } else if (LOCK == command) {
                            _dataCache.lock(path);
                            getAudioData(path); // preload

                        } else if (UNLOCK == command) {
                            _dataCache.unlock(path);

                        } else if (FLUSH == command) {
                            flushResources(false);
                            // and re-start the resource freer timer
                            // to do it again in 3 seconds
                            _resourceFreer.restart();

                        } else if (DIE == command) {
                            _resourceFreer.stop();
                            flushResources(true);
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
     * Sets the volume of a particular type of sound.
     *
     * @param val a volume parameter between 0f and 1f, inclusive.
     */
    public void setVolume (SoundType type, float val)
    {
        // bound it in
        val = Math.max(0f, Math.min(1f, val));
        _volumes.put(type, new Float(val));
    }

    /**
     * Is the specified soundtype enabled?
     */
    public float getVolume (SoundType type)
    {
        Float vol = (Float) _volumes.get(type);

        // now we're nice to unregistered sounds..
        return (vol != null) ? vol.floatValue() : 1f;
    }

    public void setMusicVolume (float vol)
    {
        // TODO
    }

    public float getMusicVolume ()
    {
        return 1f; // TODO
    }

    /**
     * Optionally lock the sound data prior to playing, to guarantee
     * that it will be quickly available for playing.
     */
    public void lock (String path)
    {
        synchronized (_queue) {
            _queue.append(LOCK);
            _queue.append(path);
        }
    }

    /**
     * Unlock the specified sound so that its resources can be freed.
     */
    public void unlock (String path)
    {
        synchronized (_queue) {
            _queue.append(UNLOCK);
            _queue.append(path);
        }
    }

    /**
     * Play the specified sound of as the specified type of sound.
     * Note that a sound need not be locked prior to playing.
     */
    public void play (SoundType type, String path)
    {
        if (type == null) {
            // let the lazy kids play too
            type = DEFAULT;
        }

        if (_player != null && (0f != getVolume(type))) {
            synchronized (_queue) {
                if (_queue.size() < MAX_QUEUE_SIZE) {
                    _queue.append(PLAY);
                    _queue.append(type);
                    _queue.append(path);

                } else {
                    Log.warning("SoundManager not playing sound because " +
                        "too many sounds in queue [path=" + path +
                        ", type=" + type + "].");
                }
            }
        }
    }

    public void pushMusic (String path)
    {
        synchronized (_queue) {
            _queue.append(PLAYMUSIC);
            _queue.append(path);
        }
    }

    public void removeMusic (String path)
    {
        synchronized (_queue) {
            _queue.append(STOPMUSIC);
            _queue.append(path);
        }
    }

    /**
     * On the SoundManager thread,
     * plays the sound file with the given pathname.
     */
    protected void playSound (SoundType type, String path)
    {
        // see if we can restart a previously used sound that's still
        // hanging out.
        if (restartSound(type, path)) {
            return;
        }

        // get the sound data from our LRU cache
        Object[] stuff = getAudioData(path);
        if (stuff == null) {
            return; // borked!
        }

        try {
            DataLine.Info info = (DataLine.Info) stuff[0];
            AudioInputStream stream = (AudioInputStream) stuff[1];

            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);

            SoundRecord rec = new SoundRecord(path, clip);
            rec.start(type);
            _activeClips.add(rec);

            // and rewind the stream to the beginning for next time
            stream.reset();

        } catch (IOException ioe) {
            Log.warning("Error loading sound file [path=" + path +
                ", e=" + ioe + "].");

        } catch (LineUnavailableException lue) {
            Log.warning("Line not available to play sound [path=" + path +
                ", e=" + lue + "].");
        }
    }

    /**
     * Attempt to reuse a clip that's already been loaded.
     */
    protected boolean restartSound (SoundType type, String path)
    {
        long now = System.currentTimeMillis();

        // we just go through all the sounds. There'll be 32 max, so fuckit.
        for (int ii=0, nn=_activeClips.size(); ii < nn; ii++) {
            SoundRecord rec = (SoundRecord) _activeClips.get(ii);
            if (rec.path.equals(path) && rec.isStoppedSince(now)) {
                rec.restart(type);
                return true;
            }
        }

        return false;
    }

    /**
     * Play a sequence from the specified path.
     */
    protected void playSequence (String path)
    {
        if (_sequencer == null) {
            try {
                Sequencer seq = MidiSystem.getSequencer();
                seq.open();
                if (seq instanceof Synthesizer) {
                    _midiChannels = ((Synthesizer)  seq).getChannels();
                }
                _sequencer = seq;
                _sequencer.addMetaEventListener(this);

            } catch (MidiUnavailableException mue) {
                Log.warning("Midi unavailable. Can't play music.");
                return;
            }
        }

        // stop the existing song
        _sequencer.stop();

        // start the new one
        try {
            _sequencer.setSequence(getResource(path));
            _sequencer.start();
            _midiStack.addFirst(path);

        } catch (InvalidMidiDataException imda) {
            Log.warning("Invalid midi data, not playing [path=" + path + "].");

        } catch (IOException ioe) {
            Log.warning("ioe=" + ioe);
        }
    }

    /**
     * Stop the sequence at the specified path.
     */
    protected void stopSequence (String path)
    {
        if (_midiStack.isEmpty()) {
            return;
        }

        // if we're currently playing this song..
        if (path.equals(_midiStack.getFirst())) {
            // remove it from the stack
            _midiStack.removeFirst();

            if (_midiStack.isEmpty()) {
                // no more to play? Stop and shutdown.
                _sequencer.stop();
                _sequencer.close();
                _sequencer.removeMetaEventListener(this);
                _sequencer = null;
                _midiChannels = null;

            } else {
                // play the next one on the stack (will also stop this one)
                playSequence((String) _midiStack.removeFirst());
            }
        }
    }

    // documentation inherited from interface MetaEventListener
    public void meta (MetaMessage msg)
    {
        if (msg.getType() == MIDI_END_OF_TRACK) {
            // loop that puppy
//            _sequencer.stop();
//            _sequencer.setTickPosition(0);
            _sequencer.start();
        }
//
//        Log.info("meta message: " + msg.getType() + ", msg=" +
//                    new String(msg.getData()));
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
    protected Object[] getAudioData (String path)
    {
        Object[] stuff = (Object[]) _dataCache.get(path);
        if (stuff != null) {
            return stuff;
        }

        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(
                getResource(path));
            DataLine.Info info = new DataLine.Info(
                Clip.class, stream.getFormat());
            stuff = new Object[] { info, stream };
            _dataCache.put(path, stuff);

        } catch (UnsupportedAudioFileException uafe) {
            Log.warning("Unsupported sound format [path=" + path + ", e=" +
                uafe + "].");

        } catch (IOException ioe) {
            Log.warning("Error loading sound file [path=" + path + ", e=" + 
                ioe + "].");
        }

        return stuff;
    }

    /**
     * Get the data specified by the path from the resource bundle.
     * No caching is done.
     */
    protected InputStream getResource (String path)
        throws IOException
    {
        return new ByteArrayInputStream(StreamUtils.streamAsBytes(
                _rmgr.getResource(path), BUFFER_SIZE));
    }

    /**
     * A record to help us manage the use of sound resources.
     * We don't free the resources associated with a clip immediately, because
     * it may be played again shortly.
     */
    protected class SoundRecord
        implements LineListener
    {
        public String path;

        /**
         * Construct a SoundRecord.
         */
        public SoundRecord (String path, Clip clip)
        {
            this.path = path;
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
        public void start (SoundType type)
        {
            adjustVolume(_clip, type);
            _clip.start();
            didStart();
        }

        /**
         * Restart the sound from the beginning.
         */
        public void restart (SoundType type)
        {
// this seems to be unneeded
//            if (_clip.isRunning()) {
//                Log.info("Restarted a sound and sent it a stop..");
//                _clip.stop();
//            }
            _clip.setFramePosition(0);
            start(type);
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
     * Adjust the volume of this clip.
     */
    protected void adjustVolume (Line c, SoundType type)
    {
        if (c.isControlSupported(FloatControl.Type.VOLUME)) {
            try {
                FloatControl vol = (FloatControl) 
                    c.getControl(FloatControl.Type.VOLUME);

                float min = vol.getMinimum();
                float max = vol.getMaximum();

                float ourval = (getVolume(type) * (max - min)) + min;
                Log.debug("adjust vol: [min=" + min + ", ourval=" + ourval +
                    ", max=" + max + "].");
                vol.setValue(ourval);
                return;
            } catch (Exception e) {
                Log.logStackTrace(e);
            }
        }
        // fall back
        adjustVolumeFallback(c, type);
    }

    /**
     * Use the gain control to implement volume.
     */
    protected void adjustVolumeFallback (Line c, SoundType type)
    {
        FloatControl vol = (FloatControl) 
            c.getControl(FloatControl.Type.MASTER_GAIN);

        // the only problem is that gain is specified in decibals,
        // which is a logarithmic scale.
        // Since we want max volume to leave the sample unchanged, our
        // maximum volume translates into a 0db gain.
        float ourSetting = getVolume(type);
        float gain;
        if (ourSetting == 1f) {
            // because our max is a 0db gain, and we're short-circuiting
            // divide-by-zero problems down below
            gain = 0f;

        } else {

            // if our setting isn't 1, we need to scale it by the
            // linear value of the minimum gain.
            // And we have to be very careful with the sign, since
            // it fucks up these log/pow methods.
            double absmin = Math.abs(vol.getMinimum());
            double minlin = Math.pow(10.0, absmin / 20.0);
            double ourval = ((1f - ourSetting) * absmin);

            gain = (float) ((Math.log(ourval) / Math.log(10)) * -20d);
        }

        vol.setValue(gain);
        //Log.info("Set gain: " + gain);
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
    
    /** The cache of recent audio clips . */
    protected LockableLRUHashMap _dataCache = new LockableLRUHashMap(10);

    /** The clips that are currently active. */
    protected ArrayList _activeClips = new ArrayList();

    /** The sequencer that plays midi music. */
    protected Sequencer _sequencer;

    /** The channels in the sequencer, which we'll use to fuxor volumes. */
    protected MidiChannel[] _midiChannels;

    /** The stack of songs that we're playing. */
    protected LinkedList _midiStack = new LinkedList();

    /** A set of soundTypes for which sound is enabled. */
    protected HashMap _volumes = new HashMap();

    /** Signals to the queue to do different things. */
    protected Object PLAY = new Object();
    protected Object PLAYMUSIC = new Object();
    protected Object STOPMUSIC = new Object();
    protected Object LOCK = new Object();
    protected Object UNLOCK = new Object();
    protected Object FLUSH = new Object();
    protected Object DIE = new Object();

    /** This is apparently the midi code for end of track. Wack. */
    protected static final int MIDI_END_OF_TRACK = 47;

    /** Default volume float object if no others found for a sound type. */
    protected static final Float DEFAULT_VOLUME = new Float(1f);

    /** The queue size at which we start to ignore requests to play sounds. */
    protected static final int MAX_QUEUE_SIZE = 100;

    /** The buffer size in bytes used when reading audio file data. */
    protected static final int BUFFER_SIZE = 2048;

    /** How long a clip may linger after stopping before we clear
     * its resources. */
    protected static final long EXPIRE_TIME = 4000L;
}
