//
// $Id: DummyAuthenticator.java,v 1.4 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.Log;
import com.threerings.presents.net.*;
import com.threerings.presents.server.net.Authenticator;

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
