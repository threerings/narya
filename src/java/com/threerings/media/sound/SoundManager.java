//
// $Id: SoundManager.java,v 1.18 2002/11/19 02:24:35 ray Exp $

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
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
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

        initMidi();

        // create a thread to plays sounds and load sound
        // data from the resource manager
        _player = new Thread() {
            public void run () {
                Object command = null;
                String path = null;
                MidiInfo midiInfo = null;

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

                                path = (String) _queue.get();

                            } else if (PLAYMUSIC == command) {
                                midiInfo = (MidiInfo) _queue.get();
                            }
                        }

                        // execute the command outside of the queue synch
                        if (PLAY == command) {
                            playSound(path);

                        } else if (PLAYMUSIC == command) {
                            playSequence(midiInfo);

                        } else if (STOPMUSIC == command) {
                            stopSequence(path);

                        } else if (LOCK == command) {
                            _clipCache.lock(path);
                            getClipData(path); // preload

                        } else if (UNLOCK == command) {
                            _clipCache.unlock(path);

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
                            shutdownMidi();
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

        if (_player != null && isEnabled(type)) {
            synchronized (_queue) {
                if (_queue.size() < MAX_QUEUE_SIZE) {
                    _queue.append(PLAY);
                    _queue.append(path);

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
    public void pushMusic (String path)
    {
        pushMusic(path, -1);
    }

    /**
     * Start playing music for the specified number of loops.
     */
    public void pushMusic (String path, int numloops)
    {
        synchronized (_queue) {
            _queue.append(PLAYMUSIC);
            _queue.append(new MidiInfo(path, numloops));
        }
    }

    /**
     * Remove the specified music from the playlist. If it is currently
     * playing, it will be stopped and the previous song will be started.
     */
    public void removeMusic (String path)
    {
        synchronized (_queue) {
            _queue.append(STOPMUSIC);
            _queue.append(path);
        }
    }

    /**
     * Get a list of alternate midi devices.
     */
    public MidiDevice.Info[] getAlternateMidiDevices ()
    {
        ArrayList infos = new ArrayList();
        CollectionUtil.addAll(infos, MidiSystem.getMidiDeviceInfo());

        // remove the synth/seqs, leaving only hardware midi thingies
        for (Iterator iter=infos.iterator(); iter.hasNext(); ) {
            try {
                MidiDevice dev = MidiSystem.getMidiDevice(
                    (MidiDevice.Info) iter.next());
                if ((dev instanceof Sequencer) ||
                    (dev instanceof Synthesizer)) {
                    iter.remove();
                }
            } catch (MidiUnavailableException mue) {
                iter.remove();
            }
        }

        return (MidiDevice.Info[]) infos.toArray(
            new MidiDevice.Info[infos.size()]);
    }

    /**
     * Attempt to use the alternate midi device for output.
     * Return true if we're using it.
     */
    public boolean useAlternateDevice (MidiDevice.Info devinfo)
    {
        Log.info("Trying alternate device: " + devinfo);
        try {
            MidiDevice dev = MidiSystem.getMidiDevice(devinfo);
            Receiver rec = dev.getReceiver();
            if (rec == null) {
                Log.info("Got no device!");
                return false;
            }
            _stoppingSong = true;
            _sequencer.stop();
            _sequencer.close();

            Receiver old = _sequencer.getTransmitter().getReceiver();
            Log.info("Old receiver: " + old);
            if (old != null) {
                old.close();
            }
            _sequencer.open();

            // THIS DOESN'T WORK.
            // See bug #4347135, specifically notes on the bottom.
            _sequencer.getTransmitter().setReceiver(rec);
            playTopSong();

            // possibly shut down an old receiver
            if (_receiver != null) {
                _receiver.close();
            }
            // set the new receiver
            _receiver = rec;

            return true;

        } catch (MidiUnavailableException mue) {
            Log.warning("Use of alternate device failed [e=" + mue +
                ", device=" + devinfo + "].");
            return false;
        }
    }

    /**
     * On the SoundManager thread,
     * plays the sound file with the given pathname.
     */
    protected void playSound (String path)
    {
        // see if we can restart a previously used sound that's still
        // hanging out.
        if (restartSound(path)) {
            return;
        }

        try {
            // get the sound data from our LRU cache
            ClipInfo info = getClipData(path);
            if (info == null) {
                return; // borked!
            }

            Clip clip = (Clip) AudioSystem.getLine(info.info);
            clip.open(info.stream);

            SoundRecord rec = new SoundRecord(path, clip);
            rec.start(_clipVol);
            _activeClips.add(rec);

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
     * Attempt to reuse a clip that's already been loaded.
     */
    protected boolean restartSound (String path)
    {
        long now = System.currentTimeMillis();

        // we just go through all the sounds. There'll be 32 max, so fuckit.
        for (int ii=0, nn=_activeClips.size(); ii < nn; ii++) {
            SoundRecord rec = (SoundRecord) _activeClips.get(ii);
            if (rec.path.equals(path) && rec.isStoppedSince(now)) {
                rec.restart(_clipVol);
                return true;
            }
        }

        return false;
    }

    /**
     * Play a sequence from the specified path.
     */
    protected void playSequence (MidiInfo info)
    {
        stopCurrentSong(false);
        _midiStack.addFirst(info);
        playTopSong();
    }

    /**
     * Start the specified sequence.
     */
    protected void playTopSong ()
    {
        if (_midiStack.isEmpty()) {
            return;
        }

        MidiInfo info = (MidiInfo) _midiStack.getFirst();

        // start the new one
        try {
            _sequencer.setSequence(getMidiData(info.path));
            if (info.msPosition != -1) {
                // TODO: this doesn't work correctly
                _sequencer.setTickPosition(info.tickPosition);
                _sequencer.setMicrosecondPosition(info.msPosition);
                info.msPosition = -1;
            }
            _sequencer.start();
            //updateMusicVolume();
            //Log.info("Now playing : " + info.path);

        } catch (InvalidMidiDataException imda) {
            Log.warning("Invalid midi data, not playing [path=" +
                info.path + "].");

        } catch (IOException ioe) {
            Log.warning("ioe=" + ioe);
        }
    }

    /**
     * Stop whatever song is currently playing and deal with the
     * MidiInfo associated with it.
     */
    protected void stopCurrentSong (boolean wasStopped)
    {
        if (_midiStack.isEmpty()) {
            return;
        }

        // stop the existing song
        if (!wasStopped) {
            // TODO: fade?
            _stoppingSong = true;
            _sequencer.stop();
        }

        // see what was playing
        MidiInfo current = (MidiInfo) _midiStack.getFirst();

        switch (current.loops) {
        default:
            current.loops--;
            break;

        case 1:
            // sorry charlie
            _midiStack.removeFirst();
            break;

        case -1:
            // save info...
            // TODO: this doesn't seem to work, and is out-n-out broken
            // if we use the tickPos
            current.msPosition = _sequencer.getMicrosecondPosition();
            current.tickPosition = _sequencer.getTickPosition();
            break;
        }
    }

    /**
     * Stop the sequence at the specified path.
     */
    protected void stopSequence (String path)
    {
        if (! _midiStack.isEmpty()) {
            MidiInfo current = (MidiInfo) _midiStack.getFirst();

            // if we're currently playing this song..
            if (path.equals(current.path)) {
                // stop it
                _stoppingSong = true;
                _sequencer.stop();
                // remove it from the stack
                _midiStack.removeFirst();
                // start playing the next..
                playTopSong();

            } else {
                // we aren't currently playing this song. Simply remove.
                for (Iterator iter=_midiStack.iterator(); iter.hasNext(); ) {
                    if (path.equals(((MidiInfo) iter.next()).path)) {
                        iter.remove();
                        return;
                    }
                }

            }
        }

        Log.debug("Sequence stopped that wasn't in the stack anymore " +
            "[path=" + path + "].");
    }

    /**
     * Initialize the midi system.
     */
    protected void initMidi ()
    {
        try {
            Sequencer seq = MidiSystem.getSequencer();
            seq.open();
            if (seq instanceof Synthesizer) {
                _midiChannels = ((Synthesizer) seq).getChannels();
                //updateMusicVolume();
            }
            _sequencer = seq;
            _sequencer.addMetaEventListener(this);

        } catch (MidiUnavailableException mue) {
            Log.warning("Midi unavailable. Can't play music.");
            return;
        }
    }

    /**
     * Stop playing and shutdown the midi system.
     */
    protected void shutdownMidi ()
    {
        _sequencer.removeMetaEventListener(this);
        stopCurrentSong(false);
        _sequencer.close();
        if (_receiver != null) {
            _receiver.close();
        }
        _sequencer = null;
        _midiChannels = null;
        _midiStack.clear();
    }

    /**
     * Attempt to modify the music volume for any playing tracks.
     */
    protected void updateMusicVolume ()
    {
        if (_midiChannels == null) {
            Log.warning("Cannot modify music volume!");

        } else {
            int setting = (int) (_musicVol * 127.0);
            for (int ii=0; ii < _midiChannels.length; ii++) {
                _midiChannels[ii].controlChange(MIDI_VOLUME_CONTROL, setting);
            }
        }
    }

    // documentation inherited from interface MetaEventListener
    public void meta (MetaMessage msg)
    {
//        Log.info("meta message: " + msg.getType() + ", msg=" +
//                    new String(msg.getData()));

        if (msg.getType() == MIDI_END_OF_TRACK) {

            if (_stoppingSong) {
                _stoppingSong = false;
            } else {
                stopCurrentSong(true);
                playTopSong();
            }
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
    protected ClipInfo getClipData (String path)
        throws IOException, UnsupportedAudioFileException
    {
        ClipInfo info = (ClipInfo) _clipCache.get(path);
        if (info != null) {
            // we are re-using an old stream, make sure to rewind it.
            info.stream.reset();

        } else {
            // set it up and put it in the cache
            AudioInputStream stream = AudioSystem.getAudioInputStream(
                getResource(path));
            DataLine.Info dinfo = new DataLine.Info(
                Clip.class, stream.getFormat());
            info = new ClipInfo(dinfo, stream);
            _clipCache.put(path, info);
        }

        return info;
    }

    /**
     * Get the midi data for the specified path.
     */
    protected InputStream getMidiData (String path)
        throws IOException
    {
        InputStream stream = (InputStream) _midiCache.get(path);
        if (stream != null) {
            // reset the stream for the new user
            stream.reset();

        } else {
            stream = getResource(path);
            _midiCache.put(path, stream);
        }

        return stream;
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
    protected static class SoundRecord
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
        public void start (float volume)
        {
            adjustVolume(volume);
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

//        /**
//         * Adjust the volume of this clip.
//         */
//        protected void adjustVolume (float volume)
//        {
//            if (_clip.isControlSupported(FloatControl.Type.VOLUME)) {
//                FloatControl vol = (FloatControl) 
//                    _clip.getControl(FloatControl.Type.VOLUME);
//
//                float min = vol.getMinimum();
//                float max = vol.getMaximum();
//
//                float ourval = (volume * (max - min)) + min;
//                Log.debug("adjust vol: [min=" + min + ", ourval=" + ourval +
//                    ", max=" + max + "].");
//                vol.setValue(ourval);
//
//            } else {
//                // fall back
//                adjustVolumeFallback(vol);
//            }
//        }

        /**
         * Use the gain control to implement volume.
         */
        protected void adjustVolume (float vol)
        {
            FloatControl control = (FloatControl) 
                _clip.getControl(FloatControl.Type.MASTER_GAIN);

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

        /** The timestamp of the moment this clip last stopped playing. */
        protected long _stamp;

        /** The length of the clip, in milliseconds, or
         * AudioSystem.NOT_SPECIFIED if unknown. */
        protected long _length;

        /** The clip we're wrapping. */
        protected Clip _clip;
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
     * A class that tracks the information about our playing midi files.
     */
    protected static class MidiInfo
    {
        /** The path, duh. */
        public String path;

        /** How many times to loop, or -1 for forever. */
        public int loops;

        /** The position of big loopers, or -1 if none. */
        public long tickPosition = -1;
        public long msPosition = -1;

        public MidiInfo (String path, int loops)
        {
            this.path = path;
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
    
    /** So that we ignore the stopped meta event when we stop a song
     * ourselves. */
    protected boolean _stoppingSong = false;

    /** Volume levels for both sound clips and music. */
    protected float _clipVol = 1f, _musicVol = 1f;
    
    /** The cache of recent audio clips . */
    protected LockableLRUHashMap _clipCache = new LockableLRUHashMap(10);

    /** The cache of recent midi sequences. */
    protected LRUHashMap _midiCache = new LRUHashMap(4);

    /** The clips that are currently active. */
    protected ArrayList _activeClips = new ArrayList();

    /** The sequencer that plays midi music. */
    protected Sequencer _sequencer;

    /** The receiver used to send midi from the sequencer to an alternate
     * midi device. */
    protected Receiver _receiver;

    /** The channels in the sequencer, which we'll use to fuxor volumes. */
    protected MidiChannel[] _midiChannels;

    /** The stack of songs that we're playing. */
    protected LinkedList _midiStack = new LinkedList();

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

    /** This is apparently the midi code for end of track. Wack. */
    protected static final int MIDI_END_OF_TRACK = 47;

    /** The midi control for volume is 7. Ooooooo. */
    protected static final int MIDI_VOLUME_CONTROL = 7;

    /** The queue size at which we start to ignore requests to play sounds. */
    protected static final int MAX_QUEUE_SIZE = 100;

    /** The buffer size in bytes used when reading audio file data. */
    protected static final int BUFFER_SIZE = 2048;

    /** How long a clip may linger after stopping before we clear
     * its resources. */
    protected static final long EXPIRE_TIME = 4000L;
}
