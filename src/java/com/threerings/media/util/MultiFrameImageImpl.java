//
// $Id: MultiFrameImageImpl.java,v 1.5 2003/01/13 22:49:47 mdb Exp $

package com.threerings.media.util;

import java.awt.Graphics2D;

import com.threerings.media.image.Mirage;

/**
 * A basic implementation of the {@link MultiFrameImage} interface
 * intended to facilitate the creation of MFIs whose display frames
 * consist of multiple image objects.
 */
public class MultiFrameImageImpl implements MultiFrameImage
{
    /**
     * Constructs a multiple frame image object.
     */
    public MultiFrameImageImpl (Mirage[] mirages)
    {
        _mirages = mirages;
    }

    // documentation inherited
    public int getFrameCount ()
    {
        return _mirages.length;
    }

    // documentation inherited from interface
    public int getWidth (int index)
    {
        return _mirages[index].getWidth();
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        return _mirages[index].getHeight();
    }

    // documentation inherited from interface
    public void paintFrame (Graphics2D g, int index, int x, int y)
    {
        _mirages[index].paint(g, x, y);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        return _mirages[index].hitTest(x, y);
    }

    /** The frame images. */
    protected Mirage[] _mirages;
}
