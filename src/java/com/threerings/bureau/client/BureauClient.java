package com.threerings.bureau.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.Credentials;
import com.samskivert.util.RunQueue;

/** 
 * Represents a client embedded in a bureau.
 */
public class BureauClient extends Client
{
    /**
     * Creates a new client.
     * @param creds the credentials supplied during connection
     * @param runQueue the place to post tasks required by clients
     */
    public BureauClient (Credentials creds, RunQueue runQueue)
    {
        super(creds, runQueue);
    }
}
