//
// $Id: ActionFrames.java,v 1.7 2002/12/07 02:04:31 shaper Exp $

package com.threerings.cast;

import java.awt.Graphics;
import java.awt.Image;

import com.threerings.media.util.Colorization;
import com.threerings.util.DirectionCodes;

/**
 * Encapsulates a set of frames in each of {@link
 * DirectionCodes#DIRECTION_COUNT} orientations that are used to render a
 * character sprite.
 */
public interface ActionFrames
{
    /**
     * Returns the number of orientations available in this set of action
     * frames.
     */
    public int getOrientationCount ();

    /**
     * Returns the multi-frame image that comprises the frames for the
     * specified orientation.
     */
    public TrimmedMultiFrameImage getFrames (int orient);

    /**
     * Returns the x offset from the upper left of the image to the
     * "origin" for this character frame. A sprite with location (x, y)
     * will be rendered such that its origin is coincident with that
     * location.
     */
    public int getXOrigin (int orient, int frameIdx);

    /**
     * Returns the y offset from the upper left of the image to the
     * "origin" for this character frame. A sprite with location (x, y)
     * will be rendered such that its origin is coincident with that
     * location.
     */
    public int getYOrigin (int orient, int frameIdx);

    /**
     * Creates a clone of these action frames which will have the supplied
     * colorizations applied to the frame images.
     */
    public ActionFrames cloneColorized (Colorization[] zations);

    /**
     * Returns the estimated memory usage in bytes for all images cached
     * internally for use by the action frames.
     */
    public long getEstimatedMemoryUsage ();
}
