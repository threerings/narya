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

package com.threerings.media.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.samskivert.util.RandomUtil;

/**
 * An animation that displays raindrops spattering across an image.
 */
public class RainAnimation extends Animation
{
    /**
     * Constructs a rain animation with reasonable defaults for the number
     * of raindrops and their dimensions.
     *
     * @param bounds the bounding rectangle for the animation.
     * @param duration the number of seconds the animation should last.
     */
    public RainAnimation (Rectangle bounds, long duration)
    {
        super(bounds);

        init(duration, DEFAULT_COUNT, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Constructs a rain animation.
     *
     * @param bounds the bounding rectangle for the animation.
     * @param duration the number of seconds the animation should last.
     * @param count the number of raindrops to render in each frame.
     * @param wid the width of a raindrop's bounding rectangle.
     * @param hei the height of a raindrop's bounding rectangle.
     */
    public RainAnimation (
        Rectangle bounds, long duration, int count, int wid, int hei)
    {
        super(bounds);
        init(duration, count, wid, hei);
    }

    protected void init (long duration, int count, int wid, int hei)
    {
        // save things off
        _count = count;
        _wid = wid;
        _hei = hei;

        // create the raindrop array
        _drops = new int[count];

        // calculate ending time
        _duration = duration;
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        // grab our ending time on our first tick
        if (_end == 0L) {
            _end = tickStamp + _duration;
        }

        _finished = (tickStamp >= _end);

        // calculate the latest raindrop locations
        for (int ii = 0; ii < _count; ii++) {
            int x = RandomUtil.getInt(_bounds.width);
            int y = RandomUtil.getInt(_bounds.height);
            _drops[ii] = (x << 16 | y);
        }

        invalidate();
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _end += timeDelta;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(Color.white);
        for (int ii = 0; ii < _count; ii++) {
            int x = _drops[ii] >> 16;
            int y = _drops[ii] & 0xFFFF;
            gfx.drawLine(x, y, x + _wid, y + _hei);
        }
    }

    /** The number of raindrops. */
    protected static final int DEFAULT_COUNT = 300;

    /** The raindrop streak dimensions. */
    protected static final int DEFAULT_WIDTH = 13;
    protected static final int DEFAULT_HEIGHT = 10;

    /** The number of raindrops. */
    protected int _count;

    /** The dimensions of each raindrop's bounding rectangle. */
    protected int _wid, _hei;

    /** The raindrop locations. */
    protected int[] _drops;

    /** Animation ending timing information. */
    protected long _duration, _end;
}
