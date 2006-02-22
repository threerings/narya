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

package com.threerings.media.util;

import java.awt.Graphics2D;
import java.awt.Point;

/**
 * A convenience path that waits a specified amount of time.
 */
public class DelayPath extends TimedPath
{
    /**
     * Cause the current path to remain unchanged for the duration.
     */
    public DelayPath (long duration)
    {
        this(null, duration);
    }

    /**
     * Move to the sprite to the supplied location then wait for the duration.
     */
    public DelayPath (int x, int y, long duration)
    {
        this(new Point(x, y), duration);
    }

    /**
     * Move to the sprite to the supplied location then wait for the duration.
     */
    public DelayPath (Point source, long duration)
    {
        super(duration);
        _source = source;
    }

    // documentation inherited
    public void init (Pathable pable, long timestamp)
    {
        super.init(pable, timestamp);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
    }

    // documentation inherited
    public boolean tick (Pathable pable, long tickstamp)
    {
        if (tickstamp >= _startStamp + _duration) {
            if (_source != null) {
                pable.setLocation(_source.x, _source.y);
            }
            pable.pathCompleted(tickstamp);
            return (_source != null);
        }

        // If necessary, move the sprite to the supplied location
        if (_source != null && (pable.getX() != _source.x ||
                                pable.getY() != _source.y)) {
            pable.setLocation(_source.x, _source.y);
            return true;
        }

        return false;
    }

    /** Source point. */
    protected Point _source;
}
