//
// $Id: MessageListener.java,v 1.1 2001/10/12 00:03:03 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Implemented by entites which wish to hear about message events that are
 * dispatched on a particular distributed object.
 *
 * @see DObject#addListener
 */
public interface MessageListener
{
    /**
     * Called when an message event has been dispatched on an object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void messageReceived (MessageEvent event);
}
