//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.util {

import flash.utils.getQualifiedClassName;

/**
 * A simple logging mechanism.
 *
 * Log instances are created for modules, and the logging level can be configured per
 * module in a hierarchical fashion.
 *
 * Typically, you should create a module name based on the full path to a class:
 * calling getLog() and passing an object or Class will do this. Alternattely, you
 * may create a Log to share in several classes in a package, in which case the
 * module name can be like "com.foocorp.games.bunnywar". Finally, you can just
 * create made-up module names like "mygame" or "util", but this is not recommended.
 * You really should name things based on your packages, and your packages should be
 * named according to Sun's recommendations for Java packages.
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
 *          if (!isValid(thingy)) {
 *              Log.getLog(this).warn("Invalid thingy specified", "thingy", thingy);
 *              ....
 */
public class Log
{
    /** Log level constants. */
    public static const DEBUG :int = 0;
    public static const INFO :int = 1;
    public static const WARNING :int = 2;
    public static const OFF :int = 3;
    // if you add to this, update LEVEL_NAMES at the bottom...

    /**
     * Retrieve a Log for the specified module.
     *
     * @param moduleSpec can be a String of the module name, or any Object or Class to
     * have the module name be the full package and name of the class (recommended).
     */
    public static function getLog (moduleSpec :*) :Log
    {
        const module :String = (moduleSpec is String) ? String(moduleSpec)
            : getQualifiedClassName(moduleSpec).replace("::", ".");
        return new Log(module);
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
     * Set the log level for the specified module.
     *
     * @param module The smallest prefix desired to configure a log level.
     * For example, you can set the global level with Log.setLevel("", Log.INFO);
     * Then you can Log.setLevel("com.foo.game", Log.DEBUG). Now, everything
     * logs at INFO level except for modules within com.foo.game, which is at DEBUG.
     */
    public static function setLevel (module :String, level :int) :void
    {
        _setLevels[module] = level;
        _levels = {}; // reset cached levels
    }

    /**
     * Parses a String in the form of ":info;com.foo.game:debug;com.bar.util:warning"
     *
     * Semicolons separate modules, colons separate a module name from the log level.
     * An empty string specifies the top-level (global) module.
     */
    public static function setLevels (settingString :String) :void
    {
        for each (var module :String in settingString.split(";")) {
            var setting :Array = module.split(":");
            _setLevels[setting[0]] = stringToLevel(String(setting[1]));
        }
        _levels = {}; // reset cached levels
    }

    /**
     * Use Log.getLog();
     *
     * @private
     */
    public function Log (module :String)
    {
        if (module == null) module = "";
        _module = module;
    }

    /**
     * Log a message with 'debug' priority.
     *
     * @param args The first argument is the actual message to log. After that, each pair
     * of parameters is printed in key/value form, the benefit being that if no log
     * message is generated then toString() will not be called on the values.
     * A final parameter may be an Error, in which case the stack trace is printed.
     */
    public function debug (... args) :void
    {
        doLog(DEBUG, args);
    }

    /**
     * Log a message with 'info' priority.
     *
     * @param args The first argument is the actual message to log. After that, each pair
     * of parameters is printed in key/value form, the benefit being that if no log
     * message is generated then toString() will not be called on the values.
     * A final parameter may be an Error, in which case the stack trace is printed.
     */
    public function info (... args) :void
    {
        doLog(INFO, args);
    }

    /**
     * Log a message with 'warning' priority.
     *
     * @param args The first argument is the actual message to log. After that, each pair
     * of parameters is printed in key/value form, the benefit being that if no log
     * message is generated then toString() will not be called on the values.
     * A final parameter may be an Error, in which case the stack trace is printed.
     */
    public function warning (... args) :void
    {
        doLog(WARNING, args);
    }

    /**
     * Log just a stack trace with 'warning' priority.
     */
    public function logStackTrace (error :Error) :void
    {
        warning(error.getStackTrace());
    }

    protected function doLog (level :int, args :Array) :void
    {
        if (level < getLevel(_module)) {
            return; // we don't want to log it!
        }
        var logMessage :String = formatMessage(level, args);
        trace(logMessage);
        // possibly also dispatch to any other log targets.
        for each (var target :LogTarget in _targets) {
            target.log(logMessage);
        }
    }

    protected function formatMessage (level :int, args :Array) :String
    {
        var msg :String = getTimeStamp() + " " + LEVEL_NAMES[level] + ": " + _module;
        if (args.length > 0) {
            msg += " " + String(args[0]); // the primary log message
            var err :Error = null;
            if (args.length % 2 == 0) { // there's one extra arg
                var lastArg :Object = args.pop();
                if (lastArg is Error) {
                    err = lastArg as Error; // ok, it's an error, we like those
                } else {
                    args.push(lastArg, ""); // what? Well, cope by pushing it back with a ""
                }
            }
            if (args.length > 1) {
                for (var ii :int = 1; ii < args.length; ii += 2) {
                    msg += (ii == 1) ? " [" : ", ";
                    msg += String(args[ii]) + "=" + String(args[ii + 1]);
                }
                msg += "]";
            }
            if (err != null) {
                msg += "\n" + err.getStackTrace();
            }
        }
        return msg;
    }

    protected function getTimeStamp () :String
    {
        var d :Date = new Date();
        // return d.toLocaleTimeString();

        // format it like the date format in our java logs
        return d.fullYear + "/" +
            StringUtil.prepad(String(d.month + 1), 2, "0") + "/" +
            StringUtil.prepad(String(d.date), 2, "0") + " " +
            StringUtil.prepad(String(d.hours), 2, "0") + ":" +
            StringUtil.prepad(String(d.minutes), 2, "0") + ":" +
            StringUtil.prepad(String(d.seconds), 2, "0") + ":" +
            StringUtil.prepad(String(d.milliseconds), 3, "0");
    }

    /**
     * Get the logging level for the specified module.
     */
    protected static function getLevel (module :String) :int
    {
        // we probably already have the level cached for this module
        var lev :Object = _levels[module];
        if (lev == null) {
            // cache miss- copy some parent module's level...
            var ancestor :String = module;
            while (true) {
                lev = _setLevels[ancestor];
                if (lev != null || ancestor == "") {
                    // bail if we found a setting or get to the top level,
                    // but always save the level from _setLevels into _levels
                    _levels[module] = int(lev); // if lev was null, this will become 0 (DEBUG)
                    break;
                }
                var dex :int = ancestor.lastIndexOf(".");
                ancestor = (dex == -1) ? "" : ancestor.substring(0, dex);
            }
        }
        return int(lev);
    }

    protected static function stringToLevel (s :String) :int
    {
        switch (s.toLowerCase()) {
        default: // default to DEBUG
        case "debug": return DEBUG;
        case "info": return INFO;
        case "warning": return WARNING;
        case "off": return OFF;
        }
    }

    /** The module to which this log instance applies. */
    protected var _module :String;

    /** Other registered LogTargets, besides the trace log. */
    protected static var _targets :Array = [];

    /** A cache of log levels, copied from _setLevels. */
    protected static var _levels :Object = {};

    /** The configured log levels. */
    protected static var _setLevels :Object = { "": DEBUG }; // global: debug

    /** The names of each level. The last one isn't used, it corresponds with OFF. */
    protected static const LEVEL_NAMES :Array = [ "[debug]", "[INFO]", "[WARNING]", false ];
}
}
