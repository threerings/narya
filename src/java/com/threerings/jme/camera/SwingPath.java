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

import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

import com.threerings.jme.Log;

/**
 * Swings the camera around a point of interest (which should be somewhere
 * along the camera's view vector). Also optionally zooms the camera in or out
 * (moves it along its view vector) in the process.
 *
 * <p align="center"><img src="rotate_zoom.png">
 */
public class SwingPath extends CameraPath
{
    /**
     * Creates a rotating, zooming path for the specified camera.
     *
     * @param spot the point of interest around which to swing the camera.
     * @param axis the axis around which to rotate the camera.
     * @param angle the angle through which to rotate the camera.
     * @param angvel the (absolute value of the) velocity at which to rotate
     * the camera (in radians per second).
     * @param zoom the distance to zoom along the camera's view axis (negative
     * = in, positive = out).
     */
    public SwingPath (CameraHandler camhand, Vector3f spot, Vector3f axis,
                      float angle, float angvel, float zoom)
    {
        super(camhand);

        if (angle == 0) {
            Log.warning("Requested to swing camera through zero degrees " +
                        "[spot=" + spot + ", axis=" + axis +
                        ", angvel=" + angvel + ", zoom=" + zoom + "].");
            angle = 0.0001f;
        }
        if (angvel <= 0) {
            Log.warning("Requested to swing camera with invalid velocity " +
                        "[spot=" + spot + ", axis=" + axis + ", angle=" + angle +
                        ", angvel=" + angvel + ", zoom=" + zoom + "].");
            angvel = FastMath.PI;
        }

        _spot = spot;
        _axis = axis;
        _angle = angle;
        _angvel = (angle > 0) ? angvel : -1 * angvel;
        _zoom = zoom;
        _zoomvel = _zoom * _angvel / _angle;

//         Log.info("Swinging camera [angle=" + _angle + ", angvel=" + _angvel +
//                  ", zoom=" + _zoom + ", zoomvel=" + _zoomvel + "].");
    }

    // documentation inherited
    public boolean tick (float secondsSince)
    {
        float deltaAngle = (secondsSince * _angvel);
        float deltaZoom = (secondsSince * _zoomvel);
        _rotated += deltaAngle;
        _zoomed += deltaZoom;

        // clamp our rotation at the target angle and determine whether or not
        // we're done
        boolean done = false;
        if (_angle > 0 && _rotated > _angle ||
            _angle < 0 && _rotated < _angle) {
            deltaAngle -= (_rotated - _angle);
            deltaZoom -= (_zoomed - _zoom);
            _rotated = _angle;
            _zoomed = _zoom;
            done = true;
        }

        // have the camera handler do the necessary rotating and zooming
        _camhand.rotateCamera(_spot, _axis, deltaAngle, deltaZoom);

        return done;
    }

    protected Vector3f _spot, _axis;
    protected float _angle, _angvel, _rotated;
    protected float _zoom, _zoomvel, _zoomed;
}
