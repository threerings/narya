package com.threerings.bureau.server;

import com.threerings.bureau.data.BureauCredentials;
import com.threerings.presents.data.AuthCodes;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.server.ChainedAuthenticator;
import com.threerings.presents.server.net.AuthingConnection;

import static com.threerings.bureau.Log.log;

/**
 * Authenticates bureaus only.
 */
public class BureauAuthenticator extends ChainedAuthenticator
{
    /**
     * Creates a new bureau authenticator.
     */
    public BureauAuthenticator (BureauRegistry registry)
    {
        _registry = registry;
    }

    @Override // from abstract ChainedAuthenticator
    protected boolean shouldHandleConnection (AuthingConnection conn)
    {
        return (conn.getAuthRequest().getCredentials() instanceof BureauCredentials);
    }

    @Override // from Authenticator
    protected void processAuthentication (
        AuthingConnection conn, 
        AuthResponse rsp)
    {
        AuthRequest req = conn.getAuthRequest();
        BureauCredentials creds = (BureauCredentials)req.getCredentials();
        String problem = _registry.checkToken(creds);

        if (problem == null) {
            rsp.getData().code = AuthResponseData.SUCCESS;

        } else {
            log.warning("Received invalid bureau auth request [creds=" + 
                creds + "], problem: " + problem);
            rsp.getData().code = AuthCodes.SERVER_ERROR;
        }
    }

    protected BureauRegistry _registry;
}
