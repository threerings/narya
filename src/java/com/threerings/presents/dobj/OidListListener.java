//
// $Id: OidListListener.java,v 1.1 2001/10/12 00:03:03 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Implemented by entites which wish to hear about changes that occur to
 * oid list attributes of a particular distributed object.
 *
 * @see DObject#addListener
 */
public interface OidListListener
{
    /**
     * Called when an object added event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void objectAdded (ObjectAddedEvent event);

    /**
     * Called when an object removed event has been dispatched on an
     * object. This will be called <em>after</em> the event has been
     * applied to the object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void objectRemoved (ObjectRemovedEvent event);
}
