//
// $Id: Log.java,v 1.1 2001/07/18 21:16:12 mdb Exp $

package com.threerings.resource;

/**
 * A placeholder class that contains a reference to the log object used by
 * the resource management package.
 */
public class Log
{
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("resource");

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
