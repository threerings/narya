//
// $Id: DisplayMisoSceneImpl.java,v 1.12 2001/07/24 19:15:51 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.tile.Tile;
import com.threerings.miso.tile.TileManager;

import java.awt.Point;
import java.io.*;

/**
 * A scene represents the data model corresponding to a single screen
 * for game play.  For instance, one scene might display a portion of
 * a street with several buildings scattered about on the periphery.
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

    /** The tiles comprising the scene. */
    public Tile tiles[][][];

    /**
     * Construct a new Scene object initialized to a default state,
     * specifying the tile manager from which the scene obtains tiles.
     *
     * @param tilemgr the tile manager.
     */
    public Scene (TileManager tilemgr, int sid)
    {
	_tilemgr = tilemgr;
	_name = DEF_SCENE_NAME;
        _hotspots = new Point[0];
        _exits = new ExitPoint[0];

	tiles = new Tile[TILE_WIDTH][TILE_HEIGHT][NUM_LAYERS];
	Tile tile = _tilemgr.getTile(DEF_TSID, DEF_TID);
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

    /** The default scene name. */
    protected static final String DEF_SCENE_NAME = "Untitled Scene";

    /** The default tileset id. */
    protected static final short DEF_TSID = 1000;

    /** The default tile id. */
    protected static final short DEF_TID = 1;

    /** The scene name. */
    protected String _name;

    /** The unique scene id. */
    protected short _sid;

    /** Hot-spot zone points */
    protected Point _hotspots[];

    /** Exit points to different scenes. */
    protected ExitPoint _exits[];

    /** The tile manager. */
    protected TileManager _tilemgr;
}
