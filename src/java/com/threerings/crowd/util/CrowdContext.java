//
// $Id: CrowdContext.java,v 1.3 2001/08/20 21:45:37 mdb Exp $

package com.threerings.cocktail.party.util;

import com.threerings.cocktail.cher.util.CherContext;
import com.threerings.cocktail.party.client.LocationManager;
import com.threerings.cocktail.party.client.OccupantManager;

/**
 * The party context provides access to the various managers, etc. that
 * are needed by the party client code.
 */
public interface PartyContext extends CherContext
{
    /**
     * Returns a reference to the location manager.
     */
    public LocationManager getLocationManager ();

    /**
     * Returns a reference to the occupant manager.
     */
    public OccupantManager getOccupantManager ();
}
