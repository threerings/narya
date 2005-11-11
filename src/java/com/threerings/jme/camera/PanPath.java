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

package com.threerings.jme.camera;

import com.jme.math.Vector3f;

/**
 * Pans the camera to the specified location in the specified amount of time.
 */
public class PanPath extends CameraPath
{
    /**
     * Creates a panning path for the specified camera.
     *
     * @param target the target position for the camera.
     * @param duration the number of seconds in which to pan the camera.
     */
    public PanPath (CameraHandler camhand, Vector3f target, float duration)
    {
        super(camhand);

        _start = new Vector3f(camhand.getCamera().getLocation());
        _velocity = target.subtract(_start);
        _velocity.divideLocal(duration);
        _duration = duration;
    }

    // documentation inherited
    public boolean tick (float secondsSince)
    {
        _elapsed += secondsSince;
        _camloc.scaleAdd(Math.min(_elapsed, _duration), _velocity, _start);
        _camhand.setLocation(_camloc);
        return (_elapsed >= _duration);
    }

    protected Vector3f _start, _velocity, _camloc = new Vector3f();
    protected float _elapsed, _duration;
}
