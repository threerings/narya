//
// $Id: MessageListener.java,v 1.2 2002/02/03 04:38:05 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Implemented by entites which wish to hear about message events that are
 * dispatched on a particular distributed object.
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
    public void messageReceived (MessageEvent event);
}
