//
// $Id: TileMultiFrameImage.java,v 1.1 2002/09/17 20:39:03 mdb Exp $

package com.threerings.media.tile;

import java.awt.Graphics;

import com.threerings.media.Log;
import com.threerings.media.util.MultiFrameImage;

/**
 * A {@link MultiFrameImage} implementation that obtains its image frames
 * from a tileset.
 */
public class TileMultiFrameImage implements MultiFrameImage
{
    /**
     * Creates a tile MFI which will obtain its image frames from the
     * specified source tileset.
     */
    public TileMultiFrameImage (TileSet source)
    {
        _source = source;
    }

    // documentation inherited from interface
    public int getFrameCount ()
    {
        return _source.getTileCount();
    }

    // documentation inherited from interface
    public int getWidth (int index)
    {
        try {
            return _source.getTile(index).getWidth();
        } catch (NoSuchTileException nste) {
            Log.warning("Eh? Tile set reported 'no such tile' " +
                        "[tcount=" + _source.getTileCount() +
                        ", tindex=" + index + "].");
            return -1;
        }
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        try {
            return _source.getTile(index).getHeight();
        } catch (NoSuchTileException nste) {
            Log.warning("Eh? Tile set reported 'no such tile' " +
                        "[tcount=" + _source.getTileCount() +
                        ", tindex=" + index + "].");
            return -1;
        }
    }

    // documentation inherited from interface
    public void paintFrame (Graphics g, int index, int x, int y)
    {
        try {
            _source.getTile(index).paint(g, x, y);
        } catch (NoSuchTileException nste) {
            Log.warning("Eh? Tile set reported 'no such tile' " +
                        "[tcount=" + _source.getTileCount() +
                        ", tindex=" + index + "].");
        }
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        try {
            return _source.getTile(index).hitTest(x, y);
        } catch (NoSuchTileException nste) {
            Log.warning("Eh? Tile set reported 'no such tile' " +
                        "[tcount=" + _source.getTileCount() +
                        ", tindex=" + index + "].");
            return false;
        }
    }

    protected TileSet _source;
}
