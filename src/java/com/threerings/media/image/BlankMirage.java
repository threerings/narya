//
// $Id: BlankMirage.java,v 1.2 2003/01/17 02:30:21 mdb Exp $

package com.threerings.media.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A mirage implementation that contains no image data. Generally only
 * useful for testing.
 */
public class BlankMirage implements Mirage
{
    public BlankMirage (int width, int height)
    {
        _width = width;
        _height = height;
    }

    // documentation inherited from interface
    public void paint (Graphics2D gfx, int x, int y)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public int getWidth ()
    {
        return _width;
    }

    // documentation inherited from interface
    public int getHeight ()
    {
        return _height;
    }

    // documentation inherited from interface
    public boolean hitTest (int x, int y)
    {
        return false;
    }

    // documentation inherited from interface
    public long getEstimatedMemoryUsage ()
    {
        return 0;
    }

    // documentation inherited from interface
    public BufferedImage getSnapshot ()
    {
        return null;
    }

    protected int _width;
    protected int _height;
}
