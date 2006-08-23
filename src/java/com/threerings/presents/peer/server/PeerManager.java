//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.util.Interval;
import com.samskivert.util.Invoker;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.server.ClientManager;
import com.threerings.presents.server.PresentsClient;
import com.threerings.presents.server.PresentsServer;

import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.net.PeerBootstrapData;
import com.threerings.presents.peer.net.PeerCreds;
import com.threerings.presents.peer.server.persist.NodeRecord;
import com.threerings.presents.peer.server.persist.NodeRepository;

import static com.threerings.presents.Log.log;

/**
 * Manages connections to the other nodes in a Presents server cluster. Each
 * server maintains a client connection to the other servers and subscribes to
 * the {@link NodeObject} of all peer servers and uses those objects to
 * communicate cross-node information.
 */
public class PeerManager
    implements ClientManager.ClientObserver
{
    /**
     * Creates a peer manager which will create a {@link NodeRepository} which
     * will be used to publish our existence and discover the other nodes.
     */
    public PeerManager (ConnectionProvider conprov, Invoker invoker)
        throws PersistenceException
    {
        _invoker = invoker;
        _noderepo = new NodeRepository(conprov);
    }

    /**
     * Returns the distributed object that represents this node to its peers.
     */
    public NodeObject getNodeObject ()
    {
        return _nodeobj;
    }

    /**
     * Initializes this peer manager and initiates the process of connecting to
     * its peer nodes. This will also reconfigure the ConnectionManager and
     * ClientManager with peer related bits, so this should not be called until
     * <em>after</em> the main server has set up its client factory and
     * authenticator.
     *
     * @param nodeName this node's unique name.
     * @param sharedSecret a shared secret used to allow the peers to
     * authenticate with one another.
     * @param hostName the DNS name of the server running this node.
     * @param publicHostName if non-null, a separate public DNS hostname by
     * which the node is to be known to normal clients (we may want inter-peer
     * communication to take place over a different network than the
     * communication between real clients and the various peer servers).
     * @param port the port on which other nodes should connect to us.
     * @param conprov used to obtain our JDBC connections.
     * @param invoker we will perform all database operations on the supplied
     * invoker thread.
     */
    public void init (String nodeName, String sharedSecret, String hostName,
                      String publicHostName, int port)
    {
        _nodeName = nodeName;
        _hostName = hostName;
        _publicHostName = (publicHostName == null) ? publicHostName : hostName;
        _port = port;
        _sharedSecret = sharedSecret;

        // wire ourselves into the server
        PresentsServer.conmgr.setAuthenticator(
            new PeerAuthenticator(
                this, PresentsServer.conmgr.getAuthenticator()));
        PresentsServer.clmgr.setClientFactory(
            new PeerClientFactory(
                this, PresentsServer.clmgr.getClientFactory()));

        // create our node object
        _nodeobj = PresentsServer.omgr.registerObject(createNodeObject());

        // register ourselves with the node table
        _invoker.postUnit(new Invoker.Unit() {
            public boolean invoke () {
                NodeRecord record = new NodeRecord(
                    _nodeName, _hostName, _publicHostName, _port);
                try {
                    _noderepo.updateNode(record);
                } catch (PersistenceException pe) {
                    log.warning("Failed to register node record " +
                                "[rec=" + record + ", error=" + pe + "].");
                }
                return false;
            }
        });

        // register ourselves as a client observer
        PresentsServer.clmgr.addClientObserver(this);

        // and start our peer refresh interval (this need not use a runqueue as
        // all it will do is post an invoker unit)
        new Interval() {
            public void expired () {
                refreshPeers();
            }
        }.schedule(5000L, 60*1000L);

        // give derived classes an easy way to get in on the init action
        didInit();
    }

    /**
     * Call this when the server is shutting down to give this node a chance to
     * cleanly logoff from its peers and remove its record from the nodes
     * table.
     */
    public void shutdown ()
    {
        // clear out our client observer registration
        PresentsServer.clmgr.removeClientObserver(this);

        // TODO: clear our record from the node table
        for (PeerNode peer : _peers.values()) {
            peer.shutdown();
        }
    }

    /**
     * Returns true if the supplied peer credentials match our shared secret.
     */
    public boolean isAuthenticPeer (PeerCreds creds)
    {
        return PeerCreds.createPassword(
            creds.getNodeName(), _sharedSecret).equals(creds.getPassword());
    }

    // documentation inherited from interface ClientManager.ClientObserver
    public void clientSessionDidStart (PresentsClient client)
    {
        // if this is another peer, don't publish their info
        if (client instanceof PeerClient) {
            return;
        }

        // create and publish a ClientInfo record for this client
        ClientInfo clinfo = createClientInfo();
        initClientInfo(client, clinfo);

        // sanity check
        if (_nodeobj.clients.contains(clinfo)) {
            log.warning("Received clientSessionDidStart() for already " +
                        "registered client!? " +
                        "[old=" + _nodeobj.clients.get(clinfo.getKey()) +
                        ", new=" + clinfo + "].");
            // go ahead and update the record
            _nodeobj.updateClients(clinfo);
        } else {
            _nodeobj.addToClients(clinfo);
        }
    }

    // documentation inherited from interface ClientManager.ClientObserver
    public void clientSessionDidEnd (PresentsClient client)
    {
        // if this is another peer, don't worry about it
        if (client instanceof PeerClient) {
            return;
        }

        // we scan through the list instead of relying on ClientInfo.getKey()
        // because we want derived classes to be able to override that for
        // lookups that happen way more frequently than logging off
        Name username = client.getCredentials().getUsername();
        for (ClientInfo clinfo : _nodeobj.clients) {
            if (clinfo.username.equals(username)) {
                _nodeobj.removeFromClients(clinfo.getKey());
                return;
            }
        }
        log.warning("Session ended for unregistered client " +
                    "[who=" + username + "].");
    }

    /**
     * Called after we have finished our initialization.
     */
    protected void didInit ()
    {
    }

    /**
     * Reloads the list of peer nodes from our table and refreshes each with a
     * call to {@link #refreshPeer}.
     */
    protected void refreshPeers ()
    {
        // load up information on our nodes
        _invoker.postUnit(new Invoker.Unit() {
            public boolean invoke () {
                try {
                    _nodes = _noderepo.loadNodes();
                    return true;
                } catch (PersistenceException pe) {
                    log.warning("Failed to load node records: " + pe + ".");
                    // we'll just try again next time
                    return false;
                }
            }
            public void handleResult () {
                for (NodeRecord record : _nodes) {
                    if (record.nodeName.equals(_nodeName)) {
                        continue;
                    }
                    try {
                        refreshPeer(record);
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Failure refreshing peer " +
                                record + ".", e);
                    }
                }
            }
            protected ArrayList<NodeRecord> _nodes;
        });
    }

    /**
     * Ensures that we have a connection to the specified node if it has
     * checked in since we last failed to connect.
     */
    protected void refreshPeer (NodeRecord record)
    {
        PeerNode peer = _peers.get(record.nodeName);
        if (peer == null) {
            _peers.put(record.nodeName, peer = createPeerNode(record));
        }
        peer.refresh(record);
    }

    /**
     * Creates the appropriate derived class of {@link NodeObject} which will
     * be registered with the distributed object system.
     */
    protected NodeObject createNodeObject ()
    {
        return new NodeObject();
    }

    /**
     * Creates a {@link ClientInfo} record which will subsequently be
     * initialized by a call to {@link #initClientInfo}.
     */
    protected ClientInfo createClientInfo ()
    {
        return new ClientInfo();
    }

    /**
     * Initializes the supplied client info for the supplied client.
     */
    protected void initClientInfo (PresentsClient client, ClientInfo info)
    {
        info.username = client.getCredentials().getUsername();
    }

    /**
     * Creates a {@link PeerNode} to manage our connection to the specified
     * peer.
     */
    protected PeerNode createPeerNode (NodeRecord record)
    {
        return new PeerNode(record);
    }

    /**
     * Contains all runtime information for one of our peer nodes.
     */
    protected class PeerNode
        implements ClientObserver, Subscriber<NodeObject>
    {
        /** This peer's node object. */
        public NodeObject nodeobj;

        public PeerNode (NodeRecord record)
        {
            _record = record;
            _client = new Client(null, PresentsServer.omgr);
            _client.addClientObserver(this);
        }

        public Client getClient ()
        {
            return _client;
        }

        public void refresh (NodeRecord record)
        {
            // if the hostname of this node changed, kill our existing client
            // connection and connect anew
            if (!record.hostName.equals(_record.hostName) &&
                _client.isActive()) {
                _client.logoff(false);
            }

            // if our client is active, we're groovy
            if (_client.isActive()) {
                return;
            }

            // if our client hasn't updated its record since we last tried to
            // logon, then just chill
            if (_lastConnectStamp > record.lastUpdated.getTime()) {
                log.fine("Not reconnecting to stale client [record=" + _record +
                         ", lastTry=" + new Date(_lastConnectStamp) + "].");
                return;
            }

            // otherwise configure our client with the right bits and logon
            _client.setCredentials(
                new PeerCreds(_record.nodeName, _sharedSecret));
            _client.setServer(record.hostName, new int[] { _record.port });
            _client.logon();
            _lastConnectStamp = System.currentTimeMillis();
        }

        public void shutdown ()
        {
            if (_client.isActive()) {
                _client.logoff(false);
            }
        }

        // documentation inherited from interface ClientObserver
        public void clientFailedToLogon (Client client, Exception cause)
        {
            // we'll reconnect at most one minute later in refreshPeers()
            log.warning("Peer logon attempt failed " + _record + ": " + cause);
        }

        // documentation inherited from interface ClientObserver
        public void clientConnectionFailed (Client client, Exception cause)
        {
            // we'll reconnect at most one minute later in refreshPeers()
            log.warning("Peer connection failed " + _record + ": " + cause);
        }

        // documentation inherited from interface ClientObserver
        public void clientDidLogon (Client client)
        {
            log.info("Connected to peer " + _record + ".");

            // subscribe to this peer's node object
            PeerBootstrapData pdata = (PeerBootstrapData)
                client.getBootstrapData();
            client.getDObjectManager().subscribeToObject(pdata.nodeOid, this);
        }

        // documentation inherited from interface ClientObserver
        public void clientObjectDidChange (Client client)
        {
            // nothing doing
        }

        // documentation inherited from interface ClientObserver
        public boolean clientWillLogoff (Client client)
        {
            return true;
        }

        // documentation inherited from interface ClientObserver
        public void clientDidLogoff (Client client)
        {
            // TODO: clean things up?
        }

        // documentation inherited from interface ClientObserver
        public void clientDidClear (Client client)
        {
            // nothing doing
        }

        // documentation inherited from interface Subscriber
        public void objectAvailable (NodeObject object)
        {
            nodeobj = object;
            // TODO: stuff!
        }

        // documentation inherited from interface Subscriber
        public void requestFailed (int oid, ObjectAccessException cause)
        {
            log.warning("Failed to subscribe to peer's node object " +
                        "[peer=" + _record + ", cause=" + cause + "].");
        }

        protected NodeRecord _record;
        protected Client _client;
        protected long _lastConnectStamp;
    }

    protected String _nodeName, _hostName, _publicHostName, _sharedSecret;
    protected int _port;
    protected Invoker _invoker;
    protected NodeRepository _noderepo;
    protected NodeObject _nodeobj;
    protected HashMap<String,PeerNode> _peers = new HashMap<String,PeerNode>();
}
