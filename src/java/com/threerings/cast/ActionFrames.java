//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.cast;

import com.threerings.media.image.Colorization;
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
}
