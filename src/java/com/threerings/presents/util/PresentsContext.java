//
// $Id: PresentsContext.java,v 1.3 2001/07/21 01:06:24 mdb Exp $

package com.threerings.cocktail.cher.util;

import com.samskivert.util.Context;

import com.threerings.cocktail.cher.client.Client;
import com.threerings.cocktail.cher.dobj.DObjectManager;

public interface CherContext extends Context
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
