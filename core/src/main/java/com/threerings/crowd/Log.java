//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd;

import com.samskivert.util.Logger;

/**
 * Contains a reference to the log object used by the Crowd services.
 */
public class Log
{
    /** We dispatch our log messages through this logger. */
    public static Logger log = Logger.getLogger("com.threerings.crowd");
}
