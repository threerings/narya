//
// $Id: Keyboard.java,v 1.2 2003/01/23 19:00:44 mdb Exp $

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
