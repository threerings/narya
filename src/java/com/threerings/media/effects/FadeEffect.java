//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.media.effects;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
 * Handles the math and timing of doing a fade effect in a sprite or animation.
 */
public class FadeEffect
{
    public FadeEffect (float alpha, float step, float target)
    {
        if ((step == 0f) || (alpha > target && step > 0f) ||
                (alpha < target && step < 0f)) {
            throw new IllegalArgumentException("Step specified is illegal: " +
                "Fade would never finish (start=" + alpha + ", step=" + step +
                ", target=" + target + ")");
        }

        // save things off
        _startAlpha = alpha; 
        _step = step;
        _target = target;

        // create the initial composite
        _alpha = Math.max(0f, Math.min(1f, _startAlpha));
        _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);
    }

    public void init (long tickStamp)
    {
        _finished = false;
        _initStamp = tickStamp;
    }

    public boolean finished ()
    {
        return _finished;
    }

    public float getAlpha ()
    {
        return _alpha;
    }

    public boolean tick (long tickStamp)
    {
        // figure out the current alpha
        long msecs = tickStamp - _initStamp;
        float alpha = _startAlpha + (msecs * _step);
        _finished = (_startAlpha < _target) ? (alpha >= _target)
                                            : (alpha <= _target);
        if (alpha < 0.0f) {
            alpha = 0.0f;
        } else if (alpha > 1.0f) {
            alpha = 1.0f;
        }

        if (_alpha != alpha) {
            _alpha = alpha;
            _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);
            return true;
        }

        // return false, unless we're finished
        return _finished;
    }

    public void beforePaint (Graphics2D gfx)
    {
        _ocomp = gfx.getComposite();
        gfx.setComposite(_comp);
    }

    public void afterPaint (Graphics2D gfx)
    {
        gfx.setComposite(_ocomp);
    }

    /** The composite used to render the image with the current alpha. */
    protected Composite _comp;

    /** The composite in effect outside our faded render. */
    protected Composite _ocomp;

    /** The current alpha of the image. */
    protected float _alpha;

    /** The target alpha. */
    protected float _target;

    /** The alpha step per millisecond. */
    protected float _step;

    /** The starting alpha. */
    protected float _startAlpha;

    /** Time zero. */
    protected long _initStamp;

    /** True when we're finished. */
    protected boolean _finished;
}
