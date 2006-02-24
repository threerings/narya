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
        if (_targ == null) {
            // goddamn I wish we could just have static initializers
            _targ = new TraceTarget();
            _targ.filters = ["*"];
            _targ.level = LogEventLevel.DEBUG;
            mx.logging.Log.addTarget(_targ);
            // we could do some stuff here: set up different targets
            // with different log levels...
        }

        return mx.logging.Log.getLogger(pkg);
    }

    /** The logging target for all packages. Needed only because we
     * cannot have static initializers, so we need to know in getLogger
     * if we've set up the target yet or not. */
    private static var _targ :TraceTarget;
}
}
