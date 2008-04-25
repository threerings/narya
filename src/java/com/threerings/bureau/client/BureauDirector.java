package com.threerings.bureau.client;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.util.PresentsContext;

/**
 * Allows the server to create and destroy agents on a client.
 *  @see BureauRegistry
 */
public class BureauDirector extends BasicDirector
    implements BureauReceiver
{
    protected BureauDirector (PresentsContext ctx)
    {
        super(ctx);

        // Set up our decoder so we can receive method calls
        // from the server
        _ctx.getClient().getInvocationDirector().
            registerReceiver(new BureauDecoder(this));
    }

    // from BureauReceiver
    public void createAgent (ClientObject client, int agentId)
    {
    }

    // from BureauReceiver
    public void destroyAgent (ClientObject client, int agentId)
    {
    }
}
