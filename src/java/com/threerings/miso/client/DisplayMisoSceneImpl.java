//
// $Id: DisplayMisoSceneImpl.java,v 1.10 2001/07/23 22:31:47 shaper Exp $

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
     * Return the scene exit points array.
     */
    public ExitPoint[] getExits ()
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
     * Populate the scene object by reading the contents from the given
     * input stream.
     */
    public void readFrom (InputStream in) throws IOException
    {
	DataInputStream dis = new DataInputStream(in);

	// read scene header information
	_name    = dis.readUTF();
	_sid     = dis.readShort();
	short version = dis.readShort();

	// make sure we can understand the file format
	if (version < 0 || version > VERSION) {
	    throw new IOException("Can't understand scene file format " +
				  " [version=" + version + "]");
	}

	// allocate full tile array.  null tiles denote tiles in absentia.
	tiles = new Tile[TILE_WIDTH][TILE_HEIGHT][NUM_LAYERS];

	// read all tiles for the base layer
	for (int xx = 0; xx < TILE_WIDTH; xx++) {
	    for (int yy = 0; yy < TILE_HEIGHT; yy++) {
		// read tile details
		short tsid = dis.readShort();
		short tid = dis.readShort();

		// retrieve tile from tile manager
		tiles[xx][yy][LAYER_BASE] = _tilemgr.getTile(tsid, tid);
	    }
	}

	// read tiles for each of the subsequent layers
	for (int lnum = 1; lnum < NUM_LAYERS; lnum++) {
	    // read the number of tiles in this layer
	    int numTiles = dis.readShort();

	    for (int ii = 0; ii < numTiles; ii++) {
		// read tile details
		short tsid = dis.readShort();
		short tid = dis.readShort();
		byte tx = dis.readByte();
		byte ty = dis.readByte();

		// retrieve tile from tile manager
		tiles[tx][ty][lnum] = _tilemgr.getTile(tsid, tid);
	    }
	}
	
	// read hotspot points
	short numSpots = dis.readShort();
	_hotspots = new Point[numSpots];
	for (int ii = 0; ii < numSpots; ii++) {
	    _hotspots[ii] = new Point();
	    _hotspots[ii].x = dis.readByte();
	    _hotspots[ii].y = dis.readByte();
	}

	// read exit points
	short numExits = dis.readShort();
	_exits = new ExitPoint[numExits];
	for (int ii = 0; ii < numExits; ii++) {
	    _exits[ii] = new ExitPoint();
	    _exits[ii].x = dis.readByte();
	    _exits[ii].y = dis.readByte();
	    _exits[ii].sid = dis.readShort();
	}
    }

    /**
     * Write this scene object to the specified output stream.
     */
    public void writeTo (OutputStream out) throws IOException
    {
	DataOutputStream dos = new DataOutputStream(out);

	// write scene header information
	dos.writeUTF(_name);
	dos.writeShort(_sid);
	dos.writeShort(VERSION);

	// write tiles for the base layer
	for (int xx = 0; xx < TILE_WIDTH; xx++) {
	    for (int yy = 0; yy < TILE_HEIGHT; yy++) {
		Tile tile = tiles[xx][yy][LAYER_BASE];
		dos.writeShort(tile.tsid);
		dos.writeShort(tile.tid);
	    }
	}

	// write tiles for each of the subsequent layers
	for (int lnum = 1; lnum < NUM_LAYERS; lnum++) {
	    // write the number of tiles in this layer
	    dos.writeShort(getNumLayerTiles(lnum));

	    for (int xx = 0; xx < TILE_WIDTH; xx++) {
		for (int yy = 0; yy < TILE_HEIGHT; yy++) {
		    Tile tile = tiles[xx][yy][lnum];
		    if (tile != null) {
			dos.writeShort(tile.tsid);
			dos.writeShort(tile.tid);
		    }
		}
	    }
	}

	// write hotspot points
	int numSpots = (_hotspots == null) ? 0 : _hotspots.length;
	dos.writeShort(numSpots);
	for (int ii = 0; ii < numSpots; ii++) {
	    dos.writeByte(_hotspots[ii].x);
	    dos.writeByte(_hotspots[ii].y);
	}
	
	// write exit points
	int numExits = (_exits == null) ? 0 : _exits.length;
	dos.writeShort(numExits);
	for (int ii = 0; ii < numExits; ii++) {
	    dos.writeByte(_exits[ii].x);
	    dos.writeByte(_exits[ii].y);
	    dos.writeByte(_exits[ii].sid);
	}
    }

    /** The latest scene file format version. */
    protected static final short VERSION = 1;

    /** The scene width in tiles. */
    protected static final int TILE_WIDTH = 55;

    /** The scene height in tiles. */
    protected static final int TILE_HEIGHT = 55;

    /** The base layer id. */
    protected static final int LAYER_BASE = 0;

    /** The object layer id. */
    protected static final int LAYER_OBJECT = 1;

    /** The total number of layers. */
    protected static final int NUM_LAYERS = 2;

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
