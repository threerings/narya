//
// $Id: TileSet.java,v 1.22 2001/11/27 08:40:34 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.Serializable;

import com.threerings.media.Log;

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

	// create and initialize the tile object
	Tile tile = createTile(tileIndex, checkedGet(tileIndex));
        initTile(tile);
        return tile;
    }

    /**
     * Returns the tile image at the specified index. In some cases, a
     * tile object is not desired or required, and so this method can be
     * used to fetch the image directly. A null tile image will never be
     * returned, but an error image may be returned if a problem occurs
     * loading the underlying tileset image.
     *
     * @param tileIndex the index of the image in the tileset.
     *
     * @return the tile image.
     *
     * @exception NoSuchTileException thrown if the specified tile index
     * is out of range for this tileset.
     */
    public Image getTileImage (int tileIndex)
        throws NoSuchTileException
    {
	// bail if there's no such tile
	if (tileIndex < 0 || tileIndex >= getTileCount()) {
	    throw new NoSuchTileException(tileIndex);
	}

	// retrieve the tile image
        return checkedGet(tileIndex);
    }

    // used to ensure TileSet derivations adhere to the extractTileImage()
    // policy of not returning null
    private Image checkedGet (int tileIndex)
    {
        Image image = extractTileImage(tileIndex);
	if (image == null) {
            String errmsg = "TileSet implementation violated return " +
                "policy for TileSet.extractTileImage().";
            throw new RuntimeException(errmsg);
	}
        return image;
    }

    /**
     * Extracts the image corresponding to the specified tile from the
     * tileset image.
     *
     * @param tileIndex the index of the tile to be retrieved.
     *
     * @return the tile image. This should not return null in cases of
     * failure, but should instead call {@link #createErrorImage} to
     * return a valid image.
     */
    protected abstract Image extractTileImage (int tileIndex);

    /**
     * Creates a blank image to be used in failure situations. If {@link
     * #extractTileImage} is unable to return the actual tile image
     * (because the tileset image could not be loaded or for some other
     * reason), it should not return null. Instead it should return an
     * error image created with this method.
     *
     * @param width the width of the error image in pixels.
     * @param height the height of the error image in pixels.
     */
    protected Image createErrorImage (int width, int height)
    {
        // return a blank image for now
        return new BufferedImage(width, height,
                                 BufferedImage.TYPE_BYTE_INDEXED);
    }

    /**
     * Construct and return a new tile object for further population with
     * tile-specific information. Derived classes can override this method
     * to create their own sub-class of {@link Tile}.
     *
     * @param tileIndex the index of the tile being created.
     * @param image the tile image.
     *
     * @return the new tile object.
     */
    protected Tile createTile (int tileIndex, Image image)
    {
        // construct a basic tile
	return new Tile(image);
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
     * loaded).
     *
     * @return the tileset image or null if an error occurred loading the
     * image.
     */
    protected BufferedImage getTilesetImage ()
    {
        // return it straight away if it's already loaded
	if (_tilesetImg != null) {
            return _tilesetImg;
        }

        // load up the tileset image via the image provider
        try {
            _tilesetImg = _improv.loadImage(_imagePath);

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
     * Tileset derived classes should override this, calling
     * <code>super.toString(buf)</code> and then appending additional
     * information to the buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("name=").append(_name);
	buf.append(", path=").append(_imagePath);
	buf.append(", tileCount=").append(getTileCount());
    }

    /** The entity from which we obtain our tile image. */
    protected transient ImageProvider _improv;

    /** The path to the file containing the tile images. */
    protected String _imagePath;

    /** The tileset name. */
    protected String _name;

    /** The image containing all tile images for this set. This is private
     * because it should be accessed via {@link #getTilesetImage} even by
     * derived classes. */
    private transient BufferedImage _tilesetImg;
}
