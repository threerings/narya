//
// $Id: MultiFrameImage.java,v 1.1 2001/08/14 23:35:22 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Image;

/**
 * The multi-frame image interface provides encapsulated access to a set
 * of images that are used to create a multi-frame animation.
 */
public interface MultiFrameImage
{
    /**
     * Returns the number of frames in this multi-frame image.
     */
    public int getFrameCount ();

    /**
     * Returns the image for the specified frame index.
     */
    public Image getFrame (int index);
}
