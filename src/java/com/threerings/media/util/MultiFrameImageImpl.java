//
// $Id: MultiFrameImageImpl.java,v 1.2 2002/05/04 19:35:31 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Graphics;
import java.awt.Image;

import com.threerings.media.util.ImageUtil;

/**
 * A basic implementation of the {@link MultiFrameImage} interface
 * intended to facilitate the creation of sprites whose display frames
 * consist of multiple image objects.
 */
public class MultiFrameImageImpl implements MultiFrameImage
{
    /**
     * Constructs a multiple frame image object.
     */
    public MultiFrameImageImpl (Image[] imgs)
    {
        _imgs = imgs;
    }

    // documentation inherited
    public int getFrameCount ()
    {
        return _imgs.length;
    }

    // documentation inherited from interface
    public int getWidth (int index)
    {
        return _imgs[index].getWidth(null);
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        return _imgs[index].getHeight(null);
    }

    // documentation inherited from interface
    public void paintFrame (Graphics g, int index, int x, int y)
    {
        g.drawImage(_imgs[index], x, y, null);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        return ImageUtil.hitTest(_imgs[index], x, y);
    }

    /** The frame images. */
    protected Image _imgs[];
}
