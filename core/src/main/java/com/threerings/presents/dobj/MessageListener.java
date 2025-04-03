//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * Implemented by entities which wish to hear about message events that are dispatched on a
 * particular distributed object.
 *
 * @see DObject#addListener
 */
public interface MessageListener extends ChangeListener
{
    /**
     * Called when an message event has been dispatched on an object.
     *
     * @param event The event that was dispatched on the object.
     */
    void messageReceived (MessageEvent event);
}
