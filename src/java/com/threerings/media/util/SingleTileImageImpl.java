//
// $Id: SingleTileImageImpl.java,v 1.1 2003/01/13 22:49:47 mdb Exp $

package com.threerings.media.util;

import java.awt.Graphics2D;

import com.threerings.media.tile.Tile;

/**
 * The single frame image class is a basic implementation of the {@link
 * MultiFrameImage} interface intended to facilitate the creation of MFIs
 * whose display frames consist of only a single tile image.
 */
public class SingleTileImageImpl implements MultiFrameImage
{
    /**
     * Constructs a single frame image object.
     */
    public SingleTileImageImpl (Tile tile)
    {
        _tile = tile;
    }

    // documentation inherited
    public int getFrameCount ()
    {
        return 1;
    }

    // documentation inherited from interface
    public int getWidth (int index)
    {
        return _tile.getWidth();
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        return _tile.getHeight();
    }

    // documentation inherited from interface
    public void paintFrame (Graphics2D g, int index, int x, int y)
    {
        _tile.paint(g, x, y);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        return _tile.hitTest(x, y);
    }

    /** The frame image. */
    protected Tile _tile;
}
