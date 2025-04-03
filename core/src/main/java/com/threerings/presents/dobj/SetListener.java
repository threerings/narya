//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * Implemented by entities which wish to hear about changes that occur to set attributes of a
 * particular distributed object.
 *
 * <p> <b>NOTE:</b> This listener will receive <em>all</em> Entry events from a DObject it's
 * listening to, so it should check that the event's name matches the field it's interested in
 * before acting on the event.
 *
 * @see DObject#addListener
 *
 * @param <T> the type of entry being handled by this listener. This must match the type on the set
 * that generates the events.
 */
public interface SetListener<T extends DSet.Entry> extends ChangeListener
{
    /**
     * Called when an entry added event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    void entryAdded (EntryAddedEvent<T> event);

    /**
     * Called when an entry updated event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    void entryUpdated (EntryUpdatedEvent<T> event);

    /**
     * Called when an entry removed event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    void entryRemoved (EntryRemovedEvent<T> event);
}
