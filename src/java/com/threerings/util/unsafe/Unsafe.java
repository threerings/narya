//
// $Id$
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

package com.threerings.util.unsafe;

import com.threerings.util.Log;
import com.samskivert.util.RunAnywhere;

/**
 * A native library for doing unsafe things. Don't use this library. If
 * you must ignore that warning, then be sure you use it sparingly and
 * only in very well considered cases.
 */
public class Unsafe
{
    /**
     * Enables or disables garbage collection. <em>Warning:</em> you will
     * be fucked if you leave it disabled for too long. Do not do this
     * unless you are dang sure about what you're doing and are prepared
     * to test your code on every platform this side of Nantucket.
     *
     * <p> Calls to this method do not nest. Regardless of how many times
     * you disable GC, only one call is required to reenable it.
     */
    public static void setGCEnabled (boolean enabled)
    {
        // we don't support nesting, NOOP if the state doesn't change
        if (_loaded && enabled != _gcEnabled) {
            if (_gcEnabled = enabled) {
                enableGC();
            } else {
                disableGC();
            }
        }
    }

    /**
     * Causes the current thread to block for the specified number of
     * milliseconds. This exists primarily to work around the fact that on
     * Linux, {@link Thread#sleep} is only accurate to around 12ms which
     * is wholly unacceptable.
     */
    public static void sleep (int millis)
    {
        if (_loaded && RunAnywhere.isLinux()) {
            nativeSleep(millis);
        } else {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ie) {
                Log.info("Thread.sleep(" + millis + ") interrupted.");
            }
        }
    }

    /**
     * Sets the processes uid to the specified value.
     *
     * @return true if the uid was changed, false if we were unable to do so.
     */
    public static boolean setuid (int uid)
    {
        if (_loaded && !RunAnywhere.isWindows()) {
            return nativeSetuid(uid);
        }
        return false;
    }

    /**
     * Sets the processes uid to the specified value.
     *
     * @return true if the uid was changed, false if we were unable to do so.
     */
    public static boolean setgid (int gid)
    {
        if (_loaded && !RunAnywhere.isWindows()) {
            return nativeSetgid(gid);
        }
        return false;
    }

    /**
     * Reenable garbage collection after a call to {@link #disableGC}.
     */
    protected static native void enableGC ();

    /**
     * Disables garbage collection.
     */
    protected static native void disableGC ();

    /**
     * Sleeps the current thread for the specified number of milliseconds.
     */
    protected static native void nativeSleep (int millis);

    /**
     * Calls through to the native OS system call to change our uid.
     */
    protected static native boolean nativeSetuid (int uid);

    /**
     * Calls through to the native OS system call to change our gid.
     */
    protected static native boolean nativeSetgid (int gid);

    /**
     * Called to initialize our library.
     */
    protected static native boolean init ();

    /** The current state of GC enablement. */
    protected static boolean _gcEnabled = true;

    /** Whether or not we were able to load and initialize our library. */
    protected static boolean _loaded;

    static {
        try {
            System.loadLibrary("unsafe");
            _loaded = init();
        } catch (UnsatisfiedLinkError e) {
            Log.warning("Failed to load 'unsafe' library: " + e + ".");
        }
    }
}
