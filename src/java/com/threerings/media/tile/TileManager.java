//
// $Id: TileManager.java,v 1.20 2001/11/08 03:04:44 mdb Exp $

package com.threerings.media.tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.samskivert.util.HashIntMap;

import com.threerings.media.Log;
import com.threerings.media.ImageManager;

/**
 * The tile manager provides a simplified interface for retrieving and
 * caching tiles. Tiles can be loaded in two different ways. An
 * application can load a tileset by hand, specifying the path to the
 * tileset image and all of the tileset metadata necessary for extracting
 * the image tiles, or it can provide a tileset repository which loads up
 * tilesets using whatever repository mechanism is implemented by the
 * supplied repository. In the latter case, tilesets are loaded by a
 * unique identifier.
 *
 * <p> Loading tilesets by hand is intended for things like toolbar icons
 * or games with a single set of tiles (think Stratego, for example).
 * Loading tilesets from a repository supports games with vast numbers of
 * tiles to which more tiles may be added on the fly (think the tiles for
 * an isometric-display graphical MUD).
 *
 * <p> When the tile manager is used to load tiles via the tileset
 * repository, it caches the resulting tile instance so that they can be
 * fetched again without rebuilding the tile image. Tilesets that are
 * fetched by hand are not cached and it is assumed that the requesting
 * application will cache the tile objects itself (probably by retaining
 * references directly to the tile instances in which it is interested).
 * The tile creation process is not hugely expensive, but does involve
 * extracting the tile image from the larger tileset image.
 */
public class TileManager
{
    /**
     * Creates a tile manager and provides it with a reference to the
     * image manager from which it will load its tileset images.
     */
    public TileManager (ImageManager imgmgr)
    {
        // keep this guy around for later
        _imgmgr = imgmgr;
    }

    /**
     * Loads up a tileset from the specified image with the specified
     * metadata parameters.
     */
    public TileSet loadTileSet (
        String imgPath, int count, int width, int height)
    {
        return new UniformTileSet(_imgmgr, imgPath, count, width, height);
    }

    /**
     * Sets the tileset repository that will be used by the tile manager
     * when tiles are requested by tileset id.
     */
    public void setTileSetRepository (TileSetRepository tsrepo)
    {
        _tsrepo = tsrepo;
    }

    /**
     * Returns the tileset repository currently in use.
     */
    public TileSetRepository getTileSetRepository ()
    {
	return _tsrepo;
    }

    /**
     * Returns the {@link Tile} object for the specified tileset and
     * tile id, or null if an error occurred.
     *
     * @param tsid the tileset id.
     * @param tid the tile id.
     *
     * @return the tile object, or null if an error occurred.
     */
    public Tile getTile (int tsid, int tid)
	throws NoSuchTileSetException, NoSuchTileException
    {
        // make sure we have a repository configured
        if (_tsrepo == null) {
            throw new NoSuchTileSetException(tsid);
        }

	// the fully unique tile id is the conjoined tile set and tile id
	int utid = (tsid << 16) | tid;

	// look the tile up in our hash
	Tile tile = (Tile) _tiles.get(utid);
	if (tile != null) {
  	    // Log.info("Retrieved tile from cache [tsid=" + tsid +
	    // ", tid=" + tid + "].");
	    return tile;
	}

	// retrieve the tile from the tileset
	tile = _tsrepo.getTileSet(tsid).getTile(tid);
	if (tile != null) {
	    // Log.info("Loaded tile into cache [tsid=" + tsid +
	    // ", tid=" + tid + "].");
	    _tiles.put(utid, tile);
	}

	return tile;
    }

    /** The entity through which we load images. */
    protected ImageManager _imgmgr;

    /** Cache of tiles that have been requested thus far. */
    protected HashIntMap _tiles = new HashIntMap();

    /** The tile set repository. */
    protected TileSetRepository _tsrepo;
}
