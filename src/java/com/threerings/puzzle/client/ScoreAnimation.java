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

package com.threerings.puzzle.client;

import com.samskivert.swing.Label;
import com.threerings.media.animation.FloatingTextAnimation;

public class ScoreAnimation extends FloatingTextAnimation
{
    /**
     * Constructs a score animation for the given score value centered at
     * the given coordinates.
     */
    public ScoreAnimation (Label label, int x, int y)
    {
        super(label, x, y);
    }

    /**
     * Constructs a score animation for the given score value centered at
     * the given coordinates. The animation will float up the screen for
     * 30 pixels.
     */
    public ScoreAnimation (Label label, int x, int y, long floatPeriod)
    {
        super(label, x, y, floatPeriod);
    }

    /**
     * Constructs a score animation for the given score value starting at
     * the given coordinates and floating toward the specified
     * coordinates.
     */
    public ScoreAnimation (Label label, int sx, int sy,
                           int destx, int desty, long floatPeriod)
    {
        super(label, sx, sy, destx, desty, floatPeriod);
    }
}
