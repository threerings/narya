//
// $Id: LinePath.java,v 1.14 2004/08/27 02:12:47 mdb Exp $
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

package com.threerings.media.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import com.samskivert.util.StringUtil;

/**
 * The line path is used to cause a pathable to go from point A to point B
 * in a certain number of milliseconds.
 */
public class LinePath extends TimedPath
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
        super(duration);
        _source = source;
        _dest = dest;
    }

    /**
     * Constructs a line path that moves a pathable from
     * whatever its location is at init time to the dest point over
     * the specified number of milliseconds.
     */
    public LinePath (Point dest, long duration)
    {
        this(null, dest, duration);
    }

    // documentation inherited
    public void init (Pathable pable, long timestamp)
    {
        super.init(pable, timestamp);

        // fill in the source if necessary.
        if (_source == null) {
            _source = new Point(pable.getX(), pable.getY());
        }
    }

    // documentation inherited
    public boolean tick (Pathable pable, long timestamp)
    {
        // if we've blown past our arrival time, we need to get our bootay
        // to the prearranged spot and get the hell out
        if (timestamp >= _startStamp + _duration) {
            pable.setLocation(_dest.x, _dest.y);
            pable.pathCompleted(timestamp);
            return true;
        }

        // determine where we should be along the path
        long elapsed = timestamp - _startStamp;
        computePosition(_source, _dest, elapsed, _duration, _tpos);

        // only update the pathable's location if it actually moved
        if (pable.getX() != _tpos.x || pable.getY() != _tpos.y) {
            pable.setLocation(_tpos.x, _tpos.y);
            return true;
        }

        return false;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
	gfx.setColor(Color.red);
        gfx.drawLine(_source.x, _source.y, _dest.x, _dest.y);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", src=").append(StringUtil.toString(_source));
        buf.append(", dest=").append(StringUtil.toString(_dest));
    }

    /**
     * Computes the position of an entity along the path defined by the
     * supplied start and end points assuming that it must finish the path
     * in the specified duration (in millis) and has been traveling the
     * path for the specified number of elapsed milliseconds.
     */
    public static void computePosition (Point start, Point end, long elapsed,
                                        long duration, Point pos)
    {
        float pct = (float)elapsed / duration;
        int travx = (int)((end.x - start.x) * pct);
        int travy = (int)((end.y - start.y) * pct);
        pos.setLocation(start.x + travx, start.y + travy);
    }

    /** Our source and destination points. */
    protected Point _source, _dest;

    /** A temporary point used when computing our position along the
     * path. */
    protected Point _tpos = new Point();
}
