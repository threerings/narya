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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

/**
 * Pans the camera to the specified location in the specified amount of time.
 */
public class PanPath extends CameraPath
{
    /**
     * Creates a panning path for the specified camera that will retain the
     * camera's current orientation.
     *
     * @param target the target position for the camera.
     * @param duration the number of seconds in which to pan the camera.
     */
    public PanPath (CameraHandler camhand, Vector3f target, float duration)
    {
        this(camhand, target, null, duration);
    }

    /**
     * Creates a panning path for the specified camera.
     *
     * @param target the target position for the camera.
     * @param trot the target rotation for the camera (or <code>null</code> to
     * keep its current orientation)
     * @param duration the number of seconds in which to pan the camera.
     */
    public PanPath (CameraHandler camhand, Vector3f target, Quaternion trot,
        float duration)
    {
        super(camhand);

        Camera cam = camhand.getCamera();
        _start = new Vector3f(cam.getLocation());
        _velocity = target.subtract(_start);
        _velocity.divideLocal(duration);
        if (trot != null) {
            _irot = new Quaternion();
            _irot.fromAxes(cam.getLeft(), cam.getUp(), cam.getDirection());
            _trot = trot;
        }
        
        _duration = duration;
    }
    
    // documentation inherited
    public boolean tick (float secondsSince)
    {
        _elapsed = Math.min(_elapsed + secondsSince, _duration);
        _camloc.scaleAdd(_elapsed, _velocity, _start);
        _camhand.setLocation(_camloc);
        if (_irot != null) {
            _camhand.getCamera().setAxes(_axes.slerp(_irot, _trot,
                _elapsed / _duration));
        }
        return (_elapsed >= _duration);
    }

    protected Vector3f _start, _velocity, _camloc = new Vector3f();
    protected Quaternion _irot, _trot, _axes = new Quaternion();
    protected float _elapsed, _duration;
}
