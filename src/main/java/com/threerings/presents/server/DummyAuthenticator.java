//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.presents.server;

import com.samskivert.io.PersistenceException;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.UsernamePasswordCreds;
import com.threerings.presents.server.net.AuthingConnection;

import static com.threerings.presents.Log.log;

/**
 * A simple authenticator implementation that simply accepts all authentication requests.
 */
public class DummyAuthenticator extends Authenticator
{
    @Override
    protected void processAuthentication (AuthingConnection conn, AuthResponse rsp)
        throws PersistenceException
    {
        log.info("Accepting request: " + conn.getAuthRequest());

        // we need to provide some sort of authentication username
        Credentials creds = conn.getAuthRequest().getCredentials();
        if (creds instanceof UsernamePasswordCreds) {
            conn.setAuthName(((UsernamePasswordCreds)creds).getUsername());
        } else {
            conn.setAuthName(new Name(conn.getInetAddress().getHostAddress()));
        }

        rsp.getData().code = AuthResponseData.SUCCESS;
    }
}
