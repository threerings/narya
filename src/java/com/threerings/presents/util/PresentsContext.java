//
// $Id: PresentsContext.java,v 1.4 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.util;

import com.samskivert.util.Context;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

public interface PresentsContext extends Context
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
