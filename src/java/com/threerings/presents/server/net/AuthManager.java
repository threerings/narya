//
// $Id: AuthManager.java,v 1.1 2001/05/29 03:27:59 mdb Exp $

package com.samskivert.cocktail.cher.server.net;

import com.samskivert.util.LoopingThread;
import com.samskivert.util.Queue;

import com.samskivert.cocktail.cher.Log;
import com.samskivert.cocktail.cher.net.AuthRequest;
import com.samskivert.cocktail.cher.net.AuthResponse;

/**
 * The authentication manager takes care of the authentication process.
 * Authentication happens on multiple threads. The conmgr thread parses
 * the authentication request and passes it on to the authmgr thread. The
 * authmgr thread invokes the (pluggable) authenticator to do the actual
 * authentication. Then the response message is queued up to be delivered
 * by the conmgr thread.
 *
 * <p> This structure prevents authentication to take place
 * asynchronously, but in a controlled manner. Only one authentication
 * will be processed at a time, but the dobj and conmgr threads will
 * continue to operate independent of the authentication process.
 */
public class AuthManager extends LoopingThread
{
    public AuthManager (Authenticator author)
    {
        _author = author;
    }

    /**
     * Puts an authenticating connection on the queue. The connection will
     * be authenticated and an auth response delivered.
     */
    public void postAuthingConnection (AuthingConnection aconn)
    {
        _authq.append(aconn);
    }

    /**
     * Process auth requests.
     */
    protected void iterate ()
    {
        // grab the next authing connection from the queue
        AuthingConnection aconn = (AuthingConnection)_authq.get();

        try {
            // instruct the authenticator to process the auth request
            AuthResponse rsp = _author.process(aconn.getAuthRequest());

            // now ship the response back
            aconn.postMessage(rsp);

        } catch (Exception e) {
            Log.warning("Failure processing authreq [conn=" + aconn + "].");
            Log.logStackTrace(e);
        }
    }

    protected Authenticator _author;
    protected Queue _authq = new Queue();
}
