//
// $Id: DummyAuthenticator.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.server;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.net.*;
import com.threerings.cocktail.cher.server.net.Authenticator;
import com.threerings.cocktail.cher.util.Codes;

public class DummyAuthenticator implements Authenticator
{
    /**
     * We just accept all authentication requests.
     */
    public AuthResponse process (AuthRequest req)
    {
        Log.info("Accepting request: " + req);
        AuthResponseData rdata = new AuthResponseData();
        rdata.code = Codes.SUCCESS;
        return new AuthResponse(rdata);
    }
}
