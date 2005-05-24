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

package com.threerings.jme.sprite;

import com.jme.math.Vector3f;

/**
 * Moves a sprite along a series of straight lines.
 */
public class LineSegmentPath extends Path
{
    /**
     * Creates a path for the supplied sprite traversing the supplied
     * series of points with the specified duration between points.
     *
     * @param points a list of points between which the sprite will be
     * moved linearly (the sprite will be moved immediately to the first
     * point).
     * @param durations defines the elapsed time between each successive
     * traversal. This will as a result be shorter by one element than the
     * points array.
     */
    public LineSegmentPath (Sprite sprite, Vector3f[] points, float[] durations)
    {
        super(sprite);
        _points = points;
        _durations = durations;
    }

    // documentation inherited
    public void update (float time)
    {
        // note the accumulated time
        _accum += time;

        // if we have surpassed the time for this segment, subtract the
        // segment time and move on to the next segment
        if (_accum > _durations[_current]) {
            _accum -= _durations[_current];
            _current++;
        }

        // if we have completed our path, move the sprite to the final
        // position and wrap everything up
        if (_current >= _durations.length) {
            _sprite.setLocalTranslation(_points[_points.length-1]);
            _sprite.pathCompleted();
            return;
        }

        // move the sprite to the appropriate position between points
        _temp.interpolate(_points[_current], _points[_current+1],
                          _accum / _durations[_current]);
        _sprite.setLocalTranslation(_temp);
    }

    protected Vector3f[] _points;
    protected float[] _durations;
    protected float _accum;
    protected int _current;
    protected Vector3f _temp = new Vector3f(0, 0, 0);
}
