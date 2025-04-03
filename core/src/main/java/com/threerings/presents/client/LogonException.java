//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

/**
 * A logon exception is used to indicate a failure to log on to the
 * server. The reason for failure is encoded as a string and stored in the
 * message field of the exception.
 */
public class LogonException extends Exception
{
    /**
     * Constructs a logon exception with the supplied logon failure code.
     */
    public LogonException (String code)
    {
        this(code, false);
    }

    /**
     * Constructs a logon exception with the supplied logon failure code.
     *
     * @param stillInProgress indicates that the logon attempt has not totally
     * failed, rather we are still trying but want to report that our initial
     * attempt did not work and that we're falling back to alternative methods.
     */
    public LogonException (String code, boolean stillInProgress)
    {
        super(code);
        _stillInProgress = stillInProgress;
    }

    /**
     * Returns true if this exception is reporting an intermediate status
     * rather than total logon failure. The client may be falling back to an
     * alternative port or delaying an auto-retry attempt due to server
     * overload. If this method returns true, the client <em>should not</em>
     * allow additional calls to {@link Client#logon} as the current attempt is
     * still in progress.
     */
    public boolean isStillInProgress ()
    {
        return _stillInProgress;
    }

    protected boolean _stillInProgress;
}
