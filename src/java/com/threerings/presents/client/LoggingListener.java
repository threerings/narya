//
// $Id: LoggingListener.java,v 1.1 2002/08/14 19:07:54 mdb Exp $

package com.threerings.presents.client;

import com.samskivert.util.Log;

/**
 * Implements the basic {@link InvocationListener} and logs the failure.
 */
public class LoggingListener
    implements InvocationService.InvocationListener
{
    /**
     * Constructs a listener that will report the supplied error message
     * along with the reason for failure to the supplied log object.
     */
    public LoggingListener (Log log, String errmsg)
    {
        _log = log;
        _errmsg = errmsg;
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        _log.warning(_errmsg + " [reason=" + reason + "].");
    }

    protected Log _log;
    protected String _errmsg;
}
