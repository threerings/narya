//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import javax.annotation.Generated;
import com.threerings.util.Name;

import com.threerings.presents.client.InvocationReceiver;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

/**
 * A distributed object to which only the client subscribes. Used to deliver messages solely to a
 * particular client as well as to publish client-specific data.
 */
public class ClientObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>username</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String USERNAME = "username";

    /** The field name of the <code>receivers</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String RECEIVERS = "receivers";
    // AUTO-GENERATED: FIELDS END

    /** The name of a message event delivered to the client when they switch usernames (and
     * therefore user objects). */
    public static final String CLOBJ_CHANGED = "!clobj_changed!";

    /** This client's authentication username. */
    public Name username;

    /** Used to publish all invocation service receivers registered on this client. */
    public DSet<InvocationReceiver.Registration> receivers = DSet.newDSet();

    /**
     * Returns a short string identifying this client.
     */
    public String who ()
    {
        return "(" + username + ":" + getOid() + ")";
    }

    /**
     * Checks whether or not this client has the specified permission.
     *
     * @return null if the user has access, a fully-qualified translatable message string
     * indicating the reason for denial of access.
     *
     * @see ClientObject.PermissionPolicy
     */
    public String checkAccess (Permission perm, Object context)
    {
        if (_permPolicy == null) {
            _permPolicy = createPermissionPolicy();
        }
        return _permPolicy.checkAccess(perm, context);
    }

    /**
     * A version of {@link #checkAccess(Permission,Object)} that provides no context.
     */
    public String checkAccess (Permission perm)
    {
        return checkAccess(perm, null);
    }

    /**
     * Convenience wrapper around {@link #checkAccess(Permission,Object)} that simply returns a
     * boolean indicating whether or not this client has the permission rather than an explanation.
     */
    public boolean hasAccess (Permission perm, Object context)
    {
        return checkAccess(perm, context) == null;
    }

    /**
     * Convenience wrapper around {@link #checkAccess(Permission)} that simply returns a boolean
     * indicating whether or not this client has the permission rather than an explanation.
     */
    public boolean hasAccess (Permission perm)
    {
        return checkAccess(perm) == null;
    }

    /**
     * Used for reference counting client objects, adds a reference to this object.
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

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>username</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setUsername (Name value)
    {
        Name ovalue = this.username;
        requestAttributeChange(
            USERNAME, value, ovalue);
        this.username = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>receivers</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToReceivers (InvocationReceiver.Registration elem)
    {
        requestEntryAdd(RECEIVERS, receivers, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>receivers</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromReceivers (Comparable<?> key)
    {
        requestEntryRemove(RECEIVERS, receivers, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>receivers</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updateReceivers (InvocationReceiver.Registration elem)
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setReceivers (DSet<InvocationReceiver.Registration> value)
    {
        requestAttributeChange(RECEIVERS, value, this.receivers);
        DSet<InvocationReceiver.Registration> clone = (value == null) ? null : value.clone();
        this.receivers = clone;
    }
    // AUTO-GENERATED: METHODS END

    protected PermissionPolicy createPermissionPolicy ()
    {
        return new PermissionPolicy();
    }

    /**
     * ClientObject derived classes can extend this class to provide more sophisticated permission
     * policies, and should return their customized classes from {@link #createPermissionPolicy}.
     * This class, and its children must <em>only</em> make use of data available in the
     * ClientObject (and its children). Permissions may be checked on the client or server.
     */
    protected class PermissionPolicy implements InvocationCodes
    {
        /**
         * Returns null if the specified client has the specified permission, an error code
         * explaining the lack of access if they do not. {@link InvocationCodes#ACCESS_DENIED}
         * should be returned if no more specific explanation is available.
         *
         * @param perm the permission to be checked.
         * @param context a potential context for the request, if any.
         */
        public String checkAccess (Permission perm, Object context) {
            return ACCESS_DENIED; // by default, you can't do it!
        }
    }

    /** Handles our fine-grained permissions. */
    protected transient PermissionPolicy _permPolicy;

    /** Used to reference count resolved client objects. */
    protected transient int _references;
}
