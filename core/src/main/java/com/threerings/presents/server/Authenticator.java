//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.samskivert.util.Invoker;
import com.samskivert.util.ResultListener;

import com.threerings.presents.data.AuthCodes;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.server.net.AuthingConnection;

import static com.threerings.presents.Log.log;

/**
 * The authenticator is a pluggable component of the authentication framework. The base class
 * handles the basic mechanics of authentication and a system would extend the base authenticator
 * and add code that does the actual client authentication.
 */
public abstract class Authenticator
{
    /**
     * An exception that can be thrown during {@link #processAuthentication}. The results of
     * {@link #getMessage} string will be filled in as the auth failure code.
     */
    public static class AuthException extends Exception
    {
        public AuthException (String code) {
            super(code);
        }
    }

    /**
     * Called by the connection management code when an authenticating connection has received its
     * authentication request from the client.
     */
    public void authenticateConnection (Invoker invoker, final AuthingConnection conn,
                                        final ResultListener<AuthingConnection> onComplete)
    {
        final AuthRequest req = conn.getAuthRequest();
        final AuthResponseData rdata = createResponseData();
        final AuthResponse rsp = new AuthResponse(rdata);

        invoker.postUnit(new Invoker.Unit("authenticateConnection") {
            @Override
            public boolean invoke () {
                try {
                    processAuthentication(conn, rsp);
                    if (AuthResponseData.SUCCESS.equals(rdata.code) &&
                        conn.getAuthName() == null) { // fail early, fail (less) often
                        throw new IllegalStateException("Authenticator failed to provide authname");
                    }
                } catch (AuthException e) {
                    rdata.code = e.getMessage();
                } catch (Exception e) {
                    log.warning("Error authenticating user", "areq", req, e);
                    rdata.code = AuthCodes.SERVER_ERROR;
                }
                return true;
            }

            @Override
            public void handleResult () {
                // stuff a reference to the auth response into the connection so that we have
                // access to it later in the authentication process
                conn.setAuthResponse(rsp);

                // send the response back to the client
                conn.postMessage(rsp);

                // if the authentication request was granted, let the connection manager know that
                // we just authed
                if (AuthResponseData.SUCCESS.equals(rdata.code)) {
                    onComplete.requestCompleted(conn);
                }
            }
        });
    }

    /**
     * Create a new AuthResponseData instance to use for authenticating a connection.
     */
    protected AuthResponseData createResponseData ()
    {
        return new AuthResponseData();
    }

    /**
     * Process the authentication for the specified connection. The method may return after it has
     * stuffed a valid response code in rsp.getData().code.
     *
     * @param conn The client connection.
     * @param rsp The response to the client, which will already contain an AuthResponseData
     * created by {@link #createResponseData}.
     */
    protected abstract void processAuthentication (AuthingConnection conn, AuthResponse rsp)
        throws Exception;
}
