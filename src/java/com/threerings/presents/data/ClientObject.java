//
// $Id: ClientObject.java,v 1.3 2002/08/14 19:07:54 mdb Exp $

package com.threerings.presents.data;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

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
    public void removeFromReceivers (Object key)
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
        this.receivers = receivers;
        requestAttributeChange(RECEIVERS, receivers);
    }
}
