//
// $Id: LinePath.java,v 1.1 2002/05/17 21:14:06 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.util.MathUtil;

/**
 * The line path is used to cause a sprite to go from point A to point B
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
    public void init (Sprite sprite, long timestamp)
    {
        // give the sprite a chance to perform any starting antics
        sprite.pathBeginning();

        // make a note of the time at which we expect to arrive
        _arrivalTime = timestamp + _duration;

        // pretend like we just moved the sprite
        _lastMove = timestamp;

        // update our position to the start of the path
        updatePosition(sprite, timestamp);
    }

    // documentation inherited
    public boolean updatePosition (Sprite sprite, long timestamp)
    {
        // if we've blown past our arrival time, we need to get our bootay
        // to the prearranged spot and get the hell out
        if (timestamp >= _arrivalTime) {
            sprite.setLocation(_dest.x, _dest.y);
            sprite.pathCompleted();
            return true;
        }

        // the number of milliseconds since we last moved (move delta)
        float dt = (float)(timestamp - _lastMove);
        // the number of milliseconds until we're expected to finish
        float rt = (float)(_arrivalTime - _lastMove);
        // how many pixels we have left to go
        int leftx = _dest.x - sprite.getX(), lefty = _dest.y - sprite.getY();

        // we want to move the sprite by the remaining distance multiplied
        // by the move delta divided by the remaining time
        int dx = Math.round((float)(leftx * dt) / rt);
        int dy = Math.round((float)(lefty * dt) / rt);

//         Log.info("Updated sprite [duration=" + _duration +
//                  ", dist=" + _distance + ", dt=" + dt + ", rt=" + rt +
//                  ", leftx=" + leftx + ", lefty=" + lefty +
//                  ", dx=" + dx + ", dy=" + dy + "].");

        // only update the sprite's location if it actually moved
        if (dx != 0 || dy != 0) {
            _lastMove = timestamp;
            sprite.setLocation(sprite.getX() + dx, sprite.getY() + dy);
            return true;
        }

        return false;
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _arrivalTime += timeDelta;
        _lastMove += timeDelta;
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

    /** The time at which we last actually moved the sprite. */
    protected long _lastMove;
}
