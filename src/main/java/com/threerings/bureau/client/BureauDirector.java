//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.bureau.client;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.util.SafeSubscriber;

import com.threerings.bureau.data.AgentObject;
import com.threerings.bureau.data.BureauCodes;
import com.threerings.bureau.server.BureauRegistry;
import com.threerings.bureau.util.BureauContext;

import static com.threerings.bureau.Log.log;

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
        _bureauService.bureauInitialized(_ctx.getBureauId());
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

        log.info("Subscribing to object " + agentId);

        SafeSubscriber<AgentObject> subscriber =
            new SafeSubscriber<AgentObject>(agentId, delegator);
        _subscribers.put(agentId, subscriber);
        subscriber.subscribe(_ctx.getDObjectManager());
    }

    /**
     * Destroys an agent at the server's request.
     */
    protected synchronized void destroyAgent (int agentId)
    {
        Agent agent = null;
        agent = _agents.remove(agentId);

        if (agent == null) {
            log.warning("Lost an agent, id " + agentId);
        } else {
            try {
                agent.stop();
            } catch (Throwable t) {
                log.warning("Stopping an agent caused an exception", t);
            }
            SafeSubscriber<AgentObject> subscriber = _subscribers.remove(agentId);
            if (subscriber == null) {
                log.warning("Lost a subscriber for agent " + agent);
            } else {
                subscriber.unsubscribe(_ctx.getDObjectManager());
            }
            _bureauService.agentDestroyed(agentId);
        }
    }

    /**
     * Callback for when the a request to subscribe to an object finishes and the object is
     * available.
     */
    protected synchronized void objectAvailable (AgentObject agentObject)
    {
        int oid = agentObject.getOid();

        log.info("Object " + oid + " now available");

        Agent agent;
        try {
            agent = createAgent(agentObject);
            agent.init(agentObject);
            agent.start();
        } catch (Throwable t) {
            log.warning("Could not create agent", "obj", agentObject, t);
            _bureauService.agentCreationFailed(oid);
            return;
        }

        _agents.put(oid, agent);
        _bureauService.agentCreated(oid);
    }

    /**
     * Callback for when the a request to subscribe to an object fails.
     */
    protected synchronized void requestFailed (int oid, ObjectAccessException cause)
    {
        log.warning("Could not subscribe to agent", "oid", oid, cause);
    }

    @Override // from BasicDirector
    protected void registerServices (Client client)
    {
        super.registerServices(client);

        // Require the bureau services
        client.addServiceGroup(BureauCodes.BUREAU_GROUP);

        // Set up our decoder so we can receive method calls from the server
        BureauReceiver receiver = new BureauReceiver() {
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
    protected IntMap<SafeSubscriber<AgentObject>> _subscribers =
        IntMaps.newHashIntMap();
}
