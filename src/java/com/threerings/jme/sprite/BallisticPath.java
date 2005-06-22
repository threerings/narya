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
 * Moves a sprite ballistically.
 */
public class BallisticPath extends Path
{
    /**
     * Moves the supplied sprite from the starting coordinate (which will
     * be modified) using the starting velocity, under the specified
     * acceleration.
     */
    public BallisticPath (Sprite sprite, Vector3f start, Vector3f velocity,
                          Vector3f accel, float duration)
    {
        super(sprite);
        _curpos = start;
        _velocity = velocity;
        _accel = accel;
        _duration = duration;
    }

    // documentation inherited
    public void update (float time)
    {
        // adjust the position
        _velocity.mult(time, _temp);
        _curpos.addLocal(_temp);
        _sprite.setLocalTranslation(_curpos);

        // check to see if we're done
        _accum += time;
        if (_accum >= _duration) {
            _sprite.pathCompleted();
        } else {
            // adjust our velocity due to acceleration
            _accel.mult(time, _temp);
            _velocity.addLocal(_temp);
        }
    }

    protected Vector3f _curpos, _velocity, _accel;
    protected float _duration, _accum;
    protected Vector3f _temp = new Vector3f(0, 0, 0);
}
