//
// $Id: MultiFrameImage.java,v 1.3 2002/09/17 19:11:13 mdb Exp $

package com.threerings.media.util;

import java.awt.Graphics;
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
     * Returns the width of the specified frame image.
     */
    public int getWidth (int index);

    /**
     * Returns the height of the specified frame image.
     */
    public int getHeight (int index);

    /**
     * Renders the specified frame into the specified graphics object at
     * the specified coordinates.
     */
    public void paintFrame (Graphics g, int index, int x, int y);

    /**
     * Returns true if the specified frame contains a non-transparent
     * pixel at the specified coordinates.
     */
    public boolean hitTest (int index, int x, int y);
}
