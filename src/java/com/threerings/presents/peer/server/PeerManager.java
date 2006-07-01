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

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.server.PresentsServer;

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
{
    /**
     * Creates a peer manager which will create a {@link NodeRepository} which
     * is used to publish our existence and discover the other nodes.
     *
     * @param nodeName this node's unique name.
     * @param sharedSecret a shared secret used to allow the peers to
     * authenticate with one another.
     * @param hostName the DNS name of the server running this node.
     * @param port the port on which other nodes should connect to us.
     * @param conprov used to obtain our JDBC connections.
     * @param invoker we will perform all database operations on the supplied
     * invoker thread.
     */
    public PeerManager (
        String nodeName, String sharedSecret, String hostName, int port,
        ConnectionProvider conprov, Invoker invoker)
        throws PersistenceException
    {
        _nodeName = nodeName;
        _hostName = hostName;
        _port = port;
        _sharedSecret = sharedSecret;
        _invoker = invoker;
        _noderepo = new NodeRepository(conprov);
    }

    /**
     * Instructs the node manager to load up information about its other nodes
     * and attempt to establish connections with those nodes.
     */
    public void init ()
    {
        // first register ourselves with the node table
        _invoker.postUnit(new Invoker.Unit() {
            public boolean invoke () {
                NodeRecord record = new NodeRecord(_nodeName, _hostName, _port);
                try {
                    _noderepo.updateNode(record);
                } catch (PersistenceException pe) {
                    log.warning("Failed to register node record " +
                                "[rec=" + record + ", error=" + pe + "].");
                }
                return false;
            }
        });

        // then start our peer refresh interval (this need not use a runqueue
        // as all it will do is post an invoker unit)
        new Interval() {
            public void expired () {
                refreshPeers();
            }
        }.schedule(5000L, 60*1000L);
    }

    /**
     * Call this when the server is shutting down to give this node a chance to
     * cleanly logoff from its peers and remove its record from the nodes
     * table.
     */
    public void shutdown ()
    {
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
            _peers.put(record.nodeName, peer = new PeerNode(record));
        }
        peer.refresh(record);
    }

    /**
     * Contains all runtime information for one of our peer nodes.
     */
    protected class PeerNode
        implements ClientObserver
    {
        public PeerNode (NodeRecord record)
        {
            _record = record;
            _client = new Client(null, PresentsServer.omgr);
            _client.addClientObserver(this);
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
        }

        // documentation inherited from interface ClientObserver
        public void clientObjectDidChange (Client client)
        {
        }

        // documentation inherited from interface ClientObserver
        public boolean clientWillLogoff (Client client)
        {
            return true;
        }

        // documentation inherited from interface ClientObserver
        public void clientDidLogoff (Client client)
        {
        }

        protected NodeRecord _record;
        protected Client _client;
        protected long _lastConnectStamp;
    }

    protected String _nodeName, _hostName, _sharedSecret;
    protected int _port;
    protected Invoker _invoker;
    protected NodeRepository _noderepo;
    protected HashMap<String,PeerNode> _peers = new HashMap<String,PeerNode>();
}
