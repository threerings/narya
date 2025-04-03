//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * Implemented by entities which wish to hear about object destruction events.
 *
 * @see DObject#addListener
 */
public interface ObjectDeathListener extends ChangeListener
{
    /**
     * Called when this object has been destroyed. This will be called <em>after</em> the event has
     * been applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    void objectDestroyed (ObjectDestroyedEvent event);
}
