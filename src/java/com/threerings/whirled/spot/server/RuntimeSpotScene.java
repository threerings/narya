//
// $Id: RuntimeSpotScene.java,v 1.1 2001/11/13 02:25:35 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.whirled.server.RuntimeScene;

/**
 * Makes available the spot scene information that the server needs to
 * manage requests to move from location to location, to enter and exit a
 * scene at a location and to manage speaking among bodies that occupy the
 * same clusters.
 */
public interface RuntimeSpotScene extends RuntimeScene
{
    /**
     * Returns the total number of clusters in this scene.
     */
    public int getClusterCount ();

    /**
     * Returns the cluster index associated with the specified location or
     * -1 if the location is not associated with a cluster.
     */
    public int getClusterIndex (int locationId);

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
}
