package {

import flash.utils.getQualifiedClassName;

//import mx.logging.ILogger;
//import mx.logging.LogEventLevel;

//import mx.logging.targets.TraceTarget;

import com.threerings.util.LogTarget;

/**
 * A simple logging mechanism.
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
// TODO: We should really change the name of this class.
// The reason for this is that when we load a client .swf we load it into
// a child ApplicationDomain so that it can share and interoperate with
// a few of our classes. Any classes we define will block classes of the
// same name being used by client swfs, as they'll instead instantiate
// our versions. Normally this isn't a problem because our classnames are
// things like com.threerings.io.Streamable, but this class is just "Log".
// So: we should fix this up.
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
        return new Log(path);
    }

    /**
     * A convenience function for quickly and easily inserting printy
     * statements during application development.
     */
    public static function testing (... params) :void
    {
        var log :Log = new Log("testing");
        log.debug.apply(log, params);
    }

    /**
     * A convenience function for quickly printing a stack trace
     * to the log, useful for debugging.
     */
    public static function dumpStack () :void
    {
        testing(new Error("dumpStack").getStackTrace());
    }

    /**
     * Add a logging target.
     */
    public static function addTarget (target :LogTarget) :void
    {
        _targets.push(target);
    }

    /**
     * Remove a logging target.
     */
    public static function removeTarget (target :LogTarget) :void
    {
        var dex :int = _targets.indexOf(target);
        if (dex != -1) {
            _targets.splice(dex, 1);
        }
    }

    /**
     * @private
     */
    public function Log (spec :String)
    {
        _spec = spec;
    }

    /**
     * Log a message with 'debug' priority.
     */
    public function debug (... messages) :void
    {
        doLog("[debug]", messages);
    }

    /**
     * Log a message with 'info' priority.
     */
    public function info (... messages) :void
    {
        doLog("[INFO]", messages);
    }

    /**
     * Log a message with 'debug' priority.
     */
    public function warning (... messages) :void
    {
        doLog("[WARNING]", messages);
    }

    /**
     * Log a message with 'debug' priority.
     */
    public function logStackTrace (error :Error) :void
    {
        warning(error.getStackTrace());
    }

    protected function doLog (level :String, messages :Array) :void
    {
        // TODO: better Date formatting?
        messages.unshift(new Date().toLocaleTimeString(), level, _spec);
        trace.apply(null, messages);

        // possibly also dispatch to any other log targets.
        if (_targets.length > 0) {
            var asOne :String = messages.join(" ");
            for each (var target :LogTarget in _targets) {
                target.log(asOne);
            }
        }
    }

    /** Our log specification. */
    protected var _spec :String;

    protected static var _targets :Array = [];
}

}
