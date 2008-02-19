//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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
 * Implements the methods in SetListener so that you don't have to implement the ones you don't
 * want to.
 * 
 * <p> <b>NOTE:</b> This adapter will receive <em>all</em> Entry events from a DObject it's
 * listening to, so it should check that the event's name matches the field it's interested in
 * before acting on the event.
 */
public class SetAdapter<T extends DSet.Entry> implements SetListener<T>
{
    // documentation inherited from interface SetListener
    public void entryAdded (EntryAddedEvent<T> event)
    {
        // override to provide functionality
    }

    // documentation inherited from interface SetListener
    public void entryUpdated (EntryUpdatedEvent<T> event)
    {
        // override to provide functionality
    }

    // documentation inherited from interface SetListener
    public void entryRemoved (EntryRemovedEvent<T> event)
    {
        // override to provide functionality
    }
}
