//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.micasa;

/**
 * A placeholder class that contains a reference to the log object used by
 * the MiCasa package. This is a useful pattern to use when using the
 * samskivert logging facilities. One creates a top-level class like this
 * one that instantiates a log object with an name that identifies log
 * messages from that package and then provides static methods that
 * generate log messages using that instance. Then, classes in that
 * package need only import the log wrapper class and can easily use it to
 * generate log messages. For example:
 *
 * <pre>
 * import com.threerings.micasa.Log;
 * // ...
 * Log.warning("All hell is breaking loose!");
 * // ...
 * </pre>
 *
 * @see com.samskivert.util.Log
 */
public class Log
{
    /** The static log instance configured for use by this package. */
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("micasa");

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
