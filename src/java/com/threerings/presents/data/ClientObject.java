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
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>receivers</code> field. */
    public static final String RECEIVERS = "receivers";
    // AUTO-GENERATED: FIELDS END

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

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the
     * <code>receivers</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToReceivers (DSet.Entry elem)
    {
        requestEntryAdd(RECEIVERS, receivers, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>receivers</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromReceivers (Comparable key)
    {
        requestEntryRemove(RECEIVERS, receivers, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>receivers</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateReceivers (DSet.Entry elem)
    {
        requestEntryUpdate(RECEIVERS, receivers, elem);
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
    public void setReceivers (DSet value)
    {
        requestAttributeChange(RECEIVERS, value, this.receivers);
        this.receivers = (value == null) ? null : value.typedClone();
    }
    // AUTO-GENERATED: METHODS END
}
