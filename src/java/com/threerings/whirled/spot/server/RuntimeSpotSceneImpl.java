//
// $Id: RuntimeSpotSceneImpl.java,v 1.7 2003/02/06 18:58:30 mdb Exp $

package com.threerings.whirled.spot.server;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.IntListUtil;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.server.RuntimeSceneImpl;

import com.threerings.whirled.spot.data.SpotSceneModel;

/**
 * A basic implementation of the {@link RuntimeSpotScene} interface which
 * is used by default if no extended implementation is desired.
 */
public class RuntimeSpotSceneImpl extends RuntimeSceneImpl
    implements RuntimeSpotScene
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
        return IntListUtil.indexOf(_model.locationIds, locationId);
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

    // make or may not make it into the public interface
    public int addLocation (int locX, int locY, int orient, int cluster)
    {
        // compute the highest location id already used in the scene and
        // add one to it for our new location id
        int nlocid = Math.max(
            IntListUtil.getMaxValue(_model.locationIds), 0) + 1;

        // expand the necessary arrays
        _model.locationIds = ArrayUtil.append(_model.locationIds, nlocid);
        _model.locationX = ArrayUtil.append(_model.locationX, locX);
        _model.locationY = ArrayUtil.append(_model.locationY, locY);
        _model.locationOrients =
            ArrayUtil.append(_model.locationOrients, orient);
        _model.locationClusters =
            ArrayUtil.append(_model.locationOrients, cluster);

        return nlocid;
    }

    // documentation inherited from interface
    public int addPortal (int locX, int locY, int orient,
                          int targetSceneId, int targetLocId)
    {
        // add the location information for this portal
        int nlocid = addLocation(locX, locY, orient, 0);

        // expand the necessary portal arrays
        _model.neighborIds =
            ArrayUtil.append(_model.neighborIds, targetSceneId);
        _model.portalIds = ArrayUtil.append(_model.portalIds, nlocid);
        _model.targetLocIds =
            ArrayUtil.append(_model.targetLocIds, targetLocId);

        return nlocid;
    }

    // documentation inherited from interface
    public SpotSceneModel getModel ()
    {
        return _model;
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
