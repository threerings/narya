//
// $Id: LinePath.java,v 1.5 2002/06/11 00:03:30 mdb Exp $

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

        // make a note of when we started
        _startStamp = timestamp;

        // update our position to the start of the path
        tick(pable, timestamp);
    }

    // documentation inherited
    public boolean tick (Pathable pable, long timestamp)
    {
        // if we've blown past our arrival time, we need to get our bootay
        // to the prearranged spot and get the hell out
        if (timestamp >= _startStamp + _duration) {
            pable.setLocation(_dest.x, _dest.y);
            pable.pathCompleted();
            return true;
        }

        // determine where we should be along the path
        float pct = (timestamp - _startStamp) / (float)_duration;
        int travx = Math.round((_dest.x - _source.x) * pct);
        int travy = Math.round((_dest.y - _source.y) * pct);
        int nx = _source.x + travx, ny = _source.y + travy;

//         Log.info("Updated pathable [duration=" + _duration + ", pct=" + pct +
//                  ", travx=" + travx + ", travy=" + travy +
//                  ", newx=" + nx + ", newy=" + ny + "].");

        // only update the pathable's location if it actually moved
        int cx = pable.getX(), cy = pable.getY();
        if (cx != nx || cy != ny) {
            pable.setLocation(nx, ny);
            return true;
        }

        return false;
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _startStamp += timeDelta;
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

    /** The time at which we started along the path. */
    protected long _startStamp;

    /** The duration that we're to spend following the path. */
    protected long _duration;
}
