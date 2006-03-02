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
import com.jme.math.Quaternion;
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
        this(camhand, spot, axis, angle, angvel, null, 0f, zoom);
    }
    
    /**
     * Creates a rotating, zooming path for the specified camera.
     *
     * @param spot the point of interest around which to swing the camera.
     * @param paxis the primary axis around which to rotate the camera.
     * @param pangle the angle through which to rotate the camera about the
     * primary axis.
     * @param angvel the (absolute value of the) velocity at which to rotate
     * the camera (in radians per second) about the primary angle.
     * @param saxis the secondary axis around which to rotate the camera, or
     * <code>null</code> for none
     * @param sangle the angle through which to rotate the camera about the
     * secondary axis
     * @param zoom the distance to zoom along the camera's view axis (negative
     * = in, positive = out).
     */
    public SwingPath (CameraHandler camhand, Vector3f spot, Vector3f paxis,
                      float pangle, float angvel, Vector3f saxis, float sangle,
                      float zoom)
    {
        super(camhand);

        if (pangle == 0) {
            Log.warning("Requested to swing camera through zero degrees " +
                        "[spot=" + spot + ", paxis=" + paxis +
                        ", angvel=" + angvel + ", zoom=" + zoom + "].");
            pangle = 0.0001f;
        }
        if (angvel <= 0) {
            Log.warning("Requested to swing camera with invalid velocity " +
                        "[spot=" + spot + ", paxis=" + paxis + ", pangle=" +
                        pangle + ", angvel=" + angvel + ", zoom=" + zoom +
                        "].");
            angvel = FastMath.PI;
        }

        _spot = spot;
        _paxis = paxis;
        _pangle = pangle;
        _pangvel = (pangle > 0) ? angvel : -1 * angvel;
        _saxis = (saxis == null) ? null : new Vector3f(saxis);
        _sangle = sangle;
        _sangvel = _sangle * _pangvel / _pangle;
        _zoom = zoom;
        _zoomvel = _zoom * _pangvel / _pangle;

//         Log.info("Swinging camera [angle=" + _angle + ", angvel=" + _angvel +
//                  ", zoom=" + _zoom + ", zoomvel=" + _zoomvel + "].");
    }

    // documentation inherited
    public boolean tick (float secondsSince)
    {
        float deltaPAngle = (secondsSince * _pangvel);
        float deltaSAngle = (secondsSince * _sangvel);
        float deltaZoom = (secondsSince * _zoomvel);
        _protated += deltaPAngle;
        _srotated += deltaSAngle;
        _zoomed += deltaZoom;

        // clamp our rotation at the target angle and determine whether or not
        // we're done
        boolean done = false;
        if (_pangle > 0 && _protated > _pangle ||
            _pangle < 0 && _protated < _pangle) {
            deltaPAngle -= (_protated - _pangle);
            deltaSAngle -= (_srotated - _sangle);
            deltaZoom -= (_zoomed - _zoom);
            _protated = _pangle;
            _srotated = _sangle;
            _zoomed = _zoom;
            done = true;
        }

        // have the camera handler do the necessary rotating and zooming
        _camhand.rotateCamera(_spot, _paxis, deltaPAngle, deltaZoom);
        if (_saxis != null) {
            _rot.fromAngleAxis(deltaPAngle, _paxis).multLocal(_saxis);
            _camhand.rotateCamera(_spot, _saxis, deltaSAngle, 0f);
        }
            
        return done;
    }

    protected Vector3f _spot, _paxis, _saxis;
    protected float _pangle, _pangvel, _protated;
    protected float _sangle, _sangvel, _srotated;
    protected float _zoom, _zoomvel, _zoomed;
    
    protected Quaternion _rot = new Quaternion();
}
