//
// $Id: Keyboard.java,v 1.3 2004/08/27 02:20:37 mdb Exp $
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

package com.threerings.util.keybd;

import com.threerings.util.Log;

/**
 * Provides access to the native operating system's auto-repeat keyboard
 * settings.
 */
public class Keyboard
{
    /**
     * Sets whether key auto-repeating is enabled.
     */
    public static native void setKeyRepeat (boolean enabled);

    /**
     * Returns whether key auto-repeating is enabled.
     */
    public static native boolean isKeyRepeatEnabled ();

    /**
     * Tests keyboard functionality.
     */
    public static void main (String[] args)
    {
        boolean enabled = (args.length > 0 && args[0].equals("on"));
        Keyboard.setKeyRepeat(enabled);
    }

    /**
     * Returns whether the native keyboard interface is available. 
     */
    public static boolean isAvailable ()
    {
        return _haveLib;
    }

    /**
     * Initializes the library and returns true if it successfully did so.
     */
    protected static native boolean init ();

    /** Whether the keyboard native library was successfully loaded. */
    protected static boolean _haveLib;

    static {
        try {
            System.loadLibrary("keybd");
            _haveLib = init();
            if (_haveLib) {
                Log.info("Loaded native keyboard library.");
            } else {
                Log.info("Native keyboard library initialization failed.");
            }

        } catch (UnsatisfiedLinkError e) {
            Log.warning("Failed to load native keyboard library " +
                        "[e=" + e + "].");
            _haveLib = false;
        }
    }
}
