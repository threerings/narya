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

package com.threerings.presents.dobj;

/**
 * An object added event is dispatched when an object is added to an <code>OidList</code> attribute
 * of a distributed object. It can also be constructed to request the addition of an oid to an
 * <code>OidList</code> attribute of an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectAddedEvent extends NamedEvent
{
    /**
     * Constructs a new object added event on the specified target object with the supplied oid
     * list attribute name and object id to add.
     *
     * @param targetOid the object id of the object to whose oid list we will add an oid.
     * @param name the name of the attribute (data member) to which to add the specified oid.
     * @param oid the oid to add to the oid list attribute.
     */
    public ObjectAddedEvent (int targetOid, String name, int oid)
    {
        super(targetOid, name);
        _oid = oid;
    }

    /**
     * Returns the oid that has been added.
     */
    public int getOid ()
    {
        return _oid;
    }

    @Override
    public boolean alreadyApplied ()
    {
        return _alreadyApplied;
    }

    @Override
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        if (!_alreadyApplied) {
            OidList list = (OidList)target.getAttribute(_name);
            list.add(_oid);
        }
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof OidListListener) {
            ((OidListListener)listener).objectAdded(this);
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("OBJADD:");
        super.toString(buf);
        buf.append(", oid=").append(_oid);
    }

    /** Used by {@link DObject} to note if this event has already been applied locally. */
    protected ObjectAddedEvent setAlreadyApplied (boolean alreadyApplied)
    {
        _alreadyApplied = alreadyApplied;
        return this;
    }

    protected int _oid;
    protected transient boolean _alreadyApplied;
}
