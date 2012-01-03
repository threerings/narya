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

import com.samskivert.util.StringUtil;

/**
 * An entry added event is dispatched when an entry is added to a {@link DSet} attribute of a
 * distributed entry. It can also be constructed to request the addition of an entry to a set and
 * posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 *
 * @param <T> the type of entry being handled by this event. This must match the type on the set
 * that generated this event.
 */
public class EntryAddedEvent<T extends DSet.Entry> extends EntryEvent<T>
{
    /**
     * Constructs a new entry added event on the specified target object with the supplied set
     * attribute name and entry to add.
     *
     * @param targetOid the object id of the object to whose set we will add an entry.
     * @param name the name of the attribute to which to add the specified entry.
     * @param entry the entry to add to the set attribute.
     */
    public EntryAddedEvent (int targetOid, String name, T entry)
    {
        super(targetOid, name);
        _entry = entry;
    }

    @Override
    public Comparable<?> getKey ()
    {
        return _entry.getKey();
    }

    /**
     * {@inheritDoc}
     * This implementation never returns <code>null</code>.
     */
    @Override
    public T getEntry ()
    {
        return _entry;
    }

    /**
     * {@inheritDoc}
     * This implementation always returns <code>null</code>.
     */
    @Override
    public T getOldEntry ()
    {
        return null;
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
            if (!target.getSet(_name).add(_entry)) {
                return false; // DSet will have already complained
            }
        }
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof SetListener<?>) {
            @SuppressWarnings("unchecked") SetListener<T> setlist = (SetListener<T>)listener;
            setlist.entryAdded(this);
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("ELADD:");
        super.toString(buf);
        buf.append(", entry=");
        StringUtil.toString(buf, _entry);
    }

    /** Used by {@link DObject} to note if this event has already been applied locally. */
    protected EntryAddedEvent<T> setAlreadyApplied (boolean alreadyApplied)
    {
        _alreadyApplied = alreadyApplied;
        return this;
    }

    protected T _entry;

    /** Used when this event is generated on the authoritative server where object changes are made
     * immediately. This lets us know not to apply ourselves when we're actually dispatched. */
    protected transient boolean _alreadyApplied;
}
