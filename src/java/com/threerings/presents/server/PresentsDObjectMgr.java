//
// $Id: PresentsDObjectMgr.java,v 1.14 2001/08/11 01:00:26 mdb Exp $

package com.threerings.cocktail.cher.server;

import java.lang.reflect.*;
import java.util.HashMap;

import com.samskivert.util.Queue;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.util.IntMap;

/**
 * The cher distributed object manager implements the
 * <code>DObjectManager</code> interface, providing an object manager that
 * runs on the server. By virtue of running on the server, it manages its
 * objects directly rather than managing proxies of objects which is what
 * is done on the client. Thus it simply queues up events and dispatches
 * them to subscribers.
 *
 * <p> The server object manager is meant to run on the main thread of the
 * server application and thus provides a method to be invoked by the
 * application main thread which won't return until the manager has been
 * requested to shut down.
 */
public class CherDObjectMgr implements DObjectManager
{
    /**
     * Creates the dobjmgr and prepares it for operation.
     */
    public CherDObjectMgr ()
    {
        // we create a dummy object to live as oid zero and we'll use that
        // for some internal event trickery
        DObject dummy = new DObject();
        dummy.setOid(0);
        dummy.setManager(this);
        _objects.put(0, new DObject());
    }

    // inherit documentation from the interface
    public void createObject (Class dclass, Subscriber target,
                              boolean subscribe)
    {
        // queue up a create object event
        postEvent(new CreateObjectEvent(dclass, target, subscribe));
    }

    // inherit documentation from the interface
    public void subscribeToObject (int oid, Subscriber target)
    {
        // queue up an access object event
        postEvent(new AccessObjectEvent(oid, target,
                                        AccessObjectEvent.SUBSCRIBE));
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
    public void removedLastSubscriber (DObject obj)
    {
        // nothing to do here, our objects live forever!
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
     * Runs the dobjmgr event loop until it is requested to exit. This
     * should be called from the main application thread.
     */
    public void run ()
    {
        Log.info("DOMGR running.");

        while (isRunning()) {
            // pop the next unit off the queue
            Object unit = _evqueue.get();

            // if this is a runnable, it's just an executable unit that
            // should be invoked
            if (unit instanceof Runnable) {
                try {
                    ((Runnable)unit).run();
                } catch (Exception e) {
                    Log.warning("Execution unit failed [unit=" + unit + "].");
                    Log.logStackTrace(e);
                }
                continue;
            }

            // otherwise it's an event, so we do more complicated
            // processing
            DEvent event = (DEvent)unit;

            // look up the target object
            DObject target = (DObject)_objects.get(event.getTargetOid());
            if (target == null) {
                Log.warning("Event target no longer exists " +
                            "[event=" + event + "].");
                continue;
            }

            // check the event's permissions
            if (!target.checkPermissions(event)) {
                Log.warning("Event failed permissions check " +
                            "[event=" + event + ", target=" + target + "].");
                continue;
            }

            try {
                // do any internal management necessary based on this
                // event
                Method helper = (Method)_helpers.get(event.getClass());
                if (helper != null) {
                    // invoke the helper method
                    Object rv =
                        helper.invoke(this, new Object[] { event, target });
                    // if helper returns false, we abort event processing
                    if (!((Boolean)rv).booleanValue()) {
                        continue;
                    }
                }

                // everything's good so far, apply the event to the object
                boolean notify = event.applyToObject(target);

                // if the event returns false from applyToObject, this
                // means it's a silent event and we shouldn't notify the
                // subscribers
                if (notify) {
                    target.notifySubscribers(event);
                }

            } catch (Exception e) {
                Log.warning("Failure processing event [event=" + event +
                            ", target=" + target + "].");
                Log.logStackTrace(e);
            }
        }

        Log.info("DOMGR exited.");
    }

    /**
     * Requests that the dobjmgr shut itself down. It will exit the event
     * processing loop which cause <code>run()</code> to return.
     */
    public void shutdown ()
    {
        _running = false;

        // stick a bogus object on the event queue to ensure that the mgr
        // wakes up and smells the coffee
        _evqueue.append(new ShutdownEvent());
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
        if (!_objects.contains(oid)) {
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
            if (refs[i].equals(toid, field)) {
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
        } while (_objects.contains(_nextOid));

        return _nextOid;
    }

    /**
     * Used to create a distributed object and register it with the
     * system.
     */
    protected class CreateObjectEvent extends DEvent
    {
        public CreateObjectEvent (Class clazz, Subscriber target,
                                  boolean subscribe)
        {
            super(0); // target the fake object
            _class = clazz;
            _target = target;
            _subscribe = subscribe;
        }

        public boolean applyToObject (DObject target)
            throws ObjectAccessException
        {
            int oid = getNextOid();

            try {
                // create a new instance of this object
                DObject obj = (DObject)_class.newInstance();

                // initialize this object
                obj.setOid(oid);
                obj.setManager(CherDObjectMgr.this);
                // insert it into the table
                _objects.put(oid, obj);

//                  Log.info("Created object [obj=" + obj + "].");

                if (_target != null) {
                    // add the subscriber to this object's subscriber list
                    // if they requested it
                    if (_subscribe) {
                        obj.addSubscriber(_target);
                    }

                    // let the target subscriber know that their object is
                    // available
                    _target.objectAvailable(obj);
                }

            } catch (Exception e) {
                Log.warning("Object creation failure " +
                            "[class=" + _class.getName() +
                            ", error=" + e + "].");

                // let the subscriber know shit be fucked
                if (_target != null) {
                    String errmsg = "Object instantiation failed: " + e;
                    _target.requestFailed(
                        oid, new ObjectAccessException(errmsg));
                }
            }

            // and return false to ensure that this event is not
            // dispatched to the fake object's subscriber list (even
            // though it's empty)
            return false;
        }

        protected Class _class;
        protected Subscriber _target;
        protected boolean _subscribe;
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
            _target.objectAvailable(obj);

            // return false to ensure that this event is not dispatched to
            // the fake object's subscriber list (even though it's empty)
            return false;
        }

        protected int _oid;
        protected Subscriber _target;
        protected int _action;
    }

    protected class ShutdownEvent extends DEvent
    {
        public ShutdownEvent ()
        {
            super(0); // target the bogus object
        }

        public boolean applyToObject (DObject target)
            throws ObjectAccessException
        {
            // nothing doing!
            return false;
        }
    }

    /**
     * Registers our event helper methods.
     */
    protected static void registerEventHelpers ()
    {
        Class[] ptypes = new Class[] { DEvent.class, DObject.class };
        Class omgrcl = CherDObjectMgr.class;
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

    /** A flag indicating that the event dispatcher is still running. */
    protected boolean _running = true;

    /** The event queue via which all events are processed. */
    protected Queue _evqueue = new Queue();

    /** The managed distributed objects table. */
    protected IntMap _objects = new IntMap();

    /** Used to assign a unique oid to each distributed object. */
    protected int _nextOid = 0;

    /** Used to track oid list references of distributed objects. */
    protected IntMap _refs = new IntMap();

    /** The default size of an oid list refs vector. */
    protected static final int DEFREFVEC_SIZE = 4;

    /**
     * This table maps event classes to helper methods that perform some
     * additional processing for particular events.
     */
    protected static HashMap _helpers = new HashMap();
    static { registerEventHelpers(); }
}
