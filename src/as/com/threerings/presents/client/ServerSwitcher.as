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

import com.threerings.util.StringUtil;

import com.threerings.presents.data.AuthCodes;

/**
 * Handles the process of switching between servers.
 *
 * @see Client#moveToServer
 */
public class ServerSwitcher extends ClientAdapter
{
    public function ServerSwitcher (
        client :Client, hostname :String, ports :Array, obs :InvocationService_ConfirmListener)
    {
        _client = client;
        _hostname = hostname;
        _ports = ports;
        _observer = obs;
    }

    public function switchServers () :void
    {
        _client.addClientObserver(this);
        if (!_client.isLoggedOn()) {
            // if we're not logged on right now, just do the switch immediately
            clientDidClear(null);

        } else {
            // note our current connection information so that we can restore it if our logon
            // attempt fails
            _oldHostname = _client.getHostname();
            _oldPorts = _client.getPorts();

            // otherwise logoff and wait for all of our callbacks to clear
            _client.logoff(true);
        }
    }

    // from ClientAdapter
    override public function clientDidClear (event :ClientEvent) :void
    {
        // configure the client to point to the new server and logon
        _client.setServer(_hostname, _ports);

        if (!_client.logon()) {
            Log.getLog(this).warning("logon() failed during server switch? [hostname=" + _hostname +
                                     ", ports=" + StringUtil.toString(_ports) + "].");
            handleLogonFailure(null);
        }
    }

    // from ClientAdapter
    override public function clientDidLogon (event :ClientEvent) :void
    {
        _client.removeClientObserver(this);
        if (_observer != null) {
            _observer.requestProcessed();
        }
    }

    // from ClientAdapter
    override public function clientFailedToLogon (event :ClientEvent) :void
    {
        handleLogonFailure(event.getCause());
    }

    protected function handleLogonFailure (error :Error) :void
    {
        _client.removeClientObserver(this);
        if (_oldHostname != null) { // restore our previous server and ports
            _client.setServer(_oldHostname, _oldPorts);
        }
        if (_observer != null) {
            _observer.requestFailed((error is LogonError) ? error.message : AuthCodes.SERVER_ERROR);
        }
    }

    protected var _client :Client;
    protected var _hostname :String, _oldHostname :String;
    protected var _ports :Array, _oldPorts :Array;
    protected var _observer :InvocationService_ConfirmListener;
}
}
