//
// $Id: SoundTestApp.java,v 1.7 2004/08/27 02:20:58 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.media.sound;

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
