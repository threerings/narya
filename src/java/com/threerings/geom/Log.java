//
// $Id: Log.java,v 1.1 2003/04/28 20:27:52 mdb Exp $

package com.threerings.geom;

/**
 * A placeholder class that contains a reference to the log object used by
 * this package.
 */
public class Log
{
    public static final String PACKAGE = "geom";

    public static com.samskivert.util.Log log =
        new com.samskivert.util.Log(PACKAGE);

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

    public static int getLevel ()
    {
        return log.getLevel(PACKAGE);
    }
}
