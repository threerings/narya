//
// $Id: MultiFrameImageImpl.java,v 1.1 2001/10/25 18:06:17 shaper Exp $

package com.threerings.media.sprite;

import java.awt.Image;

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
    public MultiFrameImageImpl (Image imgs[])
    {
        _imgs = imgs;
    }

    // documentation inherited
    public int getFrameCount ()
    {
        return _imgs.length;
    }

    // documentation inherited
    public Image getFrame (int index)
    {
        return _imgs[index];
    }

    /** The frame images. */
    protected Image _imgs[];
}
