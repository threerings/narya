//
// $Id: TileSet.java,v 1.19 2001/11/08 03:04:44 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.*;

import com.threerings.media.Log;
import com.threerings.media.ImageManager;

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
 */
public abstract class TileSet implements Cloneable
{
    /**
     * Provides the basic information needed to load a tileset image to
     * the tileset base class.
     *
     * @param imgmgr an image manager from which the tileset image can be
     * loaded.
     * @param imgPath the path to the tileset image.
     * @param name the name of the tileset (optional, can be null).
     * @param tsid the unique integer identifier of the tileset (optional,
     * can be zero if the tileset is not to be loaded by id).
     */
    public TileSet (ImageManager imgmgr, String imgPath,
                    String name, int tsid)
    {
        _imgmgr = imgmgr;
        _imgPath = imgPath;
        _name = name;
        _tsid = tsid;
    }

    /**
     * Returns the tileset identifier.
     */
    public int getId ()
    {
	return _tsid;
    }

    /**
     * Returns the tileset name.
     */
    public String getName ()
    {
	return _name;
    }

    /**
     * Returns the number of tiles in the tileset.
     */
    public abstract int getTileCount ();

    /**
     * Sets the image file to be used as the source for the tile
     * images produced by this tileset.
     */
    public void setImageFile (String imgPath)
    {
        _imgPath = imgPath;
        _tilesetImg = null;
    }

    /**
     * Returns a new tileset that is a clone of this tileset with the
     * image file updated to reference the given file name. Useful for
     * configuring a single tileset and then generating additional
     * tilesets with new images with the same configuration.
     */
    public TileSet clone (String imgPath)
        throws CloneNotSupportedException
    {
        TileSet dup = (TileSet)clone();
        dup.setImageFile(imgPath);
        return dup;
    }

    /**
     * Creates a @link Tile} object from this tileset corresponding to the
     * specified tile id and returns that tile, or null if an error
     * occurred.
     *
     * @param tileId the tile identifier. Tile identifiers start with zero
     * as the upper left tile and increase by one as the tiles move left
     * to right and top to bottom over the source image.
     *
     * @return the tile object, or null if an error occurred.
     *
     * @exception NoSuchTileException thrown if the specified tile id is
     * out of range for this tileset.
     */
    public Tile getTile (int tileId)
        throws NoSuchTileException
    {
	// bail if there's no such tile
	if (tileId < 0 || tileId > (getTileCount() - 1)) {
	    throw new NoSuchTileException(tileId);
	}

	// create and populate the tile object
	Tile tile = createTile(tileId);

	// retrieve the tile image
	tile.img = getTileImage(tile.tid);
	if (tile.img == null) {
	    Log.warning("Null tile image [tile=" + tile + "].");
            return null;
	}

	// populate the tile's dimensions
        BufferedImage bimg = (BufferedImage)tile.img;
	tile.height = (short)bimg.getHeight();
        tile.width = (short)bimg.getWidth();

	// allow sub-classes to fill in their tile information
	populateTile(tile);

	return tile;
    }

    /**
     * Construct and return a new tile object for further population with
     * tile-specific information. Derived classes can override this method
     * to create their own sub-class of {@link Tile}.
     *
     * @param tileId the tile id for the new tile.
     *
     * @return the new tile object.
     */
    protected Tile createTile (int tileId)
    {
        // construct a basic tile
	return new Tile(_tsid, tileId);
    }

    /**
     * Returns the image corresponding to the specified tile within this
     * tileset.
     *
     * @param tileId the index of the tile to be retrieved.
     *
     * @return the tile image.
     */
    protected abstract Image getTileImage (int tileId);

    /**
     * Populates the given tile object with its detailed tile
     * information.  Derived classes can override this method to add
     * in their own tile information, but should be sure to call
     * <code>super.populateTile()</code>.
     *
     * @param tile the tile to populate.
     */
    protected void populateTile (Tile tile)
    {
	// nothing for now
    }

    /**
     * Returns the tileset image (which is loaded if it has not yet been
     * loaded).
     *
     * @return the tileset image or null if an error occurred loading the
     * image.
     */
    protected Image getTilesetImage ()
    {
        // return it straight away if it's already loaded
	if (_tilesetImg != null) {
            return _tilesetImg;
        }

        // load up the tileset image via the image manager
        if ((_tilesetImg = _imgmgr.getImage(_imgPath)) == null) {
            Log.warning("Failed to retrieve tileset image " +
                        "[tsid=" + _tsid + ", path=" + _imgPath + "].");
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
     * Tileset derived classes should override this, calling
     * <code>super.toString(buf)</code> and then appending additional
     * information to the buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("name=").append(_name);
	buf.append(", tsid=").append(_tsid);
	buf.append(", path=").append(_imgPath);
	buf.append(", tileCount=").append(getTileCount());
    }

    /** The default image manager for retrieving tile images. */
    protected ImageManager _imgmgr;

    /** The path to the file containing the tile images. */
    protected String _imgPath;

    /** The tileset name. */
    protected String _name;

    /** The tileset unique identifier. */
    protected int _tsid;

    /** The image containing all tile images for this set. This is private
     * because it should be accessed via {@link #getTilesetImage} even by
     * derived classes. */
    private Image _tilesetImg;
}
