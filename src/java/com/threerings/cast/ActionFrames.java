//
// $Id: ActionFrames.java,v 1.1 2002/05/04 19:38:13 mdb Exp $

package com.threerings.cast;

import java.awt.Graphics;
import java.awt.Image;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.util.DirectionCodes;

/**
 * Encapsulates a set of frames in each of {@link
 * DirectionCodes#DIRECTION_COUNT} orientations that are used to render a
 * character sprite.
 */
public interface ActionFrames extends MultiFrameImage
{
    /**
     * Updates the orientation that is currently being returned by the
     * {@link MultiFrameImage} implementation.
     */
    public void setOrientation (int orient);

    /**
     * Renders the specified frame at the specified offset applying the
     * supplied colorizations in the process. Note, the colorizations
     * should not be applied to the source image, only the rendered copy.
     */
    public void paintColoredFrame (
        Graphics g, int index, int x, int y, Colorization[] zations);
}
