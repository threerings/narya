//
// $Id: TileManager.java,v 1.22 2001/11/30 02:34:57 mdb Exp $

package com.threerings.media.tile;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.samskivert.io.PersistenceException;
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
public class TileManager implements ImageProvider
{
    /**
     * Creates a tile manager and provides it with a reference to the
     * image manager from which it will load tileset images.
     *
     * @param imgr the image manager via which the tile manager will
     * decode and cache images.
     */
    public TileManager (ImageManager imgr)
    {
        // keep this guy around for later
        _imgr = imgr;
    }

    /**
     * Loads up a tileset from the specified image with the specified
     * metadata parameters.
     */
    public TileSet loadTileSet (
        String imgPath, int count, int width, int height)
    {
        UniformTileSet uts = new UniformTileSet();
        uts.setImageProvider(this);
        uts.setImagePath(imgPath);
        uts.setTileCount(count);
        uts.setWidth(width);
        uts.setHeight(height);
        return uts;
    }

    /**
     * Sets the tileset repository that will be used by the tile manager
     * when tiles are requested by tileset id.
     */
    public void setTileSetRepository (TileSetRepository setrep)
    {
        _setrep = setrep;
    }

    /**
     * Returns the tileset repository currently in use.
     */
    public TileSetRepository getTileSetRepository ()
    {
	return _setrep;
    }

    /**
     * Returns the tileset with the specified id. Tilesets are fetched
     * from the tileset repository supplied via {@link
     * #setTileSetRepository}, and are subsequently cached.
     *
     * @param tileSetId the unique identifier for the desired tileset.
     *
     * @exception NoSuchTileSetException thrown if no tileset exists with
     * the specified id or if an underlying error occurs with the tileset
     * repository's persistence mechanism.
     */
    public TileSet getTileSet (int tileSetId)
        throws NoSuchTileSetException
    {
        // make sure we have a repository configured
        if (_setrep == null) {
            throw new NoSuchTileSetException(tileSetId);
        }

        try {
            TileSet set = (TileSet)_cache.get(tileSetId);
            if (set == null) {
                set = _setrep.getTileSet(tileSetId);
                _cache.put(tileSetId, set);
            }
            return set;

        } catch (PersistenceException pe) {
            Log.warning("Unable to load tileset [id=" + tileSetId +
                        ", error=" + pe + "].");
            throw new NoSuchTileSetException(tileSetId);
        }
    }

    /**
     * Returns the {@link Tile} object from the specified tileset at the
     * specified index.
     *
     * @param tileSetId the tileset id.
     * @param tileIndex the index of the tile to be retrieved.
     *
     * @return the tile object.
     */
    public Tile getTile (int tileSetId, int tileIndex)
	throws NoSuchTileSetException, NoSuchTileException
    {
        TileSet set = getTileSet(tileSetId);
        return set.getTile(tileIndex);
    }

    // documentation inherited
    public BufferedImage loadImage (String path)
        throws IOException
    {
        // load up the image data from the resource manager
        return _imgr.getImage(path);
    }

    /** The entity through which we decode and cache images. */
    protected ImageManager _imgr;

    /** Cache of tilesets that have been requested thus far. */
    protected HashIntMap _cache = new HashIntMap();

    /** The tile set repository. */
    protected TileSetRepository _setrep;
}
