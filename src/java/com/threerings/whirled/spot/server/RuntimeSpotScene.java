//
// $Id: RuntimeSpotScene.java,v 1.4 2002/04/17 00:20:56 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.whirled.server.RuntimeScene;
import com.threerings.whirled.spot.data.SpotSceneModel;

/**
 * Makes available the spot scene information that the server needs to
 * manage requests to move from location to location, to enter and exit a
 * scene at a location and to manage speaking among bodies that occupy the
 * same clusters.
 */
public interface RuntimeSpotScene extends RuntimeScene
{
    /**
     * Returns the number of locations (and portals) in this scene.
     */
    public int getLocationCount ();

    /**
     * Returns the location id of the location at the specified index.
     */
    public int getLocationId (int locidx);

    /**
     * Returns the index of the specified location id.
     */
    public int getLocationIndex (int locationId);

    /**
     * Returns the total number of clusters in this scene.
     */
    public int getClusterCount ();

    /**
     * Returns the cluster index associated with the specified location
     * index or -1 if the location at that index is not associated with a
     * cluster.
     */
    public int getClusterIndex (int locationIdx);

    /**
     * Returns the location id of the default entrance to this scene. If a
     * body enters the scene at logon time rather than entering from some
     * other scene, this is the location at which they would appear.
     */
    public int getDefaultEntranceId ();

    /**
     * Returns the target scene id associated with the specified portal
     * (identified by its location id) or -1 if the location id specified
     * is not a portal.
     */
    public int getTargetSceneId (int locationId);

    /**
     * Returns the target location id associated with the specified portal
     * (identified by its location id) or -1 if the location id specified
     * is not a portal.
     */
    public int getTargetLocationId (int locationId);

    /**
     * Returns a reference to the underlying model.
     */
    public SpotSceneModel getModel ();
}
