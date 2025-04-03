//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import java.lang.reflect.Constructor;

import com.threerings.util.Name;

import com.threerings.presents.data.AuthCodes;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.ServiceCreds;
import com.threerings.presents.server.net.AuthingConnection;

import static com.threerings.presents.Log.log;

/**
 * Works in conjunction with {@link ServiceCreds} to handle the authentication of service clients
 * (bureaus, peers, etc.).
 */
public abstract class ServiceAuthenticator<T extends ServiceCreds> extends ChainedAuthenticator
{
    /**
     * Creates an authenticator that will handle requests using the supplied credentials class and
     * which will create instances of the supplied auth name class to identify those clients. Note
     * that the auth name class <em>must</em> have a public constructor that takes a single string.
     */
    public ServiceAuthenticator (Class<T> credsClass, Class<? extends Name> authNameClass)
    {
        _credsClass = credsClass;
        try {
            _authNamer = authNameClass.getConstructor(String.class);
        } catch (NoSuchMethodException nsme) {
            throw new IllegalArgumentException("AuthName must have AuthName(String) constructor.");
        }
    }

    @Override // from abstract ChainedAuthenticator
    public boolean shouldHandleConnection (AuthingConnection conn)
    {
        return _credsClass.isInstance(conn.getAuthRequest().getCredentials());
    }

    @Override // from abstract Authenticator
    protected void processAuthentication (AuthingConnection conn, AuthResponse rsp)
        throws Exception
    {
        T creds = _credsClass.cast(conn.getAuthRequest().getCredentials());
        if (!areValid(creds)) {
            log.warning("Received invalid service auth request?", "creds", creds);
            throw new AuthException(AuthCodes.SERVER_ERROR);
        }

        try {
            conn.setAuthName(_authNamer.newInstance(creds.clientId));
            rsp.getData().code = AuthResponseData.SUCCESS;
        } catch (Exception e) {
            log.warning("Failed to construct auth name", "namer", _authNamer, e);
            throw new AuthException(AuthCodes.SERVER_ERROR);
        }
    }

    /**
     * Returns true if the creds in question are valid.
     */
    protected abstract boolean areValid (T creds);

    protected Class<T> _credsClass;
    protected Constructor<? extends Name> _authNamer;
}
