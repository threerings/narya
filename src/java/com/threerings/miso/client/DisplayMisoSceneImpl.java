//
// $Id: DisplayMisoSceneImpl.java,v 1.21 2001/08/10 01:31:25 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;

import com.samskivert.util.StringUtil;
import com.threerings.miso.Log;
import com.threerings.miso.tile.Tile;
import com.threerings.miso.tile.TileManager;

/**
 * A Scene object represents the data model corresponding to a single
 * screen for game play.  For instance, one scene might display a
 * portion of a street with several buildings scattered about on the
 * periphery.
 */
public class Scene
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
     * Construct a new Scene object.  The base layer tiles are
     * initialized to contain tiles of the specified default tileset
     * and tile id.
     *
     * @param tilemgr the tile manager.
     * @param deftsid the default tileset id.
     * @param deftid the default tile id.
     */
    public Scene (TileManager tilemgr, int deftsid, int deftid)
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
     * Construct a new Scene object with the given values.
     *
     * @param tilemgr the tile manager.
     * @param name the scene name.
     * @param locations the locations.
     * @param exits the exits.
     * @param tiles the tiles comprising the scene.
     */
    public Scene (TileManager tilemgr, String name,
                  ArrayList locations, ArrayList clusters, ArrayList exits,
                  Tile tiles[][][])
    {
        _tilemgr = tilemgr;
        _sid = SID_INVALID;
        _name = name;
        _locations = locations;
	_clusters = clusters;
        _exits = exits;
        this.tiles = tiles;
    }

    public void updateLocation (int x, int y, int orient, int clusteridx)
    {
	Log.info("updateLocation [x=" + x + ", y=" + y +
		 ", orient=" + orient + ", clusteridx=" + clusteridx + "].");

	// look the location up in our existing location list
	int size = _locations.size();
	Location loc = null;
	for (int ii = 0; ii < size; ii++) {
	    Location tloc = (Location)_locations.get(ii);

	    if (tloc.x == x && tloc.y == y) {
		// if we found it, update the location information
		tloc.x = x;
		tloc.y = y;
		tloc.orient = orient;

		// and update the cluster contents
		ClusterUtil.regroup(_clusters, tloc, clusteridx);

		return;
	    }
	}

	// else we didn't find a location object, so create one
	_locations.add(loc = new Location(x, y, orient));
	ClusterUtil.regroup(_clusters, loc, clusteridx);
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
    public short getId ()
    {
        return _sid;
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
