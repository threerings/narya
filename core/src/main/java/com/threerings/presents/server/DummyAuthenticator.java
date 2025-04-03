//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
