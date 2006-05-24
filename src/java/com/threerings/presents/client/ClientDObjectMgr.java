//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.client;

import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.DebugChords;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Queue;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Interval;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;
import com.threerings.presents.net.*;

/**
 * The client distributed object manager manages a set of proxy objects
 * which mirror the distributed objects maintained on the server.
 * Requests for modifications, etc. are forwarded to the server and events
 * are dispatched from the server to this client for objects to which this
 * client is subscribed.
 */
public class ClientDObjectMgr
    implements DObjectManager, Runnable
{
    /**
     * Constructs a client distributed object manager.
     *
     * @param comm a communicator instance by which it can communicate
     * with the server.
     * @param client a reference to the client that is managing this whole
     * communications and event dispatch business.
     */
    public ClientDObjectMgr (Communicator comm, Client client)
    {
        _comm = comm;
        _client = client;

        // register a debug hook for dumping all objects in the
        // distributed object table
        DebugChords.registerHook(
            DUMP_OTABLE_MODMASK, DUMP_OTABLE_KEYCODE, DUMP_OTABLE_HOOK);

        // register a flush interval
        new Interval(client.getRunQueue()) {
            public void expired () {
                flushObjects();
            }
        }.schedule(FLUSH_INTERVAL, true);
    }

    // documentation inherited from interface
    public boolean isManager (DObject object)
    {
        // we are never authoritative in the present implementation
        return false;
    }

    // inherit documentation from the interface
    public void createObject (Class dclass, Subscriber target)
    {
        // not presently supported
        throw new RuntimeException("createObject() not supported");
    }

    // inherit documentation from the interface
    public void subscribeToObject (int oid, Subscriber target)
    {
        if (oid <= 0) {
            target.requestFailed(
                oid, new ObjectAccessException("Invalid oid " + oid + "."));
        } else {
            queueAction(oid, target, true);
        }
    }

    // inherit documentation from the interface
    public void unsubscribeFromObject (int oid, Subscriber target)
    {
        queueAction(oid, target, false);
    }

    protected void queueAction (int oid, Subscriber target, boolean subscribe)
    {
        // queue up an action
        _actions.append(new ObjectAction(oid, target, subscribe));
        // and queue up the omgr to get invoked on the invoker thread
        _client.getRunQueue().postRunnable(this);
    }

    // inherit documentation from the interface
    public void postEvent (DEvent event)
    {
        // send a forward event request to the server
        _comm.postMessage(new ForwardEventRequest(event));
    }

    // inherit documentation from the interface
    public void destroyObject (int oid)
    {
        // forward an object destroyed event to the server
        postEvent(new ObjectDestroyedEvent(oid));
    }

    // inherit documentation from the interface
    public void removedLastSubscriber (DObject obj, boolean deathWish)
    {
        // if this object has a registered flush delay, don't can it just
        // yet, just slip it onto the flush queue
        Class oclass = obj.getClass();
        for (Iterator iter = _delays.keySet().iterator(); iter.hasNext(); ) {
            Class dclass = (Class)iter.next();
            if (dclass.isAssignableFrom(oclass)) {
                long expire =  System.currentTimeMillis() +
                    ((Long)_delays.get(dclass)).longValue();
                _flushes.put(obj.getOid(), new FlushRecord(obj, expire));
//                 Log.info("Flushing " + obj.getOid() + " at " +
//                          new java.util.Date(expire));
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
    public void registerFlushDelay (Class objclass, long delay)
    {
        _delays.put(objclass, Long.valueOf(delay));
    }

    /**
     * Called by the communicator when a downstream message arrives from
     * the network layer. We queue it up for processing and request some
     * processing time on the main thread.
     */
    public void processMessage (DownstreamMessage msg)
    {
        // append it to our queue
        _actions.append(msg);
        // and queue ourselves up to be run
        _client.getRunQueue().postRunnable(this);
    }

    /**
     * Invoked on the main client thread to process any newly arrived
     * messages that we have waiting in our queue.
     */
    public void run ()
    {
        // process the next event on our queue
        Object obj;
        if ((obj = _actions.getNonBlocking()) != null) {
            // do the proper thing depending on the object
            if (obj instanceof BootstrapNotification) {
                BootstrapData data = ((BootstrapNotification)obj).getData();
                _client.gotBootstrap(data, this);

            } else if (obj instanceof EventNotification) {
                DEvent evt = ((EventNotification)obj).getEvent();
//                 Log.info("Dispatch event: " + evt);
                dispatchEvent(evt);

            } else if (obj instanceof ObjectResponse) {
                registerObjectAndNotify(((ObjectResponse)obj).getObject());

            } else if (obj instanceof UnsubscribeResponse) {
                int oid = ((UnsubscribeResponse)obj).getOid();
                if (_dead.remove(oid) == null) {
                    Log.warning("Received unsub ACK from unknown object " +
                                "[oid=" + oid + "].");
                }

            } else if (obj instanceof FailureResponse) {
                int oid = ((FailureResponse)obj).getOid();
                notifyFailure(oid);

            } else if (obj instanceof PongResponse) {
                _client.gotPong((PongResponse)obj);

            } else if (obj instanceof ObjectAction) {
                ObjectAction act = (ObjectAction)obj;
                if (act.subscribe) {
                    doSubscribe(act.oid, act.target);
                } else {
                    doUnsubscribe(act.oid, act.target);
                }
            }
        }
    }

    /**
     * Called when a new event arrives from the server that should be
     * dispatched to subscribers here on the client.
     */
    protected void dispatchEvent (DEvent event)
    {
        // if this is a compound event, we need to process its contained
        // events in order
        if (event instanceof CompoundEvent) {
            List events = ((CompoundEvent)event).getEvents();
            int ecount = events.size();
            for (int i = 0; i < ecount; i++) {
                dispatchEvent((DEvent)events.get(i));
            }
            return;
        }

        // look up the object on which we're dispatching this event
        int toid = event.getTargetOid();
        DObject target = (DObject)_ocache.get(toid);
        if (target == null) {
            if (!_dead.containsKey(toid)) {
                Log.warning("Unable to dispatch event on non-proxied " +
                            "object [event=" + event + "].");
            }
            return;
        }

        try {
            // apply the event to the object
            boolean notify = event.applyToObject(target);

            // if this is an object destroyed event, we need to remove the
            // object from our object table
            if (event instanceof ObjectDestroyedEvent) {
//                 Log.info("Pitching destroyed object " +
//                          "[oid=" + toid + ", class=" +
//                          StringUtil.shortClassName(target) + "].");
                _ocache.remove(toid);
            }

            // have the object pass this event on to its listeners
            if (notify) {
                target.notifyListeners(event);
            }

        } catch (Exception e) {
            Log.warning("Failure processing event [event=" + event +
                        ", target=" + target + "].");
            Log.logStackTrace(e);
        }
    }

    /**
     * Registers this object in our proxy cache and notifies the
     * subscribers that were waiting for subscription to this object.
     */
    protected void registerObjectAndNotify (DObject obj)
    {
        // let the object know that we'll be managing it
        obj.setManager(this);

        // stick the object into the proxy object table
        _ocache.put(obj.getOid(), obj);

        // let the penders know that the object is available
        PendingRequest req = (PendingRequest)_penders.remove(obj.getOid());
        if (req == null) {
            Log.warning("Got object, but no one cares?! " +
                        "[oid=" + obj.getOid() + ", obj=" + obj + "].");
            return;
        }

        for (int i = 0; i < req.targets.size(); i++) {
            Subscriber target = (Subscriber)req.targets.get(i);
            // add them as a subscriber
            obj.addSubscriber(target);
            // and let them know that the object is in
            target.objectAvailable(obj);
        }
    }

    /**
     * Notifies the subscribers that had requested this object (for
     * subscription) that it is not available.
     */
    protected void notifyFailure (int oid)
    {
        // let the penders know that the object is not available
        PendingRequest req = (PendingRequest)_penders.remove(oid);
        if (req == null) {
            Log.warning("Failed to get object, but no one cares?! " +
                        "[oid=" + oid + "].");
            return;
        }

        for (int i = 0; i < req.targets.size(); i++) {
            Subscriber target = (Subscriber)req.targets.get(i);
            // and let them know that the object is in
            target.requestFailed(oid, null);
        }
    }

    /**
     * This is guaranteed to be invoked via the invoker and can safely do
     * main thread type things like call back to the subscriber.
     */
    protected void doSubscribe (int oid, Subscriber target)
    {
        // Log.info("doSubscribe: " + oid + ": " + target);

        // first see if we've already got the object in our table
        DObject obj = (DObject)_ocache.get(oid);
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
        PendingRequest req = (PendingRequest)_penders.get(oid);
        if (req != null) {
            // add this subscriber to the list of subscribers to be
            // notified when the request is satisfied
            req.addTarget(target);
            return;
        }

        // otherwise we need to create a new request
        req = new PendingRequest(oid);
        req.addTarget(target);
        _penders.put(oid, req);
        // Log.info("Registering pending request [oid=" + oid + "].");

        // and issue a request to get things rolling
        _comm.postMessage(new SubscribeRequest(oid));
    }

    /**
     * This is guaranteed to be invoked via the invoker and can safely do
     * main thread type things like call back to the subscriber.
     */
    protected void doUnsubscribe (int oid, Subscriber target)
    {
        DObject dobj = (DObject)_ocache.get(oid);
        if (dobj != null) {
            dobj.removeSubscriber(target);

        } else {
            Log.info("Requested to remove subscriber from " +
                     "non-proxied object [oid=" + oid +
                     ", sub=" + target + "].");
        }
    }

    /**
     * Flushes a distributed object subscription, issuing an unsubscribe
     * request to the server.
     */
    protected void flushObject (DObject obj)
    {
        // move this object into the dead pool so that we don't claim to
        // have it around anymore; once our unsubscribe message is
        // processed, it'll be 86ed
        int ooid = obj.getOid();
        _ocache.remove(ooid);
        _dead.put(ooid, obj);

        // ship off an unsubscribe message to the server; we'll remove the
        // object from our table when we get the unsub ack
        _comm.postMessage(new UnsubscribeRequest(ooid));
    }

    /**
     * Called periodically to flush any objects that have been lingering
     * due to a previously enacted flush delay.
     */
    protected void flushObjects ()
    {
        long now = System.currentTimeMillis();
        for (Iterator iter = _flushes.keySet().iterator(); iter.hasNext(); ) {
            int oid = ((Integer)iter.next()).intValue();
            FlushRecord rec = (FlushRecord)_flushes.get(oid);
            if (rec.expire <= now) {
                iter.remove();
                flushObject(rec.object);
//                 Log.info("Flushed object " + oid + ".");
            }
        }
    }

    /**
     * The object action is used to queue up a subscribe or unsubscribe
     * request.
     */
    protected static final class ObjectAction
    {
        public int oid;
        public Subscriber target;
        public boolean subscribe;

        public ObjectAction (int oid, Subscriber target, boolean subscribe)
        {
            this.oid = oid;
            this.target = target;
            this.subscribe = subscribe;
        }

        public String toString ()
        {
            return StringUtil.fieldsToString(this);
        }
    }

    protected static final class PendingRequest
    {
        public int oid;
        public ArrayList targets = new ArrayList();

        public PendingRequest (int oid)
        {
            this.oid = oid;
        }

        public void addTarget (Subscriber target)
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

    /** A reference to the communicator that sends and receives messages
     * for this client. */
    protected Communicator _comm;

    /** A reference to our client instance. */
    protected Client _client;

    /** Our primary dispatch queue. */
    protected Queue _actions = new Queue();

    /** All of the distributed objects that are active on this client. */
    protected HashIntMap _ocache = new HashIntMap();

    /** Objects that have been marked for death. */
    protected HashIntMap _dead = new HashIntMap();

    /** Pending object subscriptions. */
    protected HashIntMap _penders = new HashIntMap();

    /** A mapping from distributed object class to flush delay. */
    protected HashMap _delays = new HashMap();

    /** A set of objects waiting to be flushed. */
    protected HashIntMap _flushes = new HashIntMap();

    /** A debug hook that allows the dumping of all objects in the object
     * table out to the log. */
    protected DebugChords.Hook DUMP_OTABLE_HOOK = new DebugChords.Hook() {
        public void invoke () {
            Log.info("Dumping " + _ocache.size() + " objects:");
            Iterator iter = _ocache.values().iterator();
            while (iter.hasNext()) {
                DObject obj = (DObject)iter.next();
                Log.info(obj.getClass().getName() + " " + obj);
            }
        }
    };

    /** The modifiers for our dump table debug hook (Alt+Shift). */
    protected static int DUMP_OTABLE_MODMASK =
        KeyEvent.ALT_DOWN_MASK|KeyEvent.SHIFT_DOWN_MASK;

    /** The key code for our dump table debug hook (o). */
    protected static int DUMP_OTABLE_KEYCODE = KeyEvent.VK_O;

    /** Flush expired objects every 30 seconds. */
    protected static final long FLUSH_INTERVAL = 30 * 1000L;
}
