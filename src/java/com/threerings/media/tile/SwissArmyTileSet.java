//
// $Id: SwissArmyTileSet.java,v 1.9 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.IOException;
import java.io.ObjectInputStream;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;

/**
 * The swiss army tileset supports a diverse variety of tiles in the
 * tileset image. Each row can contain varying numbers of tiles and each
 * row can have its own width and height. Tiles can be separated from the
 * edge of the tileset image by some border offset and can be separated
 * from one another by a gap distance.
 */
public class SwissArmyTileSet extends TileSet
{
    // documentation inherited
    public int getTileCount ()
    {
	return _numTiles;
    }

    /**
     * Sets the tile counts which are the number of tiles in each row of
     * the tileset image. Each row can have an arbitrary number of tiles.
     */
    public void setTileCounts (int[] tileCounts)
    {
        _tileCounts = tileCounts;

        // compute our total tile count
        computeTileCount();
    }

    /**
     * Returns the tile count settings.
     */
    public int[] getTileCounts ()
    {
        return _tileCounts;
    }

    /**
     * Computes our total tile count from the individual counts for each
     * row.
     */
    protected void computeTileCount ()
    {
        // compute our number of tiles
        _numTiles = 0;
        for (int i = 0; i < _tileCounts.length; i++) {
            _numTiles += _tileCounts[i];
        }
    }

    /**
     * Sets the tile widths for each row. Each row can have tiles of a
     * different width.
     */
    public void setWidths (int[] widths)
    {
        _widths = widths;
    }

    /**
     * Returns the width settings.
     */
    public int[] getWidths ()
    {
        return _widths;
    }

    /**
     * Sets the tile heights for each row. Each row can have tiles of a
     * different height.
     */
    public void setHeights (int[] heights)
    {
        _heights = heights;
    }

    /**
     * Returns the height settings.
     */
    public int[] getHeights ()
    {
        return _heights;
    }

    /**
     * Sets the offset in pixels of the upper left corner of the first
     * tile in the first row. If the tileset image has a border, this can
     * be set to account for it.
     */
    public void setOffsetPos (Point offsetPos)
    {
        _offsetPos = offsetPos;
    }

    /**
     * Sets the size of the gap between tiles (in pixels). If the tiles
     * have space between them, this can be set to account for it.
     */
    public void setGapSize (Dimension gapSize)
    {
        _gapSize = gapSize;
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", widths=").append(StringUtil.toString(_widths));
	buf.append(", heights=").append(StringUtil.toString(_heights));
	buf.append(", tileCounts=").append(StringUtil.toString(_tileCounts));
	buf.append(", offsetPos=").append(StringUtil.toString(_offsetPos));
	buf.append(", gapSize=").append(StringUtil.toString(_gapSize));
    }

    // documentation inherited
    protected Rectangle computeTileBounds (int tileIndex)
    {
	// find the row number containing the sought-after tile
	int ridx, tcount, ty, tx;
	ridx = tcount = 0;

        // start tile image position at image start offset
        tx = _offsetPos.x;
        ty = _offsetPos.y;

	while ((tcount += _tileCounts[ridx]) < tileIndex + 1) {
            // increment tile image position by row height and gap distance
	    ty += (_heights[ridx++] + _gapSize.height);
	}

        // determine the horizontal index of this tile in the row
	int xidx = tileIndex - (tcount - _tileCounts[ridx]);

        // final image x-position is based on tile width and gap distance
        tx += (xidx * (_widths[ridx] + _gapSize.width));

// 	Log.info("Computed tile bounds [tileIndex=" + tileIndex +
//                  ", ridx=" + ridx + ", xidx=" + xidx +
//                  ", tx=" + tx + ", ty=" + ty + "].");

	// crop the tile-sized image chunk from the full image
        return new Rectangle(tx, ty, _widths[ridx], _heights[ridx]);
    }

    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        // compute our total tile count
        computeTileCount();
    }

    /** The number of tiles in each row. */
    protected int[] _tileCounts;

    /** The number of tiles in the tileset. */
    protected int _numTiles;

    /** The width of the tiles in each row in pixels. */
    protected int[] _widths;

    /** The height of the tiles in each row in pixels. */
    protected int[] _heights;

    /** The offset distance (x, y) in pixels from the top-left of the
     * image to the start of the first tile image.  */
    protected Point _offsetPos = new Point();

    /** The distance (x, y) in pixels between each tile in each row
     * horizontally, and between each row of tiles vertically.  */
    protected Dimension _gapSize = new Dimension();
}
