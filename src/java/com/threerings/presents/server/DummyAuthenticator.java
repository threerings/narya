//
// $Id: DummyAuthenticator.java,v 1.1 2001/05/29 03:27:59 mdb Exp $

package com.samskivert.cocktail.cher.server;

import com.samskivert.cocktail.cher.Log;
import com.samskivert.cocktail.cher.net.*;
import com.samskivert.cocktail.cher.server.net.Authenticator;
import com.samskivert.cocktail.cher.util.Codes;

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
