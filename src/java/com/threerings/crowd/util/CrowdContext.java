//
// $Id: CrowdContext.java,v 1.4 2001/10/01 22:14:55 mdb Exp $

package com.threerings.cocktail.party.util;

import com.threerings.cocktail.cher.util.CherContext;
import com.threerings.cocktail.party.client.LocationDirector;
import com.threerings.cocktail.party.client.OccupantManager;

/**
 * The party context provides access to the various managers, etc. that
 * are needed by the party client code.
 */
public interface PartyContext extends CherContext
{
    /**
     * Returns a reference to the location director.
     */
    public LocationDirector getLocationDirector ();

    /**
     * Returns a reference to the occupant manager.
     */
    public OccupantManager getOccupantManager ();
}
