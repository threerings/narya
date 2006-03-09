package com.threerings.util {

import mx.logging.ILogger;
import mx.logging.LogEventLevel;

import mx.logging.targets.TraceTarget;

/**
 * Jesus Horseradish Christ. I wanted to just call this class Log, but
 * other classes that don't even import it are getting confused between
 * this and the subclasses with the same name. It's possible that this
 * is some wacky compiler bug.
 */
public class LogDaddy
{
    /**
     * Retrieve the Logger for the specified package name, and ensure
     * that our log target is set up.
     */
    public static function getLogger (pkg :String) :ILogger
    {
        return mx.logging.Log.getLogger(pkg);
    }

    /**
     * Our static (class) initializer.
     */
    private static function staticInit () :void
    {
        var targ :TraceTarget = new TraceTarget();
        targ.filters = ["*"];
        targ.level = LogEventLevel.DEBUG;
        mx.logging.Log.addTarget(targ);
        // we could do some stuff here: set up different targets
        // with different log levels...
    }

    staticInit(); // call our static initializer
}
}
