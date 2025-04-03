//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import com.samskivert.util.Logger;

/**
 * Implements the basic {@link InvocationService.InvocationListener} and logs the failure.
 */
public class LoggingListener
    implements InvocationService.InvocationListener
{
    /**
     * Constructs a listener that will report the supplied error message along with the reason for
     * failure to the supplied log object.
     */
    public LoggingListener (Logger log, String errmsg)
    {
        _logger = log;
        _errmsg = errmsg;
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        _logger.warning(_errmsg + " [reason=" + reason + "].");
    }

    protected Logger _logger;
    protected String _errmsg;
}
