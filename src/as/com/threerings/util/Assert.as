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

import flash.system.Capabilities;

/**
 * Simple implementation of assertion checks for debug environments.
 * When running in a debug player, each function will test the assert expression,
 * and if it fails, log an error message with a stack trace. When running in a
 * release player, these functions do not run any tests, and exit immediately.
 *
 * Note: stack dumping is controlled via the Assert.dumpStack parameter.
 *
 * Usage example:
 * <pre>
 *   Assert.isNotNull(mystack.top());
 *   Assert.isTrue(mystack.length == 1, "Unexpected number of items on stack!");
 * </pre>
 */
public class Assert
{
    /** Controls whether stack dumps should be included in the error log (default value is true).*/
    public static var dumpStack :Boolean = true;
    
    /** Asserts that the value is equal to null. */
    public static function isNull (value :Object, message :String = null) :void
    {
        if (_debug && (value != null)) {
            fail(message);
        }
    }

    /** Asserts that the value is not equal to null. */
    public static function isNotNull (value :Object, message :String = null) :void
    {
        if (_debug && (value == null)) {
            fail(message);
        }
    }

    /** Asserts that the value is false. */
    public static function isFalse (value :Boolean, message :String = null) :void
    {
        if (_debug && value) {
            fail(message);
        }
    }
    
    /** Asserts that the value is true. */
    public static function isTrue (value :Boolean, message :String = null) :void
    {
        if (_debug && ! value) {
            fail(message);
        }
    }

    /** Displays an error message, with an optional stack trace. */
    public static function fail (message :String) :void
    {
        _log.warning("Failure" + ((message != null) ? (": " + message) : ""));
        if (dumpStack) {
            _log.warning(new Error("dumpStack").getStackTrace());
        }
    }        

    protected static var _log :Log = Log.getLog(Assert);
    protected static var _debug :Boolean = Capabilities.isDebugger;
}
}
