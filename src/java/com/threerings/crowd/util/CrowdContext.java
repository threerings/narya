//
// $Id: CrowdContext.java,v 1.1 2001/07/20 20:07:38 mdb Exp $

package com.threerings.cocktail.party.util;

import com.threerings.cocktail.cher.util.CherContext;
import com.threerings.cocktail.party.client.LocationManager;

public interface PartyContext extends CherContext
{
    /**
     * Returns a reference to the location manager.
     */
    public LocationManager getLocationManager ();
}
