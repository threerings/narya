//
// $Id: SoundTestApp.java,v 1.5 2003/04/17 19:21:17 mdb Exp $

package com.threerings.media.sound;

import com.samskivert.util.StringUtil;
import com.threerings.resource.ResourceManager;
import com.threerings.media.Log;

public class SoundTestApp
{
    public SoundTestApp (String[] args)
    {
        if (args.length == 0) {
            Log.info("Usage: runjava com.threerings.media.SoundTestApp " +
                     "<key1> [<key2> <key3> ...]");
            System.exit(0);
        }

        ResourceManager rmgr = new ResourceManager("rsrc");
        _soundmgr = new SoundManager(rmgr, null, null);
        _keys = args;
    }

    public void run ()
    {
        for (int ii = 0; ii < _keys.length; ii++) {
            System.out.println("Playing " + _keys[ii] + ".");
            _soundmgr.play(SoundManager.DEFAULT,
                           "com/threerings/media/sound/", _keys[ii]);
        }
        _soundmgr.shutdown();

        // the sound manager starts up threads that never seem to exit so
        // we have to stick a fork in things after a short while
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException ie) {
        }
        System.exit(0);
    }

    public static void main (String[] args)
    {
        SoundTestApp app = new SoundTestApp(args);
        app.run();
    }

    protected String[] _keys;
    protected SoundManager _soundmgr;
}
