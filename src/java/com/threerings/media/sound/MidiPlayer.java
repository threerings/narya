//
// $Id: MidiPlayer.java,v 1.4 2003/01/08 04:09:02 mdb Exp $

package com.threerings.media.sound;

import java.io.BufferedInputStream;
import java.io.InputStream;

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

/**
 * Plays midi/rmf sounds using Java's sequencer, which is susceptible
 * to the accuracy of System.currentTimeMillis() and so currently sounds
 * like "ass" under Windows.
 */
public class MidiPlayer extends MusicPlayer
    implements MetaEventListener
{
    // documentation inherited
    public void init ()
        throws Exception
    {
        _sequencer = MidiSystem.getSequencer();
        _sequencer.open();
        if (_sequencer instanceof Synthesizer) {
            _channels = ((Synthesizer) _sequencer).getChannels();
        }
    }

    // documentation inherited
    public void shutdown ()
    {
        _sequencer.close();
    }

    // documentation inherited
    public void start (InputStream stream)
        throws Exception
    {
        _sequencer.setSequence(new BufferedInputStream(stream));
        _sequencer.start();
        _sequencer.addMetaEventListener(this);
    }

    // documentation inherited
    public void stop ()
    {
        _sequencer.removeMetaEventListener(this);
        _sequencer.stop();
    }

    // documentation inherited
    public void setVolume (float volume)
    {
        if (_channels != null) {
            int setting = (int) (volume * 127.0);
            for (int ii=0; ii < _channels.length; ii++) {
                _channels[ii].controlChange(VOLUME_CONTROL, setting);
            }
        }
    }

    // documentation inherited from interface MetaEventListener
    public void meta (MetaMessage msg)
    {
        if (msg.getType() == END_OF_TRACK) {
            _musicListener.musicStopped();
        }
    }

// STUFF FROM ANOTHER TIME
//    /**
//     * Get a list of alternate midi devices.
//     */
//    public MidiDevice.Info[] getAlternateMidiDevices ()
//    {
//        ArrayList infos = new ArrayList();
//        CollectionUtil.addAll(infos, MidiSystem.getMidiDeviceInfo());
//
//        // remove the synth/seqs, leaving only hardware midi thingies
//        for (Iterator iter=infos.iterator(); iter.hasNext(); ) {
//            try {
//                MidiDevice dev = MidiSystem.getMidiDevice(
//                    (MidiDevice.Info) iter.next());
//                if ((dev instanceof Sequencer) ||
//                    (dev instanceof Synthesizer)) {
//                    iter.remove();
//                }
//            } catch (MidiUnavailableException mue) {
//                iter.remove();
//            }
//        }
//
//        return (MidiDevice.Info[]) infos.toArray(
//            new MidiDevice.Info[infos.size()]);
//    }
//
//    /**
//     * Attempt to use the alternate midi device for output.
//     * Return true if we're using it.
//     */
//    public boolean useAlternateDevice (MidiDevice.Info devinfo)
//    {
//        Log.info("Trying alternate device: " + devinfo);
//        try {
//            MidiDevice dev = MidiSystem.getMidiDevice(devinfo);
//            Receiver rec = dev.getReceiver();
//            if (rec == null) {
//                Log.info("Got no device!");
//                return false;
//            }
//            _stoppingSong = true;
//            _sequencer.stop();
//            _sequencer.close();
//
//            Receiver old = _sequencer.getTransmitter().getReceiver();
//            Log.info("Old receiver: " + old);
//            if (old != null) {
//                old.close();
//            }
//            _sequencer.open();
//
//            // THIS DOESN'T WORK.
//            // See bug #4347135, specifically notes on the bottom.
//            _sequencer.getTransmitter().setReceiver(rec);
//            playTopSong();
//
//            // possibly shut down an old receiver
//            if (_receiver != null) {
//                _receiver.close();
//            }
//            // set the new receiver
//            _receiver = rec;
//
//            return true;
//
//        } catch (MidiUnavailableException mue) {
//            Log.warning("Use of alternate device failed [e=" + mue +
//                ", device=" + devinfo + "].");
//            return false;
//        }
//    }

    /** This is apparently the midi code for end of track. Wack. */
    protected static final int END_OF_TRACK = 47;

    /** The midi control for volume is 7. Ooooooo. */
    protected static final int VOLUME_CONTROL = 7;

    /** The sequencer. */
    protected Sequencer _sequencer;

    /** The channels in the sequencer, which we'll use to fuxor volumes. */
    protected MidiChannel[] _channels;
}
