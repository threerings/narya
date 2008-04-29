//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.bureau.server;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import com.threerings.bureau.data.AgentObject;
import com.threerings.bureau.data.BureauCodes;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.dobj.ObjectDeathListener;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.server.InvocationManager;
import com.samskivert.util.StringUtil;
import com.samskivert.util.ProcessLogger;

import com.threerings.bureau.Log;

/** 
 *  Abstracts the launching and termination of external processes (bureaus) that host instances of 
 *  server-side code (agents).
 */
public class BureauRegistry 
{
    /**
     * Defines the commands that are responsible for invoking a bureau. Instances are associated to
     * bureau types by the server on startup. The instances are used whenever the registry needs to 
     * launch a bureau for an agent with the assocated bureau type.
     * @see setCommandGenerator
     */
    public static interface CommandGenerator
    {
        /** 
         * Launches a new bureau using the given server connect-back url and other information. 
         * Called by the registry when it decides a new bureau is needed.
         * @param serverNameAndPort the name and port the bureau should use to connect back to the 
         * server, e.g. server.com:47624
         * @param bureauId the id of the bureau being launched
         * @param token the token string to use for the credentials when logging in
         * @return the builder, ready to launch
         */
        String[] createCommand (
            String serverNameAndPort, 
            String bureauId, 
            String token);
    }

    /**
     * Creates a new registry, prepared to provide bureau services.
     */
    public BureauRegistry (
        String serverNameAndPort, 
        InvocationManager invmgr, 
        RootDObjectManager omgr)
    {
        _serverNameAndPort = serverNameAndPort;
        _invmgr = invmgr;
        _omgr = omgr;

        BureauProvider provider = new BureauProvider () {
            public void bureauInitialized (ClientObject client, String bureauId) {
                BureauRegistry.this.bureauInitialized(client, bureauId);
            }
            public void agentCreated (ClientObject client, int agentId) {
                BureauRegistry.this.agentCreated(client, agentId);
            }
            public void agentCreationFailed (ClientObject client, int agentId) {
                BureauRegistry.this.agentCreationFailed(client, agentId);
            }
            public void agentDestroyed (ClientObject client, int agentId) {
                BureauRegistry.this.agentDestroyed(client, agentId);
            }
        };

        _invmgr.registerDispatcher(
            new BureauDispatcher(provider), 
            BureauCodes.BUREAU_GROUP);
    }

    /**
     * Registers a command generator for a given type. When an agent is started and no bureaus are 
     * running, the <code>bureauType</code> is used to determine the <code>CommandGenerator</code> 
     * instance to call.
     * @param bureauType the type of bureau that will be launched
     * @param cmdGenerator the generator to be used for bureaus of <code>bureauType</code>
     */
    public void setCommandGenerator (String bureauType, CommandGenerator cmdGenerator)
    {
        if (_generators.get(bureauType) != null) {
            Log.warning("Generator for type already exists [type=" + 
                bureauType + "]");
            return;
        }

        _generators.put(bureauType, cmdGenerator);
    }

    /** 
     * Starts a new agent using the data in the given object, creating a new bureau if necessary.
     */
    public synchronized void startAgent (AgentObject agent)
    {
        Bureau bureau = _bureaus.get(agent.bureauId);
        if (bureau != null && bureau.ready()) {

            Log.info("Bureau ready, sending createAgent " + 
                StringUtil.toString(agent));

            BureauSender.createAgent(bureau.clientObj, agent.getOid());
            // !TODO: is this the right place to register the object?
            _omgr.registerObject(agent);
            bureau.agentStates.put(agent, Bureau.STARTED);

            bureau.summarize();

            return;
        }

        if (bureau == null) {

            CommandGenerator generator = _generators.get(agent.bureauType);
            if (generator == null) {
                Log.warning("CommandGenerator not found for agent's " + 
                       "bureau type " + StringUtil.toString(agent));
                return;
            }

            Log.info("Creating new bureau " + 
                StringUtil.toString(agent) + " " +
                StringUtil.toString(generator));

            bureau = new Bureau();
            bureau.bureauId = agent.bureauId;

            try {
                // kick off the bureau's process
                ProcessBuilder builder = new ProcessBuilder(
                    generator.createCommand(
                        _serverNameAndPort, agent.bureauId, ""));

                builder.redirectErrorStream(true);
                bureau.process = builder.start();

                // log the output of the process and prefix with bureau id
                ProcessLogger.copyMergedOutput(
                    Log.log, bureau.bureauId, bureau.process);
            }
            catch (Exception e) {
                Log.warning("Could not launch process for bureau " + 
                    StringUtil.toString(agent));
                Log.logStackTrace(e);

                return;
            }

            _bureaus.put(agent.bureauId, bureau);
        }

        _omgr.registerObject(agent);
        bureau.agentStates.put(agent, Bureau.PENDING);

        bureau.summarize();
    }

    /** 
     * Destroys a previously started agent using the data in the given object.
     */
    public synchronized void destroyAgent (AgentObject agent)
    {
        FoundAgent found = resolve(null, agent.getOid(), "destroyAgent");

        if (found == null) {
            return;
        }

        Log.warning("Destroying agent " + StringUtil.toString(agent));

        // transition the agent to a new state and perform the effect of the transition
        switch (found.state) {

        case Bureau.PENDING:
            found.bureau.agentStates.remove(found.agent);
            // !TODO: is the the right place to destroy it?
            _omgr.destroyObject(found.agent.getOid());
            break;

        case Bureau.STARTED:
            found.bureau.agentStates.put(found.agent, Bureau.STILL_BORN);
            break;

        case Bureau.RUNNING:
            BureauSender.destroyAgent(found.bureau.clientObj, agent.getOid());
            found.bureau.agentStates.put(found.agent, Bureau.DESTROYED);
            break;

        case Bureau.DESTROYED:
        case Bureau.STILL_BORN:
            Log.warning("Acknowledging a request to destory an agent, but agent " +
                "is in state " + found.state + ", ignoring request " + 
                StringUtil.toString(found.agent));
            break;
        }

        found.bureau.summarize();
    }

    /**
     * Callback for when the bureau client acknowledges starting up. Starts all pending agents and 
     * causes subsequent agent start requests to be sent directly to the bureau.
     */
    protected synchronized void bureauInitialized (ClientObject client, String bureauId)
    {
        final Bureau bureau = _bureaus.get(bureauId);
        if (bureau == null) {
            Log.warning("Acknowledging initialization of non-existent bureau " + 
                StringUtil.toString(bureauId));
            return;
        }

        bureau.clientObj = client;

        bureau.clientObj.addListener(new ObjectDeathListener() {
            public void objectDestroyed (ObjectDestroyedEvent e) {
                BureauRegistry.this.clientDestroyed(bureau);
            }
        });

        Log.info("Bureau created " + StringUtil.toString(bureau) + 
            ", launching pending agents");

        // find all pending agents
        Set<AgentObject> pending = new HashSet<AgentObject>();

        for (Map.Entry<AgentObject, Integer> entry : 
            bureau.agentStates.entrySet()) {

            if (entry.getValue() == Bureau.PENDING) {
                pending.add(entry.getKey());
            }
        }

        // create them
        for (AgentObject agent : pending) {
            Log.info("Creating agent " + StringUtil.toString(agent));
            BureauSender.createAgent(bureau.clientObj, agent.getOid());
            bureau.agentStates.put(agent, Bureau.STARTED);
        }

        bureau.summarize();
    }

    /**
     * Callback for when the bureau client acknowledges the creation of an agent.
     */
    protected synchronized void agentCreated (ClientObject client, int agentId)
    {
        FoundAgent found = resolve(client, agentId, "agentCreated");
        if (found == null) {
            return;
        }

        switch (found.state) {
        case Bureau.STARTED:
            found.bureau.agentStates.put(found.agent, Bureau.RUNNING);
            break;

        case Bureau.STILL_BORN:
            BureauSender.destroyAgent(found.bureau.clientObj, agentId);
            found.bureau.agentStates.put(found.agent, Bureau.DESTROYED);
            break;

        case Bureau.PENDING:
        case Bureau.RUNNING:
        case Bureau.DESTROYED:
            Log.warning("Received acknowledgement of the creation of an " + 
                "agent in state " + found.state + ", ignoring request " +
                StringUtil.toString(found.agent));
            break;
        }

        found.bureau.summarize();
    }

    /**
     * Callback for when the bureau client acknowledges the creation of an agent.
     */
    protected synchronized void agentCreationFailed (ClientObject client, int agentId)
    {
        FoundAgent found = resolve(client, agentId, "agentCreationFailed");
        if (found == null) {
            return;
        }

        switch (found.state) {
        case Bureau.STARTED:
            found.bureau.agentStates.remove(found.agent);
            break;

        case Bureau.STILL_BORN:
            found.bureau.agentStates.remove(found.agent);
            break;

        case Bureau.PENDING:
        case Bureau.RUNNING:
        case Bureau.DESTROYED:
            Log.warning("Received acknowledgement of creation failure for " + 
                "agent in state " + found.state + ", ignoring request " +
                StringUtil.toString(found.agent));
            break;
        }

        found.bureau.summarize();
    }

    /**
     * Callback for when the bureau client acknowledges the destruction of an agent.
     */
    protected synchronized void agentDestroyed (ClientObject client, int agentId)
    {
        FoundAgent found = resolve(client, agentId, "agentDestroyed");
        if (found == null) {
            return;
        }
        
        switch (found.state) {
        case Bureau.DESTROYED:
            found.bureau.agentStates.remove(found.agent);
            break;

        case Bureau.PENDING:
        case Bureau.STARTED:
        case Bureau.RUNNING:
        case Bureau.STILL_BORN:
            Log.warning("Acknowledging agent destruction, but state is " + 
                found.state + ", ignoring request " + 
                StringUtil.toString(found.agent));
            break;
        }

        found.bureau.summarize();
    }

    /** 
     * Callback for when a client is destroyed.
     */
    protected synchronized void clientDestroyed (Bureau bureau)
    {
        // clean up any agents attached to this bureau
        for (AgentObject agent : bureau.agentStates.keySet()) {
            _omgr.destroyObject(agent.getOid());
        }
    }

    /**
     * Does lots of null checks and lookups and resolves the given information into FoundAgent.
     */
    protected FoundAgent resolve (ClientObject client, int agentId, String resolver)
    {
        com.threerings.presents.dobj.DObject dobj = _omgr.getObject(agentId);
        if (dobj == null) {
            Log.warning("Non-existent agent in " + resolver +
                " [agentId=" + agentId + "]");
            return null;
        }

        if (!(dobj instanceof AgentObject)) {
            Log.warning("Object not an agent in " + resolver + 
                " " + StringUtil.toString(dobj));
            return null;
        }

        AgentObject agent = (AgentObject)dobj;
        Bureau bureau = _bureaus.get(agent.bureauId);
        if (bureau == null) {
            Log.warning("Bureau not found for agent in " + resolver +
                " " + StringUtil.toString(agent));
            return null;
        }

        if (!bureau.agentStates.containsKey(agent)) {
            Log.warning("Bureau does not have agent in " + resolver + 
                " " + StringUtil.toString(agent));
            return null;
        }

        if (bureau.clientObj == null) {
            Log.warning("Bureau not yet connected in " + resolver + 
                " " + StringUtil.toString(agent));
            return null;
        }

        if (client != null && bureau.clientObj != client) {
            Log.warning("Masquerading request in " + resolver + 
                " " + StringUtil.toString(agent) + 
                " " + StringUtil.toString(bureau.clientObj) +
                " " + StringUtil.toString(client));
            return null;
        }

        return new FoundAgent(bureau, agent, bureau.agentStates.get(agent));
    }

    // Models the results of searching for an agent
    protected static class FoundAgent
    {
        FoundAgent (
           Bureau bureau, 
           AgentObject agent, 
           int state)
        {
            this.bureau = bureau;
            this.agent = agent;
            this.state = state;
        }

        // Bureau containing the agent
        Bureau bureau;

        // The object
        AgentObject agent;

        // The state of the agent
        int state;
    }

    // Models a bureau, including the process handle, all running agents and their states
    protected static class Bureau
    {
        // Agent states {

        // Not yet stated, waiting for bureau to ack
        static final int PENDING = 0;

        // Bureau acked, agent told to start
        static final int STARTED = 1;

        // Agent ack'ed, now live and hosting, ready to tell other clients
        static final int RUNNING = 2;

        // Agent destruction requested, waiting for acknowledge (after which the agent is removed 
        // from the Bureau, so has no state)
        static final int DESTROYED = 3;

        // Edge case: destroy request prior to RUNNING
        static final int STILL_BORN = 4;

        // }

        // Should be non-null once the bureau is kicked off
        Process process;

        // The bureau's key in the map of bureaus. All requests for this bureau
        // with this id should be associated with one instance
        String bureauId;

        // The client object of the bureau that has opened a dobj connection to 
        // the registry
        ClientObject clientObj;

        // The states of the various agents allocated to this bureau
        Map<AgentObject, Integer> agentStates = 
            new HashMap<AgentObject, Integer>();

        boolean ready ()
        {
            return clientObj != null;
        }

        int countInState (int state)
        {
            Integer state1 = state;
            if (!agentStates.containsValue(state1)) {
                return 0;
            }

            int count = 0;
            for (Map.Entry<AgentObject, Integer> me : agentStates.entrySet()) {
                if (me.getValue() == state1) {
                    ++count;
                }
            }
            return count;
        }

        void summarize ()
        {
            Log.info("Bureau " + bureauId + " [" + 
                countInState(PENDING) + " pending, " +
                countInState(STARTED) + " started, " +
                countInState(RUNNING) + " running, " +
                countInState(DESTROYED) + " destroyed, " +
                countInState(STILL_BORN) + " still born]");
        }
    }

    // More readable generic map creation
    // TODO: add to library or use existing
    protected static <K, V> HashMap<K, V> hashMap ()
    {
        return new HashMap<K, V>();
    }

    protected String _serverNameAndPort;
    protected InvocationManager _invmgr;
    protected RootDObjectManager _omgr;
    protected Map<String, CommandGenerator> _generators = hashMap();
    protected Map<String, Bureau> _bureaus = hashMap();
}
