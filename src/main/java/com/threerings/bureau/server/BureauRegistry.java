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

package com.threerings.bureau.server;

import java.util.Map;
import java.util.Set;

import java.io.IOException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Interval;
import com.samskivert.util.Invoker;
import com.samskivert.util.RunQueue;
import com.samskivert.util.StringUtil;

import com.threerings.presents.annotation.MainInvoker;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.ClientManager;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.PresentsSession;
import com.threerings.presents.server.ServiceAuthenticator;
import com.threerings.presents.server.SessionFactory;
import com.threerings.presents.server.net.PresentsConnectionManager;

import com.threerings.bureau.data.AgentObject;
import com.threerings.bureau.data.BureauAuthName;
import com.threerings.bureau.data.BureauCodes;
import com.threerings.bureau.data.BureauCredentials;
import com.threerings.bureau.data.BureauMarshaller;
import com.threerings.bureau.util.BureauLogRedirector;

import static com.threerings.bureau.Log.log;

/**
 *  Abstracts the launching and termination of external processes (bureaus) that host instances of
 *  server-side code (agents).
 */
@Singleton
public class BureauRegistry
{
    /**
     * Defines how a bureau is launched. Instances are associated to bureau types by the server on
     * startup. The instances are used whenever the registry needs to launch a bureau for an agent
     * with the associated bureau type.
     */
    public static interface Launcher
    {
        /**
         * Kicks off a new bureau. This method will always be called on the unit invocation
         * thread since it may do extensive I/O.
         * @param bureauId the id of the bureau being launched
         * @param token the secret string for the bureau to use in its credentials
         */
        void launchBureau (String bureauId, String token)
            throws IOException;
    }

    /**
     * Defines how to generate a command to launch a bureau in a local process.
     * @see #setCommandGenerator(String,CommandGenerator,int)
     * @see Launcher
     */
    public static interface CommandGenerator
    {
        /**
         * Creates the command line to launch a new bureau using the given information.
         * Called by the registry when a new bureau is needed whose type was registered
         * with <code>setCommandGenerator</code>.
         * @param bureauId the id of the bureau being launched
         * @param token the token string to use for the credentials when logging in
         * @return command line arguments, including executable name
         */
        String[] createCommand (String bureauId, String token);
    }

    /**
     * Creates an uninitialized registry.
     */
    @Inject public BureauRegistry (
        InvocationManager invmgr, PresentsConnectionManager conmgr, ClientManager clmgr)
    {
        invmgr.registerProvider(new BureauProvider() {
            public void bureauInitialized (ClientObject client, String bureauId) {
                BureauRegistry.this.bureauInitialized(client, bureauId);
            }
            public void bureauError (ClientObject caller, String message) {
                BureauRegistry.this.bureauError(caller, message);
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
        }, BureauMarshaller.class, BureauCodes.BUREAU_GROUP);
        conmgr.addChainedAuthenticator(new ServiceAuthenticator<BureauCredentials>(
                                           BureauCredentials.class, BureauAuthName.class) {
            @Override protected boolean areValid (BureauCredentials creds) {
                return checkToken(creds) == null;
            }
        });
        clmgr.addSessionFactory(
            SessionFactory.newSessionFactory(BureauCredentials.class, getSessionClass(),
                                             BureauAuthName.class, getClientResolverClass()));
        clmgr.addClientObserver(new ClientManager.ClientObserver() {
            public void clientSessionDidStart (PresentsSession client) {
                if (client.getCredentials() instanceof BureauCredentials) {
                    sessionDidStart(client, ((BureauCredentials)client.getCredentials()).clientId);
                }
            }
            public void clientSessionDidEnd (PresentsSession client) {
                if (client.getCredentials() instanceof BureauCredentials) {
                    sessionDidEnd(client, ((BureauCredentials)client.getCredentials()).clientId);
                }
            }
        });
    }

    /**
     * Check the credentials to make sure this is one of our bureaus.
     * @return null if all's well, otherwise a string describing the authentication failure
     */
    public String checkToken (BureauCredentials creds)
    {
        Bureau bureau = _bureaus.get(creds.clientId);
        if (bureau == null) {
            return "Bureau " + creds.clientId + " not found";
        }
        if (bureau.clientObj != null) {
            return "Bureau " + creds.clientId + " already logged in";
        }
        if (!creds.areValid(bureau.token)) {
            return "Bureau " + creds.clientId + " does not match credentials token";
        }
        return null;
    }

    /**
     * Registers a command generator for a given type. When an agent is started and no bureaus are
     * running, the <code>bureauType</code> is used to determine the <code>CommandGenerator</code>
     * instance to call. The registry will wait indefinitely for the bureau to connect back.
     * @param bureauType the type of bureau that will be launched
     * @param cmdGenerator the generator to be used for bureaus of <code>bureauType</code>
     */
    public void setCommandGenerator (String bureauType, final CommandGenerator cmdGenerator)
    {
        setCommandGenerator(bureauType, cmdGenerator, 0);
    }

    /**
     * Registers a command generator for a given type. When an agent is started and no bureaus are
     * running, the <code>bureauType</code> is used to determine the <code>CommandGenerator</code>
     * instance to call. If the launched bureau does not connect within the given number of
     * milliseconds, it will be logged as an error and future attempts to launch the bureau
     * will try launching the command again.
     * @param bureauType the type of bureau that will be launched
     * @param cmdGenerator the generator to be used for bureaus of <code>bureauType</code>
     * @param timeout milliseconds to wait for the bureau or 0 to wait forever
     */
    public void setCommandGenerator (
        String bureauType, final CommandGenerator cmdGenerator, int timeout)
    {
        setLauncher(bureauType, new Launcher() {
            public void launchBureau (String bureauId, String token)
                throws IOException {
                ProcessBuilder builder = new ProcessBuilder(
                    cmdGenerator.createCommand(bureauId, token));
                builder.redirectErrorStream(true);
                Process process = builder.start();
                // log the output of the process and prefix with bureau id
                new BureauLogRedirector(bureauId, process.getInputStream());
            }

            @Override
            public String toString () {
                return "DefaultLauncher for " + cmdGenerator;
            }
        }, timeout);
    }

    /**
     * Registers a launcher for a given type. When an agent is started and no bureaus are
     * running, the <code>bureauType</code> is used to determine the <code>Launcher</code>
     * instance to call. The registry will wait indefinitely for the launched bureau
     * to connect back.
     * @param bureauType the type of bureau that will be launched
     * @param launcher the launcher to be used for bureaus of <code>bureauType</code>
     */
    public void setLauncher (String bureauType, Launcher launcher)
    {
        setLauncher(bureauType, launcher, 0);
    }

    /**
     * Registers a launcher for a given type. When an agent is started and no bureaus are
     * running, the <code>bureauType</code> is used to determine the <code>Launcher</code>
     * instance to call. If the launched bureau does not connect within the given number of
     * milliseconds, it will be logged as an error and future attempts to launch the bureau
     * will invoke the <code>launch</code> method again.
     * @param bureauType the type of bureau that will be launched
     * @param launcher the launcher to be used for bureaus of <code>bureauType</code>
     * @param timeout milliseconds to wait for the bureau or 0 to wait forever
     */
    public void setLauncher (String bureauType, Launcher launcher, int timeout)
    {
        if (_launchers.get(bureauType) != null) {
            log.warning("Launcher for type already exists", "type", bureauType);
            return;
        }

        _launchers.put(bureauType, new LauncherEntry(launcher, timeout));
    }

    /**
     * Starts a new agent using the data in the given object, creating a new bureau if necessary.
     */
    public void startAgent (AgentObject agent)
    {
        agent.setLocal(AgentData.class, new AgentData());

        Bureau bureau = _bureaus.get(agent.bureauId);
        if (bureau != null && bureau.ready()) {
            _omgr.registerObject(agent);

            log.info("Bureau ready, sending createAgent", "agent", agent.which());
            BureauSender.createAgent(bureau.clientObj, agent.getOid());
            bureau.agentStates.put(agent, AgentState.STARTED);
            bureau.summarize();
            return;
        }

        if (bureau == null) {
            LauncherEntry launcherEntry = _launchers.get(agent.bureauType);
            if (launcherEntry == null) {
                log.warning("Launcher not found", "agent", agent.which());
                return;
            }

            log.info("Creating new bureau", "bureauId", agent.bureauId, "launcher", launcherEntry);
            bureau = new Bureau();
            bureau.bureauId = agent.bureauId;
            bureau.token = generateToken(bureau.bureauId);
            bureau.launcherEntry = launcherEntry;
            _invoker.postUnit(new LauncherUnit(bureau, _omgr));
            _bureaus.put(agent.bureauId, bureau);
        }

        _omgr.registerObject(agent);
        bureau.agentStates.put(agent, AgentState.PENDING);

        log.info("Bureau not ready, pending agent", "agent", agent.which());
        bureau.summarize();
    }

    /**
     * Destroys a previously started agent using the data in the given object.
     */
    public void destroyAgent (AgentObject agent)
    {
        FoundAgent found = resolve(null, agent.getOid(), "destroyAgent");
        if (found == null) {
            return;
        }

        log.info("Destroying agent", "agent", agent.which());

        // transition the agent to a new state and perform the effect of the transition
        if (found.state == AgentState.PENDING) {
            found.bureau.agentStates.remove(found.agent);
            _omgr.destroyObject(found.agent.getOid());

        } else if (found.state == AgentState.STARTED) {
            found.bureau.agentStates.put(found.agent, AgentState.STILL_BORN);

        } else if (found.state == AgentState.RUNNING) {
            // TODO: have a timeout for this in case the client is misbehaving or hung
            BureauSender.destroyAgent(found.bureau.clientObj, agent.getOid());
            found.bureau.agentStates.put(found.agent, AgentState.DESTROYED);

        } else if (found.state == AgentState.DESTROYED ||
            found.state == AgentState.STILL_BORN) {
            log.warning("Ignoring request to destroy agent in unexpected state",
                        "state", found.state, "agent", found.agent.which());
        }

        found.bureau.summarize();
    }

    /**
     * Returns the active session for a bureau of the given id.
     */
    public PresentsSession lookupClient (String bureauId)
    {
        Bureau bureau = _bureaus.get(bureauId);
        if (bureau == null) {
            return null;
        }
        return bureau.client;
    }

    /**
     * If this agent's bureau encountered an error on launch, return it.
     */
    public Exception getLaunchError (AgentObject agentObj)
    {
        AgentData data = agentObj.getLocal(AgentData.class);
        if (data == null) {
            return null;
        }
        return data.launchError;
    }

    protected void sessionDidStart (PresentsSession client, String id)
    {
        Bureau bureau = _bureaus.get(id);
        if (bureau == null) {
            log.warning("Starting session for unknown bureau", "id", id, "client", client);
            return;
        }
        if (bureau.client != null) {
            log.warning("Multiple sessions for the same bureau", "id", id, "client", client,
                        "bureau", bureau);
        }
        bureau.client = client;
    }

    protected void sessionDidEnd (PresentsSession client, String id)
    {
        Bureau bureau = _bureaus.get(id);
        if (bureau == null) {
            log.warning("Ending session for unknown bureau", "id", id, "client", client);
            return;
        }
        if (bureau.client == null) {
            log.warning("Multiple logouts from the same bureau", "id", id, "client", client,
                        "bureau", bureau);
        }
        bureau.client = null;

        clientDestroyed(bureau);
    }

    /**
     * Callback for when the bureau client acknowledges starting up. Starts all pending agents and
     * causes subsequent agent start requests to be sent directly to the bureau.
     */
    protected void bureauInitialized (ClientObject client, String bureauId)
    {
        final Bureau bureau = _bureaus.get(bureauId);
        if (bureau == null) {
            log.warning("Initialization of non-existent bureau", "bureauId", bureauId);
            return;
        }

        bureau.clientObj = client;

        log.info("Bureau created, launching pending agents", "bureau", bureau);

        // find all pending agents
        Set<AgentObject> pending = Sets.newHashSet();

        for (Map.Entry<AgentObject, AgentState> entry :
            bureau.agentStates.entrySet()) {

            if (entry.getValue() == AgentState.PENDING) {
                pending.add(entry.getKey());
            }
        }

        // create them
        for (AgentObject agent : pending) {
            log.info("Creating agent", "agent", agent.which());
            BureauSender.createAgent(bureau.clientObj, agent.getOid());
            bureau.agentStates.put(agent, AgentState.STARTED);
        }

        bureau.summarize();
    }

    protected void bureauError (ClientObject caller, String message)
    {
        for (Bureau bureau : _bureaus.values()) {
            if (bureau.clientObj == caller) {
                log.info(
                    "Bureau error occurred", "caller", caller.who(), "message", message,
                    "bureau", bureau.bureauId);
                bureau.client.endSession();
                return;
            }
        }
        log.warning(
            "Bureau error occurred in unregistered bureau", "caller", caller.who(),
            "message", message);
    }

    /**
     * Callback for when the bureau client acknowledges the creation of an agent.
     */
    protected void agentCreated (ClientObject client, int agentId)
    {
        FoundAgent found = resolve(client, agentId, "agentCreated");
        if (found == null) {
            return;
        }

        log.info("Agent creation confirmed", "agent", found.agent.which());

        if (found.state == AgentState.STARTED) {
            found.bureau.agentStates.put(found.agent, AgentState.RUNNING);
            found.agent.setClientOid(client.getOid());

        } else if (found.state == AgentState.STILL_BORN) {
            // TODO: have a timeout for this in case the client is misbehaving or hung
            BureauSender.destroyAgent(found.bureau.clientObj, agentId);
            found.bureau.agentStates.put(found.agent, AgentState.DESTROYED);

        } else if (found.state == AgentState.PENDING ||
            found.state == AgentState.RUNNING ||
            found.state == AgentState.DESTROYED) {
            log.warning("Ignoring confirmation of creation of an agent in an unexpected state",
                        "state", found.state, "agent", found.agent.which());
        }

        found.bureau.summarize();
    }

    /**
     * Callback for when the bureau client acknowledges the failure to create an agent.
     */
    protected void agentCreationFailed (ClientObject client, int agentId)
    {
        FoundAgent found = resolve(client, agentId, "agentCreationFailed");
        if (found == null) {
            return;
        }

        log.info("Agent creation failed", "agent", found.agent.which());

        if (found.state == AgentState.STARTED ||
            found.state == AgentState.STILL_BORN) {
            found.bureau.agentStates.remove(found.agent);
            _omgr.destroyObject(found.agent.getOid());

        } else if (found.state == AgentState.PENDING ||
            found.state == AgentState.RUNNING ||
            found.state == AgentState.DESTROYED) {
            log.warning("Ignoring failure of creation of an agent in an unexpected state",
                        "state", found.state, "agent", found.agent.which());
        }

        found.bureau.summarize();
    }

    /**
     * Callback for when the bureau client acknowledges the destruction of an agent.
     */
    protected void agentDestroyed (ClientObject client, int agentId)
    {
        FoundAgent found = resolve(client, agentId, "agentDestroyed");
        if (found == null) {
            return;
        }

        log.info("Agent destruction confirmed", "agent", found.agent.which());

        if (found.state == AgentState.DESTROYED) {
            found.bureau.agentStates.remove(found.agent);
            _omgr.destroyObject(found.agent.getOid());

        } else if (found.state == AgentState.PENDING ||
            found.state == AgentState.STARTED ||
            found.state == AgentState.RUNNING ||
            found.state == AgentState.STILL_BORN) {
            log.warning("Ignoring confirmation of destruction of agent in unexpected state",
                        "state", found.state, "agent", found.agent.which());
        }

        found.bureau.summarize();
    }

    /**
     * Callback for when a client is destroyed.
     */
    protected void clientDestroyed (Bureau bureau)
    {
        log.info("Client destroyed, destroying all agents", "bureau", bureau);

        // clean up any agents attached to this bureau
        for (AgentObject agent : bureau.agentStates.keySet()) {
            _omgr.destroyObject(agent.getOid());
        }
        bureau.agentStates.clear();

        if (_bureaus.remove(bureau.bureauId) == null) {
            log.info("Bureau not found to remove", "bureau", bureau);
        }
    }

    /**
     * Does lots of null checks and lookups and resolves the given information into FoundAgent.
     */
    protected FoundAgent resolve (ClientObject client, int agentId, String resolver)
    {
        com.threerings.presents.dobj.DObject dobj = _omgr.getObject(agentId);
        if (dobj == null) {
            log.warning("Non-existent agent", "function", resolver, "agentId", agentId);
            return null;
        }

        if (!(dobj instanceof AgentObject)) {
            log.warning("Object not an agent", "function", resolver, "obj", dobj.getClass());
            return null;
        }

        AgentObject agent = (AgentObject)dobj;
        Bureau bureau = _bureaus.get(agent.bureauId);
        if (bureau == null) {
            log.warning("Bureau not found for agent", "function", resolver, "agent", agent.which());
            return null;
        }

        if (!bureau.agentStates.containsKey(agent)) {
            log.warning("Bureau does not have agent", "function", resolver, "agent", agent.which());
            return null;
        }

        if (client != null && bureau.clientObj != client) {
            log.warning("Masquerading request", "function", resolver, "agent", agent.which(),
                        "client", bureau.clientObj, "client", client);
            return null;
        }

        return new FoundAgent(bureau, agent, bureau.agentStates.get(agent));
    }

    /**
     * Create a hard-to-guess token that the bureau can use to authenticate itself when it tries
     * to log in.
     */
    protected String generateToken (String bureauId)
    {
        String tokenSource = bureauId + "@" + System.currentTimeMillis() + "r" + Math.random();
        return StringUtil.md5hex(tokenSource);
    }

    /**
     * Called by the launcher unit timeout time after launching.
     * @param bureau bureau whose launch occurred
     */
    protected void launchTimeoutExpired (Bureau bureau)
    {
        if (bureau.clientObj != null) {
            return; // all's well, ignore
        }

        if (!_bureaus.containsKey(bureau.bureauId)) {
            // bureau has already managed to get destroyed before the launch timeout, ignore
            return;
        }

        handleLaunchError(bureau, null, "timeout");
    }

    /**
     * Called when something goes wrong with launching a bureau.
     */
    protected void handleLaunchError (Bureau bureau, Exception error, String cause)
    {
        if (cause == null && error != null) {
            cause = error.getMessage();
        }
        log.info("Bureau failed to launch", "bureau", bureau, "cause", cause);

        // clean up any agents attached to this bureau
        for (AgentObject agent : bureau.agentStates.keySet()) {
            agent.getLocal(AgentData.class).launchError = error;
            _omgr.destroyObject(agent.getOid());
        }
        bureau.agentStates.clear();

        _bureaus.remove(bureau.bureauId);
    }

    /**
     * Returns the class used to handle bureau sessions.
     */
    protected Class<? extends BureauSession> getSessionClass ()
    {
        return BureauSession.class;
    }

    /**
     * Returns the class used to resolve bureau client data.
     */
    protected Class<? extends BureauClientResolver> getClientResolverClass ()
    {
        return BureauClientResolver.class;
    }

    /**
     * Invoker unit to launch a bureau's process, then assign the result on the main thread.
     */
    protected class LauncherUnit extends Invoker.Unit
    {
        LauncherUnit (Bureau bureau, RunQueue runQueue) {
            super("LauncherUnit for " + bureau + ": " + StringUtil.toString(bureau.launcherEntry));
            _bureau = bureau;
            _runQueue = runQueue;
        }

        @Override public boolean invoke () {
            try {
                _bureau.launch();
            } catch (Exception e) {
                _error = e;
            }
            return true;
        }

        @Override
        public void handleResult () {
            if (_error == null) {
                // bureau launched ok, but it may still not connect. wait for timeout
                int timeout = _bureau.launcherEntry.timeout;
                if (timeout != 0) {
                    new Interval(_runQueue) {
                        @Override public void expired () {
                            launchTimeoutExpired(_bureau);
                        }
                    }.schedule(timeout);
                }
                _bureau.launched = true;
                _bureau.launcherEntry = null;
                log.info("Bureau launch requested", "bureau", _bureau);

            } else {
                handleLaunchError(_bureau, _error, null);
            }
        }

        protected Bureau _bureau;
        protected Exception _error;
        protected RunQueue _runQueue;
    }

    protected static class LauncherEntry
    {
        public Launcher launcher;
        public int timeout;

        public LauncherEntry (Launcher launcher, int timeout) {
            this.launcher = launcher;
            this.timeout = timeout;
        }

        @Override
        public String toString () {
            return StringUtil.fieldsToString(this);
        }
    }

    protected enum AgentState
    {
        // Not yet stated, waiting for bureau to ack
        PENDING,
        // Bureau acked, agent told to start
        STARTED,
        // Agent ack'ed, now live and hosting, ready to tell other clients
        RUNNING,
        // Agent destruction requested, waiting for acknowledge (after which the agent is removed
        // from the Bureau, so has no state)
        DESTROYED,
        // Edge case: destroy request prior to RUNNING
        STILL_BORN
    }

    /** Models the results of searching for an agent. */
    protected static class FoundAgent
    {
        FoundAgent (Bureau bureau, AgentObject agent, AgentState state) {
            this.bureau = bureau;
            this.agent = agent;
            this.state = state;
        }

        // Bureau containing the agent
        Bureau bureau;

        // The object
        AgentObject agent;

        // The state of the agent
        AgentState state;
    }

    /** Models a bureau, including the process handle, all running agents and their states. */
    protected static class Bureau
    {
        // non-null once the bureau is scheduled but not yet kicked off
        LauncherEntry launcherEntry;

        // non-null once the bureau is kicked off
        boolean launched;

        // The token given to this bureau for authentication
        String token;

        // The bureau's key in the map of bureaus. All requests for this bureau
        // with this id should be associated with one instance
        String bureauId;

        // The client object of the bureau that has opened a dobj connection to
        // the registry
        ClientObject clientObj;

        // The client session
        PresentsSession client;

        // The states of the various agents allocated to this bureau
        Map<AgentObject, AgentState> agentStates = Maps.newHashMap();

        @Override
        public String toString () {
            StringBuilder builder = new StringBuilder();
            builder.append("[Bureau id=").append(bureauId).append(", client=");
            if (clientObj == null) {
                builder.append("null");
            } else {
                builder.append(clientObj.getOid());
            }
            builder.append(", launcherEntry=").append(launcherEntry);
            builder.append(", launched=").append(launched);
            builder.append(", totalAgents=").append(agentStates.size());
            agentSummary(builder.append(", ")).append("]");
            return builder.toString();
        }

        boolean ready () {
            return clientObj != null;
        }

        StringBuilder agentSummary (StringBuilder str) {
            int[] counts = new int[AgentState.values().length];
            for (Map.Entry<AgentObject, AgentState> me : agentStates.entrySet()) {
                counts[me.getValue().ordinal()]++;
            }
            for (AgentState state : AgentState.values()) {
                if (state.ordinal() > 0) {
                    str.append(", ");
                }
                str.append(counts[state.ordinal()]).append(" ").append(state.name());
            }
            return str;
        }

        void summarize () {
            StringBuilder str = new StringBuilder();
            str.append("Bureau ").append(bureauId).append(" [");
            agentSummary(str).append("]");
            log.info(str.toString());
        }

        void launch () throws IOException {
            launcherEntry.launcher.launchBureau(bureauId, token);
        }
    }

    protected static class AgentData
    {
        Exception launchError;
    }

    protected Map<String, LauncherEntry> _launchers = Maps.newHashMap();
    protected Map<String, Bureau> _bureaus = Maps.newHashMap();

    @Inject protected RootDObjectManager _omgr;
    @Inject protected @MainInvoker Invoker _invoker;
}
