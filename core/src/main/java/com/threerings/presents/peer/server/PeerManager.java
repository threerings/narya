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

package com.threerings.presents.peer.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.ChainedResultListener;
import com.samskivert.util.Interval;
import com.samskivert.util.Invoker;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.ObserverList;
import com.samskivert.util.ResultListener;
import com.samskivert.util.ResultListenerList;
import com.samskivert.util.Tuple;

import com.samskivert.jdbc.RepositoryUnit;
import com.samskivert.jdbc.WriteOnlyUnit;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.util.Name;

import com.threerings.presents.annotation.PeerInvoker;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.Message;
import com.threerings.presents.peer.client.PeerService;
import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.DObjectAddress;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.data.PeerAuthName;
import com.threerings.presents.peer.data.PeerMarshaller;
import com.threerings.presents.peer.net.PeerCreds;
import com.threerings.presents.peer.server.persist.NodeRecord;
import com.threerings.presents.peer.server.persist.NodeRepository;
import com.threerings.presents.server.ClientManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.PresentsDObjectMgr;
import com.threerings.presents.server.PresentsSession;
import com.threerings.presents.server.ReportManager;
import com.threerings.presents.server.ServiceAuthenticator;
import com.threerings.presents.server.SessionFactory;
import com.threerings.presents.server.net.PresentsConnectionManager;

import static com.threerings.presents.Log.log;

/**
 * Manages connections to the other nodes in a Presents server cluster. Each server maintains a
 * client connection to the other servers and subscribes to the {@link NodeObject} of all peer
 * servers and uses those objects to communicate cross-node information.
 */
public abstract class PeerManager
    implements PeerProvider, ClientManager.ClientObserver, Lifecycle.ShutdownComponent
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
        void changedCacheData (Streamable data);
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
        void droppedLock (NodeObject.Lock lock);
    }

    /**
     * Wraps an operation that needs a shared resource lock to be acquired before it can be
     * performed, and released after it completes. Used by {@link PeerManager#performWithLock}.
     */
    public static interface LockedOperation
    {
        /**
         * Called when the resource lock was acquired successfully. The lock will be released
         * immediately after this function call finishes.
         */
        void run ();

        /**
         * Called when the resource lock was not acquired successfully, with the name of the peer
         * who is holding the lock (or null in case of a generic failure).
         */
        void fail (String peerName);
    }

    public static interface NodeApplicant
    {
        /** Returns true if this should be executed on the specified node. This will be
         * called on the originating server to decide whether or not to deliver the action to the
         * server in question. */
        boolean isApplicable (NodeObject nodeobj);
    }

    /**
     * Encapsulates code that is meant to be executed one or more servers.
     */
    public static abstract class NodeAction implements Streamable.Closure, NodeApplicant
    {

        /** Invokes the action on the target server. */
        public void invoke () {
            try {
                execute();
            } catch (Throwable t) {
                log.warning(getClass().getName() + " failed.", t);
            }
        }

        protected abstract void execute ();
    }

    /**
     * Encapsulates code that is meant to be executed one or more servers and return a result.
     */
    public static abstract class NodeRequest implements Streamable.Closure, NodeApplicant
    {
        /** Invokes the action on the target server. */
        public void invoke (final InvocationService.ResultListener listener) {
            try {
                execute(listener);
            } catch (Throwable t) {
                log.warning(getClass().getName() + " failed.", t);
            }
        }

        protected abstract void execute (InvocationService.ResultListener listener);
    }

    /** Returned by {@link #getStats}. */
    public static class Stats implements Cloneable
    {
        /** The number of locks this node has acquired. */
        public long locksAcquired;

        /** The number of milliseconds spent waiting to acquire locks. */
        public long lockAcquireWait;

        /** The number of locks this node has released. */
        public long locksReleased;

        /** The number of locks this node has had hijacked. */
        public long locksHijacked;

        /** The number of lock requests that have timed out. */
        public long lockTimeouts;

        /** The number of node actions we've invoked. */
        public long nodeActionsInvoked;

        /** The total number of messages received from all of our peers. This is updated on the
         * conmgr thread which is why it's atomic. */
        public AtomicLong peerMessagesIn = new AtomicLong(0);

        /** The total number of messages sent to all of our peers. */
        public long peerMessagesOut;

        public void noteNodeActionInvoked (NodeAction action) {
            nodeActionsInvoked++;
        }

        public void notePeerMessageReceived (Message msg) {
            peerMessagesIn.incrementAndGet();
        }

        public void notePeerMessageSent (DownstreamMessage msg) {
            peerMessagesOut++;
        }

        @Override public Stats clone () {
            try {
                Stats cstats = (Stats)super.clone();
                cstats.peerMessagesIn = new AtomicLong(peerMessagesIn.get());
                return cstats;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates an uninitialized peer manager.
     */
    @Inject public PeerManager (Lifecycle cycle)
    {
        cycle.addComponent(this);
    }

    /**
     * Returns the distributed object that represents this node to its peers.
     */
    public NodeObject getNodeObject ()
    {
        return _nodeobj;
    }

    /**
     * Returns an iterable over our node object and that of our peers.
     */
    public Iterable<NodeObject> getNodeObjects ()
    {
        return Iterables.filter(
            Iterables.concat(Collections.singleton(_nodeobj),
                             Iterables.transform(_peers.values(), GET_NODE_OBJECT)),
            Predicates.notNull());
    }

    /**
     * Initializes this peer manager to connect to all other nodes in the NODES table. See
     * {@link #init(String, String, String, String, int, String)} for the behavior of the method
     * and the meaning of its parameters.
     */
    public void init (String nodeName, String sharedSecret, String hostName,
        String publicHostName, int port)
    {
        init(nodeName, sharedSecret, hostName, publicHostName, port, "");
    }

    /**
     * Initializes this peer manager and initiates the process of connecting to its peer nodes.
     * This will also reconfigure the ConnectionManager and ClientManager with peer related bits,
     * so this should not be called until <em>after</em> the main server has set up its client
     * factory and authenticator.
     *
     * @param nodeName this node's unique name.
     * @param sharedSecret a shared secret used to allow the peers to authenticate with one
     * another.
     * @param hostName the DNS name of the server running this node.
     * @param publicHostName if non-null, a separate public DNS hostname by which the node is to
     * be known to normal clients (we may want inter-peer communication to take place over a
     * different network than the communication between real clients and the various peer
     * servers).
     * @param port the port on which other nodes should connect to us.
     * @param nodeNamespace The namespace for nodes to peer with. This node will connect to other
     * nodes with the same prefix from the NODES table.
     */
    public void init (
        String nodeName, String sharedSecret, String hostName,
        String publicHostName, int port, String nodeNamespace)
    {
        init(nodeName, sharedSecret, hostName, publicHostName, null, port, nodeNamespace);
    }

    /**
     * Initializes this peer manager and initiates the process of connecting to its peer nodes.
     * This will also reconfigure the ConnectionManager and ClientManager with peer related bits,
     * so this should not be called until <em>after</em> the main server has set up its client
     * factory and authenticator.
     *
     * @param nodeName this node's unique name.
     * @param sharedSecret a shared secret used to allow the peers to authenticate with one
     * another.
     * @param hostName the DNS name of the server running this node.
     * @param publicHostName if non-null, a separate public DNS hostname by which the node is to
     * be known to normal clients (we may want inter-peer communication to take place over a
     * different network than the communication between real clients and the various peer
     * servers).
     * @param region the region in which the node lives, which may be null.  Nodes in different
     * regions must connect to each other through the public host name.
     * @param port the port on which other nodes should connect to us.
     * @param nodeNamespace The namespace for nodes to peer with. This node will connect to other
     * nodes with the same prefix from the NODES table.
     */
    public void init (
        String nodeName, String sharedSecret, String hostName, String publicHostName,
        String region, int port, String nodeNamespace)
    {
        init(nodeName, sharedSecret, hostName, publicHostName, region, port, nodeNamespace, false);
    }

    /**
     * Initializes this peer manager and initiates the process of connecting to its peer nodes.
     * This will also reconfigure the ConnectionManager and ClientManager with peer related bits,
     * so this should not be called until <em>after</em> the main server has set up its client
     * factory and authenticator.
     *
     * @param nodeName this node's unique name.
     * @param sharedSecret a shared secret used to allow the peers to authenticate with one
     * another.
     * @param hostName the DNS name of the server running this node.
     * @param publicHostName if non-null, a separate public DNS hostname by which the node is to
     * be known to normal clients (we may want inter-peer communication to take place over a
     * different network than the communication between real clients and the various peer
     * servers).
     * @param region the region in which the node lives, which may be null.  Nodes in different
     * regions must connect to each other through the public host name.
     * @param port the port on which other nodes should connect to us.
     * @param nodeNamespace The namespace for nodes to peer with. This node will connect to other
     * nodes with the same prefix from the NODES table.
     */
    public void init (
        String nodeName, String sharedSecret, String hostName, String publicHostName,
        String region, int port, String nodeNamespace, boolean adHoc)
    {
        _nodeName = nodeName;
        _sharedSecret = sharedSecret;
        _nodeNamespace = nodeNamespace;
        _adHoc = adHoc;

        // wire ourselves into the server
        _conmgr.addChainedAuthenticator(
            new ServiceAuthenticator<PeerCreds>(PeerCreds.class, PeerAuthName.class) {
            @Override protected boolean areValid (PeerCreds creds) {
                return isAuthenticPeer(creds);
            }
        });
        _clmgr.addSessionFactory(
            SessionFactory.newSessionFactory(PeerCreds.class, PeerSession.class,
                                             PeerAuthName.class, PeerClientResolver.class));

        // create our node object
        _nodeobj = _omgr.registerObject(createNodeObject());
        _nodeobj.setNodeName(nodeName);
        _nodeobj.setBootStamp(System.currentTimeMillis());

        // register ourselves with the node table
        _self = new NodeRecord(
            _nodeName, hostName, (publicHostName == null) ? hostName : publicHostName,
            region, port);
        if (!adHoc) {
            _invoker.postUnit(new WriteOnlyUnit("registerNode(" + _self + ")") {
                @Override
                public void invokePersist () throws Exception {
                    _noderepo.updateNode(_self);
                }
            });
        }

        // set the invocation service
        _nodeobj.setPeerService(_invmgr.registerProvider(this, PeerMarshaller.class));

        // register ourselves as a client observer
        _clmgr.addClientObserver(this);

        // and start our peer refresh interval (this lives for the lifetime of the server)
        if (!adHoc) {
            _omgr.newInterval(new Runnable() {
                public void run () {
                    refreshPeers();
                }
            }).schedule(5000L, 60*1000L);
        }

        // give derived classes an easy way to get in on the init action
        didInit();
    }

    /**
     * Returns true if the supplied peer credentials match our shared secret.
     */
    public boolean isAuthenticPeer (PeerCreds creds)
    {
        return creds.areValid(_sharedSecret);
    }

    /**
     * Locates the client with the specified name. Returns null if the client is not logged onto
     * any peer.
     */
    public ClientInfo locateClient (final Name key)
    {
        return lookupNodeDatum(new Function<NodeObject,ClientInfo>() {
            public ClientInfo apply (NodeObject nodeobj) {
                return nodeobj.clients.get(key);
            }
        });
    }

    /**
     * Locates a datum from among the set of peer {@link NodeObject}s. Objects are searched in
     * arbitrary order and the first non-null value returned by the supplied lookup operation is
     * returned to the caller. Null if all lookup operations returned null.
     */
    public <T> T lookupNodeDatum (Function<NodeObject,T> op)
    {
        for (T value :
                Iterables.filter(Iterables.transform(getNodeObjects(), op), Predicates.notNull())) {
            return value;
        }
        return null;
    }

    /**
     * Invokes the supplied function on <em>all</em> node objects (except the local node). A caller
     * that needs to call an invocation service method on a remote node should use this mechanism
     * to locate the appropriate node (or nodes) and call the desired method.
     *
     * @return the number of times the invoked function returned true.
     */
    public int invokeOnNodes (Function<Tuple<Client,NodeObject>,Boolean> func)
    {
        int invoked = 0;
        for (PeerNode peer : _peers.values()) {
            if (peer.nodeobj != null) {
                if (func.apply(Tuple.newTuple(peer.getClient(), peer.nodeobj))) {
                    invoked++;
                }
            }
        }
        return invoked;
    }

    /**
     * Invokes the supplied action on this and any other server that it indicates is appropriate.
     * The action will be executed on the distributed object thread, but this method does not need
     * to be called from the distributed object thread.
     */
    public void invokeNodeAction (final NodeAction action)
    {
        invokeNodeAction(action, null);
    }

    /**
     * Invokes the supplied action on this and any other server that it indicates is appropriate.
     * The action will be executed on the distributed object thread, but this method does not need
     * to be called from the distributed object thread.
     *
     * @param onDropped a runnable to be executed if the action was not invoked on the local server
     * or any peer node due to failing to match any of the nodes. The runnable will be executed on
     * the dobj event thread.
     */
    public void invokeNodeAction (final NodeAction action, final Runnable onDropped)
    {
        // if we're not on the dobjmgr thread, get there
        if (!_omgr.isDispatchThread()) {
            _omgr.postRunnable(new Runnable() {
                public void run () {
                    invokeNodeAction(action, onDropped);
                }
            });
            return;
        }

        // first serialize the action to make sure we can
        byte[] actionBytes = flattenAction(action);

        // invoke the action on our local server if appropriate
        boolean invoked = false;
        if (action.isApplicable(_nodeobj)) {
            invokeAction(null, actionBytes);
            invoked = true;
        }

        // now send it to any remote node that is also appropriate
        for (PeerNode peer : _peers.values()) {
            if (peer.nodeobj != null && action.isApplicable(peer.nodeobj)) {
                peer.nodeobj.peerService.invokeAction(actionBytes);
                invoked = true;
            }
        }

        // if we did not invoke the action on any node, call the onDropped handler
        if (!invoked && onDropped != null) {
            onDropped.run();
        }

        if (invoked) {
            _stats.noteNodeActionInvoked(action); // stats!
        }
    }

    /**
     * Invokes a node action on a specific node <em>without</em> executing {@link
     * NodeAction#isApplicable} to determine whether the action is applicable.
     */
    public void invokeNodeAction (String nodeName, NodeAction action)
    {
        PeerNode peer = _peers.get(nodeName);
        if (peer != null) {
            if (peer.nodeobj != null) {
                peer.nodeobj.peerService.invokeAction(flattenAction(action));

            } else {
                log.warning("Dropped NodeAction", "nodeName", nodeName, "action", action);
            }

        } else if (Objects.equal(nodeName, _nodeName)) {
            invokeAction(null, flattenAction(action));
        }
    }

    /**
     * Invokes the supplied request on all servers in parallel. The request will execute on the
     * distributed object thread, but this method does not need to be called from there.
     *
     * If any one node reports failure, this function reports failure. If all nodes report success,
     * this function will report success.
     */
    public <T> void invokeNodeRequest (
        final NodeRequest request, final NodeRequestsListener<T> listener)
    {
        // if we're not on the dobjmgr thread, get there
        if (!_omgr.isDispatchThread()) {
            _omgr.postRunnable(new Runnable() {
                public void run () {
                    invokeNodeRequest(request, listener);
                }
            });
            return;
        }

        // serialize the action to make sure we can
        byte[] requestBytes = flattenRequest(request);

        // build a set of node names (including the local node) to which to send the request
        final Set<String> nodes = findApplicableNodes(request);
        if (nodes.isEmpty()) {
            listener.requestsProcessed(new NodeRequestsResultImpl<T>());
            return;
        }

        final Map<String, T> results = Maps.newHashMap();
        final Map<String, String> failures = Maps.newHashMap();
        final AtomicInteger completedNodes = new AtomicInteger();
        for (final String node : nodes) {
            invokeNodeRequest(node, requestBytes, new InvocationService.ResultListener() {
                public void requestProcessed (Object result) {
                    // check off this node's successful response
                    @SuppressWarnings("unchecked")
                    T castResult = (T) result;
                    results.put(node, castResult);
                    nodeDone();
                }
                public void requestFailed (String cause) {
                    failures.put(node, cause);
                    nodeDone();
                }
                protected void nodeDone () {
                    if (completedNodes.incrementAndGet() == nodes.size()) {
                        // if all nodes have responded, let caller know
                        listener.requestsProcessed(new NodeRequestsResultImpl<T>(results, failures));
                    }
                }
            });
        }
    }

    /**
     * Returns all nodes for which <code>applicant.isApplicable</code> returns true.
     */
    public Set<String> findApplicableNodes (NodeApplicant applicant)
    {
        Set<String> nodes = Sets.newHashSet();
        if (applicant.isApplicable(_nodeobj)) {
            nodes.add(_nodeobj.nodeName);
        }
        for (PeerNode peer : _peers.values()) {
            if (peer.nodeobj != null && applicant.isApplicable(peer.nodeobj)) {
                nodes.add(peer.getNodeName());
            }
        }
        return nodes;
    }

    /**
     * Invokes a node request on a specific node and returns the result through the listener.
     */
    public void invokeNodeRequest (String nodeName, NodeRequest request,
        InvocationService.ResultListener listener)
    {
        invokeNodeRequest(nodeName, flattenRequest(request), listener);
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
        String nodeName, final int remoteOid, final ResultListener<Integer> listener)
    {
        proxyRemoteObject(new DObjectAddress(nodeName, remoteOid), listener);
    }

    public <T extends DObject> void proxyRemoteObject (
        final DObjectAddress remote, final ResultListener<Integer> listener)
    {
        if (Objects.equal(remote.nodeName, _nodeName)) {
            // Still subscribe if the DObject is local to preserve the behavior of
            // DObject.setDestroyOnLastSubscriberRemoved on the proxied object
            _omgr.subscribeToObject(remote.oid, new Subscriber<T>() {
                public void objectAvailable (T object) {
                    _proxies.put(remote, new Tuple<Subscriber<?>, DObject>(this, object));
                    listener.requestCompleted(remote.oid);
                }

                public void requestFailed(int oid, ObjectAccessException oae) {
                    listener.requestFailed(oae);
                }
            });
            return;
        }
        final Client peer = getPeerClient(remote.nodeName);
        if (peer == null) {
            String errmsg = "Have no connection to peer [node=" + remote.nodeName + "].";
            listener.requestFailed(new ObjectAccessException(errmsg));
            return;
        }

        if (_proxies.containsKey(remote)) {
            String errmsg = "Cannot proxy already proxied object [key=" + remote + "].";
            listener.requestFailed(new ObjectAccessException(errmsg));
            return;
        }

        // issue a request to subscribe to the remote object
        peer.getDObjectManager().subscribeToObject(remote.oid, new Subscriber<T>() {
            public void objectAvailable (T object) {
                // make a note of this proxy mapping
                _proxies.put(remote, new Tuple<Subscriber<?>, DObject>(this, object));
                // map the object into our local oid space
                _omgr.registerProxyObject(object, peer.getDObjectManager());
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
        unproxyRemoteObject(new DObjectAddress(nodeName, remoteOid));
    }

    /**
     * Unsubscribes from and clears a proxied object. The caller must be sure that there are no
     * remaining subscribers to the object on this local server.
     */
    public void unproxyRemoteObject (DObjectAddress addr)
    {
        Tuple<Subscriber<?>, DObject> bits = _proxies.remove(addr);
        if (bits == null) {
            log.warning("Requested to clear unknown proxy", "addr", addr);
            return;
        }

        // If it's local, just remove the subscriber we added and bail
        if (Objects.equal(addr.nodeName, _nodeName)) {
            bits.right.removeSubscriber(bits.left);
            return;
        }


        // clear out the local object manager's proxy mapping
        _omgr.clearProxyObject(addr.oid, bits.right);

        final Client peer = getPeerClient(addr.nodeName);
        if (peer == null) {
            log.warning("Unable to unsubscribe from proxy, missing peer", "addr", addr);
            return;
        }

        // restore the object's omgr reference to our ClientDObjectMgr and its oid back to the
        // remote oid so that it can properly finish the unsubscription process
        bits.right.setOid(addr.oid);
        bits.right.setManager(peer.getDObjectManager());

        // finally unsubscribe from the object on our peer
        peer.getDObjectManager().unsubscribeFromObject(addr.oid, bits.left);
    }

    /**
     * Returns the NodeObject of the named peer, or <code>null</code> if null if the peer is not
     * currently connected to this server.
     */
    public NodeObject getPeerNodeObject (String nodeName)
    {
        if (Objects.equal(_nodeName, nodeName)) {
            return _nodeobj;
        }
        PeerNode peer = _peers.get(nodeName);
        return (peer == null) ? null : peer.nodeobj;
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
        if (Objects.equal(_nodeName, nodeName)) {
            return _self.publicHostName;
        }
        PeerNode peer = _peers.get(nodeName);
        return (peer == null) ? null : peer.getPublicHostName();
    }

    /**
     * Returns the internal hostname to use when connecting to the specified peer or null if the
     * peer is not currently connected to this server. Peers connect to one another via their
     * internal hostname. Do not publish this data to clients out on the Internets.
     */
    public String getPeerInternalHostName (String nodeName)
    {
        if (Objects.equal(_nodeName, nodeName)) {
            return _self.hostName;
        }
        PeerNode peer = _peers.get(nodeName);
        return (peer == null) ? null : peer.getInternalHostName();
    }

    /**
     * Returns the port on which to connect to the specified peer or -1 if the peer is not
     * currently connected to this server.
     */
    public int getPeerPort (String nodeName)
    {
        if (Objects.equal(_nodeName, nodeName)) {
            return _self.port;
        }
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
        queryLock(lock, new ChainedResultListener<String, String>(listener) {
            public void requestCompleted (String result) {
                if (result == null) {
                    if (_suboids.isEmpty()) {
                        lockAcquired(lock, 0L, listener);
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
        queryLock(lock, new ChainedResultListener<String, String>(listener) {
            public void requestCompleted (String result) {
                if (Objects.equal(_nodeName, result)) {
                    if (_suboids.isEmpty()) {
                        lockReleased(lock, listener);
                    } else {
                        _locks.put(lock, new LockHandler(lock, false, listener));
                    }
                } else {
                    if (result != null) {
                        log.warning("Tried to release lock held by another peer", "lock", lock,
                                    "owner", result);
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
        if (handler == null || !Objects.equal(handler.getNodeName(), _nodeName) ||
                handler.isAcquiring()) {
            log.warning("Tried to reacquire lock not being released", "lock", lock,
                        "handler", handler);
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
        for (NodeObject nodeobj : getNodeObjects()) {
            if (nodeobj.locks.contains(lock)) {
                return nodeobj.nodeName;
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
                    try {
                        operation.run();
                    } finally {
                        releaseLock(lock, new ResultListener.NOOP<String>());
                    }
                } else {
                    // some other peer beat us to it
                    operation.fail(nodeName);
                    if (nodeName == null) {
                        log.warning("Lock acquired by null?", "lock", lock);
                    }
                }
            }
            public void requestFailed (Exception cause) {
                log.warning("Lock acquisition failed", "lock", lock, cause);
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

    /**
     * Called by {@link PeerSession}s when clients subscribe to the {@link NodeObject}.
     */
    public void clientSubscribedToNode (int cloid)
    {
        _suboids.add(cloid);
    }

    /**
     * Called by {@link PeerSession}s when clients unsubscribe from the {@link NodeObject}.
     */
    public void clientUnsubscribedFromNode (int cloid)
    {
        _suboids.remove(cloid);
        for (LockHandler handler : _locks.values().toArray(new LockHandler[_locks.size()])) {
            if (Objects.equal(handler.getNodeName(), _nodeName)) {
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
            list = ObserverList.newFastUnsafe();
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
     * Returns a snapshot of runtime statistics tracked by the peer manager.
     */
    public Stats getStats ()
    {
        return _stats.clone();
    }

    // from interface Lifecycle.ShutdownComponent
    public void shutdown ()
    {
        if (_self == null) { // never initialized
            return;
        }

        // clear out our invocation service
        if (_nodeobj != null) {
            _invmgr.clearDispatcher(_nodeobj.peerService);
        }

        // clear out our client observer registration
        _clmgr.removeClientObserver(this);

        // clear our record from the node table
        if (!_adHoc) {
            _invoker.postUnit(new WriteOnlyUnit("shutdownNode(" + _nodeName + ")") {
                @Override
                public void invokePersist () throws Exception {
                    _noderepo.shutdownNode(_nodeName);
                }
            });
        }

        // shut down the peers
        for (PeerNode peer : _peers.values()) {
            peer.shutdown();
        }
    }

    // from interface PeerProvider
    public void ratifyLockAction (ClientObject caller, NodeObject.Lock lock, boolean acquire)
    {
        LockHandler handler = _locks.get(lock);
        if (handler != null && Objects.equal(handler.getNodeName(), _nodeName)) {
            handler.ratify(caller, acquire);
        } else {
            // this is not an error condition, as we may have cancelled the handler or
            // allowed another to take priority
        }
    }

    // from interface PeerProvider
    public void invokeAction (ClientObject caller, byte[] serializedAction)
    {
        NodeAction action = null;
        try {
            ObjectInputStream oin =
                new ObjectInputStream(new ByteArrayInputStream(serializedAction));
            action = (NodeAction)oin.readObject();
            _injector.injectMembers(action);
            action.invoke();
        } catch (Exception e) {
            log.warning("Failed to execute node action",
                        "from", (caller == null) ? "self" : caller.who(),
                        "action", action, "serializedSize", serializedAction.length, e);
        }
    }

    // from interface PeerProvider
    public void invokeRequest (ClientObject caller, byte[] serializedAction,
        InvocationService.ResultListener listener)
    {
        NodeRequest request = null;
        try {
            ObjectInputStream oin =
                new ObjectInputStream(new ByteArrayInputStream(serializedAction));
            request = (NodeRequest)oin.readObject();
            _injector.injectMembers(request);
            request.invoke(listener);

        } catch (Exception e) {
            log.warning("Failed to execute node request",
                        "from", (caller == null) ? "self" : caller.who(),
                        "request", request, "serializedSize", serializedAction.length, e);
            listener.requestFailed("Failed to execute node request");
        }
    }

    // from interface PeerProvider
    public void generateReport (ClientObject caller, String type,
                                PeerService.ResultListener listener)
        throws InvocationException
    {
        listener.requestProcessed(_repmgr.generateReport(type));
    }

    // from interface ClientManager.ClientObserver
    public void clientSessionDidStart (PresentsSession client)
    {
        if (ignoreClient(client)) {
            return;
        }

        // create and publish a ClientInfo record for this client
        ClientInfo clinfo = createClientInfo();
        initClientInfo(client, clinfo);

        // sanity check
        if (_nodeobj.clients.contains(clinfo)) {
            log.warning("Received clientSessionDidStart() for already registered client!?",
                        "old", _nodeobj.clients.get(clinfo.getKey()), "new", clinfo);
            // go ahead and update the record
            _nodeobj.updateClients(clinfo);
        } else {
            _nodeobj.addToClients(clinfo);
        }
    }

    // from interface ClientManager.ClientObserver
    public void clientSessionDidEnd (PresentsSession client)
    {
        if (ignoreClient(client)) {
            return;
        }

        // we scan through the list instead of relying on ClientInfo.getKey() because we want
        // derived classes to be able to override that for lookups that happen way more frequently
        // than logging off
        Name username = client.getAuthName();
        for (ClientInfo clinfo : _nodeobj.clients) {
            if (clinfo.username.equals(username)) {
                _nodeobj.startTransaction();
                try {
                    // we clear our client info in a transaction so that derived classes can remove
                    // other things from the NodeObject and we'll send that out to all of our peers
                    // in a single compound event
                    clearClientInfo(client, clinfo);
                } finally {
                    _nodeobj.commitTransaction();
                }
                return;
            }
        }
        log.warning("Session ended for unregistered client", "who", username);
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
        if (_adHoc) {
            return;
        }

        // load up information on our nodes
        _invoker.postUnit(new RepositoryUnit("refreshPeers") {
            @Override
            public void invokePersist () throws Exception {
                // let the world know that we're alive
                _noderepo.heartbeatNode(_nodeName);

                // then load up all the peer records
                _nodes = Maps.newHashMap();
                for (NodeRecord record : _noderepo.loadNodes(_nodeNamespace)) {
                    _nodes.put(record.nodeName, record);
                }
            }
            @Override
            public void handleSuccess () {
                // refresh peers with loaded records
                long now = System.currentTimeMillis();
                for (Iterator<NodeRecord> it = _nodes.values().iterator(); it.hasNext(); ) {
                    NodeRecord record = it.next();
                    if (Objects.equal(record.nodeName, _nodeName)) {
                        continue;
                    }
                    if ((now - record.lastUpdated.getTime()) > PeerNode.STALE_INTERVAL) {
                        it.remove();
                        continue;
                    }
                    try {
                        refreshPeer(record);
                    } catch (Exception e) {
                        log.warning("Failure refreshing peer " + record + ".", e);
                    }
                }

                // remove peers for which we no longer have up-to-date records
                for (Iterator<PeerNode> it = _peers.values().iterator(); it.hasNext(); ) {
                    PeerNode peer = it.next();
                    if (!_nodes.containsKey(peer.getNodeName())) {
                        peer.shutdown();
                        it.remove();
                    }
                }
            }
            @Override
            public long getLongThreshold () {
                return 700L;
            }
            protected Map<String, NodeRecord> _nodes;
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
            peer = _injector.getInstance(getPeerNodeClass());
            _peers.put(record.nodeName, peer);
            peer.init(record);
        }
        peer.refresh(record);
    }

    /**
     * Returns true if we should ignore the supplied client, false if we should let our other peers
     * know that this client is authenticated with this server. <em>Note:</em> this is called at
     * the beginning and end of the client session, so this method should return the same value
     * both times.
     */
    protected boolean ignoreClient (PresentsSession client)
    {
        // if this is another peer, don't publish their info
        return (client instanceof PeerSession);
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
        _stats.locksHijacked++;
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
    protected void initClientInfo (PresentsSession client, ClientInfo info)
    {
        info.username = client.getAuthName();
    }

    /**
     * Called when a client ends their session to clear their information from our node object.
     */
    protected void clearClientInfo (PresentsSession client, ClientInfo info)
    {
        _nodeobj.removeFromClients(info.getKey());
    }

    /**
     * Creates a {@link PeerNode} to manage our connection to the specified peer.
     */
    protected Class<? extends PeerNode> getPeerNodeClass ()
    {
        return PeerNode.class;
    }

    /**
     * Creates credentials that a {@link PeerNode} can use to authenticate with another node.
     */
    protected PeerCreds createCreds ()
    {
        return new PeerCreds(_nodeName, _sharedSecret);
    }

    /**
     * Returns the region in which the local node exists.
     */
    protected String getRegion ()
    {
        return _self.region;
    }

    /**
     * Called when we hear about a client logging on to another node.
     */
    protected void clientLoggedOn (String nodeName, ClientInfo clinfo)
    {
        PresentsSession session = _clmgr.getClient(clinfo.username);
        if (session != null) {
            log.info("Booting user that has connected on another node",
                "username", clinfo.username, "otherNode", nodeName);
            session.endSession();
        }
    }

    /**
     * Called when we hear about a client logging off of another node.
     */
    protected void clientLoggedOff (String nodeName, ClientInfo clinfo)
    {
        // nothing to do by default
    }

    /**
     * Called when a peer connects to this server.
     */
    protected void peerStartedSession (PeerSession session)
    {
        // this may be the first we've heard of this guy, so let's refresh our peers and
        // potentially connect right back to him
        refreshPeers();
        // pass our stats record in so that it can count up messages in/out
        session.setStats(_stats);
    }

    /**
     * Called when a peer's session with this server ends.
     */
    protected void peerEndedSession (PeerSession session)
    {
        // nada
    }

    /**
     * Called when we have established a connection to the supplied peer.
     */
    protected void connectedToPeer (PeerNode peer)
    {
        // nothing by default
    }

    /**
     * Called when a ended our session with the supplied peer.
     */
    protected void disconnectedFromPeer (PeerNode peer)
    {
        // nothing by default
    }

    /**
     * Called when a peer announces its intention to acquire a lock.
     */
    protected void peerAcquiringLock (PeerNode peer, NodeObject.Lock lock)
    {
        // refuse to ratify if we believe someone else owns the lock
        String owner = queryLock(lock);
        if (owner != null) {
            log.warning("Refusing to ratify lock acquisition.", "lock", lock,
                "node", peer.getNodeName(), "owner", owner);
            return;
        }

        // check for an existing handler
        LockHandler handler = _locks.get(lock);
        if (handler == null) {
            createLockHandler(peer, lock, true);
            return;
        }

        // if the existing node has priority, we're done
        if (hasPriority(handler.getNodeName(), peer.getNodeName())) {
            return;
        }

        // the new node has priority, so cancel the existing handler and take over
        // its listeners
        ResultListenerList<String> olisteners = handler.listeners;
        handler.cancel();
        handler = createLockHandler(peer, lock, true);
        handler.listeners = olisteners;
    }

    /**
     * Called when a peer announces its intention to release a lock.
     */
    protected void peerReleasingLock (PeerNode peer, NodeObject.Lock lock)
    {
        // refuse to ratify if we don't believe they own the lock
        String owner = queryLock(lock);
        if (!peer.getNodeName().equals(owner)) {
            log.warning("Refusing to ratify lock release.", "lock", lock,
                "node", peer.getNodeName(), "owner", owner);
            return;
        }

        // check for an existing handler
        LockHandler handler = _locks.get(lock);
        if (handler == null) {
            createLockHandler(peer, lock, false);
        } else {
            log.warning("Received request to release resolving lock",
               "node", peer.getNodeName(), "handler", handler);
        }
    }

    /**
     * Called when a peer adds a lock.
     */
    protected void peerAddedLock (String nodeName, NodeObject.Lock lock)
    {
        // check for hijacking
        if (_nodeobj.locks.contains(lock)) {
            log.warning("Client hijacked lock owned by this node", "lock", lock, "node", nodeName);
            droppedLock(lock);
        }

        // notify the handler, if any
        LockHandler handler = _locks.get(lock);
        if (handler != null) {
            handler.peerAddedLock(nodeName);
        }
    }

    /**
     * Called when a peer updates a lock.
     */
    protected void peerUpdatedLock (String nodeName, NodeObject.Lock lock)
    {
        // notify the handler, if any
        LockHandler handler = _locks.get(lock);
        if (handler != null) {
            handler.peerUpdatedLock(nodeName);
        }
    }

    /**
     * Called when a peer removes a lock.
     */
    protected void peerRemovedLock (String nodeName, NodeObject.Lock lock)
    {
        // notify the handler, if any
        LockHandler handler = _locks.get(lock);
        if (handler != null) {
            handler.peerRemovedLock(nodeName);
        }
    }

    /**
     * Flattens the supplied node action into bytes.
     */
    protected byte[] flattenAction (NodeAction action)
    {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(action);
            return bout.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to serialize node action [action=" + action + "].", e);
        }
    }

    /**
     * Flattens the supplied node request into bytes.
     */
    protected byte[] flattenRequest (NodeRequest request)
    {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(request);
            return bout.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to serialize node request [request=" + request + "].", e);
        }
    }

    protected void invokeNodeRequest (String nodeName, byte[] requestBytes,
        InvocationService.ResultListener listener)
    {
        PeerNode peer = _peers.get(nodeName);
        if (peer != null) {
            if (peer.nodeobj != null) {
                peer.nodeobj.peerService.invokeRequest(requestBytes, listener);

            } else {
                log.warning("Dropped NodeRequest", "nodeName", nodeName);
                listener.requestFailed(InvocationCodes.INTERNAL_ERROR);
            }

        } else if (Objects.equal(nodeName, _nodeName)) {
            invokeRequest(null, requestBytes, listener);
        }
    }

    protected void lockAcquired (NodeObject.Lock lock, long wait, ResultListener<String> listener)
    {
        _nodeobj.addToLocks(lock);
        _stats.locksAcquired++;
        _stats.lockAcquireWait += wait;
        listener.requestCompleted(_nodeName);
    }

    protected void lockReleased (NodeObject.Lock lock, ResultListener<String> listener)
    {
        _nodeobj.removeFromLocks(lock);
        _stats.locksReleased++;
        listener.requestCompleted(null);
    }

    /**
     * Returns the amount of time to wait for peer ratification to complete before
     * acquiring/releasing a lock.
     */
    protected long getLockTimeout ()
    {
        return DEFAULT_LOCK_TIMEOUT;
    }

    /**
     * Determines whether the first node named has priority over the second when resolving
     * lock disputes.
     */
    protected static boolean hasPriority (String nodeName1, String nodeName2)
    {
        return nodeName1.compareTo(nodeName2) < 0;
    }

    /**
     * Handles a lock in a state of resolution.
     */
    protected class LockHandler
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
            _remoids = _suboids.clone();

            // schedule a timeout to act if something goes wrong
            (_timeout = _omgr.newInterval(new Runnable () {
                public void run () {
                    log.warning("Lock handler timed out, acting anyway", "lock", _lock,
                                "acquire", _acquire);
                    _stats.lockTimeouts++;
                    activate();
                }
            })).schedule(getLockTimeout());
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
            peer.nodeobj.peerService.ratifyLockAction(lock, acquire);
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
                log.warning("Received unexpected ratification", "handler", this,
                            "who", caller.who());
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
         * Called to notify us that a node has added the lock.
         */
        public void peerAddedLock (String nodeName)
        {
            if (!(_acquire && getNodeName().equals(nodeName))) {
                log.warning("Node hijacked lock in process of resolution.",
                    "node", nodeName, "handler", this);
                _stats.locksHijacked++;
            }
            cancel();
            wasActivated(nodeName);
        }

        /**
         * Called to notify us that a node has updated the lock.
         */
        public void peerUpdatedLock (String nodeName)
        {
            // updating is a signal of reacquisition
            if (!(!_acquire && getNodeName().equals(nodeName))) {
                log.warning("Unexpected lock update.", "node", nodeName, "handler", this);
                _stats.locksHijacked++;
            }
            cancel();
            wasActivated(nodeName);
        }

        /**
         * Called to notify us that a node has removed the lock.
         */
        public void peerRemovedLock (String nodeName)
        {
            if (getNodeName().equals(nodeName)) {
                wasActivated(null);
            } else {
                log.warning("Unexpected lock removal.", "node", nodeName, "handler", this);
            }
        }

        /**
         * Cancels this handler, as another one will be taking its place.
         */
        public void cancel ()
        {
            if (_peer == null) {
                _timeout.cancel();
            }
        }

        @Override
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
                lockAcquired(_lock, System.currentTimeMillis() - _startStamp, listeners);
            } else {
                lockReleased(_lock, listeners);
            }
        }

        /**
         * Called when the remote node has performed its action.
         */
        protected void wasActivated (String owner)
        {
            _locks.remove(_lock);
            listeners.requestCompleted(owner);
        }

        protected PeerNode _peer;
        protected NodeObject.Lock _lock;
        protected boolean _acquire;
        protected ArrayIntSet _remoids;
        protected Interval _timeout;
        protected long _startStamp = System.currentTimeMillis();
    }

    protected static class NodeRequestsResultImpl<T>
        implements NodeRequestsListener.NodeRequestsResult<T>
    {
        public NodeRequestsResultImpl (Map<String, T> results, Map<String, String> errors)
        {
            _results = Maps.newHashMap(results);
            _errors = Maps.newHashMap(errors);
        }

        public NodeRequestsResultImpl ()
        {
            _results = Maps.newHashMap();
            _errors = Maps.newHashMap();
        }

        public Map<String, T> getNodeResults ()
        {
            return _results;
        }

        public Map<String, String> getNodeErrors ()
        {
            return _errors;
        }

        public boolean wasDropped ()
        {
            return _results.isEmpty() && _errors.isEmpty();
        }

        protected Map<String, T> _results;
        protected Map<String, String> _errors;
    }

    /** Extracts the node object from the supplied peer. */
    protected static final Function<PeerNode, NodeObject> GET_NODE_OBJECT =
        new Function<PeerNode, NodeObject>() {
        public NodeObject apply (PeerNode peer) {
            return peer.nodeobj;
        }
    };

    /** The name of our node, which may be null if we are running in ad-hoc "multinoded" mode
     * with but a single node. */
    protected String _nodeName;
    protected String _sharedSecret;
    protected NodeRecord _self;
    protected NodeObject _nodeobj;
    protected String _nodeNamespace;
    protected Map<String,PeerNode> _peers = Maps.newHashMap();

    /** Are we in ad-hoc mode? (Not really connected to peers) */
    protected boolean _adHoc;

    /** The client oids of all peers subscribed to the node object. */
    protected ArrayIntSet _suboids = new ArrayIntSet();

    /** Contains a mapping of proxied objects to subscriber instances. */
    protected Map<DObjectAddress, Tuple<Subscriber<?>, DObject>> _proxies = Maps.newHashMap();

    /** Our stale cache observers. */
    protected Map<String, ObserverList<StaleCacheObserver>> _cacheobs = Maps.newHashMap();

    /** Listeners for dropped locks. */
    protected ObserverList<DroppedLockObserver> _dropobs = ObserverList.newFastUnsafe();

    /** Locks in the process of resolution. */
    protected Map<NodeObject.Lock, LockHandler> _locks = Maps.newHashMap();

    /** Used to track runtime statistics. */
    protected Stats _stats = new Stats();

    // our service dependencies
    @Inject protected @PeerInvoker Invoker _invoker;
    @Inject protected ClientManager _clmgr;
    @Inject protected PresentsConnectionManager _conmgr;
    @Inject protected Injector _injector;
    @Inject protected InvocationManager _invmgr;
    @Inject protected NodeRepository _noderepo;
    @Inject protected PresentsDObjectMgr _omgr;
    @Inject protected ReportManager _repmgr;

    /** The default lock timeout. */
    protected static final long DEFAULT_LOCK_TIMEOUT = 5000L;
}
