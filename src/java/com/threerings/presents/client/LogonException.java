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
