//
// $Id: ClientObject.java,v 1.7 2003/04/30 22:45:57 mdb Exp $

package com.threerings.presents.data;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

import com.threerings.presents.Log;

/**
 * Every client in the system has an associated client object to which
 * only they subscribe. The client object can be used to deliver messages
 * solely to a particular client as well as to publish client-specific
 * data.
 */
public class ClientObject extends DObject
{
    /** The field name of the <code>receivers</code> field. */
    public static final String RECEIVERS = "receivers";

    /** The name of a message event delivered to the client when they
     * switch usernames (and therefore user objects). */
    public static final String CLOBJ_CHANGED = "!clobj_changed!";

    /** Used to publish all invocation service receivers registered on
     * this client. */
    public DSet receivers = new DSet();

    /**
     * Returns a short string identifying this client.
     */
    public String who ()
    {
        return "(" + getOid() + ")";
    }

    /**
     * Used for reference counting client objects, adds a reference to
     * this object.
     */
    public synchronized void reference ()
    {
        _references++;
//         Log.info("Incremented references [who=" + who() +
//                  ", refs=" + _references + "].");
    }

    /**
     * Used for reference counting client objects, releases a reference to
     * this object.
     *
     * @return true if the object has remaining references, false
     * otherwise.
     */
    public synchronized boolean release ()
    {
//         Log.info("Decremented references [who=" + who() +
//                  ", refs=" + (_references-1) + "].");
        return (--_references > 0);
    }

    /** Used to reference count resolved client objects. */
    protected transient int _references;

    /**
     * Requests that the specified entry be added to the
     * <code>receivers</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToReceivers (DSet.Entry elem)
    {
        requestEntryAdd(RECEIVERS, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>receivers</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromReceivers (Comparable key)
    {
        requestEntryRemove(RECEIVERS, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>receivers</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateReceivers (DSet.Entry elem)
    {
        requestEntryUpdate(RECEIVERS, elem);
    }

    /**
     * Requests that the <code>receivers</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setReceivers (DSet receivers)
    {
        requestAttributeChange(RECEIVERS, receivers);
        this.receivers = receivers;
    }
}
