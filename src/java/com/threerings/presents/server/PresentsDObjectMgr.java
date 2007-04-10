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

package com.threerings.presents.server;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import sun.misc.Perf;

import com.samskivert.util.AuditLogger;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Histogram;
import com.samskivert.util.Interval;
import com.samskivert.util.Invoker;
import com.samskivert.util.Queue;
import com.samskivert.util.RunQueue;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Throttle;

import com.threerings.presents.dobj.*;

import static com.threerings.presents.Log.log;

/**
 * The presents distributed object manager implements the {@link DObjectManager} interface,
 * providing an object manager that runs on the server. By virtue of running on the server, it
 * manages its objects directly rather than managing proxies of objects which is what is done on
 * the client. Thus it simply queues up events and dispatches them to listeners.
 *
 * <p> The server object manager is meant to run on the main thread of the server application and
 * thus provides a method to be invoked by the application main thread which won't return until the
 * manager has been requested to shut down.
 */
public class PresentsDObjectMgr
    implements RootDObjectManager, RunQueue, PresentsServer.Reporter
{
    /** Contains operational statistics that are tracked by the distributed object manager between
     * {@link PresentsServer#Reporter} intervals. The snapshot for the most recently completed
     * period can be requested via {@link #getStats()}. . */
    public static class Stats
    {
        /** The largest size of the distributed object queue during the period. */
        public int maxQueueSize;

        /** The number of events dispatched during the period. */
        public int eventCount;
    }

    /** Post instances of these if you know you're going to tie up the distributed object thread
     * for a long time and don't want a spurious warning. <em>Note:</em> this should only be done
     * during server initialization. Tying up the distributed object thread for long periods of
     * time during normal operation is a very bad idea. */
    public static interface LongRunnable extends Runnable
    {
    }

    /**
     * Creates the dobjmgr and prepares it for operation.
     */
    public PresentsDObjectMgr ()
    {
        // we create a dummy object to live as oid zero and we'll use that for some internal event
        // trickery
        DObject dummy = new DObject();
        dummy.setOid(0);
        dummy.setManager(this);
        _objects.put(0, new DObject());

        // register ourselves as a state of server reporter
        PresentsServer.registerReporter(this);
    }

    /**
     * Sets up an access controller that will be provided to any distributed objects created on the
     * server. The controllers can subsequently be overridden if desired, but a default controller
     * is useful for implementing basic access control policies.
     */
    public void setDefaultAccessController (AccessController controller)
    {
        AccessController oldDefault = _defaultController;
        _defaultController = controller;

        // switch all objects from the old default (null, usually) to the new default.
        for (Iterator itr = _objects.elements(); itr.hasNext(); ) {
            DObject obj = (DObject) itr.next();
            if (oldDefault == obj.getAccessController()) {
                obj.setAccessController(controller);
            }
        }
    }

    /**
     * Registers an object managed by another distributed object manager (probably on another
     * server). The local server will assign the object a proxy oid, and any events that come in on
     * this object will be rewritten from their proxy oid to their original id before forwarding on
     * to the originating object manager.
     */
    public void registerProxyObject (DObject object, DObjectManager omgr)
    {
        int origObjectId = object.getOid();
        // register the object locally which will reassign its oid and set us as its manager
        registerObject(object);
        // and note a proxy reference for the object which we'll use to forward events back to its
        // originating manager after converting them back to the original oid
        _proxies.put(object.getOid(), new ProxyReference(origObjectId, omgr));
    }

    /**
     * Clears a proxy object reference from our local distributed object space. This merely removes
     * it from our internal tables, the caller is responsible for coordinating the deregistration
     * of the object with the proxying client.
     */
    public void clearProxyObject (int origObjectId, DObject object)
    {
        if (_proxies.remove(object.getOid()) == null) {
            log.warning("Missing proxy mapping for cleared proxy [ooid=" + origObjectId + "].");
        }
        _objects.remove(object.getOid());
    }

    // from interface DObjectManager
    public boolean isManager (DObject object)
    {
        // we are always authoritative in the present implementation
        return true;
    }

    // from interface DObjectManager
    public <T extends DObject> void subscribeToObject (int oid, Subscriber<T> target)
    {
        if (oid <= 0) {
            target.requestFailed(oid, new ObjectAccessException("Invalid oid " + oid + "."));
        } else {
            // queue up an access object event
            postEvent(new AccessObjectEvent<T>(oid, target, AccessObjectEvent.SUBSCRIBE));
        }
    }

    // from interface DObjectManager
    public <T extends DObject> void unsubscribeFromObject (int oid, Subscriber<T> target)
    {
        // queue up an access object event
        postEvent(new AccessObjectEvent<T>(oid, target, AccessObjectEvent.UNSUBSCRIBE));
    }

    // from interface DObjectManager
    public void postEvent (DEvent event)
    {
        // just append it to the queue
        _evqueue.append(event);
    }

    // from interface DObjectManager
    public void removedLastSubscriber (DObject obj, boolean deathWish)
    {
        // destroy the object if it so desires
        if (deathWish) {
            destroyObject(obj.getOid());
        }
    }

    // from interface RootDObjectManager
    public <T extends DObject> T registerObject (T object)
    {
        int oid = getNextOid();

        // initialize this object
        object.setOid(oid);
        object.setManager(PresentsDObjectMgr.this);
        object.setAccessController(_defaultController);

        // insert it into the table
        _objects.put(oid, object);

//         log.info("Registered object [obj=" + object + "].");

        return object;
    }

    // from interface RootDObjectManager
    public void destroyObject (int oid)
    {
        // queue up an object destroyed event
        postEvent(new ObjectDestroyedEvent(oid));
    }

    /**
     * Returns the object in the object table with the specified oid or null if no object has that
     * oid. Be sure only to call this function from the dobjmgr thread and not to do anything funny
     * with the object. If subscription is desired, use {@link #subscribeToObject}.
     */
    public DObject getObject (int oid)
    {
        return _objects.get(oid);
    }

    /**
     * Returns the runtime statistics for the most recently completed reporting period.
     */
    public Stats getStats ()
    {
        return _recent;
    }

    /**
     * Posts a self-contained unit of code that should be run on the distributed object manager
     * thread at the next available opportunity. The code will be queued up with the rest of the
     * events and invoked in turn. Like event processing code, the code should not take long to
     * complete and should <em>definitely</em> not block.
     *
     * From interface RunQueue
     */
    public void postRunnable (Runnable unit)
    {
        // just append it to the queue
        _evqueue.append(unit);
    }

    /**
     * Returns true if the thread invoking this method is the same thread that is doing distributed
     * object event dispatch. Code that wishes to enforce that it is either always or never called
     * on the event dispatch thread will want to make use of this method.
     *
     * From interface RunQueue
     */
    public synchronized boolean isDispatchThread ()
    {
        return Thread.currentThread() == _dobjThread;
    }

    /**
     * Runs the dobjmgr event loop until it is requested to exit. This should be called from the
     * main application thread.
     */
    public void run ()
    {
        log.info("DOMGR running.");

        // make a note of the thread that's processing events
        synchronized (this) {
            _dobjThread = Thread.currentThread();
        }

        while (isRunning()) {
            // pop the next unit off the queue and process it
            processUnit(_evqueue.get());
        }

        log.info("DOMGR exited.");
    }

    /**
     * Processes a single unit from the queue.
     */
    protected void processUnit (Object unit)
    {
        long start = _timer.highResCounter();
        long freq = _timer.highResFrequency();

        // keep track of the largest queue size we've seen
        int queueSize = _evqueue.size();
        if (queueSize > _current.maxQueueSize) {
            _current.maxQueueSize = queueSize;
        }

        try {
            if (unit instanceof Runnable) {
                // if this is a runnable, it's just an executable unit that should be invoked
                ((Runnable)unit).run();

            } else if (unit instanceof CompoundEvent) {
                CompoundEvent event = (CompoundEvent)unit;

                // if this event is on a proxied object, forward it to the owning manager
                ProxyReference proxy = _proxies.get(event.getTargetOid());
                if (proxy != null) {
                    // rewrite the oid into the originating manager's id space
                    event.setTargetOid(proxy.origObjectId);
                    List<DEvent> events = event.getEvents();
                    for (int ii = 0, ll = events.size(); ii < ll; ii++) {
                        events.get(ii).setTargetOid(proxy.origObjectId);
                    }
                    // then pass it on to the originating manager to handle
                    proxy.origManager.postEvent(event);

                } else {
                    processCompoundEvent(event);
                }

            } else {
                DEvent event = (DEvent)unit;

                // if this event is on a proxied object, forward it to the owning manager
                ProxyReference proxy = _proxies.get(event.getTargetOid());
                if (proxy != null) {
                    // rewrite the oid into the originating manager's id space
                    event.setTargetOid(proxy.origObjectId);
                    // then pass it on to the originating manager to handle
                    proxy.origManager.postEvent(event);

                } else {
                    processEvent(event);
                }
            }

        } catch (VirtualMachineError e) {
            handleFatalError(unit, e);

        } catch (Throwable t) {
            log.log(Level.WARNING,
                    "Execution unit failed [unit=" + StringUtil.safeToString(unit) + "].", t);
        }

        // compute the elapsed time in microseconds
        long elapsed = _timer.highResCounter() - start;
        elapsed = elapsed * 1000000 / freq;

        // report excessively long units
        if (elapsed > 500000 && !(unit instanceof LongRunnable)) {
            log.warning("Long dobj unit [u=" + StringUtil.safeToString(unit) +
                        " (" + StringUtil.shortClassName(unit) + ")" +
                        ", time=" + (elapsed/1000) + "ms].");
        }

        // periodically sample and record the time spent processing a unit
        if (UNIT_PROF_ENABLED && _eventCount % UNIT_PROF_INTERVAL == 0) {
            String cname;
            // do some jiggery pokery to get more fine grained profiling details on certain
            // "popular" unit types
            if (unit instanceof Interval.RunBuddy) {
                Interval ival = ((Interval.RunBuddy)unit).getInterval();
                cname = StringUtil.shortClassName(ival);
            } else if (unit instanceof InvocationRequestEvent) {
                InvocationRequestEvent ire = (InvocationRequestEvent)unit;
                Class c = PresentsServer.invmgr.getDispatcherClass(ire.getInvCode());
                cname = (c == null) ? "dobj.InvocationRequestEvent:(no longer registered)" :
                    StringUtil.shortClassName(c) + ":" + ire.getMethodId();
            } else {
                cname = StringUtil.shortClassName(unit);
            }
            UnitProfile uprof = _profiles.get(cname);
            if (uprof == null) {
                _profiles.put(cname, uprof = new UnitProfile());
            }
            uprof.record(start, elapsed);
        }
    }

    /**
     * Performs the processing associated with a compound event, notifying listeners and the like.
     */
    protected void processCompoundEvent (CompoundEvent event)
    {
        List events = event.getEvents();
        int ecount = events.size();

        // look up the target object
        DObject target = _objects.get(event.getTargetOid());
        if (target == null) {
            log.fine("Compound event target no longer exists [event=" + event + "].");
            return;
        }

        // check the permissions on all of the events
        for (int ii = 0; ii < ecount; ii++) {
            DEvent sevent = (DEvent)events.get(ii);
            if (!target.checkPermissions(sevent)) {
                log.warning("Event failed permissions check [event=" + sevent +
                            ", target=" + target + "].");
                return;
            }
        }

        // dispatch the events
        for (int ii = 0; ii < ecount; ii++) {
            dispatchEvent((DEvent)events.get(ii), target);
        }

        // always notify proxies of compound events
        target.notifyProxies(event);
    }

    /**
     * Performs the processing associated with an event, notifying listeners and the like.
     */
    protected void processEvent (DEvent event)
    {
        // look up the target object
        DObject target = _objects.get(event.getTargetOid());
        if (target == null) {
            log.fine("Event target no longer exists [event=" + event + "].");
            return;
        }

        // check the event's permissions
        if (!target.checkPermissions(event)) {
            log.warning("Event failed permissions check [event=" + event +
                        ", target=" + target + "].");
            return;
        }

        if (dispatchEvent(event, target)) {
            // unless requested not to, notify any proxies
            target.notifyProxies(event);
        }
    }

    /**
     * Dispatches an event after the target object has been resolved and the permissions have been
     * checked. This is used by {@link #processEvent} and {@link #processCompoundEvent}.
     *
     * @return the value returned by {@link DEvent#applyToObject}.
     */
    protected boolean dispatchEvent (DEvent event, DObject target)
    {
        boolean notify = true; // assume always notify
        try {
            // do any internal management necessary based on this event
            Method helper = _helpers.get(event.getClass());
            if (helper != null) {
                // invoke the helper method
                Object rv = helper.invoke(this, new Object[] { event, target });
                // if helper returns false, we abort event processing
                if (!((Boolean)rv).booleanValue()) {
                    return false;
                }
            }

            // everything's good so far, apply the event to the object
            notify = event.applyToObject(target);

            // if the event returns false from applyToObject, this means it's a silent event and we
            // shouldn't notify the listeners
            if (notify) {
                target.notifyListeners(event);
            }

        } catch (VirtualMachineError e) {
            handleFatalError(event, e);

        } catch (Throwable t) {
            log.log(Level.WARNING, "Failure processing event [event=" + event +
                    ", target=" + target + "].", t);
        }

        // track the number of events dispatched
        ++_eventCount;
        ++_current.eventCount;
        return true;
    }

    /**
     * Attempts to recover from fatal errors but rethrows if things are freaking out too
     * frequently.
     */
    protected void handleFatalError (Object causer, Error error)
    {
        if (_fatalThrottle.throttleOp()) {
            throw error;
        }
        log.log(Level.WARNING, "Fatal error caused by '" + causer + "': " + error, error);
    }

    /**
     * Requests that the dobjmgr shut itself down soon- you may want to try using {@link
     * Invoker#shutdown} which will make sure that both the Invoker and DObjectMgr are empty and
     * then shut them both down.
     */
    public void harshShutdown ()
    {
        postRunnable(new Runnable() {
            public void run () {
                _running = false;
            }
        });
    }

    /**
     * Dumps collected profiling information to the system log.
     */
    public void dumpUnitProfiles ()
    {
        for (Map.Entry<String,UnitProfile> entry : _profiles.entrySet()) {
            log.info("P: " + entry.getKey() + " => " + entry.getValue());
        }
    }

    /**
     * Called as a helper for <code>ObjectDestroyedEvent</code> events. It removes the object from
     * the object table.
     *
     * @return true if the event should be dispatched, false if it should be aborted.
     */
    public boolean objectDestroyed (DEvent event, DObject target)
    {
        int oid = target.getOid();

//         log.info("Removing destroyed object from table [oid=" + oid + "].");

        // remove the object from the table
        _objects.remove(oid);

        // inactivate the object
        target.setManager(null);

        // deal with any remaining oid lists that reference this object
        Reference[] refs = _refs.remove(oid);
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                // skip empty spots
                if (refs[i] == null) {
                    continue;
                }

                Reference ref = refs[i];
                DObject reffer = _objects.get(ref.reffingOid);

                // ensure that the referencing object is still around
                if (reffer != null) {
                    // post an object removed event to clear the reference
                    postEvent(new ObjectRemovedEvent(ref.reffingOid, ref.field, oid));
//                     log.info("Forcing removal " + ref + ".");

                } else {
                    log.info("Dangling reference from inactive object " + ref + ".");
                }
            }
        }

        // if this object has any oid list fields that are still referencing other objects, we need
        // to clear out those references
        Class oclass = target.getClass();
        Field[] fields = oclass.getFields();
        for (int f = 0; f < fields.length; f++) {
            Field field = fields[f];

            // ignore static and non-public fields
            int mods = field.getModifiers();
            if ((mods & Modifier.STATIC) != 0 || (mods & Modifier.PUBLIC) == 0) {
                continue;
            }

            // ignore non-oidlist fields
            if (!OidList.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                OidList list = (OidList)field.get(target);
                for (int i = 0; i < list.size(); i++) {
                    clearReference(target, field.getName(), list.get(i));
                }

            } catch (Exception e) {
                log.warning("Unable to clean up after oid list field [target=" + target +
                            ", field=" + field + "].");
            }
        }

        return true;
    }

    /**
     * Called by <code>objectDestroyed</code>; clears out the tracking info for a reference by the
     * supplied object to the specified oid via the specified field.
     */
    protected void clearReference (DObject reffer, String field, int reffedOid)
    {
        // look up the reference vector for the referenced object
        Reference[] refs = _refs.get(reffedOid);
        Reference ref = null;

        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                if (refs[i].equals(reffer.getOid(), field)) {
                    ref = refs[i];
                    refs[i] = null;
                    break;
                }
            }
        }

        // if a referred object and referring object are both destroyed without allowing the
        // referred object destruction to process the ObjectRemoved event which is auto-generated,
        // the subsequent destruction of the referring object will attempt to clear the reference
        // to the referred object which no longer exists; so we don't complain about non- existent
        // references if the referree is already destroyed
        if (ref == null && _objects.containsKey(reffedOid)) {
            log.warning("Requested to clear out non-existent reference " +
                        "[refferOid=" + reffer.getOid() + ", field=" + field +
                        ", reffedOid=" + reffedOid + "].");

//        } else {
//            log.info("Cleared out reference " + ref + ".");
        }
    }

    /**
     * Called as a helper for <code>ObjectAddedEvent</code> events. It updates the object/oid list
     * tracking structures.
     *
     * @return true if the event should be dispatched, false if it should be aborted.
     */
    public boolean objectAdded (DEvent event, DObject target)
    {
        ObjectAddedEvent oae = (ObjectAddedEvent)event;
        int oid = oae.getOid();

        // ensure that the target object exists
        if (!_objects.containsKey(oid)) {
            log.info("Rejecting object added event of non-existent object " +
                     "[refferOid=" + target.getOid() + ", reffedOid=" + oid + "].");
            return false;
        }

        // get the reference vector for the referenced object. we use bare arrays rather than
        // something like an array list to conserve memory. there will be many objects and
        // references
        Reference[] refs = _refs.get(oid);
        if (refs == null) {
            refs = new Reference[DEFREFVEC_SIZE];
            _refs.put(oid, refs);
        }

        // determine where to add the reference
        Reference ref = new Reference(target.getOid(), oae.getName(), oid);
        int rpos = -1;
        for (int i = 0; i < refs.length; i++) {
            if (ref.equals(refs[i])) {
                log.warning("Ignoring request to track existing reference " + ref + ".");
                return true;
            } else if (refs[i] == null && rpos == -1) {
                rpos = i;
            }
        }

        // expand the refvec if necessary
        if (rpos == -1) {
            Reference[] nrefs = new Reference[refs.length*2];
            System.arraycopy(refs, 0, nrefs, 0, refs.length);
            rpos = refs.length;
            _refs.put(oid, refs = nrefs);
        }

        // finally add the reference
        refs[rpos] = ref;

//        log.info("Tracked reference " + ref + ".");
        return true;
    }

    /**
     * Called as a helper for <code>ObjectRemovedEvent</code> events. It updates the object/oid
     * list tracking structures.
     *
     * @return true if the event should be dispatched, false if it should be aborted.
     */
    public boolean objectRemoved (DEvent event, DObject target)
    {
        ObjectRemovedEvent ore = (ObjectRemovedEvent)event;
        String field = ore.getName();
        int toid = target.getOid();
        int oid = ore.getOid();

//        log.info("Processing object removed [from=" + toid + ", roid=" + toid + "].");

        // get the reference vector for the referenced object
        Reference[] refs = _refs.get(oid);
        if (refs == null) {
            // this can happen normally when an object is destroyed. it will remove itself from the
            // reference system and then generate object removed events for all of its referencees.
            // so we opt not to log anything in this case

//             log.info("Object removed without reference to track it [toid=" + toid +
//                      ", field=" + field + ", oid=" + oid + "].");
            return true;
        }

        // look for the matching reference
        for (int i = 0; i < refs.length; i++) {
            Reference ref = refs[i];
            if (ref != null && ref.equals(toid, field)) {
//                log.info("Removed reference " + refs[i] + ".");
                refs[i] = null;
                return true;
            }
        }

        log.warning("Unable to locate reference for removal [reffingOid=" + toid +
                    ", field=" + field + ", reffedOid=" + oid + "].");
        return true;
    }

    /**
     * Should not need to be called except by the invoker during shutdown to ensure that things are
     * proceeding smoothly.
     */
    public boolean queueIsEmpty ()
    {
        return !_evqueue.hasElements();
    }

    protected synchronized boolean isRunning ()
    {
        return _running;
    }

    protected int getNextOid ()
    {
        // look for the next unused oid. in theory if we had two billion objects, this would loop
        // infinitely, but the world will come to an end long before we have two billion objects
        do {
            _nextOid = (_nextOid + 1) % Integer.MAX_VALUE;
        } while (_objects.containsKey(_nextOid));
        return _nextOid;
    }

    // from interface PresentsServer.Reporter
    public void appendReport (StringBuilder report, long now, long sinceLast, boolean reset)
    {
        report.append("* presents.PresentsDObjectMgr:\n");
        int queueSize = _evqueue.size();
        report.append("- Queue size: ").append(queueSize).append("\n");
        report.append("- Max queue size: ").append(_current.maxQueueSize).append("\n");
        report.append("- Units executed: ").append(_current.eventCount).append("\n");

        if (UNIT_PROF_ENABLED) {
            report.append("- Unit profiles: ").append(_profiles.size()).append("\n");
            for (Map.Entry<String,UnitProfile> entry : _profiles.entrySet()) {
                report.append("  ").append(entry.getKey());
                report.append(" ").append(entry.getValue()).append("\n");
            }
        }

        // roll over stats
        if (reset) {
            _recent = _current;
            _current = new Stats();
            _current.maxQueueSize = queueSize;
        }
    }

    /**
     * Calls {@link Subscriber#objectAvailable} and catches and logs any exception thrown by the
     * subscriber during the call.
     */
    protected static <T extends DObject> void informObjectAvailable (Subscriber<T> sub, T obj)
    {
        try {
            sub.objectAvailable(obj);
        } catch (Exception e) {
            log.log(Level.WARNING, "Subscriber choked during object available " +
                    "[obj=" + StringUtil.safeToString(obj) + ", sub=" + sub + "].", e);
        }
    }

    /**
     * Used to make an object available to a subscriber (with or without the associated
     * subscription).
     */
    protected class AccessObjectEvent<T extends DObject> extends DEvent
    {
        public static final int SUBSCRIBE = 0;
        public static final int UNSUBSCRIBE = 1;

        public AccessObjectEvent (int oid, Subscriber<T> target, int action)
        {
            super(0); // target the bogus object
            _oid = oid;
            _target = target;
            _action = action;
        }

        public boolean isPrivate ()
        {
            return true;
        }

        public boolean applyToObject (DObject target)
            throws ObjectAccessException
        {
            // look up the target object
            @SuppressWarnings("unchecked") T obj = (T)_objects.get(_oid);

            // if we're unsubscribing, take care of that and get on out
            if (_action == UNSUBSCRIBE) {
                if (obj != null) {
                    obj.removeSubscriber(_target);
                }
                return false;
            }

            // if it don't exist, let them know
            if (obj == null) {
                _target.requestFailed(_oid, new NoSuchObjectException(_oid));
                return false;
            }

            // check permissions
            if (!obj.checkPermissions(_target)) {
                String errmsg = "m.access_denied\t" + _oid;
                _target.requestFailed(_oid, new ObjectAccessException(errmsg));
                return false;
            }

            // subscribe 'em
            obj.addSubscriber(_target);

            // let them know that things are groovy
            informObjectAvailable(_target, obj);

            // return false to ensure that this event is not dispatched to the fake object's
            // subscriber list (even though it's empty)
            return false;
        }

        protected int _oid;
        protected Subscriber<T> _target;
        protected int _action;
    }

    /**
     * Registers our event helper methods.
     */
    protected static void registerEventHelpers ()
    {
        Class[] ptypes = new Class[] { DEvent.class, DObject.class };
        Class omgrcl = PresentsDObjectMgr.class;
        Method method;

        try {
            method = omgrcl.getMethod("objectDestroyed", ptypes);
            _helpers.put(ObjectDestroyedEvent.class, method);

            method = omgrcl.getMethod("objectAdded", ptypes);
            _helpers.put(ObjectAddedEvent.class, method);

            method = omgrcl.getMethod("objectRemoved", ptypes);
            _helpers.put(ObjectRemovedEvent.class, method);

        } catch (Exception e) {
            log.warning("Unable to register event helpers [error=" + e + "].");
        }
    }

    /**
     * Used to track references of objects in oid lists.
     */
    protected static class Reference
    {
        public int reffingOid;
        public String field;
        public int reffedOid;

        public Reference (int reffingOid, String field, int reffedOid)
        {
            this.reffingOid = reffingOid;
            this.field = field;
            this.reffedOid = reffedOid;
        }

        public boolean equals (Reference other)
        {
            if (other == null) {
                return false;
            } else {
                return (reffingOid == other.reffingOid && field.equals(other.field));
            }
        }

        public boolean equals (int reffingOid, String field)
        {
            return (this.reffingOid == reffingOid && this.field.equals(field));
        }

        public String toString ()
        {
            return "[reffingOid=" + reffingOid + ", field=" + field +
                ", reffedOid=" + reffedOid + "]";
        }
    }

    /** Used to profile time spent invoking units and processing events if such profiling is
     * enabled. */
    protected static class UnitProfile
    {
        public void record (long start, long elapsed)
        {
            _totalElapsed += elapsed;
            _histo.addValue((int)elapsed);
        }

        public String toString ()
        {
            int count = _histo.size();
            return _totalElapsed + "us/" + count + " = " + (_totalElapsed/count) + "us avg " +
                StringUtil.toString(_histo.getBuckets());
        }

        protected long _totalElapsed;
        protected Histogram _histo = new Histogram(0, 20000, 10);
    }

    /** Tracks necessary information on a proxy distributed object. */
    protected static class ProxyReference
    {
        public int origObjectId;
        public DObjectManager origManager;

        public ProxyReference (int origObjectId, DObjectManager origManager)
        {
            this.origObjectId = origObjectId;
            this.origManager = origManager;
        }
    }

    /** A flag indicating that the event dispatcher is still running. */
    protected boolean _running = true;

    /** The event queue via which all events are processed. */
    protected Queue<Object> _evqueue = new Queue<Object>();

    /** The managed distributed objects table. */
    protected HashIntMap<DObject> _objects = new HashIntMap<DObject>();

    /** Used to assign a unique oid to each distributed object. */
    protected int _nextOid = 0;

    /** Used to track the number of events dispatched over time. */
    protected long _eventCount = 0;

    /** Track fatal errors so that we can stick a fork in ourselves if things get too far out of
     * hand. More than 30 fatal errors in the span of a minute and we throw in the towel. */
    protected Throttle _fatalThrottle = new Throttle(30, 60*1000L);

    /** Used to track oid list references of distributed objects. */
    protected HashIntMap<Reference[]> _refs = new HashIntMap<Reference[]>();

    /** The default access controller to use when creating distributed objects. */
    protected AccessController _defaultController;

    /** Maintains proxy information for any proxied distributed objects. */
    protected HashIntMap<ProxyReference> _proxies = new HashIntMap<ProxyReference>();

    /** We keep track of which thread is executing the event loop so that other services can
     * enforce restrictions on code that should or should not be called from the event dispatch
     * thread. */
    protected Thread _dobjThread;

    /** Used during unit profiling for timing values. */
    protected Perf _timer = Perf.getPerf();

    /** Used to profile our events and runnable units. */
    protected HashMap<String,UnitProfile> _profiles = new HashMap<String,UnitProfile>();

    /** Used to track runtime statistics. */
    protected Stats _recent = new Stats(), _current = _recent;

    /** Whether or not unit profiling is enabled. */
    protected static final boolean UNIT_PROF_ENABLED = false;

    /** The frequency with which we take a profiling sample. */
    protected static final int UNIT_PROF_INTERVAL = 100;

    /** The default size of an oid list refs vector. */
    protected static final int DEFREFVEC_SIZE = 4;

    /** This table maps event classes to helper methods that perform some additional processing for
     * particular events. */
    protected static HashMap<Class,Method> _helpers = new HashMap<Class,Method>();
    static { registerEventHelpers(); }
}
