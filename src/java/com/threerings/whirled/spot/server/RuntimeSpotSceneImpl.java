//
// $Id: RuntimeSpotSceneImpl.java,v 1.2 2001/12/14 00:12:32 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.server.RuntimeSceneImpl;

import com.threerings.whirled.spot.data.SpotSceneModel;

/**
 * A basic implementation of the {@link RuntimeSpotScene} interface which
 * is used by default if no extended implementation is desired.
 */
public class RuntimeSpotSceneImpl extends RuntimeSceneImpl
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and place config.
     */
    public RuntimeSpotSceneImpl (SpotSceneModel model, PlaceConfig config)
    {
        super(model, config);

        // keep a casted reference to our scene model around
        _model = model;

        // determine the highest cluster index
        int lcount = _model.locationIds.length;
        for (int i = 0; i < lcount; i++) {
            int cidx = _model.locationClusters[i];
            if (cidx > _clusterCount) {
                _clusterCount = cidx;
            }
        }
        // now increment by one to get the count rather than the highest
        // cluster index
        _clusterCount++;
    }

    // documentation inherited
    public int getLocationCount ()
    {
        return _model.locationIds.length;
    }

    // documentation inherited
    public int getLocationId (int locidx)
    {
        return _model.locationIds[locidx];
    }

    // documentation inherited
    public int getLocationIndex (int locationId)
    {
        int lcount = _model.locationIds.length;
        for (int i = 0; i < lcount; i++) {
            if (_model.locationIds[i] == locationId) {
                return i;
            }
        }
        return -1;
    }

    // documentation inherited
    public int getClusterCount ()
    {
        return _clusterCount;
    }

    // documentation inherited
    public int getClusterIndex (int locationIdx)
    {
        return _model.locationClusters[locationIdx];
    }

    // documentation inherited
    public int getDefaultEntranceId ()
    {
        return _model.defaultEntranceId;
    }

    // documentation inherited
    public int getTargetSceneId (int locationId)
    {
        int pidx = getPortalIndex(locationId);
        return (pidx == -1) ? -1 : _model.neighborIds[pidx];
    }

    // documentation inherited
    public int getTargetLocationId (int locationId)
    {
        int pidx = getPortalIndex(locationId);
        return (pidx == -1) ? -1 : _model.targetLocIds[pidx];
    }

    /**
     * Returns the index of the specified portal in the model's internal
     * portal arrays.
     */
    protected int getPortalIndex (int portalId)
    {
        int pcount = _model.portalIds.length;
        for (int i = 0; i < pcount; i++) {
            if (_model.portalIds[i] == portalId) {
                return i;
            }
        }
        return -1;
    }

    /** A casted reference to our scene model. */
    protected SpotSceneModel _model;

    /** The total number of clusters in this scene. */
    protected int _clusterCount = -1;
}
