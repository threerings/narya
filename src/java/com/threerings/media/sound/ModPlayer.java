//
// $Id: ModPlayer.java,v 1.2 2002/11/20 23:06:33 ray Exp $

package com.threerings.media;

import java.io.DataInputStream;
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
 * Does something extraordinary.
 */
public class ModPlayer
{
    public ModPlayer (ResourceManager rmgr, SoundManager smgr)
    {
        _rmgr = rmgr;
        _smgr = smgr;

        try {
            _device = new NaryaSoundDevice();
        } catch (OutputDeviceException ode) {
            Log.warning("Unable to allocate sound channel for mod playing " +
                "[e=" + ode + "].");
        }
    }

    public void stop ()
    {
        _player = null;
    }

    public void start (String set, String path)
    {
        Module module = null;
        try {
            module = ModuleLoader.read(
                new DataInputStream(_rmgr.getResource(set, path)));
        } catch (IOException ioe) {
            Log.warning("error loading oh shit oh shit: [e=" + ioe + "].");
            return;
        }

        final MicroMod mod = new MicroMod(
            module, _device, new LinearResampler());

        _player = new Thread("narya mod player") {
            public void run () {

                _device.start();
                while ((_player == Thread.currentThread()) &&
                       (mod.getSequenceLoopCount() == 0)) {

                    mod.doRealTimePlayback();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ie) {
                        // WFCares
                    }
                }
                _device.stop();
                _smgr.songStopEvent();
            }
        };

        _player.setDaemon(true);
        _player.start();
    }

    /**
     * Set the volume of the midiplayer.
     * @param vol 0f - 1f (inclusive).
     */
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
    }

    protected Thread _player;
    protected NaryaSoundDevice _device;

    protected ResourceManager _rmgr;
    protected SoundManager _smgr;
}
