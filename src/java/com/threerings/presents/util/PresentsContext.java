//
// $Id: PresentsContext.java,v 1.2 2001/07/20 23:23:50 mdb Exp $

package com.threerings.cocktail.cher.util;

import com.samskivert.util.Context;
import com.threerings.cocktail.cher.dobj.DObjectManager;
import com.threerings.cocktail.cher.client.Client;

public interface CherContext extends Context
{
    /**
     * Returns a reference to the client.
     */
    public Client getClient ();

    /**
     * Returns a reference to the distributed object manager.
     */
    public DObjectManager getDObjectManager ();
}
