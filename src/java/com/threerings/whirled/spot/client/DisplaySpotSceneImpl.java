//
// $Id: DisplaySpotSceneImpl.java,v 1.3 2001/12/05 03:38:09 mdb Exp $

package com.threerings.whirled.spot.client;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.client.DisplaySceneImpl;

import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotSceneModel;

/**
 * A basic implementation of the {@link DisplaySpotScene} interface which
 * is used by default if no extended implementation is desired.
 */
public class DisplaySpotSceneImpl extends DisplaySceneImpl
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and place config.
     */
    public DisplaySpotSceneImpl (SpotSceneModel model, PlaceConfig config)
    {
        super(model, config);

        // keep a casted reference to our model around
        _model = model;

        // sort out our locations and portals
        int lcount = model.locationIds.length;
        int pcount = model.portalIds.length;
        _portals = new Portal[pcount];
        _locations = new Location[lcount - pcount];

        // because the portals ids are in the same order as the location
        // ids, we can parse them in one pass
        int pidx = 0;
        for (int i = 0; i < lcount; i++) {
            Location loc;
            // stop checking for portals once we've parsed them all
            int pid = (pidx < pcount) ? model.portalIds[pidx] : -1;

            if (model.locationIds[i] == pid) {
                // add this portal to the portals array
                _portals[pidx] = createPortal();
                populatePortal(model, _portals[pidx], i, pidx);
                loc = _portals[pidx];
                pidx++;

            } else {
                loc = createLocation();
                populateLocation(model, loc, i);
            }

            // everything goes into the locations array
            _locations[i] = loc;
        }
    }

    /**
     * Returns a new location instance.
     */
    protected Location createLocation ()
    {
        return new Location();
    }

    /**
     * Populates a location instance with the specified index, using
     * information from the model.
     *
     * @param model the scene model.
     * @param loc the location instance to populate.
     * @param lidx the location index in the model arrays.
     */
    protected void populateLocation (
        SpotSceneModel model, Location loc, int lidx)
    {
        loc.locationId = model.locationIds[lidx];
        loc.x = model.locationX[lidx];
        loc.y = model.locationY[lidx];
        loc.orientation = model.locationOrients[lidx];
        loc.clusterIndex = model.locationClusters[lidx];
    }

    /**
     * Returns a new portal instance.
     */
    protected Portal createPortal ()
    {
        return new Portal();
    }

    /**
     * Populates a portal instance with the specified index, using
     * information from the model.
     *
     * @param model the scene model.
     * @param port the portal to populate.
     * @param lidx the location index in the model arrays.
     * @param pidx the portal index in the model arrays.
     */
    protected void populatePortal (
        SpotSceneModel model, Portal port, int lidx, int pidx)
    {
        // populate the location fields
        populateLocation(model, port, lidx);

        // populate the portal-specific fields
        port.targetSceneId = model.neighborIds[pidx];
        port.targetLocId = model.targetLocIds[pidx];
    }

    // documentation inherited
    public int getDefaultEntranceId ()
    {
        return _model.defaultEntranceId;
    }

    // documentation inherited
    public Location[] getLocations ()
    {
        return _locations;
    }

    // documentation inherited
    public Portal[] getPortals ()
    {
        return _portals;
    }

    /** The data that makes up our scene. */
    protected SpotSceneModel _model;

    /** An array of the locations (and portals) in this scene. */
    protected Location[] _locations;

    /** An array of just the portals in this scene. */
    protected Portal[] _portals;
}
