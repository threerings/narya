//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.awt.event.KeyEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.samskivert.util.DebugChords;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.IntMap;
import com.samskivert.util.Interval;
import com.samskivert.util.Queue;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.CompoundEvent;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.net.BootstrapNotification;
import com.threerings.presents.net.CompoundDownstreamMessage;
import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.EventNotification;
import com.threerings.presents.net.FailureResponse;
import com.threerings.presents.net.ForwardEventRequest;
import com.threerings.presents.net.Message;
import com.threerings.presents.net.ObjectResponse;
import com.threerings.presents.net.PongResponse;
import com.threerings.presents.net.SubscribeRequest;
import com.threerings.presents.net.UnsubscribeRequest;
import com.threerings.presents.net.UnsubscribeResponse;
import com.threerings.presents.net.UpdateThrottleMessage;

import static com.threerings.presents.Log.log;

/**
 * The client distributed object manager manages a set of proxy objects which mirror the
 * distributed objects maintained on the server.  Requests for modifications, etc. are forwarded to
 * the server and events are dispatched from the server to this client for objects to which this
 * client is subscribed.
 */
public class ClientDObjectMgr
    implements DObjectManager, Runnable
{
    /**
     * Constructs a client distributed object manager.
     *
     * @param comm a communicator instance by which it can communicate with the server.
     * @param client a reference to the client that is managing this whole communications and event
     * dispatch business.
     */
    public ClientDObjectMgr (Communicator comm, Client client)
    {
        _comm = comm;
        _client = client;

        // register a debug hook for dumping all objects in the distributed object table
        DebugChords.registerHook(DUMP_OTABLE_MODMASK, DUMP_OTABLE_KEYCODE, new DebugChords.Hook() {
            public void invoke () {
                log.info("Dumping " + _ocache.size() + " objects:");
                for (DObject obj : _ocache.values()) {
                    log.info(obj.getClass().getName() + " " + obj);
                }
            }
        });

        // register a flush interval
        _flusher = new Interval(client.getRunQueue()) {
            @Override public void expired () {
                flushObjects();
            }
        };
        _flusher.schedule(FLUSH_INTERVAL, true);
    }

    // documentation inherited from interface
    public boolean isManager (DObject object)
    {
        // we are never authoritative in the present implementation
        return false;
    }

    // inherit documentation from the interface
    public <T extends DObject> void subscribeToObject (int oid, Subscriber<T> target)
    {
        if (oid <= 0) {
            target.requestFailed(oid, new ObjectAccessException("Invalid oid " + oid + "."));
        } else {
            queueAction(oid, target, true);
        }
    }

    // inherit documentation from the interface
    public <T extends DObject> void unsubscribeFromObject (int oid, Subscriber<T> target)
    {
        queueAction(oid, target, false);
    }

    // inherit documentation from the interface
    public void postEvent (DEvent event)
    {
        // send a forward event request to the server
        _comm.postMessage(new ForwardEventRequest(event));
    }

    // inherit documentation from the interface
    public void removedLastSubscriber (DObject obj, boolean deathWish)
    {
        // if this object has a registered flush delay, don't can it just yet, just slip it onto
        // the flush queue
        Class<?> oclass = obj.getClass();
        for (Class<?> dclass : _delays.keySet()) {
            if (dclass.isAssignableFrom(oclass)) {
                long expire =  System.currentTimeMillis() + _delays.get(dclass).longValue();
                _flushes.put(obj.getOid(), new FlushRecord(obj, expire));
//                 Log.info("Flushing " + obj.getOid() + " at " + new java.util.Date(expire));
                return;
            }
        }

        // if we didn't find a delay registration, flush immediately
        flushObject(obj);
    }

    /**
     * Registers an object flush delay.
     *
     * @see Client#registerFlushDelay
     */
    public void registerFlushDelay (Class<?> objclass, long delay)
    {
        _delays.put(objclass, Long.valueOf(delay));
    }

    /**
     * Called by the communicator when a message arrives from the network layer. We queue it up for
     * processing and request some processing time on the main thread.
     */
    public void processMessage (Message msg)
    {
        if (_client.getRunQueue().isRunning()) {
            // append it to our queue
            _actions.append(msg);
            // and queue ourselves up to be run
            _client.getRunQueue().postRunnable(this);
        } else {
            log.info("Dropping message as RunQueue is shutdown", "msg", msg);
        }
    }

    /**
     * Invoked on the main client thread to process any newly arrived messages that we have waiting
     * in our queue.
     */
    public void run ()
    {
        // process the next event on our queue
        Object obj;
        if ((obj = _actions.getNonBlocking()) != null) {
            dispatchAction(obj);
        }
    }

    protected void dispatchAction (Object obj)
    {
        if (obj instanceof EventNotification) {
            dispatchEvent(((EventNotification)obj).getEvent());

        } else if (obj instanceof BootstrapNotification) {
            BootstrapData data = ((BootstrapNotification)obj).getData();
            _client.gotBootstrap(data, this);

        } else if (obj instanceof ObjectResponse<?>) {
            registerObjectAndNotify((ObjectResponse<?>)obj);

        } else if (obj instanceof UnsubscribeResponse) {
            int oid = ((UnsubscribeResponse)obj).getOid();
            if (_dead.remove(oid) == null) {
                log.warning("Received unsub ACK from unknown object", "oid", oid);
            }

        } else if (obj instanceof FailureResponse) {
            notifyFailure(((FailureResponse)obj).getOid(), ((FailureResponse)obj).getMessage());

        } else if (obj instanceof PongResponse) {
            _client.gotPong((PongResponse)obj);

        } else if (obj instanceof UpdateThrottleMessage) {
            UpdateThrottleMessage upmsg = (UpdateThrottleMessage)obj;
            _client.setOutgoingMessageThrottle(upmsg.messagesPerSec);

        } else if (obj instanceof ObjectAction<?>) {
            ObjectAction<?> act = (ObjectAction<?>)obj;
            if (act.subscribe) {
                doSubscribe(act);
            } else {
                doUnsubscribe(act.oid, act.target);
            }

        } else if (obj instanceof CompoundDownstreamMessage) {
            for (DownstreamMessage submsg : ((CompoundDownstreamMessage)obj).msgs) {
                dispatchAction(submsg);
            }
        } else {
            log.warning("Unknown action", "action", obj);
        }
    }

    /**
     * Called when the client is cleaned up due to having disconnected from the server.
     */
    public void cleanup ()
    {
        // tell any pending object subscribers that they're not getting their bits
        for (PendingRequest<?> req : _penders.values()) {
            for (Subscriber<?> sub : req.targets) {
                sub.requestFailed(req.oid, new ObjectAccessException("Client connection closed"));
            }
        }
        _penders.clear();
        _flusher.cancel();
        _flushes.clear();
        _dead.clear();
        _client.getRunQueue().postRunnable(new Runnable() {
            public void run () {
                _ocache.clear();
            }
        });
    }

    protected <T extends DObject> void queueAction (int oid, Subscriber<T> target, boolean subscribe)
    {
        if (_client.getRunQueue().isRunning()) {
            // queue up an action
            _actions.append(new ObjectAction<T>(oid, target, subscribe));
            // and queue up the omgr to get invoked on the invoker thread
            _client.getRunQueue().postRunnable(this);
        } else {
            log.info("Dropping subscribe action as RunQueue is stopped",
                     "oid", oid, "subscribe", subscribe);
        }
    }

    /**
     * Called when a new event arrives from the server that should be dispatched to subscribers
     * here on the client.
     */
    protected void dispatchEvent (DEvent event)
    {
//         Log.info("Dispatching event: " + evt);

        // look up the object on which we're dispatching this event
        int remoteOid = event.getTargetOid();
        DObject target = _ocache.get(remoteOid);
        if (target == null) {
            if (!_dead.containsKey(remoteOid)) {
                log.warning("Unable to dispatch event on non-proxied object " + event + ".");
            }
            return;
        }

        // because we might be acting as a proxy for a remote server, we may need to fiddle with
        // this event before we dispatch it
        _client.convertFromRemote(target, event);

        // if this is a compound event, we need to process its contained events in order
        if (event instanceof CompoundEvent) {
            // notify our proxy subscribers in one fell swoop
            target.notifyProxies(event);

            // now break the event up and dispatch each event to listeners individually
            List<DEvent> events = ((CompoundEvent)event).getEvents();
            int ecount = events.size();
            for (int ii = 0; ii < ecount; ii++) {
                dispatchEvent(remoteOid, target, events.get(ii));
            }

        } else {
            // forward to any proxies (or not if we're dispatching part of a compound event)
            target.notifyProxies(event);

            // and dispatch the event to regular listeners
            dispatchEvent(remoteOid, target, event);
        }
    }

    /**
     * Dispatches an event on an already resolved target object.
     *
     * @param remoteOid is specified explicitly because we will have already translated the event's
     * target oid into our local object managers oid space if we're acting on behalf of the peer
     * manager.
     */
    protected void dispatchEvent (int remoteOid, DObject target, DEvent event)
    {
        try {
            // apply the event to the object
            boolean notify = event.applyToObject(target);

            // if this is an object destroyed event, we need to remove the object from our table
            if (event instanceof ObjectDestroyedEvent) {
//                 Log.info("Pitching destroyed object [oid=" + remoteOid +
//                          ", class=" + StringUtil.shortClassName(target) + "].");
                _ocache.remove(remoteOid);
            }

            // have the object pass this event on to its listeners
            if (notify) {
                target.notifyListeners(event);
            }

        } catch (Exception e) {
            log.warning("Failure processing event", "event", event, "target", target, e);
        }
    }

    /**
     * Registers this object in our proxy cache and notifies the subscribers that were waiting for
     * subscription to this object.
     */
    protected <T extends DObject> void registerObjectAndNotify (ObjectResponse<T> orsp)
    {
        // let the object know that we'll be managing it
        T obj = orsp.getObject();
        obj.setManager(this);

        // stick the object into the proxy object table
        _ocache.put(obj.getOid(), obj);

        // let the penders know that the object is available
        PendingRequest<?> req = _penders.remove(obj.getOid());
        if (req == null) {
            log.warning("Got object, but no one cares?!", "oid", obj.getOid(), "obj", obj);
            return;
        }

        for (int ii = 0; ii < req.targets.size(); ii++) {
            @SuppressWarnings("unchecked") Subscriber<T> target = (Subscriber<T>)req.targets.get(ii);
            // add them as a subscriber
            obj.addSubscriber(target);
            // and let them know that the object is in
            target.objectAvailable(obj);
        }
    }

    /**
     * Notifies the subscribers that had requested this object (for subscription) that it is not
     * available.
     */
    protected void notifyFailure (int oid, String message)
    {
        // let the penders know that the object is not available
        PendingRequest<?> req = _penders.remove(oid);
        if (req == null) {
            log.warning("Failed to get object, but no one cares?!", "oid", oid);
            return;
        }

        for (int ii = 0; ii < req.targets.size(); ii++) {
            req.targets.get(ii).requestFailed(oid, new ObjectAccessException(message));
        }
    }

    /**
     * This is guaranteed to be invoked via the invoker and can safely do main thread type things
     * like call back to the subscriber.
     */
    protected <T extends DObject> void doSubscribe (ObjectAction<T> action)
    {
        // Log.info("doSubscribe: " + oid + ": " + target);

        int oid = action.oid;
        Subscriber<T> target = action.target;

        // first see if we've already got the object in our table
        @SuppressWarnings("unchecked") T obj = (T)_ocache.get(oid);
        if (obj != null) {
            // clear the object out of the flush table if it's in there
            if (_flushes.remove(oid) != null) {
//                 Log.info("Resurrected " + oid + ".");
            }
            // add the subscriber and call them back straight away
            obj.addSubscriber(target);
            target.objectAvailable(obj);
            return;
        }

        // see if we've already got an outstanding request for this object
        @SuppressWarnings("unchecked") PendingRequest<T> req = (PendingRequest<T>)_penders.get(oid);
        if (req != null) {
            // add this subscriber to the list to be notified when the request is satisfied
            req.addTarget(target);
            return;
        }

        // otherwise we need to create a new request
        req = new PendingRequest<T>(oid);
        req.addTarget(target);
        _penders.put(oid, req);
        // Log.info("Registering pending request [oid=" + oid + "].");

        // and issue a request to get things rolling
        _comm.postMessage(new SubscribeRequest(oid));
    }

    /**
     * This is guaranteed to be invoked via the invoker and can safely do main thread type things
     * like call back to the subscriber.
     */
    protected void doUnsubscribe (int oid, Subscriber<?> target)
    {
        DObject dobj = _ocache.get(oid);
        if (dobj != null) {
            dobj.removeSubscriber(target);

        } else if (_client.isFailureLoggable(Client.FailureType.UNSUBSCRIBE_NOT_PROXIED)) {
            log.info("Requested to remove subscriber from non-proxied object", "oid", oid,
                     "sub", target);
        }
    }

    /**
     * Flushes a distributed object subscription, issuing an unsubscribe request to the server.
     */
    protected void flushObject (DObject obj)
    {
        // move this object into the dead pool so that we don't claim to have it around anymore;
        // once our unsubscribe message is processed, it'll be 86ed
        int ooid = obj.getOid();
        _ocache.remove(ooid);
        _dead.put(ooid, obj);

        // ship off an unsubscribe message to the server; we'll remove the object from our table
        // when we get the unsub ack
        _comm.postMessage(new UnsubscribeRequest(ooid));
    }

    /**
     * Called periodically to flush any objects that have been lingering due to a previously
     * enacted flush delay.
     */
    protected void flushObjects ()
    {
        long now = System.currentTimeMillis();
        for (Iterator<IntMap.IntEntry<FlushRecord>> iter = _flushes.intEntrySet().iterator();
             iter.hasNext(); ) {
            IntMap.IntEntry<FlushRecord> entry = iter.next();
//             int oid = entry.getIntKey();
            FlushRecord rec = entry.getValue();
            if (rec.expire <= now) {
                iter.remove();
                flushObject(rec.object);
//                 Log.info("Flushed object " + oid + ".");
            }
        }
    }

    /**
     * The object action is used to queue up a subscribe or unsubscribe request.
     */
    protected static final class ObjectAction<T extends DObject>
    {
        public int oid;
        public Subscriber<T> target;
        public boolean subscribe;

        public ObjectAction (int oid, Subscriber<T> target, boolean subscribe)
        {
            this.oid = oid;
            this.target = target;
            this.subscribe = subscribe;
        }

        @Override
        public String toString ()
        {
            return StringUtil.fieldsToString(this);
        }
    }

    /** Represents a pending subscription request. */
    protected static final class PendingRequest<T extends DObject>
    {
        public int oid;
        public ArrayList<Subscriber<T>> targets = Lists.newArrayList();

        public PendingRequest (int oid)
        {
            this.oid = oid;
        }

        public void addTarget (Subscriber<T> target)
        {
            targets.add(target);
        }
    }

    /** Used to manage pending object flushes. */
    protected static final class FlushRecord
    {
        /** The object to be flushed. */
        public DObject object;

        /** The time at which we flush it. */
        public long expire;

        public FlushRecord (DObject object, long expire)
        {
            this.object = object;
            this.expire = expire;
        }
    }

    /** A reference to the communicator that sends and receives messages for this client. */
    protected Communicator _comm;

    /** A reference to our client instance. */
    protected Client _client;

    /** Periodically calls {@link #flushObject}. */
    protected Interval _flusher;

    /** Our primary dispatch queue. */
    protected Queue<Object> _actions = new Queue<Object>();

    /** All of the distributed objects that are active on this client. */
    protected HashIntMap<DObject> _ocache = new HashIntMap<DObject>();

    /** Objects that have been marked for death. */
    protected HashIntMap<DObject> _dead = new HashIntMap<DObject>();

    /** Pending object subscriptions. */
    protected HashIntMap<PendingRequest<?>> _penders = new HashIntMap<PendingRequest<?>>();

    /** A mapping from distributed object class to flush delay. */
    protected HashMap<Class<?>, Long> _delays = Maps.newHashMap();

    /** A set of objects waiting to be flushed. */
    protected HashIntMap<FlushRecord> _flushes = new HashIntMap<FlushRecord>();

    /** The modifiers for our dump table debug hook (Alt+Shift). */
    protected static int DUMP_OTABLE_MODMASK = KeyEvent.ALT_DOWN_MASK|KeyEvent.SHIFT_DOWN_MASK;

    /** The key code for our dump table debug hook (o). */
    protected static int DUMP_OTABLE_KEYCODE = KeyEvent.VK_O;

    /** Flush expired objects every 30 seconds. */
    protected static final long FLUSH_INTERVAL = 30 * 1000L;
}
