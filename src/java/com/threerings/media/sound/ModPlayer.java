//
// $Id: ModPlayer.java,v 1.1 2002/11/20 04:03:09 ray Exp $

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
    }

    public void stop ()
    {
        _player = null;
    }

    public void start (String set, String path)
    {
        PCM16StreamOutputDevice devvy = null;

        try {
            devvy = new JavaSoundOutputDevice(
                new SS16LEAudioFormatConverter(), 44100, 1000);
        } catch (OutputDeviceException ode) {
            Log.warning("Oh we're fucked with the mod [e=" + ode + "].");
            return;
        }

        Module module = null;
        try {
            module = ModuleLoader.read(
                new DataInputStream(_rmgr.getResource(set, path)));
        } catch (IOException ioe) {
            Log.warning("error loading oh shit oh shit: [e=" + ioe + "].");
            return;
        }


        final PCM16StreamOutputDevice outdev = devvy;
        final MicroMod mod = new MicroMod(
            module, outdev, new LinearResampler());

        _player = new Thread("narya mod player") {
            public void run () {

                outdev.start();
                while ((_player == Thread.currentThread()) &&
                       (mod.getSequenceLoopCount() == 0)) {

                    mod.doRealTimePlayback();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ie) {
                        // WFCares
                    }
                }
                outdev.stop();
                _smgr.songStopEvent();
            }
        };

        _player.setDaemon(true);
        _player.start();
    }

    protected Thread _player;

    protected ResourceManager _rmgr;
    protected SoundManager _smgr;
}
