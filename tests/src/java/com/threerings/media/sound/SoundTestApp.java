//
// $Id: SoundTestApp.java,v 1.2 2002/11/12 22:55:59 shaper Exp $

package com.threerings.media;

import com.samskivert.util.StringUtil;
import com.threerings.resource.ResourceManager;
import com.threerings.media.Log;

public class SoundTestApp
{
    public SoundTestApp (String[] args)
    {
        if (args.length == 0) {
            Log.info("Usage: runjava com.threerings.media.SoundTestApp " +
                     "<file1> [<file2> <file3> ...]");
            System.exit(0);
        }

        ResourceManager rmgr = new ResourceManager("rsrc");
        _soundmgr = new SoundManager(rmgr);
        _files = args;
    }

    public void run ()
    {
        for (int ii = 0; ii < _files.length; ii++) {
            _soundmgr.play(SoundManager.DEFAULT, _files[ii]);
        }
    }

    public static void main (String[] args)
    {
        SoundTestApp app = new SoundTestApp(args);
        app.run();
    }

    protected String[] _files;
    protected SoundManager _soundmgr;
}
