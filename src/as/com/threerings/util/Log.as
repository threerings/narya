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
    /** Log level constants. */
    public static const DEBUG :int = 0;
    public static const INFO :int = 1;
    public static const WARNING :int = 2;
    public static const OFF :int = 3;
    // if you add to this, update LEVEL_NAMES at the bottom...

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
     * Set the log level for the specified package/file.
     *
     * @param spec The smallest prefix desired to configure a log level.
     * For example, you can set the global level with Log.setLevel("", Log.INFO);
     * Then you can Log.setLevel("com.foo.game", Log.DEBUG). Now, everything
     * logs at INFO level except for classes within com.foo.game, which is at DEBUG.
     */
    public static function setLevel (spec :String, level :int) :void
    {
        _setLevels[spec] = level;
        resetLevels();
    }

    /**
     * Parses a String in the form of ":info;com.foo.game:debug;com.bar.util:warning"
     *
     * Semicolons separate modules, colons separate a module name from the log level.
     * An empty string specifies the top-level (global) module.
     */
    public static function setLevels (settingString :String) :void
    {
        for (var module :String in settingString.split(";")) {
            var setting :Array = module.split(":");
            _setLevels[setting[0]] = stringToLevel(String(setting[1]));
        }
        resetLevels();
    }

    /**
     * @private
     */
    public function Log (spec :String)
    {
        if (spec == null) { // what!?
            spec = "";
        }
        _spec = spec;
    }

    /**
     * Log a message with 'debug' priority.
     */
    public function debug (... messages) :void
    {
        doLog(DEBUG, messages);
    }

    /**
     * Log a message with 'info' priority.
     */
    public function info (... messages) :void
    {
        doLog(INFO, messages);
    }

    /**
     * Log a message with 'debug' priority.
     */
    public function warning (... messages) :void
    {
        doLog(WARNING, messages);
    }

    /**
     * Log a message with 'debug' priority.
     */
    public function logStackTrace (error :Error) :void
    {
        warning(error.getStackTrace());
    }

    protected function doLog (level :int, messages :Array) :void
    {
        if (level < getLevel(_spec)) {
            return; // we don't want to log it!
        }
        messages.unshift(getTimeStamp(), LEVEL_NAMES[level], _spec);
        trace.apply(null, messages);

        // possibly also dispatch to any other log targets.
        if (_targets.length > 0) {
            var asOne :String = messages.join(" ");
            for each (var target :LogTarget in _targets) {
                target.log(asOne);
            }
        }
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
     * Get the logging level for the specified spec.
     */
    protected static function getLevel (spec :String) :int
    {
        // we probably already have the level cached for this spec
        var obj :Object = _levels[spec];
        if (obj == null) {
            // cache miss- copy some parent spec's level...
            var modSpec :String = spec;
            do {
                var dex :int = modSpec.lastIndexOf(".");
                if (dex == -1) {
                    modSpec = "";
                } else {
                    modSpec = modSpec.substring(0, dex);
                }
                obj = _levels[modSpec];
                if (obj != null) {
                    // we found the level we should use, copy it and break
                    _levels[spec] = obj;
                    break;
                }
            } while (modSpec != ""); // if we break here, we'll int(null) and return DEBUG..
        }
        return int(obj);
    }

    /**
     * Reset (clear) the log level cache to the set levels.
     */
    protected static function resetLevels () :void
    {
        _levels = {};
        for (var spec :String in _setLevels) {
            _levels[spec] = _setLevels[spec];
        }
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

    /** Our log specification. */
    protected var _spec :String;

    protected static var _targets :Array = [];

    protected static var _levels :Object;

    protected static var _setLevels :Object = { "": DEBUG }; // global: debug
    resetLevels(); // statically reset the levels to the setLevels when the class first starts...

    /** The names of each level. The last one isn't used, it corresponds with OFF. */
    protected static const LEVEL_NAMES :Array = [ "[debug]", "[INFO]", "[WARNING]", false ];
}
}
