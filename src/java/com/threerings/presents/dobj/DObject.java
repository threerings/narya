//
// $Id: DObject.java,v 1.53 2002/10/06 20:25:05 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.Log;

/**
 * The distributed object forms the foundation of the Presents system. All
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
public class DObject implements Streamable
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
     * to <code>objectAvailable</code> or to a listener callback).
     *
     * @see DObjectManager#subscribeToObject
     */
    public void addSubscriber (Subscriber sub)
    {
        // only add the subscriber if they're not already there
        Object[] subs = ListUtil.testAndAdd(_subs, sub);
        if (subs != null) {
            _subs = subs;
            _scount++;
        }
    }

    /**
     * Don't call this function! Go through the distributed object manager
     * instead to ensure that everything is done on the proper thread.
     * This function can only safely be called directly when you know you
     * are operating on the omgr thread (you are in the middle of a call
     * to <code>objectAvailable</code> or to a listener callback).
     *
     * @see DObjectManager#unsubscribeFromObject
     */
    public void removeSubscriber (Subscriber sub)
    {
        if (ListUtil.clear(_subs, sub) != null) {
            // if we removed something, check to see if we just removed
            // the last subscriber from our list; we also want to be sure
            // that we're still active otherwise there's no need to notify
            // our objmgr because we don't have one
            if (--_scount == 0 && _omgr != null) {
                _omgr.removedLastSubscriber(this);
            }
        }
    }

    /**
     * Adds an event listener to this object. The listener will be
     * notified when any events are dispatched on this object that match
     * their particular listener interface.
     *
     * <p> Note that the entity adding itself as a listener should have
     * obtained the object reference by subscribing to it or should be
     * acting on behalf of some other entity that subscribed to the
     * object, <em>and</em> that it must be sure to remove itself from the
     * listener list (via {@link #removeListener}) when it is done because
     * unsubscribing from the object (done by whatever entity subscribed
     * in the first place) is not guaranteed to result in the listeners
     * added through that subscription being automatically removed (in
     * most cases, they definitely will not be removed).
     *
     * @param listener the listener to be added.
     *
     * @see EventListener
     * @see AttributeChangeListener
     * @see SetListener
     * @see OidListListener
     */
    public void addListener (ChangeListener listener)
    {
        // only add the listener if they're not already there
        Object[] els = ListUtil.testAndAdd(_listeners, listener);
        if (els != null) {
            _listeners = els;
        }
    }

    /**
     * Removes an event listener from this object. The listener will no
     * longer be notified when events are dispatched on this object.
     *
     * @param listener the listener to be removed.
     */
    public void removeListener (ChangeListener listener)
    {
        ListUtil.clear(_listeners, listener);
    }

    /**
     * Provides this object with an entity that can be used to validate
     * subscription requests and events before they are processed. The
     * access controller is handy for ensuring that clients are behaving
     * as expected and for preventing impermissible modifications or event
     * dispatches on a distributed object.
     */
    public void setAccessController (AccessController controller)
    {
        _controller = controller;
    }        

    /**
     * Returns a reference to the access controller in use by this object
     * or null if none has been configured.
     */
    public AccessController getAccessController ()
    {
        return _controller;
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
        // check for the existence of the lock in the list and add it if
        // it's not already there
        Object[] list = ListUtil.testAndAdd(_locks, name);
        if (list == null) {
            // a null list means the object was already in the list
            return false;

        } else {
            // a non-null list means the object was added
            _locks = list;
            return true;
        }
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
        postEvent(new ReleaseLockEvent(_oid, name));
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
        // clear the lock from the list
        if (ListUtil.clearEqual(_locks, name) == null) {
            // complain if we didn't find the lock
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
        postEvent(new ObjectDestroyedEvent(_oid));
    }

    /**
     * Checks to ensure that the specified subscriber has access to this
     * object. This will be called before satisfying a subscription
     * request. If an {@link AccessController} has been specified for this
     * object, it will be used to determine whether or not to allow the
     * subscription request. If no controller is set, the subscription
     * will be allowed.
     *
     * @param sub the subscriber that will subscribe to this object.
     *
     * @return true if the subscriber has access to the object, false if
     * they do not.
     */
    public boolean checkPermissions (Subscriber sub)
    {
        if (_controller != null) {
            return _controller.allowSubscribe(this, sub);
        } else {
            return true;
        }
    }

    /**
     * Checks to ensure that this event which is about to be processed,
     * has the appropriate permissions. If an {@link AccessController} has
     * been specified for this object, it will be used to determine
     * whether or not to allow the even dispatch. If no controller is set,
     * all events are allowed.
     *
     * @param event the event that will be dispatched, object permitting.
     *
     * @return true if the event is valid and should be dispatched, false
     * if the event fails the permissions check and should be aborted.
     */
    public boolean checkPermissions (DEvent event)
    {
        if (_controller != null) {
            return _controller.allowDispatch(this, event);
        } else {
            return true;
        }
    }

    /**
     * Called by the distributed object manager after it has applied an
     * event to this object. This dispatches an event notification to all
     * of the subscribers of this object.
     *
     * @param event the event that was just applied.
     */
    public void notifyListeners (DEvent event)
    {
        // if we have no listeners, we're home free
        if (_listeners == null) {
            return;
        }

        // iterate over the listener list, performing the necessary
        // notifications
        int llength = _listeners.length;
        for (int i = 0; i < llength; i++) {
            Object listener = _listeners[i];

            // skip empty slots
            if (listener == null) {
                continue;
            }

            try {
                // do any event specific notifications
                event.notifyListener(listener);

                // and notify them if they are listening for all events
                if (listener instanceof EventListener) {
                    ((EventListener)listener).eventReceived(event);
                }

            } catch (Exception e) {
                Log.warning("Listener choked during notification " +
                            "[listener=" + listener +
                            ", event=" + event + "].");
                Log.logStackTrace(e);
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
            // for values that contain other values (arrays and DSets), we
            // need to clone them before putting them in the object
            // because otherwise a subsequent event might come along and
            // modify these values before the networking thread has had a
            // chance to propagate this event to the clients

            // i wish i could just call value.clone() but Object declares
            // clone() to be inaccessible, so we must cast the values to
            // their actual types to gain access to the widened clone()
            // methods
            if (value instanceof DSet) {
                value = ((DSet)value).clone();
            } else if (value instanceof int[]) {
                value = ((int[])value).clone();
            } else if (value instanceof String[]) {
                value = ((String[])value).clone();
            } else if (value instanceof byte[]) {
                value = ((byte[])value).clone();
            } else if (value instanceof long[]) {
                value = ((long[])value).clone();
            } else if (value instanceof float[]) {
                value = ((float[])value).clone();
            } else if (value instanceof short[]) {
                value = ((short[])value).clone();
            } else if (value instanceof double[]) {
                value = ((double[])value).clone();
            }

            // now actually set the value
            getClass().getField(name).set(this, value);

        } catch (Exception e) {
            String errmsg = "Attribute setting failure [name=" + name +
                ", value=" + value +
                ", vclass=" + value.getClass().getName() + "].";
            throw new ObjectAccessException(errmsg, e);
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
            String errmsg = "Attribute getting failure [name=" + name + "].";
            throw new ObjectAccessException(errmsg, e);
        }
    }

    /**
     * Posts a message event on this distrubuted object.
     */
    public void postMessage (String name, Object[] args)
    {
        postEvent(new MessageEvent(_oid, name, args));
    }

    /**
     * Posts the specified event either to our dobject manager or to the
     * compound event for which we are currently transacting.
     */
    public void postEvent (DEvent event)
    {
        if (_tevent != null) {
            _tevent.postEvent(event);

        } else if (_omgr != null) {
            _omgr.postEvent(event);

        } else {
            Log.warning("Unable to post event, object has no omgr " +
                        "[oid=" + getOid() + ", class=" + getClass().getName() +
                        ", event=" + event + "].");
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
        return _omgr != null;
    }

    /**
     * Don't call this function! It initializes this distributed object
     * with the supplied distributed object manager. This is called by the
     * distributed object manager when an object is created and registered
     * with the system.
     *
     * @see DObjectManager#createObject
     */
    public void setManager (DObjectManager omgr)
    {
        _omgr = omgr;
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

    /**
     * Generates a concise string representation of this object.
     */
    public String which ()
    {
        return "[" + getClass().getName() + " " + getOid() + "]";
    }

    /**
     * Generates a string representation of this object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        StringUtil.fieldsToString(buf, this, "\n");
        if (buf.length() > 0) {
            buf.insert(0, "\n");
        }
        buf.insert(0, _oid);
        buf.insert(0, "[oid=");
        return buf.append("]").toString();
    }

    /**
     * Begins a transaction on this distributed object. In some
     * situations, it is desirable to cause multiple changes to
     * distributed object fields in one unified operation. Starting a
     * transaction causes all subsequent field modifications to be stored
     * in a single compound event which can then be committed, dispatching
     * and applying all included events in a single group. Additionally,
     * the events are dispatched over the network in a single unit which
     * can significantly enhance network efficiency.
     *
     * <p> When the transaction is complete, the caller must call {@link
     * #commitTransaction} or {@link CompoundEvent#commit} to commit the
     * transaction and release all involved objects back to their normal
     * non-transacting state. If the caller decides not to commit their
     * transaction, they must call {@link #cancelTransaction} or {@link
     * CompoundEvent#cancel} to cancel the transaction and release all
     * involved objects. Failure to do so will cause the pooch to be
     * totally screwed.
     *
     * <p> Note: like all other distributed object operations,
     * transactions are not thread safe. It is expected that a single
     * thread will handle all distributed object operations and that
     * thread will begin and complete a transaction before giving up
     * control to unknown code which might try to operate on the
     * transacting distributed object (or objects).
     *
     * <p> Note also: if the object is already engaged in a transaction, a
     * transaction participant count will be incremented to note that an
     * additional call to {@link #commitTransaction} is required before
     * the transaction should actually be committed. Thus <em>every</em>
     * call to {@link #startTransaction} must be accompanied by a call to
     * either {@link #commitTransaction} or {@link
     * #cancelTransaction}. Additionally, if any transaction participant
     * cancels the transaction, the entire transaction is cancelled for
     * all participants, regardless of whether the other participants
     * attempted to commit the transaction.
     *
     * <p> Note also: nested transactions are not allowed when an object
     * is joined to another object's transaction via {@link
     * #joinTransaction}.
     *
     * @return the compound event that encapsulates the transaction. This
     * can be ignored if the transaction will be limited to this object,
     * but if it is desired that other objects be involved in the
     * transaction, the caller will need to pass the {@link CompoundEvent}
     * returned by this method to a call to {@link #joinTransaction} on
     * the other objects that are involved.
     */
    public CompoundEvent startTransaction ()
    {
        // sanity check
        if (!isActive()) {
            String errmsg = "Can't start transaction on non-active object " +
                "[oid=" + getOid() + ", type=" + getClass().getName() + "]";
            throw new IllegalStateException(errmsg);
        }

        if (_tevent != null) {
            // a transaction count of -1 indicates that we're joined to
            // another objects transaction that should not allow
            // transaction nesting
            if (_tcount == -1) {
                String errmsg = "Object involved in shared transaction, " +
                    "nesting not allowed [dobj=" + this + "]";
                throw new IllegalStateException(errmsg);
            } else {
                _tcount++;
            }

        } else {
            _tevent = new CompoundEvent(_omgr);
            _tevent.addObject(this);
        }
        return _tevent;
    }

    /**
     * Causes this object to join the supplied transaction. See {@link
     * #startTransaction} for more information on transactions. The
     * transaction can be committed by a call to {@link
     * #commitTransaction} on any participanting object.
     */
    public void joinTransaction (CompoundEvent event)
    {
        if (_tevent != null) {
            String errmsg = "Cannot join transaction while already " +
                "transacting [dobj=" + this + "]";
            throw new IllegalStateException(errmsg);
        }
        _tevent = event;
        _tevent.addObject(this);
        _tcount = -1; // make a note that nesting is not allowed
    }

    /**
     * Commits the transaction in which this distributed object is
     * involved.
     *
     * @see CompoundEvent#commit
     */
    public void commitTransaction ()
    {
        if (_tevent == null) {
            String errmsg = "Cannot commit: not involved in a transaction " +
                "[dobj=" + this + "]";
            throw new IllegalStateException(errmsg);
        }

        // if we are nested, we decrement our nesting count rather than
        // committing the transaction
        if (_tcount > 0) {
            _tcount--;

        } else {
            // we may actually be doing our final commit after someone
            // already cancelled this transaction, so we need to perform
            // the appropriate action at this point
            if (_tcancelled) {
                _tevent.cancel();
            } else {
                _tevent.commit();
            }
        }
    }        

    /**
     * Returns true if this object is in the middle of a transaction or
     * false if it is not.
     */
    public boolean inTransaction ()
    {
        return (_tevent != null);
    }

    /**
     * Cancels the transaction in which this distributed object is
     * involved.
     *
     * @see CompoundEvent#cancel
     */
    public void cancelTransaction ()
    {
        if (_tevent == null) {
            String errmsg = "Cannot cancel: not involved in a transaction " +
                "[dobj=" + this + "]";
            throw new IllegalStateException(errmsg);
        }

        // if we're in a nested transaction, make a note that it is to be
        // cancelled when all parties commit and decrement the nest count
        if (_tcount > 0) {
            _tcancelled = true;
            _tcount--;

        } else {
            _tevent.cancel();
        }
    }        

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
        out.writeInt(_oid);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        _oid = in.readInt();
    }

    /**
     * Removes this object from participation in any transaction in which
     * it might be taking part.
     */
    protected void clearTransaction ()
    {
        // sanity check
        if (_tcount != 0) {
            Log.warning("Transaction cleared with non-zero nesting count " +
                        "[dobj=" + this + "].");
            _tcount = 0;
        }

        // clear our transaction state
        _tevent = null;
        _tcancelled = false;
    }

    /**
     * Called by derived instances when an attribute setter method was
     * called.
     */
    protected void requestAttributeChange (String name, Object value)
    {
        // dispatch an attribute changed event
        postEvent(new AttributeChangedEvent(_oid, name, value));
    }

    /**
     * Called by derived instances when an element updater method was
     * called.
     */
    protected void requestElementUpdate (String name, Object value, int index)
    {
        // dispatch an attribute changed event
        postEvent(new ElementUpdatedEvent(_oid, name, value, index));
    }

    /**
     * Calls by derived instances when an oid adder method was called.
     */
    protected void requestOidAdd (String name, int oid)
    {
        // dispatch an object added event
        postEvent(new ObjectAddedEvent(_oid, name, oid));
    }

    /**
     * Calls by derived instances when an oid remover method was called.
     */
    protected void requestOidRemove (String name, int oid)
    {
        // dispatch an object removed event
        postEvent(new ObjectRemovedEvent(_oid, name, oid));
    }

    /**
     * Calls by derived instances when a set adder method was called.
     */
    protected void requestEntryAdd (String name, DSet.Entry entry)
    {
        try {
            DSet set = (DSet)getAttribute(name);
            // dispatch an entry added event
            postEvent(new EntryAddedEvent(_oid, name, entry));

        } catch (ObjectAccessException oae) {
            Log.warning("Unable to request entryAdd [name=" + name +
                        ", entry=" + entry + ", error=" + oae + "].");
        }
    }

    /**
     * Calls by derived instances when a set remover method was called.
     */
    protected void requestEntryRemove (String name, Object key)
    {
        // dispatch an entry removed event
        postEvent(new EntryRemovedEvent(_oid, name, key));
    }

    /**
     * Calls by derived instances when a set updater method was called.
     */
    protected void requestEntryUpdate (String name, DSet.Entry entry)
    {
        try {
            DSet set = (DSet)getAttribute(name);
            // dispatch an entry updated event
            postEvent(new EntryUpdatedEvent(_oid, name, entry));

        } catch (ObjectAccessException oae) {
            Log.warning("Unable to request entryUpdate [name=" + name +
                        ", entry=" + entry + ", error=" + oae + "].");
        }
    }

    /** Our object id. */
    protected int _oid;

    /** A reference to our object manager. */
    protected transient DObjectManager _omgr;

    /** The entity that tells us if an event or subscription request
     * should be allowed. */
    protected transient AccessController _controller;

    /** A list of outstanding locks. */
    protected transient Object[] _locks;

    /** Our subscribers list. */
    protected transient Object[] _subs;

    /** Our event listeners list. */
    protected transient Object[] _listeners;

    /** Our subscriber count. */
    protected transient int _scount;

    /** The compound event associated with our transaction, if we're
     * currently in a transaction. */
    protected transient CompoundEvent _tevent;

    /** The nesting depth of our current transaction. */
    protected transient int _tcount;

    /** Whether or not our nested transaction has been cancelled. */
    protected transient boolean _tcancelled;
}
