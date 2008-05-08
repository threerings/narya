package com.threerings.bureau.client {

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.data.ClientObject;
import com.threerings.bureau.data.BureauCodes;
import com.threerings.bureau.data.AgentObject;
import com.threerings.bureau.Log;
import com.threerings.bureau.util.BureauContext;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.dobj.SubscriberAdapter;
import com.threerings.presents.util.SafeSubscriber;
import com.threerings.presents.dobj.ObjectAccessError;
import com.threerings.util.HashMap;
import com.threerings.presents.client.ClientEvent;

/**
 * Allows the server to create and destroy agents on a client.
 * @see BureauRegistry
 */
public class BureauDirector extends BasicDirector
{
    /**
     * Creates a new BureauDirector.
     */
    public function BureauDirector (ctx :BureauContext)
    {
        super(ctx);
    }

    // from BasicDirector
    public override function clientDidLogon (event :ClientEvent) :void
    {
        super.clientDidLogon(event);
        var id :String = BureauContext(_ctx).getBureauId();
        _bureauService.bureauInitialized(_ctx.getClient(), id);
    }

    /**
     * Creates a new agent when the server requests it.
     */
    protected function createAgentFromId (agentId :int) :void
    {
        var delegator :Subscriber = 
            new SubscriberAdapter(objectAvailable, requestFailed);

        Log.info("Subscribing to object " + agentId);

        var subscriber :SafeSubscriber = 
            new SafeSubscriber(agentId, delegator);
        _subscribers.put(agentId, subscriber);
        subscriber.subscribe(_ctx.getDObjectManager());
    }

    /**
     * Destroys an agent at the server's request.
     */
    protected function destroyAgent (agentId :int) :void
    {
        var agent :Agent = null;
        agent = _agents.remove(agentId);

        if (agent == null) {
            Log.warning("Lost an agent, id " + agentId);
        }
        else {
            try {
                agent.stop();
            }
            catch (e :Error) {
                Log.warning("Stopping an agent caused an exception");
                Log.logStackTrace(e);
            }
            var subscriber :SafeSubscriber = _subscribers.remove(agentId);
            if (subscriber == null) {
                Log.warning("Lost a subscriber for agent " + agent);
            }
            else {
                subscriber.unsubscribe(_ctx.getDObjectManager());
            }
            _bureauService.agentDestroyed(_ctx.getClient(), agentId);
        }
    }

    /**
     * Callback for when the a request to subscribe to an object finishes and the object is available.
     */
    protected function objectAvailable (agentObject :AgentObject) :void
    {
        var oid :int = agentObject.getOid();

        Log.info("Object " + oid + " now available");

        var agent :Agent;
        try {
            agent = createAgent(agentObject);
            agent.init(agentObject);
            agent.start();
        }
        catch (e :Error) {
            Log.warning("Could not create agent [obj=" + agentObject + "]");
            Log.logStackTrace(e);
            _bureauService.agentCreationFailed(_ctx.getClient(), oid);
            return;
        }
        
        _agents.put(oid, agent);
        _bureauService.agentCreated(_ctx.getClient(), oid);
    }

    /**
     * Callback for when the a request to subscribe to an object fails.
     */
    protected function requestFailed (oid :int, cause :ObjectAccessError) :void
    {
        Log.warning("Could not subscribe to agent [oid=" + oid + "]");
        Log.logStackTrace(cause);
    }

    // from BasicDirector
    protected override function registerServices (client :Client) :void
    {
        super.registerServices(client);

        // Require the bureau services
        client.addServiceGroup(BureauCodes.BUREAU_GROUP);

        // Set up our decoder so we can receive method calls
        // from the server
        var receiver :BureauReceiver = 
            new ReceiverDelegator(createAgentFromId, destroyAgent);

        client.getInvocationDirector().
            registerReceiver(new BureauDecoder(receiver));
    }

    // from BasicDirector
    protected override function fetchServices (client :Client) :void
    {
        super.fetchServices(client);

        _bureauService = client.getService(BureauService) as BureauService;
    }

    /**
     * Called when it is time to create an Agent. Subclasses should read the 
     * <code>agentObject</code>'s type and/or properties to determine what kind of Agent to 
     * create.
     * @param agentObj the distributed and object
     * @return a new Agent that will govern the distributed object
     */
    protected function createAgent (agentObj :AgentObject) :Agent
    {
        throw new Error("Abstract function");
    }

    protected var _bureauService :BureauService;
    protected var _agents :HashMap = new HashMap();
    protected var _subscribers :HashMap = new HashMap();
}
}

import com.threerings.bureau.client.BureauReceiver;

class ReceiverDelegator implements BureauReceiver
{
    public function ReceiverDelegator (createFn :Function, destroyFn :Function)
    {
        _createFn = createFn;
        _destroyFn = destroyFn;
    }

    public function createAgent (agentId :int) :void
    {
        _createFn(agentId);
    }

    public function destroyAgent (agentId :int) :void
    {
        _destroyFn(agentId);
    }

    protected var _createFn :Function;
    protected var _destroyFn :Function;
}

