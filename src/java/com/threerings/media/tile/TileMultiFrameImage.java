//
// $Id: TileMultiFrameImage.java,v 1.2 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

import java.awt.Graphics2D;

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
        return _source.getTile(index).getWidth();
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        return _source.getTile(index).getHeight();
    }

    // documentation inherited from interface
    public void paintFrame (Graphics2D g, int index, int x, int y)
    {
        _source.getTile(index).paint(g, x, y);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        return _source.getTile(index).hitTest(x, y);
    }

    protected TileSet _source;
}
