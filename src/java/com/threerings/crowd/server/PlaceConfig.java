//
// $Id: PlaceConfig.java,v 1.1 2001/08/01 20:37:35 mdb Exp $

package com.threerings.cocktail.party.server;

/**
 * <code>PlaceConfig</code> acts as a central location and means for
 * documenting the various standard configuration properties that can be
 * provided to the place manager via its config properties object.
 */
public class PlaceConfig
{
    /**
     * This configuration parameter specifies the classname of the place
     * object derived class to be used when creating a new place.
     */
    public static final String PLACEOBJ_CLASS = "pobj";

    /**
     * This configuration parameter specifies the classname of the place
     * manager derived class to be used when creating a new place.
     */
    public static final String PLACEMGR_CLASS = "pmgr";
}
