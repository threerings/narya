//
// $Id: BlankAnimation.java,v 1.1 2002/12/29 04:32:14 mdb Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Displays nothing, but does so for a specified amount of time. Useful
 * when you want to get an animation completed event in some period of
 * time but don't actually need to display anything.
 */
public class BlankAnimation extends Animation
{
    public BlankAnimation (long duration)
    {
        super(new Rectangle(0, 0, 0, 0));
        _duration = duration;
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        if (_start == 0) {
            // initialize our starting time
            _start = timestamp;
        }

        // check whether we're done
        _finished = (timestamp - _start >= _duration);
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
        // nothing doing
    }

    protected long _duration, _start;
}
