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

package com.threerings.bureau {

import com.threerings.util.Log;

/**
 * Contains a reference to the log object used by the Bureau services.
 */
public class Log
{
    /** We dispatch our log messages through this log. */
    public static const log :com.threerings.util.Log = 
        com.threerings.util.Log.getLog(com.threerings.bureau.Log);

    /** Convenience function. */
    public static function debug (message :String) :void
    {
        log.debug(message);
    }

    /** Convenience function. */
    public static function info (message :String) :void
    {
        log.info(message);
    }

    /** Convenience function. */
    public static function warning (message :String) :void
    {
        log.warning(message);
    }

    /** Convenience function. */
    public static function logStackTrace (e :Error) :void
    {
        log.logStackTrace(e);
    }
}

}
