//
// $Id: SingleFrameImageImpl.java,v 1.1 2001/10/25 18:06:17 shaper Exp $

package com.threerings.media.sprite;

import java.awt.Image;

/**
 * The single frame image class is a basic implementation of the
 * {@link MultiFrameImage} interface intended to facilitate the
 * creation of sprites whose display frames consist of only a single
 * image.
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

    // documentation inherited
    public Image getFrame (int index)
    {
        return _img;
    }

    /** The frame image. */
    protected Image _img;
}
