//
// $Id: Authenticator.java,v 1.1 2001/05/29 03:27:59 mdb Exp $

package com.samskivert.cocktail.cher.server.net;

import com.samskivert.cocktail.cher.net.AuthRequest;
import com.samskivert.cocktail.cher.net.AuthResponse;

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
