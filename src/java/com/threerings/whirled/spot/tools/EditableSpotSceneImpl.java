//
// $Id: EditableSpotSceneImpl.java,v 1.1 2001/11/13 02:25:36 mdb Exp $

package com.threerings.whirled.spot.tools;

import com.samskivert.util.ListUtil;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.tools.EditableSceneImpl;

import com.threerings.whirled.spot.client.DisplaySpotSceneImpl;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotSceneModel;

import com.threerings.whirled.spot.Log;

/**
 * The editable spot scene interface is used in the offline scene building
 * tools as well as by the tools that load those prototype scenes into the
 * runtime database. Accordingly, it provides a means for modifying scene
 * values and for obtaining access to the underlying scene models that
 * represent the underlying scene information.
 *
 * <p> Scrutinizers of the code might cringe at the somewhat inefficient
 * array manipulation used to preserve the integrity of the arrays
 * returned by {@link #getLocations} and {@link #getPortals}. Since we
 * expect reading to happen more often than modifying, this seemed a
 * reasonable tradeoff, and one should also note that this class will only
 * ever be used in the offline editor and not in any performance
 * constrained runtime system.
 *
 * @see com.threerings.whirled.tools.EditableScene
 */
public class EditableSpotSceneImpl
    extends DisplaySpotSceneImpl
    implements EditableSpotScene
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and update it when changes are made.
     */
    public EditableSpotSceneImpl (SpotSceneModel model)
    {
        super(model, null);

        // Java doesn't support multiple inheritance (we'll let the reader
        // decide whether or not that's unfortunate), so we have to
        // instantiate an EditableSceneImpl delegate to handle the
        // extensions to DisplayScene provided by EditableScene because
        // we're extending from the DisplaySceneImpl class chain
        _edelegate = new EditableSceneImpl(model);
    }

    // documentation inherited
    public void setId (int sceneId)
    {
        _edelegate.setId(sceneId);
    }

    // documentation inherited
    public void setVersion (int version)
    {
        _edelegate.setVersion(version);
    }

    // documentation inherited
    public void setNeighborIds (int[] neighborIds)
    {
        String errmsg = "Neighbor IDs can't be set directly in the " +
            "EditableSpotScene because we need to know the associated " +
            "location information to go along with our neighbor connections.";
        throw new RuntimeException(errmsg);
    }

    // documentation inherited
    public void setDefaultEntranceId (int defaultEntranceId)
    {
        _model.defaultEntranceId = defaultEntranceId;
    }

    // documentation inherited
    public void addLocation (Location location)
    {
        // add this location to the end of the list
        int lcount = _locations.length;
        Location[] nlocs = new Location[lcount+1];
        System.arraycopy(_locations, 0, nlocs, 0, lcount);
        nlocs[lcount] = location;
        _locations = nlocs;
    }

    // documentation inherited
    public void removeLocation (Location location)
    {
        // obtain the index of this location
        int lidx = ListUtil.indexOfEqual(_locations, location);
        if (lidx == -1) {
            Log.warning("Can't remove location that isn't in our array " +
                        "[loc=" + location + "].");

        } else {
            // create a new array minus this location
            int lcount = _locations.length;
            Location[] nlocs = new Location[lcount-1];
            System.arraycopy(_locations, 0, nlocs, 0, lidx);
            System.arraycopy(_locations, lidx+1, nlocs, lidx, lcount-lidx-1);
            _locations = nlocs;
        }
    }

    // documentation inherited
    public void addPortal (Portal portal)
    {
        // add it to the locations array as well
        addLocation(portal);

        // now add it to the end of the _portals array
        int pcount = _portals.length;
        Portal[] nports = new Portal[pcount+1];
        System.arraycopy(_portals, 0, nports, 0, pcount);
        nports[pcount] = portal;
        _portals = nports;
    }

    // documentation inherited
    public void removePortal (Portal portal)
    {
        // remove it from the locations array
        removeLocation(portal);

        // and remove it from the portals array
        int pidx = ListUtil.indexOfEqual(_portals, portal);
        if (pidx == -1) {
            Log.warning("Can't remove portal that isn't in our array " +
                        "[port=" + portal + "].");

        } else {
            // create a new array minus this portal
            int pcount = _portals.length;
            Portal[] nports = new Portal[pcount-1];
            System.arraycopy(_portals, 0, nports, 0, pidx);
            System.arraycopy(_portals, pidx+1, nports, pidx, pcount-pidx-1);
            _portals = nports;
        }
    }

    // documentation inherited
    public SceneModel getSceneModel ()
    {
        flushToModel();
        return _edelegate.getSceneModel();
    }

    // documentation inherited
    public SpotSceneModel getSpotSceneModel ()
    {
        flushToModel();
        return _model;
    }

    /**
     * This ensures that the modifications we've made to the location and
     * portal arrays are flushed to the model. It is called before
     * returning our model back to the caller.
     */
    protected void flushToModel ()
    {
        // flush the locations
        int lcount = _locations.length;
        _model.locationIds = new int[lcount];
        _model.locationX = new int[lcount];
        _model.locationY = new int[lcount];
        _model.locationOrients = new int[lcount];
        _model.locationClusters = new int[lcount];

        for (int i = 0; i < lcount; i++) {
            Location loc = _locations[i];
            _model.locationIds[i] = loc.locationId;
            _model.locationX[i] = loc.x;
            _model.locationY[i] = loc.y;
            _model.locationOrients[i] = loc.orientation;
            _model.locationClusters[i] = loc.clusterIndex;
        }

        // flush the portals
        int pcount = _portals.length;
        _model.portalIds = new int[pcount];
        _model.neighborIds = new int[pcount];
        _model.targetLocIds = new int[pcount];

        for (int i = 0; i < pcount; i++) {
            Portal port = _portals[i];
            _model.portalIds[i] = port.locationId;
            _model.neighborIds[i] = port.targetSceneId;
            _model.targetLocIds[i] = port.targetLocationId;
        }
    }

    /** We have to delegate some methods to this guy. */
    protected EditableSceneImpl _edelegate;
}
