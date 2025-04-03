//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
