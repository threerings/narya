//
// $Id: ElementUpdateListener.java,v 1.1 2002/03/19 01:10:03 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Implemented by entites which wish to hear about element updates that
 * take place for a particular distributed object.
 *
 * @see DObject#addListener
 */
public interface ElementUpdateListener extends ChangeListener
{
    /**
     * Called when an element updated event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object. So fetching the element during this call
     * will provide the new value for the element.
     *
     * @param event The event that was dispatched on the object.
     */
    public void elementUpdated (ElementUpdatedEvent event);
}
