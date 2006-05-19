package {

import flash.utils.getQualifiedClassName;

import mx.logging.ILogger;
import mx.logging.LogEventLevel;

import mx.logging.targets.TraceTarget;

/**
 * A simple logging mechanism built on top of the standard mx logging
 * facilities.
 *
 * This class need not be imported.
 *
 * Typical usage for creating a Log to be used by the entire class would be:
 * public class MyClass
 * {
 *     private static const log :Log = Log.getLog(MyClass);
 *     ...
 *
 * OR, if you just need a one-off Log:
 *     protected function doStuff (thingy :Thingy) :void
 *     {
 *          if (thingy == null) {
 *              Log.getLog(this).warn("thiny is null!");
 *              ....
 */
public class Log
{
    /**
     * Retrieve a Log for the specififed class.
     *
     * @param spec can be any Object or Class specifier.
     */
    public static function getLog (spec :*) :Log
    {
        // let's just use the full classname
        var path :String = getQualifiedClassName(spec).replace("::", ".");
        return new Log(mx.logging.Log.getLogger(path));
    }

    /**
     * @private
     */
    public function Log (dest :ILogger)
    {
        _dest = dest;
    }

    /**
     * Log a message with 'debug' priority.
     */
    public function debug (... messages) :void
    {
        _dest.debug.apply(_dest, messages);
    }

    /**
     * Log a message with 'info' priority.
     */
    public function info (... messages) :void
    {
        _dest.info.apply(_dest, messages);
    }

    /**
     * Log a message with 'debug' priority.
     */
    public function warning (... messages) :void
    {
        _dest.warn.apply(_dest, messages);
    }

    /**
     * Log a message with 'debug' priority.
     */
    public function logStackTrace (error :Error) :void
    {
        _dest.warn(error.getStackTrace());
    }

    /**
     * Our static (class) initializer.
     */
    private static function staticInit () :void
    {
        var targ :TraceTarget = new TraceTarget();
        targ.includeCategory = targ.includeDate = targ.includeLevel =
                targ.includeTime = true;
        targ.filters = ["*"]; // TODO
        targ.level = LogEventLevel.DEBUG;
        mx.logging.Log.addTarget(targ);
    }

    staticInit(); // call the static initializer

    /** Our true destination. */
    protected var _dest :ILogger;
}

}
