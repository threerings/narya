//
// $Id: BobblePath.java,v 1.5 2004/02/25 14:43:17 mdb Exp $

package com.threerings.media.util;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.util.RandomUtil;

/**
 * Bobble a Pathable.
 */
public class BobblePath implements Path
{
    /**
     * Construct a bobble path that updates as often as possible.
     *
     * @param dx the variance in the x direction.
     * @param dy the variance in the y direction.
     * @param duration the duration to bobble, or -1 to bobble until
     * stop() is called.
     */
    public BobblePath (int dx, int dy, long duration)
    {
        this(dx, dy, duration, 1L);
    }

    /**
     * Construct a bobble path.
     *
     * @param dx the variance in the x direction.
     * @param dy the variance in the y direction.
     * @param duration the duration to bobble, or -1 to bobble until
     * stop() is called.
     * @param updateFreq how often to update the Pathable's location.
     */
    public BobblePath (int dx, int dy, long duration, long updateFreq)
    {
        _updateFreq = updateFreq;
        _duration = duration;
        setVariance(dx, dy);
    }

    /**
     * Set the variance of bobblin' in each direction.
     */
    public void setVariance (int dx, int dy)
    {
        if ((dx < 0) || (dy < 0) || ((dx == 0) && (dy == 0))) {
            throw new IllegalArgumentException(
                "Variance values must be positive, " +
                "and at least one must be non-zero.");
        } else {
            _dx = dx;
            _dy = dy;
        }
    }

    /**
     * Set a new update frequency.
     */
    public void setUpdateFrequency (long freq)
    {
        _updateFreq = freq;
    }

    /**
     * Have the Pathable stop bobbling asap.
     */
    public void stop ()
    {
        // it will stop on the next tick..
        _stopTime = 0L;
    }

    // documentation inherited from interface Path
    public void init (Pathable pable, long tickstamp)
    {
        _sx = pable.getX();
        _sy = pable.getY();
        // change the duration to a real stop time
        if (_duration == -1L) {
            _stopTime = Long.MAX_VALUE;
        } else {
            _stopTime = tickstamp + _duration;
        }
        _nextMove = tickstamp;
    }

    // documentation inherited from interface Path
    public boolean tick (Pathable pable, long tickStamp)
    {
        // see if we need to stop
        if (_stopTime <= tickStamp) {
            boolean updated = updatePositionTo(pable, _sx, _sy);
            pable.pathCompleted(tickStamp);
            return updated;
        }

        // see if it's time to move..
        if (_nextMove > tickStamp) {
            return false;
        }

        // when bobbling, it's bad form to bobble into the same position
        int newx, newy;
        do {
            newx = _sx + RandomUtil.getInt(_dx * 2 + 1) - _dx;
            newy = _sy + RandomUtil.getInt(_dy * 2 + 1) - _dy;
        } while (! updatePositionTo(pable, newx, newy));

        // and update the next time to move
        _nextMove = tickStamp + _updateFreq;
        return true;
    }

    // documentation inherited from interface Path
    public void fastForward (long timeDelta)
    {
        _stopTime += timeDelta;
        _nextMove += timeDelta;
    }

    // documentation inherited from interface Path
    public void paint (Graphics2D gfx)
    {
        // for debugging, show the bobble bounds
        gfx.setColor(Color.RED);
        gfx.drawRect(_sx - _dx, _sy - _dy, _dx * 2, _dy * 2);
    }

    // documentation inherited from interface
    public void wasRemoved (Pathable pable)
    {
        // reset the pathable to its initial location
        pable.setLocation(_sx, _sy);
    }

    /**
     * Update the position of the pathable or return false
     * if it's already there.
     */
    protected boolean updatePositionTo (Pathable pable, int x, int y)
    {
        if ((pable.getX() == x) && (pable.getY() == y)) {
            return false;
        } else {
            pable.setLocation(x, y);
            return true;
        }
    }

    /** The initial position of the pathable. */
    protected int _sx, _sy;

    /** The variance we will bobble around that initial position. */
    protected int _dx, _dy;

    /** How long we'll bobble. */
    protected long _duration;

    /** How often we update the locations. */
    protected long _updateFreq;

    /** The time at which we'll stop pathin'. */
    protected long _stopTime;

    /** The time at which we'll next update the position of the pathable. */
    protected long _nextMove = 0L;
}
