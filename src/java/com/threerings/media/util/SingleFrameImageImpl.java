//
// $Id: SingleFrameImageImpl.java,v 1.4 2003/01/08 04:09:03 mdb Exp $

package com.threerings.media.util;

import java.awt.Graphics;
import java.awt.Image;

import com.threerings.media.image.ImageUtil;

/**
 * The single frame image class is a basic implementation of the {@link
 * MultiFrameImage} interface intended to facilitate the creation of MFIs
 * whose display frames consist of only a single image.
 */
public class SingleFrameImageImpl implements MultiFrameImage
{
    /**
     * Constructs a single frame image object.
     */
    public SingleFrameImageImpl (Image img)
    {
        _img = img;
    }

    // documentation inherited
    public int getFrameCount ()
    {
        return 1;
    }

    // documentation inherited from interface
    public int getWidth (int index)
    {
        return _img.getWidth(null);
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        return _img.getHeight(null);
    }

    // documentation inherited from interface
    public void paintFrame (Graphics g, int index, int x, int y)
    {
        g.drawImage(_img, x, y, null);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        return ImageUtil.hitTest(_img, x, y);
    }

    /** The frame image. */
    protected Image _img;
}
