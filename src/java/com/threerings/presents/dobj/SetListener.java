//
// $Id: SetListener.java,v 1.3 2002/03/18 23:21:26 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Implemented by entites which wish to hear about changes that occur to
 * set attributes of a particular distributed object.
 *
 * @see DObject#addListener
 */
public interface SetListener extends ChangeListener
{
    /**
     * Called when an entry added event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void entryAdded (EntryAddedEvent event);

    /**
     * Called when an entry updated event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void entryUpdated (EntryUpdatedEvent event);

    /**
     * Called when an entry removed event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void entryRemoved (EntryRemovedEvent event);
}
