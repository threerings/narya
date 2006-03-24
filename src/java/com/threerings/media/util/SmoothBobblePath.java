//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.util.RandomUtil;

/**
 * Bobble a Pathable smoothly.
 */
public class SmoothBobblePath extends BobblePath
{
    public SmoothBobblePath (int dx, int dy, long duration)
    {
        this(dx, dy, duration, 1L);
    }

    public SmoothBobblePath (int dx, int dy, long duration, long updateFreq)
    {
        super(dx, dy, duration, updateFreq);
    }

    // documentation inherited
    public void init (Pathable pable, long tickstamp)
    {
        super.init(pable, tickstamp);

        _newx = _sx;
        _newy = _sy;
        _oldx = _sx;
        _oldy = _sy;
    }

    // documentation inherited
    public boolean tick (Pathable pable, long tickStamp)
    {
        // see if we need to stop
        if (_stopTime <= tickStamp) {
            boolean updated = updatePositionTo(pable, _sy, _sy);
            pable.pathCompleted(tickStamp);
            return updated;
        }

        // see if it's time to update the position
        if (_nextMove < tickStamp) {
            _oldx = _newx;
            _oldy = _newy;
            do {
                _newx = _sx + RandomUtil.getInt(_dx * 2 + 1) - _dx;
                _newy = _sy + RandomUtil.getInt(_dy * 2 + 1) - _dy;
            } while (_newx == _oldx && _newy == _oldy);

            _nextMove = tickStamp + _updateFreq;
        }

        float movePerc = (float)(_nextMove - tickStamp) / (float)_updateFreq;
        int x = _oldx + (int)((_newx - _oldx) * movePerc);
        int y = _oldy + (int)((_newy - _oldy) * movePerc);
        // update the position
        return updatePositionTo(pable, x, y);
    }

    /** The previous position. */
    protected int _oldx, _oldy;

    /** The next position. */
    protected int _newx, _newy;
}
