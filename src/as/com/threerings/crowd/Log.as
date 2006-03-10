package com.threerings.crowd {

import mx.logging.ILogger;

import com.threerings.util.LogDaddy;

public class Log extends LogDaddy
{
    /** The Logger for this package. */
    public static var log :ILogger = getLogger("crowd");

    /** Convenience function. */
    public static function debug (message :String, ... rest) :void
    {
        log.debug(message, rest);
    }

    /** Convenience function. */
    public static function info (message :String, ... rest) :void
    {
        log.info(message, rest);
    }

    /** Convenience function. */
    public static function warning (message :String, ... rest) :void
    {
        log.warn(message, rest);
    }

    /** Convenience function. */
    public static function logStackTrace (err :Error) :void
    {
        log.warn(err.getStackTrace());
    }
}
}
