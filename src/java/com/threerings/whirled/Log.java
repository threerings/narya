//
// $Id: Log.java,v 1.3 2004/02/25 14:50:28 mdb Exp $

package com.threerings.whirled;

/**
 * A placeholder class that contains a reference to the log object used by
 * the Whirled services.
 */
public class Log
{
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("whirled");

    /** Convenience function. */
    public static boolean debug ()
    {
        return (com.samskivert.util.Log.getLevel() ==
                com.samskivert.util.Log.DEBUG);
    }

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
