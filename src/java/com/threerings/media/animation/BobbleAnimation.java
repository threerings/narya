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

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.samskivert.util.RandomUtil;
import com.threerings.media.image.Mirage;

/**
 * An animation that bobbles an image around within a specific horizontal
 * and vertical pixel range for a given period of time.
 */
public class BobbleAnimation extends Animation
{
    /**
     * Constructs a bobble animation.
     *
     * @param image the image to animate.
     * @param sx the starting x-position.
     * @param sy the starting y-position.
     * @param rx the horizontal bobble range.
     * @param ry the vertical bobble range.
     * @param duration the time to animate in milliseconds.
     */
    public BobbleAnimation (
        Mirage image, int sx, int sy, int rx, int ry, int duration)
    {
        super(new Rectangle(sx - rx, sy - ry, sx + image.getWidth() + rx,
                            sy + image.getHeight() + ry));

        // save things off
        _image = image;
        _sx = sx;
        _sy = sy;
        _rx = rx;
        _ry = ry;

        // calculate animation ending time
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
        invalidate();

        // calculate the latest position
        int dx = RandomUtil.getInt(_rx);
        int dy = RandomUtil.getInt(_ry);
        _x = (_sx + dx) * ((RandomUtil.getInt(2) == 0) ? -1 : 1);
        _y = (_sy + dy) * ((RandomUtil.getInt(2) == 0) ? -1 : 1);
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _end += timeDelta;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        _image.paint(gfx, _x, _y);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);

        buf.append(", x=").append(_x);
        buf.append(", y=").append(_y);
        buf.append(", rx=").append(_rx);
        buf.append(", ry=").append(_ry);
        buf.append(", sx=").append(_sx);
        buf.append(", sy=").append(_sy);
    }

    /** The current position of the image. */
    protected int _x, _y;

    /** The horizontal and vertical bobbling range. */
    protected int _rx, _ry;

    /** The starting position. */
    protected int _sx, _sy;

    /** The image to animate. */
    protected Mirage _image;

    /** Animation ending timing information. */
    protected long _duration, _end;
}
