//
// $Id: CrowdContext.java,v 1.2 2001/08/14 03:22:19 mdb Exp $

package com.threerings.cocktail.party.util;

import com.threerings.cocktail.cher.util.CherContext;
import com.threerings.cocktail.party.client.LocationManager;

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
}
