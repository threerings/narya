package com.threerings.bureau.client;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.data.ClientObject;
import com.threerings.bureau.data.BureauCodes;
import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;
import com.threerings.bureau.data.AgentObject;
import com.threerings.bureau.Log;
import com.threerings.bureau.util.BureauContext;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.util.SafeSubscriber;
import com.threerings.presents.dobj.ObjectAccessException;

/**
 * Allows the server to create and destroy agents on a client.
 * @see BureauRegistry
 */
public abstract class BureauDirector extends BasicDirector
{
    /**
     * Creates a new BureauDirector.
     */
    public BureauDirector (BureauContext ctx)
    {
        super(ctx);
        _ctx = ctx;
    }

    @Override // from BasicDirector
    public void clientDidLogon (Client client)
    {
        super.clientDidLogon(client);
        _bureauService.bureauInitialized(_ctx.getClient(), _ctx.getBureauId());
    }

    /**
     * Creates a new agent when the server requests it.
     */
    protected synchronized void createAgent (int agentId)
    {
        Subscriber<AgentObject> delegator = new Subscriber<AgentObject>() {
            public void objectAvailable (AgentObject agentObject) {
                BureauDirector.this.objectAvailable(agentObject);
            }
            public void requestFailed (int oid, ObjectAccessException cause) {
                BureauDirector.this.requestFailed(oid, cause);
            }
        };

        _subscriber = new SafeSubscriber<AgentObject>(agentId, delegator);
        _subscriber.subscribe(_ctx.getDObjectManager());
    }

    /**
     * Destroys an agent at the server's request.
     */
    protected synchronized void destroyAgent (int agentId)
    {
        Agent agent = null;
        agent = _agents.remove(agentId);

        if (agent == null) {
        }
        else {
            try {
                agent.stop();
            }
            catch (Throwable t) {
                Log.warning("Stopping an agent caused an exception");
                Log.logStackTrace(t);
            }
            _subscriber.unsubscribe(_ctx.getDObjectManager());
            _bureauService.agentDestroyed(_ctx.getClient(), agentId);
        }
    }

    /**
     * Callback for when the a request to subscribe to an object finishes and the object is available.
     */
    protected synchronized void objectAvailable (AgentObject agentObject)
    {
        int oid = agentObject.getOid();
        Agent agent;
        try {
            agent = createAgent(agentObject);
            agent.init(agentObject);
            agent.start();
        }
        catch (Throwable t) {
            Log.warning("Could not create agent [obj=" + agentObject + "]");
            Log.logStackTrace(t);
            _bureauService.agentCreationFailed(_ctx.getClient(), oid);
            return;
        }
        
        _agents.put(oid, agent);
        _bureauService.agentCreated(_ctx.getClient(), oid);
    }

    /**
     * Callback for when the a request to subscribe to an object fails.
     */
    protected synchronized void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.warning("Could not subscribe to agent [oid=" + oid + "]");
        Log.logStackTrace(cause);
    }

    @Override // from BasicDirector
    protected void registerServices (Client client)
    {
        super.registerServices(client);

        // Require the bureau services
        client.addServiceGroup(BureauCodes.BUREAU_GROUP);

        // Set up our decoder so we can receive method calls
        // from the server
        BureauReceiver receiver = new BureauReceiver () {
            public void createAgent (int agentId) {
                BureauDirector.this.createAgent(agentId);
            }
            public void destroyAgent (int agentId) {
                BureauDirector.this.destroyAgent(agentId);
            }
        };

        client.getInvocationDirector().
            registerReceiver(new BureauDecoder(receiver));
    }

    @Override // from BasicDirector
    protected void fetchServices (Client client)
    {
        super.fetchServices(client);

        _bureauService = client.getService(BureauService.class);
    }

    /**
     * Called when it is time to create an Agent. Subclasses should read the 
     * <code>agentObject</code>'s type and/or properties to determine what kind of Agent to 
     * create.
     * @param agentObj the distributed and object
     * @return a new Agent that will govern the distributed object
     */
    protected abstract Agent createAgent (AgentObject agentObj);

    protected BureauContext _ctx;
    protected BureauService _bureauService;
    protected IntMap<Agent> _agents = IntMaps.newHashIntMap();
    protected SafeSubscriber<AgentObject> _subscriber;
}
