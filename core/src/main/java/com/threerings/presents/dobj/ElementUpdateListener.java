//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * Implemented by entities which wish to hear about element updates that take place for a
 * particular distributed object.
 *
 * @see DObject#addListener
 */
public interface ElementUpdateListener extends ChangeListener
{
    /**
     * Called when an element updated event has been dispatched on an object. This will be called
     * <em>after</em> the event has been applied to the object. So fetching the element during
     * this call will provide the new value for the element.
     *
     * @param event The event that was dispatched on the object.
     */
    void elementUpdated (ElementUpdatedEvent event);
}
