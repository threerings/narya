//
// $Id: ParlorContext.java,v 1.1 2001/10/01 02:56:35 mdb Exp $

package com.threerings.parlor.util;

import com.threerings.cocktail.party.util.PartyContext;
import com.threerings.parlor.client.ParlorDirector;

/**
 * The parlor context provides access to the various managers, etc. that
 * are needed by the parlor client code.
 */
public interface ParlorContext extends PartyContext
{
    /**
     * Returns a reference to the parlor director.
     */
    public ParlorDirector getParlorDirector ();
}
