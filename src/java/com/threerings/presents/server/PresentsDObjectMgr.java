//
// $Id: PresentsDObjectMgr.java,v 1.37 2003/08/16 04:14:56 mdb Exp $

package com.threerings.presents.server;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.Histogram;
import com.samskivert.util.Queue;
import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;

/**
 * The presents distributed object manager implements the {@link
 * DObjectManager} interface, providing an object manager that runs on the
 * server. By virtue of running on the server, it manages its objects
 * directly rather than managing proxies of objects which is what is done
 * on the client. Thus it simply queues up events and dispatches them to
 * listeners.
 *
 * <p> The server object manager is meant to run on the main thread of the
 * server application and thus provides a method to be invoked by the
 * application main thread which won't return until the manager has been
 * requested to shut down.
 */
public class PresentsDObjectMgr
    implements RootDObjectManager, PresentsServer.Reporter
{
    /**
     * Creates the dobjmgr and prepares it for operation.
     */
    public PresentsDObjectMgr ()
    {
        // we create a dummy object to live as oid zero and we'll use that
        // for some internal event trickery
        DObject dummy = new DObject();
        dummy.setOid(0);
        dummy.setManager(this);
        _objects.put(0, new DObject());

        // register ourselves as a state of server reporter
        PresentsServer.registerReporter(this);
    }

    /**
     * Sets up an access controller that will be provided to any
     * distributed objects created on the server. The controllers can
     * subsequently be overridden if desired, but a default controller is
     * useful for implementing basic access control policies.
     */
    public void setDefaultAccessController (AccessController controller)
    {
        _defaultController = controller;
    }

    // documentation inherited from interface
    public boolean isManager (DObject object)
    {
        // we are always authoritative in the present implementation
        return true;
    }

    // inherit documentation from the interface
    public void createObject (Class dclass, Subscriber target)
    {
        // queue up a create object event
        postEvent(new CreateObjectEvent(dclass, target));
    }

    // inherit documentation from the interface
    public void subscribeToObject (int oid, Subscriber target)
    {
        if (oid <= 0) {
            target.requestFailed(
                oid, new ObjectAccessException("Invalid oid " + oid + "."));
        } else {
            // queue up an access object event
            postEvent(new AccessObjectEvent(
                          oid, target, AccessObjectEvent.SUBSCRIBE));
        }
    }

    // inherit documentation from the interface
    public void unsubscribeFromObject (int oid, Subscriber target)
    {
        // queue up an access object event
        postEvent(new AccessObjectEvent(oid, target,
                                        AccessObjectEvent.UNSUBSCRIBE));
    }

    // inherit documentation from the interface
    public void destroyObject (int oid)
    {
        // queue up an object destroyed event
        postEvent(new ObjectDestroyedEvent(oid));
    }

    // inherit documentation from the interface
    public void postEvent (DEvent event)
    {
        // just append it to the queue
        _evqueue.append(event);
    }

    // inherit documentation from the interface
    public void removedLastSubscriber (DObject obj, boolean deathWish)
    {
        // destroy the object if it so desires
        if (deathWish) {
            destroyObject(obj.getOid());
        }
    }

    /**
     * Posts a self-contained unit of code that should be run on the
     * distributed object manager thread at the next available
     * opportunity. The code will be queued up with the rest of the events
     * and invoked in turn. Like event processing code, the code should
     * not take long to complete and should <em>definitely</em> not block.
     */
    public void postUnit (Runnable unit)
    {
        // just append it to the queue
        _evqueue.append(unit);
    }

    /**
     * Returns the object in the object table with the specified oid or
     * null if no object has that oid. Be sure only to call this function
     * from the dobjmgr thread and not to do anything funny with the
     * object. If subscription is desired, use
     * <code>subscribeToObject()</code>.
     *
     * @see #subscribeToObject
     */
    public DObject getObject (int oid)
    {
        return (DObject)_objects.get(oid);
    }

    /**
     * Returns true if the thread invoking this method is the same thread
     * that is doing distributed object event dispatch. Code that wishes
     * to enforce that it is either always or never called on the event
     * dispatch thread will want to make use of this method.
     */
    public synchronized boolean isEventDispatchThread ()
    {
        return Thread.currentThread() == _dobjThread;
    }

    /**
     * Runs the dobjmgr event loop until it is requested to exit. This
     * should be called from the main application thread.
     */
    public void run ()
    {
        Log.info("DOMGR running.");

        // make a note of the thread that's processing events
        synchronized (this) {
            _dobjThread = Thread.currentThread();
        }

        while (isRunning()) {
            // pop the next unit off the queue
            Object unit = _evqueue.get();
            long start = 0L;
            if (UNIT_PROFILING) {
                start = System.currentTimeMillis();
            }

            // if this is a runnable, it's just an executable unit that
            // should be invoked
            if (unit instanceof Runnable) {
                try {
                    ((Runnable)unit).run();
                } catch (Exception e) {
                    Log.warning("Execution unit failed [unit=" + unit + "].");
                    Log.logStackTrace(e);
                }

            } else if (unit instanceof CompoundEvent) {
                processCompoundEvent((CompoundEvent)unit);

            } else {
                processEvent((DEvent)unit);
            }

            if (UNIT_PROFILING) {
                long elapsed = System.currentTimeMillis() - start;

                // report excessively long units
                if (elapsed > 500) {
                    Log.warning("Unit '" + StringUtil.safeToString(unit) +
                                " [" + StringUtil.shortClassName(unit) +
                                "]' ran for " + elapsed + "ms.");
                }

                // record the time spent processing this unit
                String cname = StringUtil.shortClassName(unit);
                UnitProfile uprof = (UnitProfile)_profiles.get(cname);
                if (uprof == null) {
                    _profiles.put(cname, uprof = new UnitProfile());
                }
                uprof.record(start, elapsed);
            }
        }

        Log.info("DOMGR exited.");
    }

    /**
     * Performs the processing associated with a compound event, notifying
     * listeners and the like.
     */
    protected void processCompoundEvent (CompoundEvent event)
    {
        List events = event.getEvents();
        int ecount = events.size();

        // look up the target object
        DObject target = (DObject)_objects.get(event.getTargetOid());
        if (target == null) {
            Log.debug("Compound event target no longer exists " +
                      "[event=" + event + "].");
            return;
        }

        // check the permissions on all of the events
        for (int ii = 0; ii < ecount; ii++) {
            DEvent sevent = (DEvent)events.get(ii);
            if (!target.checkPermissions(sevent)) {
                Log.warning("Event failed permissions check " +
                            "[event=" + sevent + ", target=" + target + "].");
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
     * Performs the processing associated with an event, notifying
     * listeners and the like.
     */
    protected void processEvent (DEvent event)
    {
        // look up the target object
        DObject target = (DObject)_objects.get(event.getTargetOid());
        if (target == null) {
            Log.debug("Event target no longer exists " +
                      "[event=" + event + "].");
            return;
        }

        // check the event's permissions
        if (!target.checkPermissions(event)) {
            Log.warning("Event failed permissions check " +
                        "[event=" + event + ", target=" + target + "].");
            return;
        }

        if (dispatchEvent(event, target)) {
            // unless requested not to, notify any proxies
            target.notifyProxies(event);
        }
    }

    /**
     * Dispatches an event after the target object has been resolved and
     * the permissions have been checked. This is used by {@link
     * #processEvent} and {@link #processCompoundEvent}.
     *
     * @return the value returned by {@link DEvent#applyToObject}.
     */
    protected boolean dispatchEvent (DEvent event, DObject target)
    {
        boolean notify = true; // assume always notify
        try {
            // do any internal management necessary based on this event
            Method helper = (Method)_helpers.get(event.getClass());
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

            // if the event returns false from applyToObject, this
            // means it's a silent event and we shouldn't notify the
            // listeners
            if (notify) {
                target.notifyListeners(event);
            }

        } catch (Exception e) {
            Log.warning("Failure processing event [event=" + event +
                        ", target=" + target + "].");
            Log.logStackTrace(e);
        }

        // track the number of events dispatched
        ++_eventCount;
        return true;
    }

    /**
     * Requests that the dobjmgr shut itself down soon- you may
     * want to try using {@link Invoker#shutdown} which will make sure that
     * both the Invoker and DObjectMgr are empty and then shut them both down.
     */
    public void harshShutdown ()
    {
        postUnit(new Runnable() {
            public void run () {
                _running = false;
            }
        });
    }

    /**
     * Dumps collected profiling information to the system log. Does
     * nothing if unit profiling is not enabled.
     */
    public void dumpUnitProfiles ()
    {
        if (!UNIT_PROFILING) {
            return;
        }

        Iterator iter = _profiles.keySet().iterator();
        while (iter.hasNext()) {
            String cname = (String)iter.next();
            UnitProfile uprof = (UnitProfile)_profiles.get(cname);
            Log.info("P: " + cname + " => " + uprof);
        }
    }

    /**
     * Called as a helper for <code>ObjectDestroyedEvent</code> events. It
     * removes the object from the object table.
     *
     * @return true if the event should be dispatched, false if it should
     * be aborted.
     */
    public boolean objectDestroyed (DEvent event, DObject target)
    {
        int oid = target.getOid();

//          Log.info("Removing destroyed object from table " +
//                   "[oid=" + oid + "].");

        // remove the object from the table
        _objects.remove(oid);

        // inactivate the object
        target.setManager(null);

        // deal with any remaining oid lists that reference this object
        Reference[] refs = (Reference[])_refs.remove(oid);
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                // skip empty spots
                if (refs[i] == null) {
                    continue;
                }

                Reference ref = refs[i];
                DObject reffer = (DObject)_objects.get(ref.reffingOid);

                // ensure that the referencing object is still around
                if (reffer != null) {
                    // post an object removed event to clear the reference
                    postEvent(new ObjectRemovedEvent(
                        ref.reffingOid, ref.field, oid));
//                      Log.info("Forcing removal " + ref + ".");

                } else {
                    Log.info("Dangling reference from inactive object " +
                             ref + ".");
                }
            }
        }

        // if this object has any oid list fields that are still
        // referencing other objects, we need to clear out those
        // references
        Class oclass = target.getClass();
        Field[] fields = oclass.getFields();
        for (int f = 0; f < fields.length; f++) {
            Field field = fields[f];

            // ignore static and non-public fields
            int mods = field.getModifiers();
            if ((mods & Modifier.STATIC) != 0 ||
                (mods & Modifier.PUBLIC) == 0) {
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
                Log.warning("Unable to clean up after oid list field " +
                            "[target=" + target + ", field=" + field + "].");
            }
        }

        return true;
    }

    /**
     * Called by <code>objectDestroyed</code>; clears out the tracking
     * info for a reference by the supplied object to the specified oid
     * via the specified field.
     */
    protected void clearReference (
        DObject reffer, String field, int reffedOid)
    {
        // look up the reference vector for the referenced object
        Reference[] refs = (Reference[])_refs.get(reffedOid);
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

        if (ref == null) {
            Log.warning("Requested to clear out non-existent reference " +
                        "[refferOid=" + reffer.getOid() +
                        ", field=" + field +
                        ", reffedOid=" + reffedOid + "].");

//          } else {
//              Log.info("Cleared out reference " + ref + ".");
        }
    }

    /**
     * Called as a helper for <code>ObjectAddedEvent</code> events. It
     * updates the object/oid list tracking structures.
     *
     * @return true if the event should be dispatched, false if it should
     * be aborted.
     */
    public boolean objectAdded (DEvent event, DObject target)
    {
        ObjectAddedEvent oae = (ObjectAddedEvent)event;
        int oid = oae.getOid();

        // ensure that the target object exists
        if (!_objects.containsKey(oid)) {
            Log.info("Rejecting object added event of non-existent object " +
                     "[refferOid=" + target.getOid() +
                     ", reffedOid=" + oid + "].");
            return false;
        }

        // get the reference vector for the referenced object. we use bare
        // arrays rather than something like an array list to conserve
        // memory. there will be many objects and references
        Reference[] refs = (Reference[])_refs.get(oid);
        if (refs == null) {
            refs = new Reference[DEFREFVEC_SIZE];
            _refs.put(oid, refs);
        }

        // determine where to add the reference
        Reference ref = new Reference(target.getOid(), oae.getName(), oid);
        int rpos = -1;
        for (int i = 0; i < refs.length; i++) {
            if (ref.equals(refs[i])) {
                Log.warning("Ignoring request to track existing " +
                            "reference " + ref + ".");
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

//          Log.info("Tracked reference " + ref + ".");
        return true;
    }

    /**
     * Called as a helper for <code>ObjectRemovedEvent</code> events. It
     * updates the object/oid list tracking structures.
     *
     * @return true if the event should be dispatched, false if it should
     * be aborted.
     */
    public boolean objectRemoved (DEvent event, DObject target)
    {
        ObjectRemovedEvent ore = (ObjectRemovedEvent)event;
        String field = ore.getName();
        int toid = target.getOid();
        int oid = ore.getOid();

        // get the reference vector for the referenced object
        Reference[] refs = (Reference[])_refs.get(oid);
        if (refs == null) {
            // this can happen normally when an object is destroyed. it
            // will remove itself from the reference system and then
            // generate object removed events for all of its referencees.
            // so we opt not to log anything in this case

//              Log.warning("Object removed without reference to track it " +
//                          "[toid=" + toid + ", field=" + field +
//                          ", oid=" + oid + "].");
            return true;
        }

        // look for the matching reference
        for (int i = 0; i < refs.length; i++) {
            Reference ref = refs[i];
            if (ref != null && ref.equals(toid, field)) {
//                  Log.info("Removed reference " + refs[i] + ".");
                refs[i] = null;
                return true;
            }
        }

        Log.warning("Unable to locate reference for removal " +
                    "[reffingOid=" + toid + ", field=" + field +
                    ", reffedOid=" + oid + "].");
        return true;
    }

    /**
     * Should not need to be called except by the invoker during shutdown
     * to ensure that things are proceeding smoothly.
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
        // look for the next unused oid. in theory if we had two billion
        // objects, this would loop infinitely, but the world would have
        // come to an end long before we had two billion objects
        do {
            _nextOid = (_nextOid + 1) % Integer.MAX_VALUE;
        } while (_objects.containsKey(_nextOid));

        return _nextOid;
    }

    // documentation inherited from interface PresentsServer.Reporter
    public void appendReport (StringBuffer report, long now, long sinceLast)
    {
        report.append("* presents.PresentsDObjectMgr:\n");

        long processed = (_eventCount - _lastEventCount);
        report.append("- Events since last report: ").append(processed);
        report.append("\n");
        _lastEventCount = _eventCount;

        // summarize the objects in our dobject table
        HashMap ccount = new HashMap();
        SortableArrayList clist = new SortableArrayList();
        Iterator iter = _objects.values().iterator();
        while (iter.hasNext()) {
            DObject obj = (DObject)iter.next();
            String clazz = obj.getClass().getName();
            int[] count = (int[])ccount.get(clazz);
            if (count == null) {
                count = new int[] { 0 };
                ccount.put(clazz, count);
                clist.add(clazz);
            }
            count[0]++;
        }

        // sort our list of dobject types
        clist.sort();

        report.append("- DObject count: ").append(_objects.size());
        report.append("\n");
        for (int ii = 0; ii < clist.size(); ii++) {
            String clazz = (String)clist.get(ii);
            int count = ((int[])ccount.get(clazz))[0];
            report.append("  ").append(clazz).append(": ").append(count);
            report.append("\n");
        }
    }

    /**
     * Calls {@link Subscriber#objectAvailable} and catches and logs any
     * exception thrown by the subscriber during the call.
     */
    protected static void informObjectAvailable (Subscriber sub, DObject obj)
    {
        try {
            sub.objectAvailable(obj);
        } catch (Exception e) {
            Log.warning("Subscriber choked during object available " +
                        "[obj=" + StringUtil.safeToString(obj) +
                        ", sub=" + sub + "].");
            Log.logStackTrace(e);
        }
    }

    /**
     * Used to create a distributed object and register it with the
     * system.
     */
    protected class CreateObjectEvent extends DEvent
    {
        public CreateObjectEvent (Class clazz, Subscriber target)
        {
            super(0); // target the fake object
            _class = clazz;
            _target = target;
        }

        public boolean isPrivate ()
        {
            return true;
        }

        public boolean applyToObject (DObject target)
            throws ObjectAccessException
        {
            int oid = getNextOid();
            DObject obj = null;

            try {
                // create a new instance of this object
                obj = (DObject)_class.newInstance();

                // initialize this object
                obj.setOid(oid);
                obj.setManager(PresentsDObjectMgr.this);
                obj.setAccessController(_defaultController);

                // insert it into the table
                _objects.put(oid, obj);

//                  Log.info("Created object [obj=" + obj + "].");

            } catch (Exception e) {
                Log.warning("Object creation failure " +
                            "[class=" + _class.getName() +
                            ", error=" + e + "].");

                // let the subscriber know shit be fucked
                if (_target != null) {
                    String errmsg = "Object instantiation failed";
                    _target.requestFailed(
                        oid, new ObjectAccessException(errmsg, e));
                }

                return false;
            }

            if (_target != null) {
                // add the subscriber to this object's subscriber list
                obj.addSubscriber(_target);

                // let the target subscriber know that their object is
                // available
                informObjectAvailable(_target, obj);
            }

            // and return false to ensure that this event is not
            // dispatched to the fake object's subscriber list (even
            // though it's empty)
            return false;
        }

        protected transient Class _class;
        protected transient Subscriber _target;
    }

    /**
     * Used to make an object available to a subscriber (with or without
     * the associated subscription).
     */
    protected class AccessObjectEvent extends DEvent
    {
        public static final int SUBSCRIBE = 0;
        public static final int UNSUBSCRIBE = 1;

        public AccessObjectEvent (int oid, Subscriber target,
                                  int action)
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
            DObject obj = (DObject)_objects.get(_oid);

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

            // return false to ensure that this event is not dispatched to
            // the fake object's subscriber list (even though it's empty)
            return false;
        }

        protected int _oid;
        protected Subscriber _target;
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
            Log.warning("Unable to register event helpers " +
                        "[error=" + e + "].");
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
                return (reffingOid == other.reffingOid &&
                        field.equals(other.field));
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

    /** Used to profile time spent invoking units and processing events if
     * such profiling is enabled. */
    protected static class UnitProfile
    {
        public void record (long start, long elapsed)
        {
            if (start - _lastRecorded > RECENT_INTERVAL) {
                _recentElapsed = 0L;
                _recentCount = 0;
            }

            _recentElapsed += elapsed;
            _recentCount++;
            _totalElapsed += elapsed;
            _lastRecorded = start;
            _histo.addValue((int)elapsed);
        }

        public String toString ()
        {
            int count = _histo.size();
            return "r:" + _recentElapsed + " t:" + _totalElapsed +
                " c:" + count + " ra:" + (_recentElapsed/_recentCount) +
                " ta:" + (_totalElapsed/count) +
                " h:" + StringUtil.toString(_histo.getBuckets());
        }

        protected long _lastRecorded;
        protected int _recentCount;
        protected long _recentElapsed;
        protected long _totalElapsed;
        protected Histogram _histo = new Histogram(0, 50, 10);

        protected static final long RECENT_INTERVAL = 5 * 60 * 1000L;
    }

    /** A flag indicating that the event dispatcher is still running. */
    protected boolean _running = true;

    /** The event queue via which all events are processed. */
    protected Queue _evqueue = new Queue();

    /** The managed distributed objects table. */
    protected HashIntMap _objects = new HashIntMap();

    /** Used to assign a unique oid to each distributed object. */
    protected int _nextOid = 0;

    /** Used to track the number of events dispatched over time. */
    protected long _eventCount = 0;

    /** Used to track the number of events dispatched over of time. */
    protected long _lastEventCount = 0;

    /** The last time at which we generated a report. */
    protected long _lastReportStamp;

    /** Used to track oid list references of distributed objects. */
    protected HashIntMap _refs = new HashIntMap();

    /** The default access controller to use when creating distributed
     * objects. */
    protected AccessController _defaultController;

    /** We keep track of which thread is executing the event loop so that
     * other services can enforce restrictions on code that should or
     * should not be called from the event dispatch thread. */
    protected Thread _dobjThread;

    /** Used to profile our events and runnable units. */
    protected HashMap _profiles = new HashMap();

    /** Indicates whether or not profiling is enabled. */
    protected static final boolean UNIT_PROFILING = true;

    /** Check whether we should generate a report every 100 events. */
    protected static final long REPORT_CHECK_PERIOD = 100;

    /** Generate a report no more frequently than once per five
     * minutes. */
    protected static final long REPORT_PERIOD = 5 * 60 * 1000L;

    /** The default size of an oid list refs vector. */
    protected static final int DEFREFVEC_SIZE = 4;

    /**
     * This table maps event classes to helper methods that perform some
     * additional processing for particular events.
     */
    protected static HashMap _helpers = new HashMap();
    static { registerEventHelpers(); }
}
