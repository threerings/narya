//
// $Id: SetListener.java,v 1.2 2002/02/03 04:38:05 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Implemented by entites which wish to hear about changes that occur to
 * set attributes of a particular distributed object.
 *
 * @see DObject#addListener
 */
public interface SetListener extends ChangeListener
{
    /**
     * Called when an element added event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void elementAdded (ElementAddedEvent event);

    /**
     * Called when an element updated event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void elementUpdated (ElementUpdatedEvent event);

    /**
     * Called when an element removed event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void elementRemoved (ElementRemovedEvent event);
}
