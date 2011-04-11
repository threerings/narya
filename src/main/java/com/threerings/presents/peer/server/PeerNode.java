//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.server;

import java.net.ConnectException;

import java.util.Date;

import com.google.inject.Inject;

import com.samskivert.util.ResultListenerList;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.client.Communicator;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.SetListener;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.net.PeerBootstrapData;
import com.threerings.presents.peer.server.persist.NodeRecord;
import com.threerings.presents.server.PresentsDObjectMgr;
import com.threerings.presents.server.net.PresentsConnectionManager;
import com.threerings.presents.server.net.ServerCommunicator;

import static com.threerings.presents.Log.log;

/**
 * Contains all runtime information for one of our peer nodes.
 */
public class PeerNode
    implements ClientObserver, Subscriber<NodeObject>
{
    /** This peer's node object. */
    public NodeObject nodeobj;

    /**
     * Initializes this peer node and creates its internal client.
     */
    public void init (NodeRecord record)
    {
        _record = record;
        _client = new Client(null, _omgr) {
            @Override protected void convertFromRemote (DObject target, DEvent event) {
                super.convertFromRemote(target, event);
                // rewrite the event's target oid using the oid currently configured on the
                // distributed object (we will have it mapped in our remote server's oid space,
                // but it may have been remapped into the oid space of the local server)
                event.setTargetOid(target.getOid());
                // assign an eventId to this event so that our stale event detection code can
                // properly deal with it
                event.eventId = PeerNode.this._omgr.getNextEventId(true);
            }
            @Override protected Communicator createCommunicator () {
                return PeerNode.this.createCommunicator(this);
            }
        };
        _client.addClientObserver(this);
    }

    /**
     * Returns the {@link Client} instance that manages our connection to this peer.
     */
    public Client getClient ()
    {
        return _client;
    }

    /**
     * Returns this peer's unique string identifier.
     */
    public String getNodeName ()
    {
        return _record.nodeName;
    }

    /**
     * Returns the hostname for external clients to use when connecting to this peer.
     */
    public String getPublicHostName ()
    {
        return _record.publicHostName;
    }

    /**
     * Returns the hostname for internal clients to use when connecting to this peer.
     */
    public String getInternalHostName ()
    {
        return _record.hostName;
    }

    /**
     * Returns the port on which to connect to this peer.
     */
    public int getPort ()
    {
        return _record.port;
    }

    public void refresh (NodeRecord record)
    {
        // if the hostname of this node changed, kill our existing client and connect anew
        if (!record.hostName.equals(_record.hostName) && _client.isActive()) {
            _client.logoff(false);
        }

        // use our new record
        _record = record;

        // if our client is active, we're groovy
        if (_client.isActive()) {
            return;
        }

        // if our client hasn't updated its record since we last tried to logon, then just
        // chill
        if ((_lastConnectStamp - _record.lastUpdated.getTime()) > STALE_INTERVAL) {
            log.debug("Not reconnecting to stale client", "record", _record,
                      "lastTry", new Date(_lastConnectStamp));
            return;
        }

        // otherwise configure our client with the right bits and logon
        _client.setCredentials(_peermgr.createCreds());
        _client.setServer(_record.hostName, new int[] { _record.port });
        _client.logon();
        _lastConnectStamp = System.currentTimeMillis();
    }

    public void shutdown ()
    {
        if (_client.isActive()) {
            log.info("Logging off of peer " + _record + ".");
            _client.logoff(false);
        }
    }

    // documentation inherited from interface ClientObserver
    public void clientFailedToLogon (Client client, Exception cause)
    {
        if (cause instanceof ConnectException) {
            // we'll reconnect at most one minute later in refreshPeers()
            log.info("Peer not online " + _record + ": " + cause.getMessage());
        } else {
            log.warning("Peer logon attempt failed " + _record + ": " + cause);
        }
    }

    // documentation inherited from interface ClientObserver
    public void clientConnectionFailed (Client client, Exception cause)
    {
        // we'll reconnect at most one minute later in refreshPeers()
        log.warning("Peer connection failed " + _record + ": " + cause);
    }

    // documentation inherited from interface ClientObserver
    public void clientWillLogon (Client client)
    {
        // nothing doing
    }

    // documentation inherited from interface ClientObserver
    public void clientDidLogon (Client client)
    {
        log.info("Connected to peer " + _record + ".");

        // subscribe to this peer's node object
        PeerBootstrapData pdata = (PeerBootstrapData)client.getBootstrapData();
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
        if (nodeobj != null) {
// TODO: the MappingManager will take care of this
//             String nodeName = getNodeName();
//             for (ClientInfo clinfo : nodeobj.clients) {
//                 _peermgr.clientLoggedOff(nodeName, clinfo);
//             }

            nodeobj.removeListener(_listener);
        }

        _peermgr.disconnectedFromPeer(this);
        _listener = null;
        nodeobj = null;
    }

    // documentation inherited from interface ClientObserver
    public void clientDidClear (Client client)
    {
        // nothing doing
    }

    // documentation inherited from interface Subscriber
    public void objectAvailable (NodeObject object)
    {
        // listen for lock and cache updates
        nodeobj = object;
        nodeobj.addListener(_listener = createListener());

        _peermgr.connectedToPeer(this);

// TODO: the MappingManager will take care of this
//         String nodeName = getNodeName();
//         for (ClientInfo clinfo : nodeobj.clients) {
//             _peermgr.clientLoggedOn(nodeName, clinfo);
//         }
    }

    // documentation inherited from interface Subscriber
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        log.warning("Failed to subscribe to peer's node object", "peer", _record, "cause", cause);
    }

    /**
     * Determines whether the first node named has priority over the second when resolving
     * lock disputes.
     */
    protected static boolean hasPriority (String nodeName1, String nodeName2)
    {
        return nodeName1.compareTo(nodeName2) < 0;
    }

    protected Communicator createCommunicator (Client client)
    {
        return new ServerCommunicator(client, _conmgr, _omgr);
    }

    /**
     * Create the NodeObjectListener to use. Overrideable.
     */
    protected NodeObjectListener createListener ()
    {
        return new NodeObjectListener();
    }

    /**
     * Listens to node object changes.
     */
    protected class NodeObjectListener
        implements AttributeChangeListener //, SetListener<DSet.Entry>
    {
        // documentation inherited from interface AttributeChangeListener
        public void attributeChanged (AttributeChangedEvent event) {
            String name = event.getName();
            if (name.equals(NodeObject.ACQUIRING_LOCK)) {
                NodeObject.Lock lock = nodeobj.acquiringLock;
                PeerManager.LockHandler handler = _peermgr.getLockHandler(lock);
                if (handler == null) {
                    if (_peermgr.getNodeObject().locks.contains(lock)) {
                        log.warning("Peer trying to acquire lock owned by this node", "lock", lock,
                                    "node", _record.nodeName);
                        return;
                    }
                    _peermgr.createLockHandler(PeerNode.this, lock, true);
                    return;
                }

                // if the other node has priority, we're done
                if (hasPriority(handler.getNodeName(), _record.nodeName)) {
                    return;
                }

                // this node has priority, so cancel the existing handler and take over
                // its listeners
                ResultListenerList<String> olisteners = handler.listeners;
                handler.cancel();
                handler = _peermgr.createLockHandler(PeerNode.this, lock, true);
                handler.listeners = olisteners;

            } else if (name.equals(NodeObject.RELEASING_LOCK)) {
                NodeObject.Lock lock = nodeobj.releasingLock;
                PeerManager.LockHandler handler = _peermgr.getLockHandler(lock);
                if (handler == null) {
                    _peermgr.createLockHandler(PeerNode.this, lock, false);
                } else {
                    log.warning("Received request to release resolving lock",
                       "node", _record.nodeName, "handler", handler);
                }

            } else if (name.equals(NodeObject.CACHE_DATA)) {
                _peermgr.changedCacheData(nodeobj.cacheData.cache, nodeobj.cacheData.data);
            }
        }

// TODO: remove all this when MappingManager bits are finalized
//         // documentation inherited from interface SetListener
//         public void entryAdded (EntryAddedEvent<DSet.Entry> event) {
//             String name = event.getName();
//             if (NodeObject.CLIENTS.equals(name)) {
//                 _peermgr.clientLoggedOn(getNodeName(), (ClientInfo)event.getEntry());
//             }
//         }

//         // documentation inherited from interface SetListener
//         public void entryUpdated (EntryUpdatedEvent<DSet.Entry> event) {
//             // nada
//         }

//         // documentation inherited from interface SetListener
//         public void entryRemoved (EntryRemovedEvent<DSet.Entry> event) {
//             String name = event.getName();
//             if (NodeObject.CLIENTS.equals(name)) {
//                 _peermgr.clientLoggedOff(getNodeName(), (ClientInfo)event.getOldEntry());
//             }
//         }
    } // END: class NodeObjectListener

    protected NodeRecord _record;
    protected NodeObjectListener _listener;
    protected Client _client;
    protected long _lastConnectStamp;

    @Inject protected PeerManager _peermgr;
    @Inject protected PresentsDObjectMgr _omgr;
    @Inject protected PresentsConnectionManager _conmgr;

    /** The amount of time after which a node record can be considered out of date and invalid. */
    protected static final long STALE_INTERVAL = 5L * 60L * 1000L;
}
