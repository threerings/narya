//
// $Id: PresentsDObjectMgr.java,v 1.9 2001/07/23 21:12:55 mdb Exp $

package com.threerings.cocktail.cher.server;

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
            // pop the next event off the queue
            DEvent event = (DEvent)_evqueue.get();

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
                // everything's good so far, apply the event to the object
                boolean notify = event.applyToObject(target);

                // do any internal management necessary based on this
                // event
                // **TBD**

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

    protected boolean _running = true;
    protected Queue _evqueue = new Queue();
    protected IntMap _objects = new IntMap();
    protected int _nextOid = 0;

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

                Log.info("Created object [obj=" + obj + "].");

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
}
