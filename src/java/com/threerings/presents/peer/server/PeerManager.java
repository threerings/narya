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
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.util.Interval;
import com.samskivert.util.Invoker;
import com.samskivert.util.ObjectUtil;
import com.samskivert.util.ObserverList;
import com.samskivert.util.ResultListener;
import com.samskivert.util.Tuple;

import com.threerings.io.Streamable;
import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.SetAdapter;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.server.ClientManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.PresentsClient;
import com.threerings.presents.server.PresentsServer;

import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.Lock;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.net.PeerBootstrapData;
import com.threerings.presents.peer.net.PeerCreds;
import com.threerings.presents.peer.server.persist.NodeRecord;
import com.threerings.presents.peer.server.persist.NodeRepository;

import static com.threerings.presents.Log.log;

/**
 * Manages connections to the other nodes in a Presents server cluster. Each server maintains a
 * client connection to the other servers and subscribes to the {@link NodeObject} of all peer
 * servers and uses those objects to communicate cross-node information.
 */
public class PeerManager
    implements ClientManager.ClientObserver
{
    /**
     * Used by entities that wish to know when cached data has become stale due to a change on
     * one of our peer servers.
     */
    public static interface StaleCacheObserver
    {
        /**
         * Called when some possibly cached data has changed on one of our peer servers.
         */
        public void changedCacheData (Streamable data);
    }

    /**
     * Creates a peer manager which will create a {@link NodeRepository} which will be used to
     * publish our existence and discover the other nodes.
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
     * Initializes this peer manager and initiates the process of connecting to its peer
     * nodes. This will also reconfigure the ConnectionManager and ClientManager with peer related
     * bits, so this should not be called until <em>after</em> the main server has set up its
     * client factory and authenticator.
     *
     * @param nodeName this node's unique name.
     * @param sharedSecret a shared secret used to allow the peers to authenticate with one
     * another.
     * @param hostName the DNS name of the server running this node.
     * @param publicHostName if non-null, a separate public DNS hostname by which the node is to be
     * known to normal clients (we may want inter-peer communication to take place over a different
     * network than the communication between real clients and the various peer servers).
     * @param port the port on which other nodes should connect to us.
     * @param conprov used to obtain our JDBC connections.
     * @param invoker we will perform all database operations on the supplied invoker thread.
     */
    public void init (String nodeName, String sharedSecret, String hostName,
                      String publicHostName, int port)
    {
        _nodeName = nodeName;
        _hostName = hostName;
        _publicHostName = (publicHostName == null) ? hostName : publicHostName;
        _port = port;
        _sharedSecret = sharedSecret;

        // wire ourselves into the server
        PresentsServer.conmgr.setAuthenticator(
            new PeerAuthenticator(this, PresentsServer.conmgr.getAuthenticator()));
        PresentsServer.clmgr.setClientFactory(
            new PeerClientFactory(this, PresentsServer.clmgr.getClientFactory()));

        // create our node object
        _nodeobj = PresentsServer.omgr.registerObject(createNodeObject());

        // register ourselves with the node table
        _invoker.postUnit(new Invoker.Unit() {
            public boolean invoke () {
                NodeRecord record = new NodeRecord(_nodeName, _hostName, _publicHostName, _port);
                try {
                    _noderepo.updateNode(record);
                } catch (PersistenceException pe) {
                    log.warning("Failed to register node record [rec=" + record +
                                ", error=" + pe + "].");
                }
                return false;
            }
        });

        // register ourselves as a client observer
        PresentsServer.clmgr.addClientObserver(this);

        // and start our peer refresh interval (this need not use a runqueue as all it will do is
        // post an invoker unit)
        new Interval() {
            public void expired () {
                refreshPeers();
            }
        }.schedule(5000L, 60*1000L);

        // give derived classes an easy way to get in on the init action
        didInit();
    }

    /**
     * Call this when the server is shutting down to give this node a chance to cleanly logoff from
     * its peers and remove its record from the nodes table.
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
        return PeerCreds.createPassword(creds.getNodeName(), _sharedSecret).equals(
            creds.getPassword());
    }

    /**
     * Attempts to acquire the specified lock amongst our peers.  When the peers complete the
     * process of resolution, the supplied listener will be notified with the name of the lock's
     * owner, which will either be this or another node (if that node preempted this one).  If
     * another node holds the lock, it will not release it before this node has a chance to make
     * a request.
     */
    public void acquireLock (final Lock.Name name, final ResultListener<String> listener)
    {
        // wait for any resolution to end
        queryLock(name, new ResultListener<String>() {
            public void requestCompleted (String result) {
                if (result == null) {
                    // the lock is free; we will attempt to acquire it
                    continueAcquiringLock(name, listener);
                } else {
                    // another node has acquired the lock
                    listener.requestCompleted(result);
                }
            }
            public void requestFailed (Exception cause) {
                listener.requestFailed(cause);
            }
        });
    }
    
    /**
     * Attempts to release the specified lock.  The supplied listener will receive the lock's
     * owner (<code>null</code> if the lock was successfully released, or the node's name if
     * the release was cancelled using {@link #reacquireLock}).
     */
    public void releaseLock (final Lock.Name name, final ResultListener<String> listener)
    {
        // make sure we actually hold the lock
        Tuple<Boolean, String> result = queryLock(name, false);
        if (!result.left) {
            log.warning("Attempted to release lock in process of resolution [name=" + name + "].");
        } else if (!_nodeName.equals(result.right)) {
            log.warning("Attempted to release lock not owned [name=" + name + ", owner=" +
                result.right + "].");
        }
        
        // announce our desire to drop the lock
        _nodeobj.setReleasingLock(name);
        
        // wait for all peers to agree
        new LockResolutionListener(name, true) {
            public void lockResolved (String owner) {
                if (_acquiringLocks.contains(name)) {
                    queryLock(name, listener);
                    return;
                } else if (owner == null) {
                    _nodeobj.removeFromLocks(name);
                } else if (!owner.equals(_nodeName)) {
                    log.warning("Someone acquired the lock before we were done releasing it?! " +
                        "[name=" + name + ", owner=" + owner + "].");
                }
                listener.requestCompleted(owner);
            }
        }.add();
    }
    
    /**
     * Reacquires a lock for which we have called {@link #releaseLock}, but have not yet received
     * a response.
     */
    public void reacquireLock (final Lock.Name name, final ResultListener<String> listener)
    {
        // broadcast our desire to reacquire
        _nodeobj.setAcquiringLock(name);
        
        // remember the name
        _acquiringLocks.add(name);
        
        // wait for resolution
        new LockResolutionListener(name, false) {
            public void lockResolved (String owner) {
                _acquiringLocks.remove(name);
                if (!_nodeName.equals(owner)) {
                    log.warning("Failed to reacquire lock? [name=" + name + ", owner=" +
                        owner + "].");
                }
                listener.requestCompleted(owner);
            }
        }.add();
    }
    
    /**
     * Attempts to determine the owner of the specified lock, if any.
     *
     * @param listener a listener to receive the name of the lock's owner (or <code>null</code>
     * for none) once any necessary resolution has been performed
     */
    public void queryLock (Lock.Name name, final ResultListener<String> listener)
    {
        Tuple<Boolean, String> result = queryLock(name, false);
        if (result.left) {
            listener.requestCompleted(result.right);
        } else {
            new LockResolutionListener(name, false) {
                public void lockResolved (String owner) {
                    listener.requestCompleted(owner);
                }
            }.add();
        }
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
            log.warning("Received clientSessionDidStart() for already registered client!? " +
                        "[old=" + _nodeobj.clients.get(clinfo.getKey()) + ", new=" + clinfo + "].");
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

        // we scan through the list instead of relying on ClientInfo.getKey() because we want
        // derived classes to be able to override that for lookups that happen way more frequently
        // than logging off
        Name username = client.getCredentials().getUsername();
        for (ClientInfo clinfo : _nodeobj.clients) {
            if (clinfo.username.equals(username)) {
                _nodeobj.removeFromClients(clinfo.getKey());
                return;
            }
        }
        log.warning("Session ended for unregistered client [who=" + username + "].");
    }

    /**
     * Registers a stale cache observer.
     */
    public void addStaleCacheObserver (String cache, StaleCacheObserver observer)
    {
        ObserverList<StaleCacheObserver> list = _cacheobs.get(cache);
        if (list == null) {
            list = new ObserverList<StaleCacheObserver>(ObserverList.FAST_UNSAFE_NOTIFY);
            _cacheobs.put(cache, list);
        }
        list.add(observer);
    }

    /**
     * Removes a stale cache observer registration.
     */
    public void removeStaleCacheObserver (String cache, StaleCacheObserver observer)
    {
        ObserverList<StaleCacheObserver> list = _cacheobs.get(cache);
        if (list == null) {
            return;
        }
        list.remove(observer);
        if (list.isEmpty()) {
            _cacheobs.remove(cache);
        }
    }

    /**
     * Called when cached data has changed on the local server and needs to inform our peers.
     */
    public void broadcastStaleCacheData (String cache, Streamable data)
    {
        _nodeobj.setCacheData(new NodeObject.CacheData(cache, data));
    }

    /**
     * Called after we have finished our initialization.
     */
    protected void didInit ()
    {
    }

    /**
     * Reloads the list of peer nodes from our table and refreshes each with a call to {@link
     * #refreshPeer}.
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
                        log.log(Level.WARNING, "Failure refreshing peer " + record + ".", e);
                    }
                }
            }
            protected ArrayList<NodeRecord> _nodes;
        });
    }

    /**
     * Ensures that we have a connection to the specified node if it has checked in since we last
     * failed to connect.
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
     * Creates the appropriate derived class of {@link NodeObject} which will be registered with
     * the distributed object system.
     */
    protected NodeObject createNodeObject ()
    {
        return new NodeObject();
    }

    /**
     * Creates a {@link ClientInfo} record which will subsequently be initialized by a call to
     * {@link #initClientInfo}.
     */
    protected ClientInfo createClientInfo ()
    {
        return new ClientInfo();
    }

    /**
     * Called when possibly cached data has changed on one of our peer servers.
     */
    protected void changedCacheData (String cache, final Streamable data)
    {
        // see if we have any observers
        ObserverList<StaleCacheObserver> list = _cacheobs.get(cache);
        if (list == null) {
            return;
        }
        // if so, notify them
        list.apply(new ObserverList.ObserverOp<StaleCacheObserver>() {
            public boolean apply (StaleCacheObserver observer) {
                observer.changedCacheData(data);
                return true;
            }
        });
    }

    /**
     * Initializes the supplied client info for the supplied client.
     */
    protected void initClientInfo (PresentsClient client, ClientInfo info)
    {
        info.username = client.getCredentials().getUsername();
    }

    /**
     * Creates a {@link PeerNode} to manage our connection to the specified peer.
     */
    protected PeerNode createPeerNode (NodeRecord record)
    {
        return new PeerNode(record);
    }

    /**
     * Continues the process of acquiring a lock once it has been established that the lock is
     * free for the taking.
     */
    protected void continueAcquiringLock (
        final Lock.Name name, final ResultListener<String> listener)
    {
        // announce our desire for the lock
        _nodeobj.setAcquiringLock(name);
        
        // remember the name
        _acquiringLocks.add(name);
        
        // wait for all other nodes to resolve before adding ourself to seal the deal
        new LockResolutionListener(name, true) {
            public void lockResolved (String owner) {
                _acquiringLocks.remove(name);
                if (_nodeName.equals(owner)) {
                    _nodeobj.addToLocks(new Lock(name, _nodeName));
                }
                listener.requestCompleted(owner);
            }
        }.add();
    }
    
    /**
     * Attempts to determine the owner of the specified lock, if any.
     *
     * @param peersOnly if true, only check the peers' locks (that is, exclude this node from
     * the search)
     * @return a tuple containing as its left value a boolean indicating whether the lock's
     * state has been resolved.  If the value is <code>true</code>, the right value contains
     * the lock's owner (or <code>null</code> if the lock is free)
     */
    protected Tuple<Boolean, String> queryLock (Lock.Name name, boolean peersOnly)
    {
        Iterator<PeerNode> it = _peers.values().iterator();
        Lock lock = (peersOnly && it.hasNext()) ?
            it.next().nodeobj.locks.get(name) : _nodeobj.locks.get(name);
        while (it.hasNext()) {
            if (!ObjectUtil.equals(lock, it.next().nodeobj.locks.get(name))) {
                return new Tuple<Boolean, String>(false, null);
            }
        }
        return new Tuple<Boolean, String>(true, (lock == null) ? null : lock.getOwner());
    }
    
    /**
     * Contains all runtime information for one of our peer nodes.
     */
    protected class PeerNode
        implements ClientObserver, Subscriber<NodeObject>, AttributeChangeListener
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
            // if the hostname of this node changed, kill our existing client connection and
            // connect anew
            if (!record.hostName.equals(_record.hostName) &&
                _client.isActive()) {
                _client.logoff(false);
            }

            // if our client is active, we're groovy
            if (_client.isActive()) {
                return;
            }

            // if our client hasn't updated its record since we last tried to logon, then just
            // chill
            if (_lastConnectStamp > record.lastUpdated.getTime()) {
                log.fine("Not reconnecting to stale client [record=" + _record +
                         ", lastTry=" + new Date(_lastConnectStamp) + "].");
                return;
            }

            // otherwise configure our client with the right bits and logon
            _client.setCredentials(new PeerCreds(_record.nodeName, _sharedSecret));
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
            nodeobj.removeListener(this);
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
            
            // listen for lock and cache updates
            nodeobj.addListener(this);
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
                if (_acquiringLocks.contains(nodeobj.acquiringLock) &&
                    _nodeName.compareTo(_record.nodeName) < 0) {
                    return; // this node has priority
                }
                Lock olock = _nodeobj.locks.get(nodeobj.acquiringLock),
                    nlock = new Lock(nodeobj.acquiringLock, _record.nodeName);
                if (olock == null) {
                    _nodeobj.addToLocks(nlock);
                } else if (_record.nodeName.compareTo(olock.getOwner()) < 0) {
                    _nodeobj.updateLocks(nlock);
                }
            } else if (name.equals(NodeObject.RELEASING_LOCK)) {
                Lock lock = _nodeobj.locks.get(nodeobj.releasingLock);
                if (lock != null && lock.getOwner().equals(_record.nodeName)) {
                    _nodeobj.removeFromLocks(nodeobj.releasingLock);
                } else {
                    log.warning("Node attempting to release lock not held? [node=" +
                        _record.nodeName + ", name=" + nodeobj.releasingLock + ", lock=" +
                        lock + "].");
                }
            } else if (name.equals(NodeObject.CACHE_DATA)) {
                changedCacheData(nodeobj.cacheData.cache, nodeobj.cacheData.data);
            }
        }
        
        protected NodeRecord _record;
        protected Client _client;
        protected long _lastConnectStamp;
    }

    /**
     * Listens to all {@link NodeObject}s (possibly including our own) to determine when the lock
     * state has been agreed upon.
     */
    protected abstract class LockResolutionListener extends SetAdapter
    {
        public LockResolutionListener (Lock.Name name, boolean peersOnly)
        {
            _name = name;
            _peersOnly = peersOnly;
        }
        
        public void add ()
        {
            for (PeerNode peer : _peers.values()) {
                peer.nodeobj.addListener(this);
            }
            if (!_peersOnly) {
                _nodeobj.addListener(this);
            }
        }
        
        @Override // documentation inherited
        public void entryAdded (EntryAddedEvent event)
        {
            if (event.getName().equals(NodeObject.LOCKS)) {
                checkLockResolved();
            }
        }
        
        @Override // documentation inherited
        public void entryRemoved (EntryRemovedEvent event)
        {
            if (event.getName().equals(NodeObject.LOCKS)) {
                checkLockResolved();
            }
        }
        
        @Override // documentation inherited
        public void entryUpdated (EntryUpdatedEvent event)
        {
            if (event.getName().equals(NodeObject.LOCKS)) {
                checkLockResolved();
            }
        }
        
        protected void checkLockResolved ()
        {
            Tuple<Boolean, String> result = queryLock(_name, _peersOnly);
            if (!result.left) {
                return;
            }
            
            lockResolved(result.right);
            
            for (PeerNode peer : _peers.values()) {
                peer.nodeobj.removeListener(this);
            }
            if (!_peersOnly) {
                _nodeobj.removeListener(this);    
            }
        }
        
        /**
         * Called when the owner of the lock has been agreed upon.
         */
        protected abstract void lockResolved (String owner);
        
        /** The name of the lock. */
        protected Lock.Name _name;
        
        /** If true, wait for all nodes except this one to agree. */
        protected boolean _peersOnly;
    }
    
    protected String _nodeName, _hostName, _publicHostName, _sharedSecret;
    protected int _port;
    protected Invoker _invoker;
    protected NodeRepository _noderepo;
    protected NodeObject _nodeobj;
    protected HashMap<String,PeerNode> _peers = new HashMap<String,PeerNode>();

    /** Our stale cache observers. */
    protected HashMap<String, ObserverList<StaleCacheObserver>> _cacheobs =
        new HashMap<String, ObserverList<StaleCacheObserver>>();
    
    /** Contains the names of any locks we are currently trying to acquire. */
    protected HashSet<Lock.Name> _acquiringLocks = new HashSet<Lock.Name>();
}
