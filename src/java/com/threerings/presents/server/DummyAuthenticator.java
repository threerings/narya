//
// $Id: DummyAuthenticator.java,v 1.3 2001/10/01 22:14:55 mdb Exp $

package com.threerings.cocktail.cher.server;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.net.*;
import com.threerings.cocktail.cher.server.net.Authenticator;

public class DummyAuthenticator implements Authenticator
{
    /**
     * We just accept all authentication requests.
     */
    public AuthResponse process (AuthRequest req)
    {
        Log.info("Accepting request: " + req);
        AuthResponseData rdata = new AuthResponseData();
        rdata.code = AuthResponseData.SUCCESS;
        return new AuthResponse(rdata);
    }
}
