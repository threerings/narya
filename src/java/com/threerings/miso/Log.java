//
// $Id: Log.java,v 1.1 2001/07/12 22:38:03 shaper Exp $

package com.threerings.cocktail.miso;

/**
 * A placeholder class that contains a reference to the log object used by
 * the Spine package.
 */
public class Log
{
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("miso");

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
