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
