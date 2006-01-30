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
import com.jme.renderer.Camera;

import com.threerings.jme.Log;

/**
 * Moves the camera along a cubic Hermite spline path defined by the start and
 * end locations and directions.  Spline formulas obtained from
 * <a href="http://en.wikipedia.org/wiki/Cubic_Hermite_spline">Wikipedia</a>.
 */
public class SplinePath extends CameraPath
{
    /**
     * Creates a cubic spline path for the camera to follow.
     *
     * @param tloc the target location
     * @param tdir the target direction
     * @param duration the duration of the path
     * @param tension the tension parameter, which can range from zero to one:
     * higher tension values create a more direct path with a sharper turn,
     * lower values create a rounder path with a smoother turn
     */
    public SplinePath (CameraHandler camhand, Vector3f tloc,
        Vector3f tdir, float duration, float tension)
    {
        super(camhand);
        
        // get the spline function coefficients
        Camera cam = camhand.getCamera();
        _p0 = new Vector3f(cam.getLocation());
        _p1 = new Vector3f(tloc);
        float tscale = (1f - tension) * _p0.distance(_p1);
        _m0 = cam.getDirection().mult(tscale);
        _m1 = tdir.mult(tscale);
        
        _duration = duration;
    }
    
    // documentation inherited
    public boolean tick (float secondsSince)
    {
        _elapsed = Math.min(_elapsed + secondsSince, _duration);
        float t = _elapsed / _duration, t2 = t*t, t3 = t2*t,
            h00 = 2*t3 - 3*t2 + 1,
            h00p = 6*t2 - 6*t,
            h10 = t3 - 2*t2 + t,
            h10p = 3*t2 - 4*t + 1,
            h01 = -2*t3 + 3*t2,
            h01p = -6*t2 + 6*t,
            h11 = t3 - t2,
            h11p = 3*t2 - 2*t;
    
        // take the derivative to find the direction
        _p0.mult(h00p, _dir);
        _dir.scaleAdd(h10p, _m0, _dir);
        _dir.scaleAdd(h01p, _p1, _dir);
        _dir.scaleAdd(h11p, _m1, _dir);
        _dir.normalizeLocal();
        
        // evaluate the spline function to find the location
        _p0.mult(h00, _loc);
        _loc.scaleAdd(h10, _m0, _loc);
        _loc.scaleAdd(h01, _p1, _loc);
        _loc.scaleAdd(h11, _m1, _loc);
        
        // compute the left and up vectors using the camera's current
        // up vector
        Camera cam = _camhand.getCamera();
        cam.getUp().cross(_dir, _left);
        _left.normalizeLocal();
        _dir.cross(_left, _up);
        
        // update the camera's location and orientation
        cam.setFrame(_loc, _left, _up, _dir);
        
        return _elapsed >= _duration;
    }
 
    /** The parameters of the spline. */
    Vector3f _p0, _m0, _p1, _m1;
    
    /** Working vectors. */
    Vector3f _loc = new Vector3f(), _left = new Vector3f(),
        _up = new Vector3f(), _dir = new Vector3f();
    
    /** The elapsed time and total duration. */
    float _elapsed, _duration;
}
