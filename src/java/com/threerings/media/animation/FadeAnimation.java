//
// $Id: FadeAnimation.java,v 1.8 2004/08/27 02:12:38 mdb Exp $
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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.Log;
import com.threerings.media.image.Mirage;

/**
 * An animation that displays an image fading from one alpha level to
 * another in specified increments over time.  The animation is finished
 * when the specified target alpha is reached.
 */
public class FadeAnimation extends Animation
{
    /**
     * Constructs a fade animation.
     *
     * @param image the image to animate.
     * @param x the image x-position.
     * @param y the image y-position.
     * @param alpha the starting alpha.
     * @param step the alpha amount to step by each millisecond.
     * @param target the target alpha level.
     */
    public FadeAnimation (
        Mirage image, int x, int y, float alpha, float step, float target)
    {
        super(new Rectangle(x, y, image.getWidth(), image.getHeight()));

        // save things off
        _image = image;
        _x = x;
        _y = y;
        _startAlpha = _alpha = alpha;
        _step = step;
        _target = target;

        // create the initial composite
        _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        if (_start == 0) {
            // initialize our starting time
            _start = timestamp;
        }

        // figure out the current alpha
        long msecs = timestamp - _start;
        _alpha = _startAlpha + (msecs * _step);
        if (_alpha < 0.0f) {
            _alpha = 0.0f;
        } else if (_alpha > 1.0f) {
            _alpha = 1.0f;
        }

        _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);

        // check whether we're done
        _finished = ((_startAlpha < _target) ? (_alpha >= _target) :
                     (_alpha <= _target));

        // dirty ourselves
        invalidate();
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        if (_start > 0) {
            _start += timeDelta;
        }
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        Composite ocomp = gfx.getComposite();
        if (_comp == null) {
            Log.warning("Fade anim has null composite [anim=" + this + "].");
        } else {
            gfx.setComposite(_comp);
        }
        _image.paint(gfx, _x, _y);
        gfx.setComposite(ocomp);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);

        buf.append(", x=").append(_x);
        buf.append(", y=").append(_y);
        buf.append(", alpha=").append(_alpha);
        buf.append(", startAlpha=").append(_startAlpha);
        buf.append(", step=").append(_step);
        buf.append(", target=").append(_target);
    }

    /** The composite used to render the image with the current alpha. */
    protected Composite _comp;

    /** The current alpha of the image. */
    protected float _alpha;

    /** The target alpha. */
    protected float _target;

    /** The alpha step per millisecond. */
    protected float _step;

    /** The starting alpha. */
    protected float _startAlpha;

    /** The image position. */
    protected int _x, _y;

    /** The image to animate. */
    protected Mirage _image;

    /** The starting animation time. */
    protected long _start;
}
