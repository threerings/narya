//
// $Id: DObject.java,v 1.22 2001/08/11 00:11:53 mdb Exp $

package com.threerings.cocktail.cher.dobj;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.threerings.cocktail.cher.Log;

/**
 * The distributed object forms the foundation of the cocktail system. All
 * information shared among users of the system is done via distributed
 * objects. A distributed object has a set of subscribers. These
 * subscribers have access to the object or a proxy of the object and
 * therefore have access to the data stored in the object's members at all
 * times.
 *
 * <p> When there is any change to that data, initiated by one of the
 * subscribers, an event is generated which is dispatched to all
 * subscribers of the object, notifying them of that change and affecting
 * that change to the copy of the object maintained at each client. In
 * this way, both a respository of shared information and a mechanism for
 * asynchronous notification are made available as a fundamental
 * application building blocks.
 *
 * <p> To define what information is shared, an application creates a
 * distributed object declaration which is much like a class declaration
 * except that it is transformed into a proper derived class of
 * <code>DObject</code> by a script. A declaration looks something like
 * this:
 *
 * <pre>
 * public dclass RoomObject
 * {
 *     public String description;
 *     public int[] occupants;
 * }
 * </pre>
 *
 * which is converted into an actual Java class that looks like this:
 *
 * <pre>
 * public class RoomObject extends DObject
 * {
 *     public String getDescription ()
 *     {
 *         // ...
 *     }
 *
 *     public void setDescription (String description)
 *     {
 *         // ...
 *     }
 *
 *     public int[] getOccupants ()
 *     {
 *         // ...
 *     }
 *
 *     public void setOccupants (int[] occupants)
 *     {
 *         // ...
 *     }
 *
 *     public void setOccupantsAt (int index, int value)
 *     {
 *         // ...
 *     }
 * }
 * </pre>
 *
 * These method calls on the actual distributed object will result in the
 * proper attribute change events being generated and dispatched.
 *
 * <p> Note that distributed object fields can only be of a limited set of
 * supported types. These types are:
 *
 * <code><pre>
 * byte, short, int, long, float, double
 * Byte, Short, Integer, Long, Float, Double, String
 * byte[], short[], int[], long[], float[], double[], String[]
 * </pre></code>
 */
public class DObject
{
    /**
     * Returns the object id of this object. All objects in the system
     * have a unique object id.
     */
    public int getOid ()
    {
        return _oid;
    }

    /**
     * Don't call this function! Go through the distributed object manager
     * instead to ensure that everything is done on the proper thread.
     * This function can only safely be called directly when you know you
     * are operating on the omgr thread (you are in the middle of a call
     * to <code>objectAvailable</code> or <code>handleEvent</code>).
     *
     * @see DObjectManager#subscribeToObject
     */
    public void addSubscriber (Subscriber sub)
    {
        if (!_subscribers.contains(sub)) {
            _subscribers.add(sub);
        }
    }

    /**
     * Don't call this function! Go through the distributed object manager
     * instead to ensure that everything is done on the proper thread.
     * This function can only safely be called directly when you know you
     * are operating on the omgr thread (you are in the middle of a call
     * to <code>objectAvailable</code> or <code>handleEvent</code>).
     *
     * @see DObjectManager#unsubscribeFromObject
     */
    public void removeSubscriber (Subscriber sub)
    {
        if (_subscribers.remove(sub)) {
            // if we removed something, check to see if we just removed
            // the last subscriber from our list; we also want to be sure
            // that we're still active otherwise there's no need to notify
            // our objmgr because we don't have one
            if (_subscribers.size() == 0 && _mgr != null) {
                _mgr.removedLastSubscriber(this);
            }
        }
    }

    /**
     * At times, an entity on the server may need to ensure that events it
     * has queued up have made it through the event queue and are applied
     * to their respective objects before a service may safely be
     * undertaken again. To make this possible, it can acquire a lock on a
     * distributed object, generate the events in question and then
     * release the lock (via a call to <code>releaseLock</code>) which
     * will queue up a final event, the processing of which will release
     * the lock. Thus the lock will not be released until all of the
     * previously generated events have been processed.  If the service is
     * invoked again before that lock is released, the associated call to
     * <code>acquireLock</code> will fail and the code can respond
     * accordingly. An object may have any number of outstanding locks as
     * long as they each have a unique name.
     *
     * @param name the name of the lock to acquire.
     *
     * @return true if the lock was acquired, false if the lock was not
     * acquired because it has not yet been released from a previous
     * acquisition.
     *
     * @see #releaseLock
     */
    public boolean acquireLock (String name)
    {
        // create our lock array if we haven't already. we do all this
        // jockeying rather than just use something like an ArrayList to
        // be memory efficient because there may be very many distributed
        // objects
        if (_locks == null) {
            _locks = new String[2];
        }

        // scan the lock array to see if this lock is already acquired
        int slot = -1;
        for (int i = 0; i < _locks.length; i++) {
            if (_locks[i] == null && slot == -1) {
                // keep track of this for later
                slot = i;
            } else if (name.equals(_locks[i])) {
                return false;
            }
        }

        // if we didnt' find a blank slot in our previous scan, we'll have
        // to expand the locks array
        if (slot == -1) {
            String[] locks = new String[_locks.length*2];
            System.arraycopy(_locks, 0, locks, 0, _locks.length);
            slot = _locks.length;
        }

        // place our lock in the array and let the user know that they
        // acquired the lock
        _locks[slot] = name;
        return true;
    }

    /**
     * Queues up an event that when processed will release the lock of the
     * specified name.
     *
     * @see #acquireLock
     */
    public void releaseLock (String name)
    {
        // queue up a release lock event
        ReleaseLockEvent event = new ReleaseLockEvent(_oid, name);
        _mgr.postEvent(event);
    }

    /**
     * Don't call this function! It is called by a remove lock event when
     * that event is processed and shouldn't be called at any other time.
     * If you mean to release a lock that was acquired with
     * <code>acquireLock</code> you should be using
     * <code>releaseLock</code>.
     *
     * @see #acquireLock
     * @see #releaseLock
     */
    protected void clearLock (String name)
    {
        // track the lock index for reporting purposes
        int lockidx = -1;

        // scan through and clear the lock in question
        if (_locks != null) {
            for (int i = 0; i < _locks.length; i++) {
                if (name.equals(_locks[i])) {
                    _locks[i] = null;
                    lockidx = i;
                    break;
                }
            }
        }

        // complain if we didn't find the lock
        if (lockidx == -1) {
            Log.info("Unable to clear non-existent lock [lock=" + name +
                     ", dobj=" + this + "].");
        }
    }

    /**
     * Requests that this distributed object be destroyed. It does so by
     * queueing up an object destroyed event which the server will
     * validate and process.
     */
    public void destroy ()
    {
        _mgr.postEvent(new ObjectDestroyedEvent(_oid));
    }

    /**
     * Checks to ensure that the specified subscriber has access to this
     * object. This will be called before satisfying a subscription
     * request. By default objects are accessible to all subscribers, but
     * certain objects may wish to implement more fine grained access
     * control.
     *
     * @param sub the subscriber that will subscribe to this object.
     *
     * @return true if the subscriber has access to the object, false if
     * they do not.
     */
    public boolean checkPermissions (Subscriber sub)
    {
        return true;
    }

    /**
     * Checks to ensure that this event which is about to be processed,
     * has the appropriate permissions. By default objects accept all
     * manner of events, but certain objects may wish to implement more
     * fine grained access control.
     *
     * @param event the event that will be dispatched, object permitting.
     *
     * @return true if the event is valid and should be dispatched, false
     * if the event fails the permissions check and should be aborted.
     */
    public boolean checkPermissions (DEvent event)
    {
        return true;
    }

    /**
     * Called by the distributed object manager after it has applied an
     * event to this object. This dispatches an event notification to all
     * of the subscribers of this object.
     *
     * @param event the event that was just applied.
     */
    public void notifySubscribers (DEvent event)
    {
        // Log.info("Dispatching event to " + _subscribers.size() +
        // " subscribers: " + event);

        for (int i = 0; i < _subscribers.size(); i++) {
            Subscriber sub = (Subscriber)_subscribers.get(i);
            // notify the subscriber
            if (!sub.handleEvent(event, this)) {
                // if they return false, we need to remove them from the
                // subscriber list
                _subscribers.remove(i--);

                // if we just removed our last subscriber, we need to let
                // the omgr know about it
                if (_subscribers.size() == 0) {
                    _mgr.removedLastSubscriber(this);
                }
            }
        }
    }

    /**
     * Sets the named attribute to the specified value. This is only used
     * by the internals of the event dispatch mechanism and should not be
     * called directly by users. Use the generated attribute setter
     * methods instead.
     */
    public void setAttribute (String name, Object value)
        throws ObjectAccessException
    {
        try {
            getClass().getField(name).set(this, value);

        } catch (Exception e) {
            String errmsg = "Attribute setting failure [name=" + name +
                ", value=" + value + ", error=" + e + "].";
            throw new ObjectAccessException(errmsg);
        }
    }

    /**
     * Looks up the named attribute and returns a reference to it. This
     * should only be used by the internals of the event dispatch
     * mechanism and should not be called directly by users. Use the
     * generated attribute getter methods instead.
     */
    public Object getAttribute (String name)
        throws ObjectAccessException
    {
        try {
            return getClass().getField(name).get(this);

        } catch (Exception e) {
            String errmsg = "Attribute getting failure [name=" + name +
                ", error=" + e + "].";
            throw new ObjectAccessException(errmsg);
        }
    }

    /**
     * Returns true if this object is active and registered with the
     * distributed object system. If an object is created via
     * <code>DObjectManager.createObject</code> it will be active until
     * such time as it is destroyed.
     */
    public boolean isActive ()
    {
        return _mgr != null;
    }

    /**
     * Don't call this function! It initializes this distributed object
     * with the supplied distributed object manager. This is called by the
     * distributed object manager when an object is created and registered
     * with the system.
     *
     * @see DObjectManager#createObject
     */
    public void setManager (DObjectManager mgr)
    {
        _mgr = mgr;
    }

    /**
     * Don't call this function. It is called by the distributed object
     * manager when an object is created and registered with the system.
     *
     * @see DObjectManager#createObject
     */
    public void setOid (int oid)
    {
        _oid = oid;
    }

    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        toString(buf);
        buf.append("]");
        return buf.toString();
    }

    protected void toString (StringBuffer buf)
    {
        buf.append("oid=").append(_oid);
    }

    /**
     * Called by derived instances when an attribute setter method was
     * called.
     */
    protected void requestAttributeChange (String name, Object value)
    {
        // generate an attribute changed event
        DEvent event = new AttributeChangedEvent(_oid, name, value);
        // and dispatch it to our dobjmgr
        _mgr.postEvent(event);
    }

    /**
     * Calls by derived instances when an oid adder method was called.
     */
    protected void requestOidAdd (String name, int oid)
    {
        // generate an object added event
        DEvent event = new ObjectAddedEvent(_oid, name, oid);
        // and dispatch it to our dobjmgr
        _mgr.postEvent(event);
    }

    /**
     * Calls by derived instances when an oid remover method was called.
     */
    protected void requestOidRemove (String name, int oid)
    {
        // generate an object removed event
        DEvent event = new ObjectRemovedEvent(_oid, name, oid);
        // and dispatch it to our dobjmgr
        _mgr.postEvent(event);
    }

    protected int _oid;
    protected DObjectManager _mgr;
    protected ArrayList _subscribers = new ArrayList();
    protected String[] _locks;
}
