//
// $Id: AttributeChangeListener.java,v 1.1 2001/10/12 00:03:03 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Implemented by entites which wish to hear about attribute changes that
 * take place for a particular distributed object.
 *
 * @see DObject#addListener
 */
public interface AttributeChangeListener
{
    /**
     * Called when an attribute changed event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object. So fetching the attribute during this call
     * will provide the new value for the attribute.
     *
     * @param event The event that was dispatched on the object.
     */
    public void attributeChanged (AttributeChangedEvent event);
}
