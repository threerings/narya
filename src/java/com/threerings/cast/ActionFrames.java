//
// $Id: ActionFrames.java,v 1.2 2002/05/06 18:08:31 mdb Exp $

package com.threerings.cast;

import java.awt.Graphics;
import java.awt.Image;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.util.Colorization;
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
     * Creates a clone of these action frames which will have the supplied
     * colorizations applied to the frame images.
     */
    public ActionFrames cloneColorized (Colorization[] zations);
}
