//
// $Id: AccessController.java,v 1.1 2002/03/20 03:19:51 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Used to validate distributed object subscription requests and event
 * dispatches.
 *
 * @see DObject#setAccessController
 */
public interface AccessController
{
    /**
     * Should return true if the supplied subscriber is allowed to
     * subscribe to the specified object.
     */
    public boolean allowSubscribe (DObject object, Subscriber subscriber);

    /**
     * Should return true if the supplied event is legal for dispatch on
     * the specified distributed object.
     */
    public boolean allowDispatch (DObject object, DEvent event);
}
