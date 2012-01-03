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
 * A SetAdapter that listens for changes with a given name and calls the 'named' version of the
 * SetListener methods when they occur.
 */
public class NamedSetAdapter<T extends DSet.Entry> extends SetAdapter<T>
{
    /**
     * Listen for DSet events with the given name.
     */
    public NamedSetAdapter (String name)
    {
        _name = name;
    }

    @Override
    final public void entryAdded (EntryAddedEvent<T> event)
    {
        if (event.getName().equals(_name)) {
            namedEntryAdded(event);
        }
    }

    public void namedEntryAdded (EntryAddedEvent<T> event)
    {
        // Override to provide functionality
    }

    @Override
    final public void entryRemoved (EntryRemovedEvent<T> event)
    {
        if (event.getName().equals(_name)) {
            namedEntryRemoved(event);
        }
    }

    public void namedEntryRemoved (EntryRemovedEvent<T> event)
    {

    }

    @Override
    final public void entryUpdated (EntryUpdatedEvent<T> event)
    {
        if (event.getName().equals(_name)) {
            namedEntryUpdated(event);
        }
    }

    public void namedEntryUpdated (EntryUpdatedEvent<T> event)
    {

    }

    protected final String _name;
}
