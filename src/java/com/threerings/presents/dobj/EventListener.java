//
// $Id: EventListener.java,v 1.2 2002/02/03 04:38:05 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Implemented by entites which wish to hear about all events being
 * dispatched on a particular distributed object.
 *
 * @see DObject#addListener
 */
public interface EventListener extends ChangeListener
{
    /**
     * Called when any event has been dispatched on an object. The event
     * will be of the derived class that corresponds to the kind of event
     * that occurred on the object. This will be called <em>after</em> the
     * event has been applied to the object. So fetching an attribute upon
     * receiving an attribute changed event will provide the new value for
     * the attribute.
     *
     * @param event The event that was dispatched on the object.
     */
    public void eventReceived (DEvent event);
}
