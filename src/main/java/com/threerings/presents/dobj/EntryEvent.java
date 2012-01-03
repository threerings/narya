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
 * A common parent class for DSet entry events.
 */
public abstract class EntryEvent<T extends DSet.Entry> extends NamedEvent
{
    /**
     * Constructs a new event for the specified target object with the supplied attribute name.
     *
     * @param targetOid the object id of the object in question.
     * @param name the name associated with this event.
     */
    public EntryEvent (int targetOid, String name)
    {
        super(targetOid, name);
    }

    /**
     * Return the key that identifies the entry related to this event.
     * Never returns <code>null</code>.
     */
    public abstract Comparable<?> getKey ();

    /**
     * Return the <em>new or updated</em> entry, or <code>null</code> if the entry was removed.
     */
    public abstract T getEntry ();

    /**
     * Return the <em>old</em> entry, or <code>null</code> if the entry is newly added.
     */
    public abstract T getOldEntry ();
}
