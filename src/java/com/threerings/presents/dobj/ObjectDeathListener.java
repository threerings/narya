//
// $Id: ObjectDeathListener.java,v 1.2 2002/02/03 04:38:05 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Implemented by entites which wish to hear about object destruction
 * events.
 *
 * @see DObject#addListener
 */
public interface ObjectDeathListener extends ChangeListener
{
    /**
     * Called when this object has been destroyed. This will be called
     * <em>after</em> the event has been applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void objectDestroyed (ObjectDestroyedEvent event);
}
