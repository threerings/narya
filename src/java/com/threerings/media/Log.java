//
// $Id: Log.java,v 1.3 2004/02/25 14:43:17 mdb Exp $

package com.threerings.media;

/**
 * A placeholder class that contains a reference to the log object used by
 * the media services package.
 */
public class Log
{
    public static final String PACKAGE = "media";

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
        return com.samskivert.util.Log.getLevel(PACKAGE);
    }
}
