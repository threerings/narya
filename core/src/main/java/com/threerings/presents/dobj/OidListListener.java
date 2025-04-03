//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * Implemented by entities which wish to hear about changes that occur to oid list attributes of a
 * particular distributed object.
 *
 * @see DObject#addListener
 */
public interface OidListListener extends ChangeListener
{
    /**
     * Called when an object added event has been dispatched on an object. This will be called
     * <em>after</em> the event has been applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    void objectAdded (ObjectAddedEvent event);

    /**
     * Called when an object removed event has been dispatched on an object. This will be called
     * <em>after</em> the event has been applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    void objectRemoved (ObjectRemovedEvent event);
}
