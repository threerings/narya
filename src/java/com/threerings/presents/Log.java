//
// $Id: Log.java,v 1.5 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents;

/**
 * A placeholder class that contains a reference to the log object used by
 * the Presents services.
 */
public class Log
{
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("presents");

    /** Convenience function. */
    public static void debug (String message)
    {
	log.debug(message);
    }

    /** Convenience function. */
    public static void info (String message)
    {
	log.info(message);
    }

    /** Convenience function. */
    public static void warning (String message)
    {
	log.warning(message);
    }

    /** Convenience function. */
    public static void logStackTrace (Throwable t)
    {
	log.logStackTrace(com.samskivert.util.Log.WARNING, t);
    }
}
