//
// $Id: ObjectRemovedEvent.java 3099 2004-08-27 02:21:06Z mdb $
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

package com.threerings.presents.dobj;

/**
 * An object removed event is dispatched when an object is removed from an
 * <code>OidList</code> attribute of a distributed object. It can also be
 * constructed to request the removal of an oid from an
 * <code>OidList</code> attribute of an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectRemovedEvent extends NamedEvent
{
    /**
     * Constructs a new object removed event on the specified target
     * object with the supplied oid list attribute name and object id to
     * remove.
     *
     * @param targetOid the object id of the object from whose oid list we
     * will remove an oid.
     * @param name the name of the attribute (data member) from which to
     * remove the specified oid.
     * @param oid the oid to remove from the oid list attribute.
     */
    public ObjectRemovedEvent (int targetOid, String name, int oid)
    {
        super(targetOid, name);
        _oid = oid;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ObjectRemovedEvent ()
    {
    }

    /**
     * Returns the oid that has been removed.
     */
    public int getOid ()
    {
        return _oid;
    }

    /**
     * Applies this event to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        OidList list = (OidList)target.getAttribute(_name);
        list.remove(_oid);
        return true;
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener is OidListListener) {
            ((OidListListener)listener).objectRemoved(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("OBJREM:");
        super.toString(buf);
        buf.append(", oid=").append(_oid);
    }

    protected int _oid;
}
