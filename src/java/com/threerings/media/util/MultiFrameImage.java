//
// $Id: MultiFrameImage.java,v 1.4 2003/01/13 22:49:47 mdb Exp $

package com.threerings.media.util;

import java.awt.Graphics2D;

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
    public void paintFrame (Graphics2D g, int index, int x, int y);

    /**
     * Returns true if the specified frame contains a non-transparent
     * pixel at the specified coordinates.
     */
    public boolean hitTest (int index, int x, int y);
}
