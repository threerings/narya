//
// $Id: DisplayMisoSceneImpl.java,v 1.28 2001/08/15 01:08:49 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;

import com.samskivert.util.StringUtil;

import com.threerings.whirled.data.Scene;

import com.threerings.miso.Log;
import com.threerings.miso.tile.Tile;
import com.threerings.miso.tile.TileManager;
import com.threerings.miso.scene.util.ClusterUtil;

/**
 * A scene object represents the data model corresponding to a single
 * screen for game play. For instance, one scene might display a portion
 * of a street with several buildings scattered about on the periphery.
 */
public class MisoScene implements Scene
{
    /** The base layer id. */
    public static final int LAYER_BASE = 0;

    /** The fringe layer id. */
    public static final int LAYER_FRINGE = 1;

    /** The object layer id. */
    public static final int LAYER_OBJECT = 2;

    /** The total number of layers. */
    public static final int NUM_LAYERS = 3;

    /** The latest scene file format version. */
    public static final short VERSION = 1;

    /** The scene width in tiles. */
    public static final int TILE_WIDTH = 22;

    /** The scene height in tiles. */
    public static final int TILE_HEIGHT = 22;

    /** String translations of each tile layer name. */
    public static final String[] XLATE_LAYERS = { "Base", "Fringe", "Object" };

    /** Scene id to denote an unset or otherwise invalid scene id. */
    public static final short SID_INVALID = -1;

    /** The tiles comprising the scene. */
    public Tile tiles[][][];

    /**
     * Construct a new miso scene object. The base layer tiles are
     * initialized to contain tiles of the specified default tileset and
     * tile id.
     *
     * @param tilemgr the tile manager.
     * @param deftsid the default tileset id.
     * @param deftid the default tile id.
     */
    public MisoScene (TileManager tilemgr, int deftsid, int deftid)
    {
	_tilemgr = tilemgr;

	_sid = SID_INVALID;
	_name = DEF_SCENE_NAME;

        _locations = new ArrayList();
	_clusters = new ArrayList();
        _exits = new ArrayList();

	tiles = new Tile[TILE_WIDTH][TILE_HEIGHT][NUM_LAYERS];
	_deftile = _tilemgr.getTile(deftsid, deftid);
	for (int xx = 0; xx < TILE_WIDTH; xx++) {
	    for (int yy = 0; yy < TILE_HEIGHT; yy++) {
		for (int ii = 0; ii < NUM_LAYERS; ii++) {
		    if (ii == LAYER_BASE) {
			tiles[xx][yy][ii] = _deftile;
		    }
		}
	    }
	}
    }

    /**
     * Construct a new miso scene object with the given values.
     *
     * @param tilemgr the tile manager.
     * @param name the scene name.
     * @param locations the locations.
     * @param exits the exits.
     * @param tiles the tiles comprising the scene.
     */
    public MisoScene (
        TileManager tilemgr, String name, ArrayList locations,
        ArrayList clusters, ArrayList exits, Tile tiles[][][])
    {
        _tilemgr = tilemgr;
        _sid = SID_INVALID;
        _name = name;
        _locations = locations;
	_clusters = clusters;
        _exits = exits;
        this.tiles = tiles;
    }

    /**
     * Return the cluster index number the given location is in, or -1
     * if the location is not in any cluster.
     *
     * @param loc the location.
     */
    public int getClusterIndex (Location loc)
    {
	return ClusterUtil.getClusterIndex(_clusters, loc);
    }

    /**
     * Update the specified location in the scene.  If the cluster
     * index number is -1, the location will be removed from any
     * cluster it may reside in.
     *
     * @param loc the location.
     * @param clusteridx the cluster index number.
     */
    public void updateLocation (Location loc, int clusteridx)
    {
	// add the location if it's not already present
	if (!_locations.contains(loc)) {
	    _locations.add(loc);
	}

	// update the cluster contents
	ClusterUtil.regroup(_clusters, loc, clusteridx);
    }

    /**
     * Add the specified exit to the scene.  Adds the exit to the
     * location list as well if it's not already present and removes
     * it from any cluster it may reside in.
     *
     * @param exit the exit.
     */
    public void addExit (Exit exit)
    {
	// make sure it's in the location list and absent from any cluster
	updateLocation(exit, -1);

	// don't allow adding an exit more than once
	if (_exits.contains(exit)) {
	    Log.warning("Attempt to add already-existing exit " +
			"[exit=" + exit + "].");
	    return;
	}

	// add it to the list
	_exits.add(exit);
    }

    /**
     * Return the location object at the given full coordinates, or
     * null if no location is currently present.
     *
     * @param x the full x-position coordinate.
     * @param y the full y-position coordinate.
     *
     * @return the location object.
     */
    public Location getLocation (int x, int y)
    {
	int size = _locations.size();
	for (int ii = 0; ii < size; ii++) {
	    Location loc = (Location)_locations.get(ii);
	    if (loc.x == x && loc.y == y) return loc;
	}
	return null;
    }

    /**
     * Remove the given location object from the location list, and
     * from any containing cluster.  If the location is an exit, it
     * is removed from the exit list as well.
     *
     * @param loc the location object.
     */
    public void removeLocation (Location loc)
    {
	// remove from the location list
	if (!_locations.remove(loc)) {
	    // we didn't know about it, so it can't be in a cluster or
	    // the exit list
	    return;
	}

	// remove from any possible cluster
	ClusterUtil.remove(_clusters, loc);

	// remove from any possible existence on the exit list
	_exits.remove(loc);
    }

    /**
     * Return the scene name.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Return the scene identifier.
     */
    public int getId ()
    {
        return _sid;
    }

    /**
     * Returns this scene's version number (which is incremented when it
     * is modified and stored into the scene repository).
     */
    public int getVersion ()
    {
        // fake it for now
        return 1;
    }

    /**
     * Returns the scene ids of the exits from this scene.
     */
    public int[] getExitIds ()
    {
        return null;
    }

    /**
     * Return the scene locations list.
     */
    public ArrayList getLocations ()
    {
        return _locations;
    }

    /**
     * Return the cluster list.
     */
    public ArrayList getClusters ()
    {
	return _clusters;
    }

    /**
     * Return the scene exits list.
     */
    public ArrayList getExits ()
    {
        return _exits;
    }

    /**
     * Return the number of clusters in the scene.
     */
    public int getNumClusters ()
    {
	return _clusters.size();
    }

    /**
     * Return the number of actual (non-null) tiles present in the
     * specified tile layer for this scene.
     */
    public int getNumLayerTiles (int lnum)
    {
	if (lnum == LAYER_BASE) return TILE_WIDTH * TILE_HEIGHT;

	int numTiles = 0;

	for (int xx = 0; xx < TILE_WIDTH; xx++) {
	    for (int yy = 0; yy < TILE_HEIGHT; yy++) {
		if (tiles[xx][yy] != null) numTiles++;
	    }
	}

	return numTiles;
    }

    /**
     * Return the default tile for the base layer of the scene.
     */
    public Tile getDefaultTile ()
    {
	return _deftile;
    }

    /**
     * Return a string representation of this Scene object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[name=").append(_name);
        buf.append(", sid=").append(_sid);
        buf.append(", locations=").append(StringUtil.toString(_locations));
        buf.append(", clusters=").append(StringUtil.toString(_clusters));
        buf.append(", exits=").append(StringUtil.toString(_exits));
        return buf.append("]").toString();
    }

    /** The default scene name. */
    protected static final String DEF_SCENE_NAME = "Untitled Scene";

    /** The scene name. */
    protected String _name;

    /** The unique scene id. */
    protected short _sid;

    /** The locations within the scene. */
    protected ArrayList _locations;

    /** The clusters within the scene. */
    protected ArrayList _clusters;

    /** The exits to different scenes. */
    protected ArrayList _exits;

    /** The default tile for the base layer in the scene. */
    protected Tile _deftile;

    /** The tile manager. */
    protected TileManager _tilemgr;
}
