//
// $Id: ObjectTileSet.java,v 1.2 2001/11/18 04:09:21 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.util.Arrays;

import com.threerings.media.ImageManager;

/**
 * The object tileset supports the specification of object information for
 * object tiles in addition to all of the features of the swiss army
 * tileset.
 *
 * @see ObjectTile
 */
public class ObjectTileSet extends SwissArmyTileSet
{
    /**
     * Adds object data for the tile at the specified index.
     *
     * @param tileIndex the tile for which we are adding object data.
     * @param objWidth the width of the object in tile units.
     * @param objHeight the height of the object in tile units.
     */
    public void addObjectData (int tileIndex, int objWidth, int objHeight)
    {
        // create our objects arrays if we've not already got them
        if (_owidths == null) {
            _owidths = new int[getTileCount()];
            _oheights = new int[_owidths.length];
            // initialize the default tile dimensions to one unit
            Arrays.fill(_owidths, 1);
            Arrays.fill(_oheights, 1);
        }

        // now fill in the appropriate slot
        _owidths[tileIndex] = objWidth;
        _oheights[tileIndex] = objHeight;
    }

    /**
     * Creates instances of {@link ObjectTile}, which can span more than a
     * single tile's space in a display.
     */
    protected Tile createTile (int tileIndex, Image image)
    {
        // default object dimensions to (1, 1)
        int wid = 1, hei = 1;

        // retrieve object dimensions if known
        if (_owidths != null) {
            wid = _owidths[tileIndex];
            wid = _oheights[tileIndex];
        }

        return new ObjectTile(image, wid, hei);
    }

    /** The width (in tile units) of our object tiles. */
    protected int[] _owidths;

    /** The height (in tile units) of our object tiles. */
    protected int[] _oheights;
}
