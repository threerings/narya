//
// $Id: ModPlayer.java,v 1.5 2002/11/26 02:39:40 ray Exp $

package com.threerings.media;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

import micromod.MicroMod;
import micromod.Module;
import micromod.ModuleLoader;
import micromod.output.JavaSoundOutputDevice;
import micromod.output.OutputDeviceException;
import micromod.output.PCM16StreamOutputDevice;
import micromod.output.converters.SS16LEAudioFormatConverter;
import micromod.resamplers.LinearResampler;

import com.threerings.resource.ResourceManager;

/**
 * A player that plays .mod format music.
 */
public class ModPlayer extends MusicPlayer
{
    // documentation inherited
    public void init ()
        throws Exception
    {
        _device = new NaryaSoundDevice();
        _device.start();
    }

    // documentation inherited
    public void shutdown ()
    {
        _device.stop();
    }

    // documentation inherited
    public void start (InputStream stream)
        throws Exception
    {
        Module module = ModuleLoader.read(new DataInputStream(stream));

        final MicroMod mod = new MicroMod(
            module, _device, new LinearResampler());

        _player = new Thread("narya mod player") {
            public void run () {

                while (mod.getSequenceLoopCount() == 0) {

                    mod.doRealTimePlayback();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ie) {
                        // WFCares
                    }

                    if (_player != Thread.currentThread()) {
                        // we were stopped!
                        return;
                    }
                }
                _device.drain();
                _musicListener.musicStopped();
            }
        };

        _player.setDaemon(true);
        _player.start();
    }

    // documentation inherited
    public void stop ()
    {
        _player = null;
    }

    // documentation inherited
    public void setVolume (float vol)
    {
        _device.setVolume(vol);
    }

    /**
     * A class that allows us to access the dataline so we can adjust
     * the volume.
     */
    protected static class NaryaSoundDevice extends JavaSoundOutputDevice
    {
        public NaryaSoundDevice ()
            throws OutputDeviceException
        {
            super(new SS16LEAudioFormatConverter(), 44100, 1000);
        }

        /**
         * Adjust the volume of the line that we're sending our mod data to.
         */
        public void setVolume (float vol)
        {
            SoundManager.adjustVolume(sourceDataLine, vol);
        }

        /**
         * Access the drain method of the line.
         */
        public void drain ()
        {
            sourceDataLine.drain();
        }
    }

    /** The thread that does the work. */
    protected Thread _player;

    /** The sound output device. */
    protected NaryaSoundDevice _device;
}
