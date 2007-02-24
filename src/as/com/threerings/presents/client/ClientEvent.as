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

package com.threerings.presents.client {

import flash.events.Event;

public class ClientEvent extends Event
{
    public static const CLIENT_WILL_LOGON :String = "clientWillLogon";
    public static const CLIENT_DID_LOGON :String = "clientDidLogon";
    public static const CLIENT_FAILED_TO_LOGON :String = "clientFailedLogon";
    public static const CLIENT_OBJECT_CHANGED :String = "clobjChanged";
    public static const CLIENT_CONNECTION_FAILED :String = "clientConnFailed";
    /** The logoff itself can be cancelled if a listener calls
     * preventDefault() on this event. */
    public static const CLIENT_WILL_LOGOFF :String = "clientWillLogoff";
    public static const CLIENT_DID_LOGOFF :String = "clientDidLogoff";
    public static const CLIENT_DID_CLEAR :String = "clientDidClear";

    public function ClientEvent (type :String, client :Client,
            cause :Error = null)
    {
        super(type, false, (type === CLIENT_WILL_LOGOFF));
        _client = client;
        _cause = cause;
    }

    public function getClient () :Client
    {
        return _client;
    }

    public function getCause () :Error
    {
        return _cause;
    }

    override public function clone () :Event
    {
        return new ClientEvent(type, _client, _cause);
    }

    /** The client that generated this client event. */
    protected var _client :Client;

    /** The error that caused this event, if applicable. */
    protected var _cause :Error;
}
}
