//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

import java.net.ConnectException;
import java.util.Date;

import com.samskivert.util.ResultListenerList;

import com.threerings.presents.client.BlockingCommunicator;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.client.Communicator;
import com.threerings.presents.server.PresentsServer;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;

import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.net.PeerBootstrapData;
import com.threerings.presents.peer.net.PeerCreds;
import com.threerings.presents.peer.server.persist.NodeRecord;

import static com.threerings.presents.Log.log;

/**
 * Contains all runtime information for one of our peer nodes.
 */
public class PeerNode
    implements ClientObserver, Subscriber<NodeObject>, AttributeChangeListener
{
    /** This peer's node object. */
    public NodeObject nodeobj;

    public PeerNode (PeerManager peermgr, NodeRecord record)
    {
        _peermgr = peermgr;
        _record = record;
        _client = new Client(null, PresentsServer.omgr) {
            protected void convertFromRemote (DObject target, DEvent event) {
                super.convertFromRemote(target, event);
                // rewrite the event's target oid using the oid currently configured on the
                // distributed object (we will have it mapped in our remote server's oid space,
                // but it may have been remapped into the oid space of the local server)
                event.setTargetOid(target.getOid());
                // assign an eventId to this event so that our stale event detection code can
                // properly deal with it
                event.eventId = PresentsServer.omgr.getNextEventId(true);
            }
            protected Communicator createCommunicator () {
                // TODO: make a custom communicator that uses the ClientManager NIO system to do
                // its I/O instead of using two threads and blocking socket I/O
                return new BlockingCommunicator(this);
            }
        };
        _client.addClientObserver(this);
    }

    public Client getClient ()
    {
        return _client;
    }

    public String getNodeName ()
    {
        return _record.nodeName;
    }

    public String getPublicHostName ()
    {
        return _record.publicHostName;
    }

    public int getPort ()
    {
        return _record.port;
    }

    public void refresh (NodeRecord record)
    {
        // if the hostname of this node changed, kill our existing client connection and connect
        // anew
        if (!record.hostName.equals(_record.hostName) &&
            _client.isActive()) {
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
            log.fine("Not reconnecting to stale client [record=" + _record +
                     ", lastTry=" + new Date(_lastConnectStamp) + "].");
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
        _peermgr.peerDidLogoff(this);
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
        nodeobj.addListener(this);

        _peermgr.peerDidLogon(this);
    }

    // documentation inherited from interface Subscriber
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        log.warning("Failed to subscribe to peer's node object " +
                    "[peer=" + _record + ", cause=" + cause + "].");
    }

    // documentation inherited from interface AttributeChangeListener
    public void attributeChanged (AttributeChangedEvent event)
    {
        String name = event.getName();
        if (name.equals(NodeObject.ACQUIRING_LOCK)) {
            NodeObject.Lock lock = nodeobj.acquiringLock;
            PeerManager.LockHandler handler = _peermgr.getLockHandler(lock);
            if (handler == null) {
                if (_peermgr.getNodeObject().locks.contains(lock)) {
                    log.warning("Peer trying to acquire lock owned by this node " +
                                "[lock=" + lock + ", node=" + _record.nodeName + "].");
                    return;
                }
                _peermgr.createLockHandler(this, lock, true);
                return;
            }

            // if the other node has priority, we're done
            if (hasPriority(handler.getNodeName(), _record.nodeName)) {
                return;
            }

            // this node has priority, so cancel the existing handler and take over its listeners
            ResultListenerList<String> olisteners = handler.listeners;
            handler.cancel();
            handler = _peermgr.createLockHandler(this, lock, true);
            handler.listeners = olisteners;

        } else if (name.equals(NodeObject.RELEASING_LOCK)) {
            NodeObject.Lock lock = nodeobj.releasingLock;
            PeerManager.LockHandler handler = _peermgr.getLockHandler(lock);
            if (handler == null) {
                _peermgr.createLockHandler(this, lock, false);
            } else {
                log.warning("Received request to release resolving lock [node=" +
                            _record.nodeName + ", handler=" + handler + "].");
            }

        } else if (name.equals(NodeObject.CACHE_DATA)) {
            _peermgr.changedCacheData(nodeobj.cacheData.cache, nodeobj.cacheData.data);
        }
    }

    /**
     * Determines whether the first node named has priority over the second when resolving
     * lock disputes.
     */
    protected static boolean hasPriority (String nodeName1, String nodeName2)
    {
        return nodeName1.compareTo(nodeName2) < 0;
    }

    protected PeerManager _peermgr;
    protected NodeRecord _record;
    protected Client _client;
    protected long _lastConnectStamp;

    /** The amount of time after which a node record can be considered out of date and invalid. */
    protected static final long STALE_INTERVAL = 5L * 60L * 1000L;
}
