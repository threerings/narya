//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.media.tile;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.util.StringUtil;
import com.samskivert.util.Throttle;

import com.threerings.media.Log;
import com.threerings.media.image.Colorization;
import com.threerings.media.image.Mirage;
import com.threerings.media.image.ImageUtil;
import com.threerings.media.image.BufferedMirage;
import com.threerings.media.util.MultiFrameImage;
import com.threerings.media.util.MultiFrameImageImpl;

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
    /** Used to assign colorizations to tiles that require them. */
    public static interface Colorizer
    {
        /**
         * Returns the colorization to be used for the specified named
         * colorization class.
         *
         * @param index the index of the colorization being requested in
         * the tileset's colorization list.
         */
        public Colorization getColorization (int index, String zation);
    }

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
	return (_name == null) ? _imagePath : _name;
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
     * Creates a copy of this tileset which will apply the supplied
     * colorizations to its tileset image when creating tiles.
     */
    public TileSet clone (Colorization[] zations)
    {
        try {
            TileSet tset = (TileSet)clone();
            tset._zations = zations;
            return tset;

        } catch (CloneNotSupportedException cnse) {
            Log.warning("Unable to clone tileset prior to colorization " +
                        "[tset=" + this +
                        ", zations=" + StringUtil.toString(zations) +
                        ", error=" + cnse + "].");
            return null;
        }
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
     * Equivalent to {@link #getTile(int,Colorizer)} with a null
     * <code>Colorizer</code> argument.
     */
    public Tile getTile (int tileIndex)
    {
        return getTile(tileIndex, _zations);
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
     * @param rizer an entity that will be used to obtain colorizations
     * for tilesets that are recolorizable. Passing null if the tileset is
     * known not to be recolorizable is valid.
     *
     * @return the tile object.
     */
    public Tile getTile (int tileIndex, Colorizer rizer)
    {
        return getTile(tileIndex, getColorizations(tileIndex, rizer));
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
     * @param zations colorizations to be applied to the tile image prior
     * to returning it. These may be null for uncolorized images.
     *
     * @return the tile object.
     */
    public Tile getTile (int tileIndex, Colorization[] zations)
    {
        Tile tile = null;

        // first look in the active set; if it's in use by anyone or in
        // the cache, it will be in the active set
        synchronized (_atiles) {
            _key.tileSet = this;
            _key.tileIndex = tileIndex;
            _key.zations = zations;
            SoftReference sref = (SoftReference)_atiles.get(_key);
            if (sref != null) {
                tile = (Tile)sref.get();
            }
        }

        // if it's not in the active set, it's not in memory; so load it
        if (tile == null) {
            tile = createTile();
            tile.key = new Tile.Key(this, tileIndex, zations);
            initTile(tile, tileIndex, zations);
            synchronized (_atiles) {
                _atiles.put(tile.key, new SoftReference(tile));
            }
        }

        // periodically report our image cache performance
        reportCachePerformance();

        return tile;
    }

    /**
     * Returns a prepared version of the image that would be used by the
     * tile at the specified index. Because tilesets are often used simply
     * to provide access to a collection of uniform images, this method is
     * provided to bypass the creation of a {@link Tile} object when all
     * that is desired is access to the underlying image.
     */
    public Mirage getTileMirage (int tileIndex)
    {
        return getTileMirage(tileIndex, getColorizations(tileIndex, null));
    }

    /**
     * Returns a prepared version of the image that would be used by the
     * tile at the specified index. Because tilesets are often used simply
     * to provide access to a collection of uniform images, this method is
     * provided to bypass the creation of a {@link Tile} object when all
     * that is desired is access to the underlying image.
     */
    public Mirage getTileMirage (int tileIndex, Colorization[] zations)
    {
        Rectangle bounds = computeTileBounds(tileIndex);
        Mirage mirage = null;
        if (checkTileIndex(tileIndex)) {
            if (_improv == null) {
                Log.warning("Aiya! Tile set missing image provider " +
                            "[path=" + _imagePath + "].");
            } else {
                mirage = _improv.getTileImage(_imagePath, bounds, zations);
            }
        }
        if (mirage == null) {
            mirage = new BufferedMirage(
                ImageUtil.createErrorImage(bounds.width, bounds.height));
        }
        return mirage;
    }

    /**
     * Returns the entire, raw, uncut, unprepared tileset source image.
     * Don't use this method unless you know what you're doing! This image
     * should not be rendered directly to the screen, you should obtain a
     * tile ({@link #getTile}), or a tile mirage ({@link #getTileMirage}).
     */
    public BufferedImage getRawTileSetImage ()
    {
        return _improv.getTileSetImage(_imagePath, _zations);
    }

    /**
     * Returns the raw (unprepared) image that would be used by the tile
     * at the specified index. Don't use this method unless you know what
     * you're doing! If you're going to be painting this image onto the
     * screen directly, use {@link #getTileMirage} because that prepares
     * the image for display. Only use this if you're going to do further
     * processing and prepare the subsequent image for display onscreen.
     */
    public BufferedImage getRawTileImage (int tileIndex)
    {
        Rectangle bounds = computeTileBounds(tileIndex);
        BufferedImage img = null;
        if (checkTileIndex(tileIndex)) {
            BufferedImage timg = getRawTileSetImage();
            if (timg != null) {
                img = timg.getSubimage(bounds.x, bounds.y,
                                       bounds.width, bounds.height);
            } else {
                Log.warning("Missing source image " + this);
            }
        }
        if (img == null) {
            img = ImageUtil.createErrorImage(bounds.width, bounds.height);
        }
        return img;
    }

    /**
     * Returns colorizations for the specified tile image. The default is
     * to return any colorizations associated with the tileset via a call
     * to {@link #clone(Colorization[])}, however derived classes may have
     * dynamic colorization policies that look up colorization assignments
     * from the supplied colorizer.
     */
    protected Colorization[] getColorizations (int tileIndex, Colorizer rizer)
    {
        return _zations;
    }

    /**
     * Used to ensure that the specified tile index is valid.
     */
    protected boolean checkTileIndex (int tileIndex)
    {
        int tcount = getTileCount();
	if (tileIndex >= 0 && tileIndex < tcount) {
            return true;
        } else {
            Log.warning("Requested invalid tile [tset=" + this +
                        ", index=" + tileIndex + "].");
            Thread.dumpStack();
            return false;
        }
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
     */
    protected abstract Rectangle computeTileBounds (int tileIndex);

    /**
     * Creates a blank tile of the appropriate type for this tileset.
     *
     * @return a blank tile ready to be populated with its image and
     * metadata.
     */
    protected Tile createTile ()
    {
        return new Tile();
    }

    /**
     * Initializes the supplied tile. Derived classes can override this
     * method to add in their own tile information, but should be sure to
     * call <code>super.initTile()</code>.
     *
     * @param tile the tile to initialize.
     * @param tileIndex the index of the tile.
     * @param zations the colorizations to be used when generating the
     * tile image.
     */
    protected void initTile (Tile tile, int tileIndex, Colorization[] zations)
    {
        if (_improv != null) {
            tile.setImage(getTileMirage(tileIndex, zations));
        }
    }

    /**
     * Generates a string representation of the tileset information.
     */
    public String toString ()
    {
	StringBuilder buf = new StringBuilder("[");
        toString(buf);
	return buf.append("]").toString();
    }

    /**
     * Reports statistics detailing the image manager cache performance
     * and the current size of the cached images.
     */
    protected void reportCachePerformance ()
    {
        if (/* Log.getLevel() != Log.log.DEBUG || */
            _improv == null ||
            _cacheStatThrottle.throttleOp()) {
            return;
        }

        // compute our estimated memory usage
        long amem = 0;
        int asize = 0;
        synchronized (_atiles) {
            // first total up the active tiles
            Iterator iter = _atiles.values().iterator();
            while (iter.hasNext()) {
                SoftReference sref = (SoftReference)iter.next();
                Tile tile = (Tile)sref.get();
                if (tile != null) {
                    asize++;
                    amem += tile.getEstimatedMemoryUsage();
                }
            }
        }
        Log.info("Tile caches [amem=" + (amem / 1024) + "k" +
                 ", tmem=" + (Tile._totalTileMemory / 1024) + "k" +
                 ", seen=" + _atiles.size() + ", asize=" + asize + "].");
    }

    /**
     * Derived classes can override this, calling
     * <code>super.toString(buf)</code> and then appending additional
     * information to the buffer.
     */
    protected void toString (StringBuilder buf)
    {
        buf.append("name=").append(_name);
	buf.append(", path=").append(_imagePath);
	buf.append(", tileCount=").append(getTileCount());
    }

    /** The path to the file containing the tile images. */
    protected String _imagePath;

    /** The tileset name. */
    protected String _name;

    /** Colorizations to be applied to tiles created from this tileset. */
    protected transient Colorization[] _zations;

    /** The entity from which we obtain our tile image. */
    protected transient ImageProvider _improv;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;

    /** A map containing weak references to all "active" tiles. */
    protected static HashMap _atiles = new HashMap();

    /** A key used to look things up in the cache without creating
     * craploads of keys unduly. */
    protected static Tile.Key _key = new Tile.Key(null, 0, null);

    /** Throttle our cache status logging to once every 300 seconds. */
    protected static Throttle _cacheStatThrottle = new Throttle(1, 300000L);
}
