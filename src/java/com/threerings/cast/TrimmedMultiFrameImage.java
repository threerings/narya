//
// $Id: TrimmedMultiFrameImage.java,v 1.2 2002/09/17 19:11:13 mdb Exp $

package com.threerings.cast;

import java.awt.Rectangle;

import com.threerings.media.util.MultiFrameImage;

/**
 * Used to generate more memory efficient composited images in
 * circumstances where we have trimmed underlying component images.
 */
public interface TrimmedMultiFrameImage extends MultiFrameImage
{
    /**
     * Fills in the minimum bounding rectangle for this image that
     * contains all non-transparent pixels. If this information is
     * unavailable, the bounds of the entire image may be returned in
     * exchange for improved performance.
     */
    public void getTrimmedBounds (int index, Rectangle bounds);
}
