//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * Implemented by entities which wish to hear about all events being dispatched on a particular
 * distributed object.
 *
 * @see DObject#addListener
 */
public interface EventListener extends ChangeListener
{
    /**
     * Called when any event has been dispatched on an object. The event will be of the derived
     * class that corresponds to the kind of event that occurred on the object. This will be
     * called <em>after</em> the event has been applied to the object. So fetching an attribute
     * upon receiving an attribute changed event will provide the new value for the attribute.
     *
     * @param event The event that was dispatched on the object.
     */
    void eventReceived (DEvent event);
}
