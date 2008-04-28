package com.threerings.bureau.client;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.data.ClientObject;
import com.threerings.bureau.data.BureauCodes;
import com.samskivert.util.HashIntMap;
import com.threerings.bureau.data.AgentObject;
import com.threerings.bureau.Log;
import com.threerings.bureau.util.BureauContext;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.dobj.ObjectAccessException;

/**
 * Allows the server to create and destroy agents on a client.
 * @see BureauRegistry
 */
public class BureauDirector extends BasicDirector
    implements BureauReceiver, Subscriber<AgentObject>
{
    public BureauDirector (BureauContext ctx)
    {
        super(ctx);
        _ctx = ctx;
    }

    // from BureauReceiver
    public synchronized void createAgent (int agentId)
    {
        _ctx.getDObjectManager().subscribeToObject(agentId, this);
    }

    // from BureauReceiver
    public synchronized void destroyAgent (int agentId)
    {
        Agent agent = null;
        try {
            agent = _agents.remove(agentId);
            // TODO: stop the agent somehow
        }
        catch (Throwable t) {
            Log.warning("Could not create agent [id=" + agentId + "]");
            Log.logStackTrace(t);
            // TODO: failure notification?
            return;
        }

        _ctx.getDObjectManager().unsubscribeFromObject(agentId, this);
        doStopAgent(agent);
        _bureauService.agentDestroyed(_ctx.getClient(), agentId);
    }

    // from Subscriber
    public synchronized void objectAvailable (AgentObject agentObject)
    {
        int oid = agentObject.getOid();
        try {
            Agent agent = new Agent();
            agent.agentObject = agentObject;
            doStartAgent(agent);
            _agents.put(oid, agent);
        }
        catch (Exception e) {
            Log.warning("Could not create agent [obj=" + agentObject + "]");
            Log.logStackTrace(e);
            // TODO: failure notification?
            return;
        }
        
        // TODO: post to runqueue?
        _bureauService.agentCreated(_ctx.getClient(), oid);
    }

    // from Subscriber
    public synchronized void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.warning("Could not subscribe to agent [oid=" + oid + "]");
        Log.logStackTrace(cause);
    }

    @Override // from BasicDirector
    public void clientDidLogon (Client client)
    {
        super.clientDidLogon(client);
        _bureauService.bureauInitialized(_ctx.getClient(), _ctx.getBureauId());
    }

    @Override // from BasicDirector
    protected void registerServices (Client client)
    {
        super.registerServices(client);

        // Require the bureau services
        client.addServiceGroup(BureauCodes.BUREAU_GROUP);

        // Set up our decoder so we can receive method calls
        // from the server
        client.getInvocationDirector().
            registerReceiver(new BureauDecoder(this));
    }

    @Override // from BasicDirector
    protected void fetchServices (Client client)
    {
        super.fetchServices(client);

        _bureauService = client.getService(BureauService.class);
    }

    /**
     * Called when the agent object is ready and it is time to run his code.
     */
    protected void doStartAgent (Agent agent)
    {
    }

    /**
     * Called when the agent object is being destroyed and the client code should stop
     * processing.
     */
    protected void doStopAgent (Agent agent)
    {
    }

    protected BureauContext _ctx;
    protected BureauService _bureauService;
    protected HashIntMap<Agent> _agents = new HashIntMap<Agent>();
}
