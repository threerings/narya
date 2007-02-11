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

package com.threerings.presents.net;

import java.util.HashSet;
import java.util.TimeZone;

/**
 * Used to authenticate with the server.
 */
public class AuthRequest extends UpstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public AuthRequest ()
    {
        super();
    }

    /**
     * Constructs a auth request with the supplied credentials and client version information.
     */
    public AuthRequest (Credentials creds, String version, HashSet<String> bootGroups)
    {
        _creds = creds;
        _version = version;
        _zone = TimeZone.getDefault().getID();
        _bootGroups = bootGroups.toArray(new String[bootGroups.size()]);
    }

    /**
     * Returns a reference to the credentials provided with this request.
     */
    public Credentials getCredentials ()
    {
        return _creds;
    }

    /**
     * Returns a reference to the version information provided with this request.
     */
    public String getVersion ()
    {
        return _version;
    }

    /**
     * Returns the timezone in which this client is operating.
     */
    public TimeZone getTimeZone ()
    {
        return TimeZone.getTimeZone(_zone);
    }

    /**
     * Returns the set of bootstrap service groups in which this client is interested.
     */
    public String[] getBootGroups ()
    {
        return _bootGroups;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[type=AREQ, msgid=" + messageId + ", creds=" + _creds +
            ", version=" + _version + "]";
    }

    /** The credentials associated with this auth request. */
    protected Credentials _creds;

    /** The version information associated with the client code. */
    protected String _version;

    /** The timezone in which this client is operating. */
    protected String _zone;

    /** The set of bootstrap service groups this client is interested in. */
    protected String[] _bootGroups;
}
