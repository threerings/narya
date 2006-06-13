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

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;

/**
 * An entry added event is dispatched when an entry is added to a {@link
 * DSet} attribute of a distributed entry. It can also be constructed to
 * request the addition of an entry to a set and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryAddedEvent extends NamedEvent
{
    /**
     * Constructs a new entry added event on the specified target object
     * with the supplied set attribute name and entry to add.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute to which to add the specified
     * entry.
     * @param entry the entry to add to the set attribute.
     */
    public EntryAddedEvent (int targetOid, String name, DSet.Entry entry)
    {
        this(targetOid, name, entry, false);
    }

    /**
     * Used when the distributed object already added the entry before
     * generating the event.
     */
    public EntryAddedEvent (int targetOid, String name, DSet.Entry entry,
                            boolean alreadyApplied)
    {
        super(targetOid, name);
        _entry = entry;
        _alreadyApplied = alreadyApplied;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public EntryAddedEvent ()
    {
    }

    /**
     * Returns the entry that has been added.
     */
    public DSet.Entry getEntry ()
    {
        return _entry;
    }

    /**
     * Applies this event to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        if (!_alreadyApplied) {
            boolean added = ((DSet)target.getAttribute(_name)).add(_entry);
            if (!added) {
                Log.warning("Duplicate entry found [event=" + this + "].");
            }
        }
        return true;
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof SetListener) {
            ((SetListener)listener).entryAdded(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuilder buf)
    {
        buf.append("ELADD:");
        super.toString(buf);
        buf.append(", entry=");
        StringUtil.toString(buf, _entry);
    }

    protected DSet.Entry _entry;

    /** Used when this event is generated on the authoritative server
     * where object changes are made immediately. This lets us know not to
     * apply ourselves when we're actually dispatched. */
    protected transient boolean _alreadyApplied;
}
