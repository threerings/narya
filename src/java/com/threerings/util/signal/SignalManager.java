//
// $Id: SignalManager.java,v 1.1 2003/03/31 02:10:04 mdb Exp $

package com.threerings.util.signal;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.ObserverList;
import com.threerings.util.Log;

/**
 * Uses native code to catch Unix signals and invoke callbacks on a
 * separate signal handler thread. If the native library cannot be loaded,
 * signal handlers will be allowed to be registered but will never be
 * called.
 */
public class SignalManager
{
    /* Hangup (POSIX). */
    public static final int SIGHUP = 1;

    /* Interrupt (ANSI). */
    public static final int SIGINT = 2;

    /* Quit (POSIX). */
    public static final int SIGQUIT = 3;

    /* Illegal instruction (ANSI). */
    public static final int SIGILL = 4;

    /* Trace trap (POSIX). */
    public static final int SIGTRAP = 5;

    /* Abort (ANSI). */
    public static final int SIGABRT = 6;

    /* IOT trap (4.2 BSD). */
    public static final int SIGIOT = 6;

    /* BUS error (4.2 BSD). */
    public static final int SIGBUS = 7;

    /* Floating-point exception (ANSI). */
    public static final int SIGFPE = 8;

    /* Kill, unblockable (POSIX). */
    public static final int SIGKILL = 9;

    /* User-defined signal 1 (POSIX). */
    public static final int SIGUSR1 = 10;

    /* Segmentation violation (ANSI). */
    public static final int SIGSEGV = 11;

    /* User-defined signal 2 (POSIX). */
    public static final int SIGUSR2 = 12;

    /* Broken pipe (POSIX). */
    public static final int SIGPIPE = 13;

    /* Alarm clock (POSIX). */
    public static final int SIGALRM = 14;

    /* Termination (ANSI). */
    public static final int SIGTERM = 15;

    /* Stack fault. */
    public static final int SIGSTKFLT = 16;

    /* Child status has changed (POSIX). */
    public static final int SIGCHLD = 17;

    /* Continue (POSIX). */
    public static final int SIGCONT = 18;

    /* Stop, unblockable (POSIX). */
    public static final int SIGSTOP = 19;

    /* Keyboard stop (POSIX). */
    public static final int SIGTSTP = 20;

    /* Background read from tty (POSIX). */
    public static final int SIGTTIN = 21;

    /* Background write to tty (POSIX). */
    public static final int SIGTTOU = 22;

    /* Urgent condition on socket (4.2 BSD). */
    public static final int SIGURG = 23;

    /* CPU limit exceeded (4.2 BSD). */
    public static final int SIGXCPU = 24;

    /* File size limit exceeded (4.2 BSD). */
    public static final int SIGXFSZ = 25;

    /* Virtual alarm clock (4.2 BSD). */
    public static final int SIGVTALRM = 26;

    /* Profiling alarm clock (4.2 BSD). */
    public static final int SIGPROF = 27;

    /* Window size change (4.3 BSD, Sun). */
    public static final int SIGWINCH = 28;

    /* I/O now possible (4.2 BSD). */
    public static final int SIGIO = 29;

    /* Pollable event occurred (System V). */
    public static final int SIGPOLL = SIGIO;

    /* Power failure restart (System V). */
    public static final int SIGPWR = 30;

     /* Bad system call. */
    public static final int SIGSYS = 31;

    /** Used to dispatch signal notifications. */
    public static interface SignalHandler
    {
        /**
         * Called when the specified signal is received.
         *
         * @return true if the signal handler should remain registered,
         * false if it should be removed.
         */
        public boolean signalReceived (int signal);
    }

    /**
     * Returns true if signal dispatching services are available, false if
     * we could not load our native library.
     */
    public boolean servicesAvailable ()
    {
        return _haveLibrary;
    }

    /**
     * Registers a signal handler for the specified signal.
     */
    public synchronized static void registerSignalHandler (
        int signal, SignalHandler handler)
    {
        ObserverList list =  (ObserverList)_handlers.get(signal);
        if (list == null) {
            _handlers.put(signal, list = new ObserverList(
                              ObserverList.SAFE_IN_ORDER_NOTIFY));
            if (_haveLibrary) {
                activateHandler(signal);
            }
        }
        list.add(handler);

        // make sure the signal dispatcher thread is started
        if (_haveLibrary && _sigdis == null) {
            _sigdis = new Thread() {
                public void run () {
                    Log.info("Dispatching signals...");
                    dispatchSignals();
                }
            };
            _sigdis.setDaemon(true);
            _sigdis.start();
        }
    }

    /**
     * Removes the registration for the specified signal handler.
     */
    public synchronized static void removeSignalHandler (
        int signal, SignalHandler handler)
    {
        ObserverList list = (ObserverList)_handlers.get(signal);
        if (list == null || !list.contains(handler)) {
            Log.warning("Requested to remove non-registered handler " +
                        "[signal=" + signal + ", handler=" + handler + "].");
            return;
        }
        list.remove(handler);
        checkEmpty(signal);
    }

    /**
     * Called by native code when a signal is received.
     */
    protected synchronized static void signalReceived (final int signal)
    {
        // this is hack, but we seem to get a call to our signal handler
        // for each thread if the user presses ctrl-c in the terminal, so
        // we "collapse" those into one call back
        long now = System.currentTimeMillis();
        if (signal == SIGINT) {
            if (_lastINTed == now) {
                return;
            } else {
                _lastINTed = now;
            }
        }
        ObserverList list = (ObserverList)_handlers.get(signal);
        if (list != null) {
            list.apply(new ObserverList.ObserverOp() {
                public boolean apply (Object obs) {
                    return ((SignalHandler)obs).signalReceived(signal);
                }
            });
        }
        checkEmpty(signal);
    }

    /**
     * Called when a signal handler is removed.
     */
    protected static void checkEmpty (int signal)
    {
        ObserverList list = (ObserverList)_handlers.get(signal);
        if (list != null && list.size() == 0) {
            _handlers.remove(signal);
            if (_haveLibrary) {
                deactivateHandler(signal);
            }
        }
    }

    /**
     * Activates the signal handler for the specified signal.
     */
    protected static native void activateHandler (int signal);

    /**
     * Deactivates the signal handler for the specified signal.
     */
    protected static native void deactivateHandler (int signal);

    /**
     * Consigns a Java thread to the task of dispatching signal handler
     * callbacks.
     */
    protected static native void dispatchSignals ();

    /** A mapping from signal number to a list of handlers. */
    protected static HashIntMap _handlers = new HashIntMap();

    /** Our signal dispatcher thread. */
    protected static Thread _sigdis;

    /** Set to true if we successfully load our native library. */
    protected static boolean _haveLibrary;

    /** Used to collapse bogus multiple delivery of SIGINT when the user
     * pressed ctrl-c in the console. */
    protected static long _lastINTed = 0L;

    static {
        try {
            System.loadLibrary("signal");
            _haveLibrary = true;
        } catch (Throwable t) {
            Log.info("Could not load libsignal.so; signal handling disabled.");
        }
    }
}
