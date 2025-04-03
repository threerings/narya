//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * Implements the methods in SetListener so that you don't have to implement the ones you don't
 * want to.
 *
 * <p> <b>NOTE:</b> This adapter will receive <em>all</em> Entry events from a DObject it's
 * listening to, so it should check that the event's name matches the field it's interested in
 * before acting on the event.
 *
 * @param <T> the type of entry being handled by this listener. This must match the type on the set
 * that generates the events.
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
