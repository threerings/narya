//
// $Id: Authenticator.java,v 1.3 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.server.net;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;

/**
 * The authenticator is a pluggable component of the authentication
 * framework. It is provided with an auth request object and is expected
 * to return an auth response object indicating whether or not the client
 * is authenticated. <code>authenticate()</code> is invoked on the authmgr
 * thread which means that it can access databases or other external
 * (slow) information sources without concern for unduly blocking the
 * normal operation of the server. Of course, authentication requests are
 * processed serially, so they shouldn't take <em>too</em> long.
 */
public interface Authenticator
{
    /**
     * Requests that the authenticator process the supplied request and
     * provide a response object indicating success or failure.
     */
    public AuthResponse process (AuthRequest request);
}
