//
// $Id: MisoScene.java,v 1.3 2001/10/05 23:58:36 mdb Exp $

package com.threerings.miso.scene;

import java.util.List;

import com.threerings.media.tile.Tile;

/**
 * A scene object represents the data model corresponding to a single
 * screen for game play. For instance, one scene might display a portion
 * of a street with several buildings scattered about on the periphery.
 */
public interface MisoScene
{
    /** Scene id to denote an unset or otherwise invalid scene id. */
    public static final int SID_INVALID = -1;

    /** The total number of layers. */
    public static final int NUM_LAYERS = 3;

    /** The base layer id. */
    public static final int LAYER_BASE = 0;

    /** The fringe layer id. */
    public static final int LAYER_FRINGE = 1;

    /** The object layer id. */
    public static final int LAYER_OBJECT = 2;

    /**
     * Returns the scene's unique identifier.
     */
    public int getId ();

    /**
     * Returns the scene's name. Every scene has a descriptive name.
     */
    public String getName ();

    /**
     * Return the tiles that comprise this scene.
     */
    public Tile[][][] getTiles ();

    /**
     * Return the default tile for the base layer of the scene.
     */
    public Tile getDefaultTile ();

    /**
     * Return the locations in this scene. The locations list should
     * contain all locations and portals in the scene. The list returned
     * by this method should <em>not</em> be modified.
     */
    public List getLocations ();

    /**
     * Return the clusters in this scene. The clusters will reference all
     * of the locations that are clustered. The list returned by this
     * method should <em>not</em> be modified.
     */
    public List getClusters ();

    /**
     * Return the portals associated with this scene. Portals should never
     * be part of a cluster. The list returned by this method should
     * <em>not</em> be modified.
     */
    public List getPortals ();

    /**
     * Return the portal that is the default entrance to this scene.
     */
    public Portal getEntrance ();
}
