//
// $Id: Keyboard.java,v 1.1 2003/01/12 00:26:39 shaper Exp $

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

    /** Whether the keyboard native library was successfully loaded. */
    protected static boolean _haveLib;

    static {
        try {
            System.loadLibrary("keybd");
            _haveLib = true;
            Log.info("Loaded native keyboard library.");

        } catch (UnsatisfiedLinkError e) {
            Log.warning("Failed to load native keyboard library " +
                        "[e=" + e + "].");
            _haveLib = false;
        }
    }
}
