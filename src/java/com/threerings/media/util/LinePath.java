//
// $Id: LinePath.java,v 1.3 2002/05/31 20:47:32 mdb Exp $

package com.threerings.media.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.util.MathUtil;

/**
 * The line path is used to cause a pathable to go from point A to point B
 * in a certain number of milliseconds.
 */
public class LinePath implements Path
{
    /**
     * Constructs a line path between the two specified points that will
     * be followed in the specified number of milliseconds.
     */
    public LinePath (int x1, int y1, int x2, int y2, long duration)
    {
        this(new Point(x1, y1), new Point(x2, y2), duration);
    }

    /**
     * Constructs a line path between the two specified points that will
     * be followed in the specified number of milliseconds.
     */
    public LinePath (Point source, Point dest, long duration)
    {
        // sanity check some things
        if (duration <= 0) {
            String errmsg = "Requested path with illegal duration (<=0) " +
                "[duration=" + duration + "]";
            throw new IllegalArgumentException(errmsg);
        }

        _source = source;
        _dest = dest;
        _duration = duration;
        _distance = (int)MathUtil.distance(_source, _dest);
    }

    // documentation inherited from interface
    public void viewWillScroll (int dx, int dy)
    {
        // adjust our source and destination points
        _source.translate(-dx, -dy);
        _dest.translate(-dx, -dy);
    }

    // documentation inherited
    public void init (Pathable pable, long timestamp)
    {
        // give the pable a chance to perform any starting antics
        pable.pathBeginning();

        // make a note of the time at which we expect to arrive
        _arrivalTime = timestamp + _duration;

        // pretend like we just moved the pathable
        _lastMoveX = _lastMoveY = timestamp;

        // update our position to the start of the path
        tick(pable, timestamp);
    }

    // documentation inherited
    public boolean tick (Pathable pable, long timestamp)
    {
        // if we've blown past our arrival time, we need to get our bootay
        // to the prearranged spot and get the hell out
        if (timestamp >= _arrivalTime) {
            pable.setLocation(_dest.x, _dest.y);
            pable.pathCompleted();
            return true;
        }

        // the number of milliseconds since we last moved (move delta)
        float dtX = (float)(timestamp - _lastMoveX);
        float dtY = (float)(timestamp - _lastMoveY);
        // the number of milliseconds until we're expected to finish
        float rtX = (float)(_arrivalTime - _lastMoveX);
        float rtY = (float)(_arrivalTime - _lastMoveY);
        // how many pixels we have left to go
        int leftx = _dest.x - pable.getX(), lefty = _dest.y - pable.getY();

        // we want to move the pathable by the remaining distance
        // multiplied by the move delta divided by the remaining time and
        // we update our last move stamps if there is movement to be had
        // in either direction
        int dx = Math.round((float)(leftx * dtX) / rtX);
        if (dx != 0) {
            _lastMoveX = timestamp;
        }
        int dy = Math.round((float)(lefty * dtY) / rtY);
        if (dy != 0) {
            _lastMoveY = timestamp;
        }

//         Log.info("Updated pathable [duration=" + _duration +
//                  ", dist=" + _distance + ", dt=" + dt + ", rt=" + rt +
//                  ", leftx=" + leftx + ", lefty=" + lefty +
//                  ", dx=" + dx + ", dy=" + dy + "].");

        // only update the pathable's location if it actually moved
        if (dx != 0 || dy != 0) {
            pable.setLocation(pable.getX() + dx, pable.getY() + dy);
            return true;
        }

        return false;
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _arrivalTime += timeDelta;
        _lastMoveX += timeDelta;
        _lastMoveY += timeDelta;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
	gfx.setColor(Color.red);
        gfx.drawLine(_source.x, _source.y, _dest.x, _dest.y);
    }

    // documentation inherited
    public String toString ()
    {
        return "[src=" + StringUtil.toString(_source) +
            ", dest=" + StringUtil.toString(_dest) +
            ", duration=" + _duration + "ms]";
    }

    /** Our source and destination points. */
    protected Point _source, _dest;

    /** The direct pixel distance along the path. */
    protected int _distance;

    /** The duration that we're to spend following the path. */
    protected long _duration;

    /** The time at which we expect to complete our path. */
    protected long _arrivalTime;

    /** The time at which we last actually moved the pathable in the x
     * direction. */
    protected long _lastMoveX;

    /** The time at which we last actually moved the pathable in the y
     * direction. */
    protected long _lastMoveY;
}
