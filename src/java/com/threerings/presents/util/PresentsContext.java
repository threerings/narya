//
// $Id: PresentsContext.java,v 1.5 2002/03/28 22:32:33 mdb Exp $

package com.threerings.presents.util;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

/**
 * Provides access to standard services needed by code that is part of or
 * uses the Presents package.
 */
public interface PresentsContext
{
    /**
     * Returns a reference to the client. This reference should be valid
     * for the life of the application.
     */
    public Client getClient ();

    /**
     * Returns a reference to the distributed object manager. This
     * reference is only valid for the duration of a session.
     */
    public DObjectManager getDObjectManager ();
}
