//
// $Id: TrimmedTileSet.java,v 1.8 2004/08/27 02:12:41 mdb Exp $
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

import java.io.IOException;
import java.io.OutputStream;

import com.threerings.media.image.Colorization;
import com.threerings.media.tile.util.TileSetTrimmer;

/**
 * Contains the necessary information to create a set of trimmed tiles
 * from a base image and the associated trim metrics.
 */
public class TrimmedTileSet extends TileSet
{
    // documentation inherited
    public int getTileCount ()
    {
        return _obounds.length;
    }

    // documentation inherited
    protected Rectangle computeTileBounds (int tileIndex)
    {
        return _obounds[tileIndex];
    }

    // documentation inherited
    protected Tile createTile ()
    {
        return new TrimmedTile();
    }

    // documentation inherited
    protected void initTile (Tile tile, int tileIndex, Colorization[] zations)
    {
        super.initTile(tile, tileIndex, zations);
        ((TrimmedTile)tile).setTrimmedBounds(_tbounds[tileIndex]);
    }

    /**
     * Creates a trimmed tileset from the supplied source tileset. The
     * image path must be set by hand to the appropriate path based on
     * where the image data that is written to the <code>destImage</code>
     * parameter is actually stored on the file system. See {@link
     * TileSetTrimmer#trimTileSet} for further information.
     */
    public static TrimmedTileSet trimTileSet (
        TileSet source, OutputStream destImage)
        throws IOException
    {
        final TrimmedTileSet tset = new TrimmedTileSet();
        tset.setName(source.getName());
        int tcount = source.getTileCount();
        tset._tbounds = new Rectangle[tcount];
        tset._obounds = new Rectangle[tcount];

        // grab the dimensions of the original tiles
        for (int ii = 0; ii < tcount; ii++) {
            tset._tbounds[ii] = source.computeTileBounds(ii);
        }

        // create the trimmed tileset image
        TileSetTrimmer.TrimMetricsReceiver tmr =
            new TileSetTrimmer.TrimMetricsReceiver() {
                public void trimmedTile (int tileIndex, int imageX, int imageY,
                                         int trimX, int trimY,
                                         int trimWidth, int trimHeight) {
                    tset._tbounds[tileIndex].x = trimX;
                    tset._tbounds[tileIndex].y = trimY;
                    tset._obounds[tileIndex] =
                        new Rectangle(imageX, imageY, trimWidth, trimHeight);
                }
            };
        TileSetTrimmer.trimTileSet(source, destImage, tmr);

        return tset;
    }

    /** The width and height of the trimmed tile, and the x and y offset
     * of the trimmed image within our tileset image. */
    protected Rectangle[] _obounds;

    /** The width and height of the untrimmed image and the x and y offset
     * within the untrimmed image at which the trimmed image should be
     * rendered. */
    protected Rectangle[] _tbounds;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
}
