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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.depot.PersistenceContext;
import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.ChainedResultListener;
import com.samskivert.util.Interval;
import com.samskivert.util.Invoker;
import com.samskivert.util.ObjectUtil;
import com.samskivert.util.ObserverList;
import com.samskivert.util.ResultListener;
import com.samskivert.util.ResultListenerList;
import com.samskivert.util.Tuple;

import com.threerings.io.Streamable;
import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.SetListener;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.server.ClientManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.PresentsClient;
import com.threerings.presents.server.PresentsServer;

import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.data.PeerMarshaller;
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
    implements PeerProvider, ClientManager.ClientObserver
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
     * Used by entities that wish to know when this peer has been forced into immediately releasing
     * a lock.
     */
    public static interface DroppedLockObserver
    {
        /**
         * Called when this node has been forced to drop a lock.
         */
        public void droppedLock (NodeObject.Lock lock);
    }

    /** Used by {@link #lookupNodeDatum}. */
    public static interface Lookup<T>
    {
        public T lookup (NodeObject nodeobj);
    }

    /** Used by {@link #applyToNodes}. */
    public static interface Operation
    {
        public void apply (NodeObject nodeobj);
    }

    /**
     * Wraps an operation that needs a shared resource lock to be acquired before it can be
     * performed, and released after it completes. Used by {@link #performWithLock}.
     */
    public static interface LockedOperation
    {
        /**
         * Called when the resource lock was acquired successfully. The lock will be released
         * immediately after this function call finishes. 
         */
        public void run ();

        /**
         * Called when the resource lock was not acquired successfully, with the name of the peer
         * who is holding the lock (or null in case of a generic failure).
         */
        public void fail (String peerName);
    }
    
    /**
     * Creates a peer manager which will create a {@link NodeRepository} which will be used to
     * publish our existence and discover the other nodes.
     */
    public PeerManager (PersistenceContext ctx, Invoker invoker)
    {
        _invoker = invoker;
        _noderepo = new NodeRepository(ctx);
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
        _sharedSecret = sharedSecret;

        // wire ourselves into the server
        PresentsServer.conmgr.setAuthenticator(
            new PeerAuthenticator(this, PresentsServer.conmgr.getAuthenticator()));
        PresentsServer.clmgr.setClientFactory(
            new PeerClientFactory(this, PresentsServer.clmgr.getClientFactory()));

        // create our node object
        _nodeobj = PresentsServer.omgr.registerObject(createNodeObject());
        _nodeobj.setNodeName(nodeName);

        // register ourselves with the node table
        final NodeRecord record = new NodeRecord(
            _nodeName, hostName, (publicHostName == null) ? hostName : publicHostName, port);
        _invoker.postUnit(new Invoker.Unit("registerNode") {
            public boolean invoke () {
                try {
                    _noderepo.updateNode(record);
                } catch (PersistenceException pe) {
                    log.warning("Failed to register node record [rec=" + record +
                                ", error=" + pe + "].");
                }
                return false;
            }
        });

        // set the invocation service
        _nodeobj.setPeerService(
            (PeerMarshaller)PresentsServer.invmgr.registerDispatcher(new PeerDispatcher(this)));

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
        // clear out our invocation service
        if (_nodeobj != null) {
            PresentsServer.invmgr.clearDispatcher(_nodeobj.peerService);
        }

        // clear out our client observer registration
        PresentsServer.clmgr.removeClientObserver(this);

        // clear our record from the node table
        _invoker.postUnit(new Invoker.Unit("deleteNode") {
            public boolean invoke () {
                try {
                    _noderepo.deleteNode(_nodeName);
                } catch (PersistenceException pe) {
                    log.warning("Failed to delete node record [nodeName=" + _nodeName +
                                ", error=" + pe + "].");
                }
                return false;
            }
        });

        // shut down the peers
        for (PeerNode peer : _peers.values()) {
            peer.shutdown();
        }
    }

    /**
     * Locates the client with the specified name. Returns null if the client is not logged onto
     * any peer.
     */
    public ClientInfo locateClient (final Name key)
    {
        return lookupNodeDatum(new Lookup<ClientInfo>() {
            public ClientInfo lookup (NodeObject nodeobj) {
                return nodeobj.clients.get(key);
            }
        });
    }

    /**
     * Locates a datum from among the set of peer {@link NodeObject}s. Objects are searched in
     * arbitrary order and the first non-null value returned by the supplied lookup operation is
     * returned to the caller. Null if all lookup operations returned null.
     */
    public <T> T lookupNodeDatum (Lookup<T> op)
    {
        T value = op.lookup(_nodeobj);
        if (value != null) {
            return value;
        }
        for (PeerNode peer : _peers.values()) {
            if (peer.nodeobj == null) {
                continue;
            }
            value = op.lookup(peer.nodeobj);
            if (value != null) {
                return value;
            }
        }
        return value;
    }

    /**
     * Applies the supplied operation to all {@link NodeObject}s. The operation should not modify
     * the objects unless you really know what you're doing. more likely it will summarize
     * information contained therein.
     */
    public void applyToNodes (Operation op)
    {
        op.apply(_nodeobj);
        for (PeerNode peer : _peers.values()) {
            if (peer.nodeobj != null) {
                op.apply(peer.nodeobj);
            }
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
     * Initiates a proxy on an object that is managed by the specified peer. The object will be
     * proxied into this server's distributed object space and its local oid reported back to the
     * supplied result listener.
     *
     * <p> Note that proxy requests <em>do not</em> stack like subscription requests. Only one
     * entity must issue a request to proxy an object and that entity must be responsible for
     * releasing the proxy when it knows that there are no longer any local subscribers to the
     * object.
     */
    public <T extends DObject> void proxyRemoteObject (
        String nodeName, int remoteOid, final ResultListener<Integer> listener)
    {
        final Client peer = getPeerClient(nodeName);
        if (peer == null) {
            String errmsg = "Have no connection to peer [node=" + nodeName + "].";
            listener.requestFailed(new ObjectAccessException(errmsg));
            return;
        }

        final Tuple<String,Integer> key = new Tuple<String,Integer>(nodeName, remoteOid);
        if (_proxies.containsKey(key)) {
            String errmsg = "Cannot proxy already proxied object [key=" + key + "].";
            listener.requestFailed(new ObjectAccessException(errmsg));
            return;
        }

        // issue a request to subscribe to the remote object
        peer.getDObjectManager().subscribeToObject(remoteOid, new Subscriber<T>() {
            public void objectAvailable (T object) {
                // make a note of this proxy mapping
                _proxies.put(key, new Tuple<Subscriber<?>,DObject>(this, object));
                // map the object into our local oid space
                PresentsServer.omgr.registerProxyObject(object, peer.getDObjectManager());
                // then tell the caller about the (now remapped) oid
                listener.requestCompleted(object.getOid());
            }
            public void requestFailed (int oid, ObjectAccessException cause) {
                listener.requestFailed(cause);
            }
        });
    }

    /**
     * Unsubscribes from and clears a proxied object. The caller must be sure that there are no
     * remaining subscribers to the object on this local server.
     */
    public void unproxyRemoteObject (String nodeName, int remoteOid)
    {
        Tuple<String,Integer> key = new Tuple<String,Integer>(nodeName, remoteOid);
        Tuple<Subscriber<?>,DObject> bits = _proxies.remove(key);
        if (bits == null) {
            log.warning("Requested to clear unknown proxy [key=" + key + "].");
            return;
        }

        // clear out the local object manager's proxy mapping
        PresentsServer.omgr.clearProxyObject(remoteOid, bits.right);

        final Client peer = getPeerClient(nodeName);
        if (peer == null) {
            log.warning("Unable to unsubscribe from proxy, missing peer [key=" + key + "].");
            return;
        }

        // restore the object's omgr reference to our ClientDObjectMgr so that it can properly
        // finish the unsubscription process
        bits.right.setManager(peer.getDObjectManager());

        // finally unsubscribe from the object on our peer
        peer.getDObjectManager().unsubscribeFromObject(remoteOid, bits.left);
    }

    /**
     * Returns the client object representing the connection to the named peer, or
     * <code>null</code> if we are not currently connected to it.
     */
    public Client getPeerClient (String nodeName)
    {
        PeerNode peer = _peers.get(nodeName);
        return (peer == null) ? null : peer.getClient();
    }

    /**
     * Returns the public hostname to use when connecting to the specified peer or null if the peer
     * is not currently connected to this server.
     */
    public String getPeerPublicHostName (String nodeName)
    {
        PeerNode peer = _peers.get(nodeName);
        return (peer == null) ? null : peer.getPublicHostName();
    }

    /**
     * Returns the port on which to connect to the specified peer or -1 if the peer is not
     * currently connected to this server.
     */
    public int getPeerPort (String nodeName)
    {
        PeerNode peer = _peers.get(nodeName);
        return (peer == null) ? -1 : peer.getPort();
    }

    /**
     * Acquires a lock on a resource shared amongst this node's peers.  If the lock is successfully
     * acquired, the supplied listener will receive this node's name.  If another node acquires the
     * lock first, then the listener will receive the name of that node.
     */
    public void acquireLock (final NodeObject.Lock lock, final ResultListener<String> listener)
    {
        // wait until any pending resolution is complete
        queryLock(lock, new ChainedResultListener<String>(listener) {
            public void requestCompleted (String result) {
                if (result == null) {
                    if (_suboids.isEmpty()) {
                        _nodeobj.addToLocks(lock);
                        listener.requestCompleted(_nodeName);
                    } else {
                        _locks.put(lock, new LockHandler(lock, true, listener));
                    }
                } else {
                    listener.requestCompleted(result);
                }
            }
        });
    }

    /**
     * Releases a lock.  This can be cancelled using {@link #reacquireLock}, in which case the
     * passed listener will receive this node's name as opposed to <code>null</code>, which
     * signifies that the lock has been successfully released.
     */
    public void releaseLock (final NodeObject.Lock lock, final ResultListener<String> listener)
    {
        // wait until any pending resolution is complete
        queryLock(lock, new ChainedResultListener<String>(listener) {
            public void requestCompleted (String result) {
                if (_nodeName.equals(result)) {
                    if (_suboids.isEmpty()) {
                        _nodeobj.removeFromLocks(lock);
                        listener.requestCompleted(null);
                    } else {
                        _locks.put(lock, new LockHandler(lock, false, listener));
                    }
                } else {
                    if (result != null) {
                        log.warning("Tried to release lock held by another peer [lock=" + lock +
                                    ", owner=" + result + "].");
                    }
                    listener.requestCompleted(result);
                }
            }
        });
    }

    /**
     * Reacquires a lock after a call to {@link #releaseLock} but before the result listener
     * supplied to that method has been notified with the result of the action.  The result
     * listener will receive the name of this node to indicate that the lock is still held.  If a
     * node requests to release a lock, then receives a lock-related request from another peer, it
     * can use this method to cancel the release reliably, since the lock-related request will have
     * been sent before the peer's ratification of the release.
     */
    public void reacquireLock (NodeObject.Lock lock)
    {
        // make sure we're releasing it
        LockHandler handler = _locks.get(lock);
        if (handler == null || !handler.getNodeName().equals(_nodeName) || handler.isAcquiring()) {
            log.warning("Tried to reacquire lock not being released [lock=" + lock +
                        ", handler=" + handler + "].");
            return;
        }

        // perform an update to let other nodes know that we're reacquiring
        _nodeobj.updateLocks(lock);

        // cancel the handler and report to any listeners
        _locks.remove(lock);
        handler.cancel();
        handler.listeners.requestCompleted(_nodeName);
    }

    /**
     * Determines the owner of the specified lock, waiting for any resolution to complete before
     * notifying the supplied listener.
     */
    public void queryLock (NodeObject.Lock lock, ResultListener<String> listener)
    {
        // if it's being resolved, add the listener to the list
        LockHandler handler = _locks.get(lock);
        if (handler != null) {
            handler.listeners.add(listener);
            return;
        }

        // otherwise, return its present value
        listener.requestCompleted(queryLock(lock));
    }

    /**
     * Finds the owner of the specified lock (if any) among this node and its peers.  This answer
     * is not definitive, as the lock may be in the process of resolving.
     */
    public String queryLock (NodeObject.Lock lock)
    {
        // look for it in our own lock set
        if (_nodeobj.locks.contains(lock)) {
            return _nodeName;
        }

        // then in our peers
        for (PeerNode peer : _peers.values()) {
            if (peer.nodeobj != null && peer.nodeobj.locks.contains(lock)) {
                return peer.getNodeName();
            }
        }
        return null;
    }

    /**
     * Tries to acquire the resource lock and, if successful, performs the operation and releases
     * the lock; if unsuccessful, calls the operation's failure handler. Please note: the lock will
     * be released immediately after the operation. 
     */
    public void performWithLock (final NodeObject.Lock lock, final LockedOperation operation)
    {
        acquireLock(lock, new ResultListener<String>() {
            public void requestCompleted (String nodeName) {
                if (getNodeObject().nodeName.equals(nodeName)) {
                    // lock acquired successfully - perform the operation, and release the lock.
                    operation.run();
                    releaseLock(lock, new ResultListener.NOOP<String>());
                } else {
                    // some other peer beat us to it
                    operation.fail(nodeName);
                    if (nodeName == null) {
                        log.warning("Lock acquired by null? [lock=" + lock + "].");
                    }
                }
            }
            public void requestFailed (Exception cause) {
                log.log(Level.WARNING, "Lock acquisition failed [lock=" + lock + "].", cause);
                operation.fail(null);
            }
        });
    }

    /**
     * Adds an observer to notify when this peer has been forced to drop a lock immediately.
     */
    public void addDroppedLockObserver (DroppedLockObserver observer)
    {
        _dropobs.add(observer);
    }

    /**
     * Removes a dropped lock observer from the list.
     */
    public void removeDroppedLockObserver (DroppedLockObserver observer)
    {
        _dropobs.remove(observer);
    }

    // documentation inherited from interface PeerProvider
    public void ratifyLockAction (ClientObject caller, NodeObject.Lock lock, boolean acquire)
    {
        LockHandler handler = _locks.get(lock);
        if (handler != null && handler.getNodeName().equals(_nodeName)) {
            handler.ratify(caller, acquire);
        } else {
            // this is not an error condition, as we may have cancelled the handler or
            // allowed another to take priority
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
                clearClientInfo(client, clinfo);
                return;
            }
        }
        log.warning("Session ended for unregistered client [who=" + username + "].");
    }

    /**
     * Called by {@link PeerClient}s when clients subscribe to the {@link NodeObject}.
     */
    public void clientSubscribedToNode (int cloid)
    {
        _suboids.add(cloid);
    }

    /**
     * Called by {@link PeerClient}s when clients unsubscribe from the {@link NodeObject}.
     */
    public void clientUnsubscribedFromNode (int cloid)
    {
        _suboids.remove(cloid);
        for (LockHandler handler : _locks.values().toArray(new LockHandler[_locks.size()])) {
            if (handler.getNodeName().equals(_nodeName)) {
                handler.clientUnsubscribed(cloid);
            }
        }
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
        _invoker.postUnit(new Invoker.Unit("refreshPeers") {
            public boolean invoke () {
                try {
                    // let the world know that we're alive
                    _noderepo.heartbeatNode(_nodeName);
                    // then load up all the peer records
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
            protected List<NodeRecord> _nodes;
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
     * Returns the lock handler for the specified lock.
     */
    protected LockHandler getLockHandler (NodeObject.Lock lock)
    {
        return _locks.get(lock);
    }

    protected LockHandler createLockHandler (PeerNode peer, NodeObject.Lock lock, boolean acquire)
    {
        LockHandler handler = new LockHandler(peer, lock, acquire);
        _locks.put(lock, handler);
        return handler;
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
     * Called when we have been forced to drop a lock.
     */
    protected void droppedLock (final NodeObject.Lock lock)
    {
        _nodeobj.removeFromLocks(lock);
        _dropobs.apply(new ObserverList.ObserverOp<DroppedLockObserver>() {
            public boolean apply (DroppedLockObserver observer) {
                observer.droppedLock(lock);
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
     * Called when a client ends their session to clear their information from our node object.
     */
    protected void clearClientInfo (PresentsClient client, ClientInfo info)
    {
        _nodeobj.removeFromClients(info.getKey());
    }

    /**
     * Creates a {@link PeerNode} to manage our connection to the specified peer.
     */
    protected PeerNode createPeerNode (NodeRecord record)
    {
        return new PeerNode(this, record);
    }

    /**
     * Creates credentials that a {@link PeerNode} can use to authenticate with another node.
     */
    protected PeerCreds createCreds ()
    {
        return new PeerCreds(_nodeName, _sharedSecret);
    }

    /**
     * Called when a peer connects to this server.
     */
    protected void peerDidLogon (PeerNode peer)
    {
        // check for lock conflicts
        for (NodeObject.Lock lock : peer.nodeobj.locks) {
            PeerManager.LockHandler handler = _locks.get(lock);
            if (handler != null) {
                log.warning("Client hijacked lock in process of resolution [handler=" + handler +
                            ", node=" + peer.getNodeName() + "].");
                handler.clientHijackedLock(peer.getNodeName());

            } else if (_nodeobj.locks.contains(lock)) {
                log.warning("Client hijacked lock owned by this node [lock=" + lock +
                            ", node=" + peer.getNodeName() + "].");
                droppedLock(lock);
            }
        }
    }

    /**
     * Called when a peer disconnects from this server.
     */
    protected void peerDidLogoff (PeerNode peer)
    {
        // clear any locks held by that peer
        for (LockHandler handler : _locks.values().toArray(new LockHandler[_locks.size()])) {
            if (handler.getNodeName().equals(peer.getNodeName())) {
                handler.clientDidLogoff();
            }
        }
    }

    /**
     * Handles a lock in a state of resolution.
     */
    protected class LockHandler
        implements SetListener
    {
        /** Listeners waiting for resolution. */
        public ResultListenerList<String> listeners = new ResultListenerList<String>();

        /**
         * Creates a handler to acquire or release a lock for this node.
         */
        public LockHandler (NodeObject.Lock lock, boolean acquire, ResultListener<String> listener)
        {
            _lock = lock;
            _acquire = acquire;
            listeners.add(listener);

            // signal our desire to acquire or release the lock
            if (acquire) {
                _nodeobj.setAcquiringLock(lock);
            } else {
                _nodeobj.setReleasingLock(lock);
            }

            // take a snapshot of the set of subscriber client oids; we will act when all of them
            // ratify
            _remoids = (ArrayIntSet)_suboids.clone();

            // schedule a timeout to act if something goes wrong
            (_timeout = new Interval(PresentsServer.omgr) {
                public void expired () {
                    log.warning("Lock handler timed out, acting anyway [lock=" + _lock +
                                ", acquire=" + _acquire + "].");
                    activate();
                }
            }).schedule(LOCK_TIMEOUT);
        }

        /**
         * Creates a handle that tracks another node's acquisition or release of a lock.
         */
        public LockHandler (PeerNode peer, NodeObject.Lock lock, boolean acquire)
        {
            _peer = peer;
            _lock = lock;
            _acquire = acquire;

            // ratify the action
            peer.nodeobj.peerService.ratifyLockAction(peer.getClient(), lock, acquire);

            // listen for the act to take place
            peer.nodeobj.addListener(this);
        }

        /**
         * Returns the name of the node waiting to perform the action.
         */
        public String getNodeName ()
        {
            return (_peer == null) ? _nodeName : _peer.getNodeName();
        }

        /**
         * Checks whether we are acquiring as opposed to releasing a lock.
         */
        public boolean isAcquiring ()
        {
            return _acquire;
        }

        /**
         * Signals that one of the remote nodes has ratified the pending action.
         */
        public void ratify (ClientObject caller, boolean acquire)
        {
            if (acquire != _acquire) {
                return;
            }
            if (!_remoids.remove(caller.getOid())) {
                log.warning("Received unexpected ratification [handler=" + this +
                            ", who=" + caller.who() + "].");
            }
            maybeActivate();
        }

        /**
         * Called when a client has unsubscribed from this node (which is waiting for
         * ratification).
         */
        public void clientUnsubscribed (int cloid)
        {
            // unsubscription is implicit ratification
            if (_remoids.remove(cloid)) {
                maybeActivate();
            }
        }

        /**
         * Called when the connection to the controlling node has been broken.
         */
        public void clientDidLogoff ()
        {
            _locks.remove(_lock);
            listeners.requestCompleted(null);
        }

        /**
         * Called when a client hijacks the lock by having it in its node object when it connects.
         */
        public void clientHijackedLock (String nodeName)
        {
            cancel();
            _locks.remove(_lock);
            listeners.requestCompleted(nodeName);
        }

        /**
         * Cancels this handler, as another one will be taking its place.
         */
        public void cancel ()
        {
            if (_peer != null) {
                _peer.nodeobj.removeListener(this);
            } else {
                _timeout.cancel();
            }
        }

        // documentation inherited from interface SetListener
        public void entryAdded (EntryAddedEvent event)
        {
            if (_acquire && event.getName().equals(NodeObject.LOCKS) &&
                event.getEntry().equals(_lock)) {
                wasActivated(_peer.getNodeName());
            }
        }

        // documentation inherited from interface SetListener
        public void entryRemoved (EntryRemovedEvent event)
        {
            if (!_acquire && event.getName().equals(NodeObject.LOCKS) &&
                event.getOldEntry().equals(_lock)) {
                wasActivated(null);
            }
        }

        // documentation inherited from interface SetListener
        public void entryUpdated (EntryUpdatedEvent event)
        {
            if (!_acquire && event.getName().equals(NodeObject.LOCKS) &&
                event.getEntry().equals(_lock)) {
                wasActivated(_peer.getNodeName());
            }
        }

        @Override // documentation inherited
        public String toString ()
        {
            return "[node=" + getNodeName() + ", lock=" + _lock + ", acquire=" + _acquire + "]";
        }

        /**
         * Performs the action if all remote nodes have ratified.
         */
        protected void maybeActivate ()
        {
            if (_remoids.isEmpty()) {
                _timeout.cancel();
                activate();
            }
        }

        /**
         * Performs the configured action.
         */
        protected void activate ()
        {
            _locks.remove(_lock);
            if (_acquire) {
                _nodeobj.addToLocks(_lock);
                listeners.requestCompleted(_nodeName);
            } else {
                _nodeobj.removeFromLocks(_lock);
                listeners.requestCompleted(null);
            }
        }

        /**
         * Called when the remote node has performed its action.
         */
        protected void wasActivated (String owner)
        {
            _peer.nodeobj.removeListener(this);
            _locks.remove(_lock);
            listeners.requestCompleted(owner);
        }

        protected PeerNode _peer;
        protected NodeObject.Lock _lock;
        protected boolean _acquire;
        protected ArrayIntSet _remoids;
        protected Interval _timeout;
    }

    protected String _nodeName, _sharedSecret;
    protected Invoker _invoker;
    protected NodeRepository _noderepo;
    protected NodeObject _nodeobj;
    protected HashMap<String,PeerNode> _peers = new HashMap<String,PeerNode>();

    /** The client oids of all peers subscribed to the node object. */
    protected ArrayIntSet _suboids = new ArrayIntSet();

    /** Contains a mapping of proxied objects to subscriber instances. */
    protected HashMap<Tuple<String,Integer>,Tuple<Subscriber<?>,DObject>> _proxies =
        new HashMap<Tuple<String,Integer>,Tuple<Subscriber<?>,DObject>>();

    /** Our stale cache observers. */
    protected HashMap<String, ObserverList<StaleCacheObserver>> _cacheobs =
        new HashMap<String, ObserverList<StaleCacheObserver>>();

    /** Listeners for dropped locks. */
    protected ObserverList<DroppedLockObserver> _dropobs =
        new ObserverList<DroppedLockObserver>(ObserverList.FAST_UNSAFE_NOTIFY);

    /** Locks in the process of resolution. */
    protected HashMap<NodeObject.Lock, LockHandler> _locks =
        new HashMap<NodeObject.Lock, LockHandler>();

    /** We wait this long for peer ratification to complete before acquiring/releasing the lock. */
    protected static final long LOCK_TIMEOUT = 5000L;
}
