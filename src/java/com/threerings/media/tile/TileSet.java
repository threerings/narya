//
// $Id: TileSet.java,v 1.30 2002/09/17 20:38:42 mdb Exp $

package com.threerings.media.tile;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.Serializable;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.util.Colorization;
import com.threerings.media.util.ImageUtil;

/**
 * A tileset stores information on a single logical set of tiles. It
 * provides a clean interface for the {@link TileManager} or other
 * entities to retrieve individual tiles from the tile set and
 * encapsulates the potentially sophisticated process of extracting the
 * tile image from a composite tileset image.
 *
 * <p> Tiles are referenced by their tile id.  The tile id is essentially
 * the tile number, assuming the tile at the top-left of the image is tile
 * id zero and tiles are numbered, in ascending order, left to right, top
 * to bottom.
 *
 * <p> This class is serializable and will be serialized, so derived
 * classes should be sure to mark non-persistent fields as
 * <code>transient</code>.
 */
public abstract class TileSet
    implements Cloneable, Serializable
{
    /**
     * Configures this tileset with an image provider that it can use to
     * load its tileset image. This will be called automatically when the
     * tileset is fetched via the {@link TileManager}.
     */
    public void setImageProvider (ImageProvider improv)
    {
        _improv = improv;
    }

    /**
     * Returns the tileset name.
     */
    public String getName ()
    {
	return _name;
    }

    /**
     * Specifies the tileset name.
     */
    public void setName (String name)
    {
        _name = name;
    }

    /**
     * Sets the path to the image that will be used by this tileset. This
     * must be called before the first call to {@link #getTile}.
     */
    public void setImagePath (String imagePath)
    {
        _imagePath = imagePath;

        // clear out any reference to a loaded image
        _tilesetImg = null;
    }

    /**
     * Returns the path to the composite image used by this tileset.
     */
    public String getImagePath ()
    {
        return _imagePath;
    }

    /**
     * Returns the number of tiles in the tileset.
     */
    public abstract int getTileCount ();

    /**
     * Creates a copy of this tileset with the supplied colorizations
     * applied to its source image.
     */
    public TileSet cloneColorized (Colorization[] zations)
    {
        TileSet tset = null;
        try {
            tset = (TileSet)clone();
        } catch (CloneNotSupportedException cnse) {
            Log.warning("Unable to clone tileset prior to colorization " +
                        "[tset=" + this +
                        ", zations=" + StringUtil.toString(zations) +
                        ", error=" + cnse + "].");
            return null;
        }

        // make sure the tileset was able to load its image
        Image timg = tset.getTileSetImage();
        if (timg == null) {
            Log.warning("Failed to load tileset image in preparation " +
                        "for colorization [tset=" + tset + "].");
            // return the uncolorized tileset since it has no freaking
            // source image anyway
            return tset;

        } else if (!(timg instanceof BufferedImage)) {
            Log.warning("Can't recolor tileset with non-buffered " +
                        "image source [source=" + timg + "].");
            return tset;
        }

        // create the recolored image and update the tileset
        tset._tilesetImg =
            ImageUtil.recolorImage((BufferedImage)timg, zations);

        return tset;
    }

    /**
     * Returns a new tileset that is a clone of this tileset with the
     * image path updated to reference the given path. Useful for
     * configuring a single tileset and then generating additional
     * tilesets with new images with the same configuration.
     */
    public TileSet clone (String imagePath)
        throws CloneNotSupportedException
    {
        TileSet dup = (TileSet)clone();
        dup.setImagePath(imagePath);
        return dup;
    }

    /**
     * Creates a {@link Tile} object from this tileset corresponding to
     * the specified tile id and returns that tile. A null tile will never
     * be returned, but one with an error image may be returned if a
     * problem occurs loading the underlying tileset image.
     *
     * @param tileIndex the index of the tile in the tileset. Tile indexes
     * start with zero as the upper left tile and increase by one as the
     * tiles move left to right and top to bottom over the source image.
     *
     * @return the tile object.
     *
     * @exception NoSuchTileException thrown if the specified tile index
     * is out of range for this tileset.
     */
    public Tile getTile (int tileIndex)
        throws NoSuchTileException
    {
	// bail if there's no such tile
	if (tileIndex < 0 || tileIndex >= getTileCount()) {
	    throw new NoSuchTileException(tileIndex);
	}

        // create our tile cache array if necessary
        if (_tiles == null) {
            _tiles = new Tile[getTileCount()];
        }

        // return the cached tile if possible
        if (_tiles[tileIndex] != null) {
            return _tiles[tileIndex];
        }

        // get our tileset image
        Image tsimg = getTileSetImage();
        if (tsimg == null) {
            // we already logged an error, so we can just freak out
            throw new NoSuchTileException(tileIndex);
        }

	// create, initialize and cache the tile object
        Tile tile = createTile(tileIndex, tsimg);
        initTile(tile);
        _tiles[tileIndex] = tile;

        return tile;
    }

    /**
     * Computes and returns the bounds for the specified tile based on the
     * mechanism used by the derived class to do such things. The width
     * and height of the bounds should be the size of the tile image and
     * the x and y offset should be the offset in the tileset image for
     * the image data of the specified tile.
     *
     * @param tileIndex the index of the tile whose bounds are to be
     * computed.
     * @param tilesetImage the tileset image that contains the imagery for
     * the tile in question.
     */
    protected abstract Rectangle computeTileBounds (
        int tileIndex, Image tilesetImage);

    /**
     * Creates a tile for the specified tile index.
     *
     * @param tileIndex the index of the tile to be created.
     * @param tilesetImage the tileset image that contains the imagery for
     * the tile to be created.
     *
     * @return a configured tile.
     */
    protected Tile createTile (int tileIndex, Image tilesetImage)
    {
        return new Tile(tilesetImage,
                        computeTileBounds(tileIndex, tilesetImage));
    }

    /**
     * Initializes the supplied tile. Derived classes can override this
     * method to add in their own tile information, but should be sure to
     * call <code>super.initTile()</code>.
     *
     * @param tile the tile to initialize.
     */
    protected void initTile (Tile tile)
    {
	// nothing for now
    }

    /**
     * Returns the tileset image (which is loaded if it has not yet been
     * loaded). Generally this is not called by external entities, rather
     * {@link #getTileImage} or {@link #getTile} is used.
     *
     * @return the tileset image or null if an error occurred loading the
     * image.
     */
    public Image getTileSetImage ()
    {
        // return it straight away if it's already loaded
	if (_tilesetImg != null) {
            return _tilesetImg;
        }

        // load up the tileset image via the image provider
        try {
            _tilesetImg = _improv.loadImage(_imagePath);

            // HACKOLA: if we're a vessel tileset, colorize ourselves
            if (_name != null && (this instanceof ObjectTileSet) &&
                _name.indexOf("Vessel") != -1) {
                _tilesetImg = ImageUtil.recolorImage(
                    (BufferedImage)_tilesetImg, ZATIONS);
            }

        } catch (IOException ioe) {
            Log.warning("Failed to retrieve tileset image " +
                        "[name=" + _name + ", path=" + _imagePath +
                        ", error=" + ioe + "].");
	}

        return _tilesetImg;
    }

    /**
     * Generates a string representation of the tileset information.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer("[");
        toString(buf);
	return buf.append("]").toString();
    }

    /**
     * Derived classes can override this, calling
     * <code>super.toString(buf)</code> and then appending additional
     * information to the buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("name=").append(_name);
	buf.append(", path=").append(_imagePath);
	buf.append(", tileCount=").append(getTileCount());
    }

    /** The path to the file containing the tile images. */
    protected String _imagePath;

    /** The tileset name. */
    protected String _name;

    /** A sparse array containing the tiles that have been requested from
     * this tileset. */
    protected Tile[] _tiles;

    /** The entity from which we obtain our tile image. */
    protected transient ImageProvider _improv;

    /** The image containing all tile images for this set. This is private
     * because it should be accessed via {@link #getTileSetImage} even by
     * derived classes. */
    private transient Image _tilesetImg;

    /** Temporary HACKOLA. */
    protected static Colorization[] ZATIONS = new Colorization[] {
        new Colorization(-1, new Color(0x8A1A76),
                         new float[] { .1f, .3f, 0.6f },
                         new float[] { -.85f, .3f, 0f }),
        new Colorization(-1, new Color(0x2A864E),
                         new float[] { .1f, .3f, 0.6f },
                         new float[] { .71f, 0f, .07f }),
    };
}
