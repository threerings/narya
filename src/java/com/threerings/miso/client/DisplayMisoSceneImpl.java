//
// $Id: DisplayMisoSceneImpl.java,v 1.15 2001/08/02 01:19:47 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.io.*;

import com.samskivert.util.StringUtil;
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

    /** The object layer id. */
    public static final int LAYER_OBJECT = 1;

    /** The total number of layers. */
    public static final int NUM_LAYERS = 2;

    /** The latest scene file format version. */
    public static final short VERSION = 1;

    /** The scene width in tiles. */
    public static final int TILE_WIDTH = 55;

    /** The scene height in tiles. */
    public static final int TILE_HEIGHT = 55;

    /** String translations of each tile layer name. */
    public static final String[] XLATE_LAYERS = { "Base", "Object" };

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
        _hotspots = new Point[0];
        _exits = new ExitPoint[0];

	tiles = new Tile[TILE_WIDTH][TILE_HEIGHT][NUM_LAYERS];
	Tile tile = _tilemgr.getTile(deftsid, deftid);
	for (int xx = 0; xx < TILE_WIDTH; xx++) {
	    for (int yy = 0; yy < TILE_HEIGHT; yy++) {
		for (int ii = 0; ii < NUM_LAYERS; ii++) {
		    if (ii == LAYER_BASE) {
			tiles[xx][yy][ii] = tile;
		    }
		}
	    }
	}
    }

    /**
     * Construct a new Scene object with the given values, specifying
     * the tile manager from which the scene obtains tiles.
     *
     * @param tilemgr the tile manager.
     * @param name the scene name.
     * @param hotspots the hotspot points.
     * @param exits the exit points.
     * @param tiles the tiles comprising the scene.
     */
    public Scene (TileManager tilemgr, String name,
                  Point hotspots[], ExitPoint exits[],
                  Tile tiles[][][])
    {
        _tilemgr = tilemgr;
        _sid = SID_INVALID;
        _name = name;
        _hotspots = hotspots;
        _exits = exits;
        this.tiles = tiles;
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
     * Return the scene hot spots array.
     */
    public Point[] getHotSpots ()
    {
        return _hotspots;
    }

    /**
     * Return the scene exits array.
     */
    public ExitPoint[] getExitPoints ()
    {
        return _exits;
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
     * Return a string representation of this Scene object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[name=").append(_name);
        buf.append(", sid=").append(_sid);
        buf.append(", hotspots=").append(StringUtil.toString(_hotspots));
        buf.append(", exits=").append(StringUtil.toString(_exits));
        return buf.append("]").toString();
    }

    /** The default scene name. */
    protected static final String DEF_SCENE_NAME = "Untitled Scene";

    /** The scene name. */
    protected String _name;

    /** The unique scene id. */
    protected short _sid;

    /** Hot-spot zone points. */
    protected Point _hotspots[];

    /** Exit points to different scenes. */
    protected ExitPoint _exits[];

    /** The tile manager. */
    protected TileManager _tilemgr;
}
