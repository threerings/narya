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
import com.jme.math.Matrix3f;
import com.jme.math.Plane;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

import com.samskivert.util.ObserverList;

/**
 * Provides various useful mechanisms for manipulating the camera.
 */
public class CameraHandler
{
    /**
     * Creates a new camera handler. The camera begins life at the origin,
     * facing in the negative z direction (pointing at the ground).
     */
    public CameraHandler (Camera camera)
    {
        _camera = camera;
        resetAxes();
    }

    /**
     * Resets the camera orientation to its initial state.
     */
    public void resetAxes ()
    {
        _camera.getDirection().set(0, 0, -1);
        _camera.getLeft().set(-1, 0, 0);
        _camera.getUp().set(0, 1, 0);
        _camera.update(); 
        
        _rxdir.set(1, 0, 0);
        _rydir.set(0, 1, 0);
    }
    
    /**
     * Configures limits on the camera tilt.
     */
    public void setTiltLimits (float minAngle, float maxAngle)
    {
        _minTilt = minAngle;
        _maxTilt = maxAngle;
    }

    /**
     * Configures limits on the distance the camera can be panned.
     *
     * @param boundViaFrustum if true instead of bounding the camera's
     * position, we will compute the intersections of the view frustum with the
     * ground plan and bound that rectangle into the specified bounds.
     * <em>Note:</em> the camera must generally be pointing down at the ground
     * (up to perhaps 45 degrees or so) for this to work. At higher angles the
     * back of the view frustum will intersect the ground plane at or near
     * infinity.
     */
    public void setPanLimits (float minX, float minY, float maxX, float maxY,
                              boolean boundViaFrustum)
    {
        _minX = minX;
        _minY = minY;
        _maxX = maxX;
        _maxY = maxY;
        _boundViaFrustum = boundViaFrustum;
    }

    /**
     * Configures the minimum and maximum zoom values allowed for the
     * camera.
     */
    public void setZoomLimits (float minZoom, float maxZoom)
    {
        _minZoom = minZoom;
        _maxZoom = maxZoom;
    }

    /**
     * Enables or disables the current set of limits.
     */
    public void setLimitsEnabled (boolean enabled)
    {
        _limitsEnabled = enabled;
    }
    
    /**
     * Sets the camera zoom level to a value between zero (zoomed in maximally)
     * and 1 (zoomed out maximally). Zoom limits must have already been set up
     * via a call to {@link #setZoomLimits}.
     */
    public void setZoomLevel (float level)
    {
//         Log.info("Zoom " + level + " " + _camera.getLocation());
        level = Math.max(0f, Math.min(level, 1f));
        _camera.getLocation().z = _minZ + (_maxZ - _minZ) * level;
        _camera.update();
    }

    /**
     * Returns the current camera zoom level.
     */
    public float getZoomLevel ()
    {
        return (_camera.getLocation().z - _minZ) / (_maxZ - _minZ);
    }

    /**
     * Adds a camera path observer.
     */
    public void addCameraObserver (CameraPath.Observer camobs)
    {
        _campathobs.add(camobs);
    }

    /**
     * Removes a camera path observer.
     */
    public void removeCameraObserver (CameraPath.Observer camobs)
    {
        _campathobs.remove(camobs);
    }

    /**
     * Starts the camera moving along a path which will be updated every tick
     * until it is complete.
     */
    public void moveCamera (CameraPath path)
    {
        if (_campath != null) {
            _campath.abort();
            _campathobs.apply(new CompletedOp(_campath));
        }
        _campath = path;
    }

    /**
     * Returns true if the camera is currently animating along a path, false if
     * it is not.
     */
    public boolean cameraIsMoving ()
    {
        return (_campath != null);
    }

    /**
     * Skips immediately to the end of the current camera path.
     */
    public void skipPath ()
    {
        // fake an update far enough into the future to trick the camera path
        // into thinking it's done
        update(100);
    }

    /**
     * This is called by the {@link JmeApp} on every frame to allow the handler
     * to update the camera as necessary.
     */
    public void update (float frameTime)
    {
        if (_campath != null) {
            if (_campath.tick(frameTime)) {
                CameraPath opath = _campath;
                _campath = null;
                _campathobs.apply(new CompletedOp(opath));
            }
        }
    }

    /**
     * Returns the camera being manipulated by this handler.
     */
    public Camera getCamera ()
    {
        return _camera;
    }

    /**
     * Adjusts the camera's location. The specified location will be bounded
     * within the current pan and zoom limits.
     */
    public void setLocation (Vector3f location)
    {
        _camera.setLocation(bound(location));
        _camera.update();
    }

    /**
     * Pans the camera the specified distance in the x and y directions. These
     * distances will be multiplied by the current "view" x and y axes and
     * added to the camera's position.
     */
    public void panCamera (float x, float y)
    {
        Vector3f loc = _camera.getLocation();
        loc.addLocal(_rxdir.mult(x, _temp));
        loc.addLocal(_rydir.mult(y, _temp));
        setLocation(loc);
    }

    /**
     * Zooms the camera in (distance < 0) and out (distance > 0) by the
     * specified amount. The distance is multiplied by the camera's current
     * direction and added to its current position, which is then bounded into
     * the pan and zoom volume.
     */
    public void zoomCamera (float distance)
    {
        Vector3f loc = _camera.getLocation();
        float dist = getGroundPoint().distance(loc),
            ndist = Math.min(Math.max(dist + distance, _minZoom), _maxZoom);
        if ((distance = ndist - dist) == 0f) {
            return;
        }
        loc.subtractLocal(_camera.getDirection().mult(distance, _temp));
        setLocation(loc);
    }

    /**
     * Locates the point on the ground at which the camera is "looking" and
     * rotates the camera from that point around the specified vector by the
     * specified angle. Additionally zooms the camera in (deltaZoom < 0) or out
     * (deltaZoom > 0) along its direction of view by the specified amount.
     */
    public void rotateCamera (
        Vector3f spot, Vector3f axis, float deltaAngle, float deltaZoom)
    {
        // get a vector from the camera's current position to the point around
        // which we're going to orbit
        Vector3f direction = _camera.getLocation().subtract(spot);

        // if we're rotating around the left vector, impose tilt limits
        if (axis == _camera.getLeft()) {
            float angle = FastMath.asin(_ground.normal.dot(direction) /
                direction.length());
            float nangle = Math.min(Math.max(angle + deltaAngle, _minTilt),
                _maxTilt);
            if ((deltaAngle = nangle - angle) == 0f) {
                return;
            }
        }
        
        // create a rotation matrix
        _rotm.fromAxisAngle(axis, deltaAngle);

        // rotate the direction vector and the camera itself
        _rotm.mult(direction, direction);
        _rotm.mult(_camera.getUp(), _camera.getUp());
        _rotm.mult(_camera.getLeft(), _camera.getLeft());
        _rotm.mult(_camera.getDirection(), _camera.getDirection());

        // if we're rotating around the ground normal, we need to update our
        // notion of side-to-side and forward for panning
        if (axis == _ground.normal) {
            _rotm.mult(_rxdir, _rxdir);
            _rotm.mult(_rydir, _rydir);
        }

        // finally move the camera to its new location, zooming in or out in
        // the process
        float scale = 1 + (deltaZoom / direction.length());
        direction.scaleAdd(scale, spot);
        setLocation(direction);
    }

    /**
     * Swings the camera perpendicular to the ground normal, around the point
     * on the ground at which it is looking.
     */
    public void orbitCamera (float deltaAngle)
    {
        rotateCamera(getGroundPoint(), _ground.normal, deltaAngle, 0);
    }

    /**
     * Swings the camera perpendicular to its left vector around the point on
     * the ground at which it is looking.
     */
    public void tiltCamera (float deltaAngle)
    {
        rotateCamera(getGroundPoint(), _camera.getLeft(), deltaAngle, 0);
    }

    /**
     * Returns the point on the ground (z = 0) at which the camera is looking.
     */
    public Vector3f getGroundPoint ()
    {
        float dist = -1f * _ground.normal.dot(_camera.getLocation()) /
            _ground.normal.dot(_camera.getDirection());
        return _camera.getLocation().add(_camera.getDirection().mult(dist));
    }

    /**
     * Returns the ground normal. Z is the default ground plane and the normal
     * points in the positive z direction. <em>Do not modify this value.</em>
     */
    public Vector3f getGroundNormal ()
    {
        return _ground.normal;
    }

    protected Vector3f bound (Vector3f loc)
    {
        if (!_limitsEnabled) {
            return loc;
        }
        if (_boundViaFrustum) {
            bound(_camera.getFrustumLeft(), _camera.getFrustumTop(), loc);
            bound(_camera.getFrustumLeft(), _camera.getFrustumBottom(), loc);
            bound(_camera.getFrustumRight(), _camera.getFrustumTop(), loc);
            bound(_camera.getFrustumRight(), _camera.getFrustumBottom(), loc);
        } else {
            loc.x = Math.max(Math.min(loc.x, _maxX), _minX);
            loc.y = Math.max(Math.min(loc.y, _maxY), _minY);
        }
        loc.z = Math.max(Math.min(loc.z, _maxZ), _minZ);
        return loc;
    }

    protected void bound (float left, float up, Vector3f loc)
    {
        // start with the location of the camera moved out into the near
        // frustum plane
        _temp.set(loc);
        _temp.scaleAdd(_camera.getFrustumNear(), _camera.getDirection(), loc);

        // then slide it over to a corner of the near frustum rectangle
        _temp.scaleAdd(left, _camera.getLeft(), _temp);
        _temp.scaleAdd(up, _camera.getUp(), _temp);

        // turn this into a vector with origin at the camera's location
        _temp.subtractLocal(loc);
        _temp.normalizeLocal();

        // determine the intersection of said vector with the ground plane
        float dist = -1f * _ground.normal.dot(loc) / _ground.normal.dot(_temp);
        _temp.scaleAdd(dist, _temp, loc);

        // we then assume that if the corner of the "viewable ground rectangle"
        // is outside our bounds that we can simply adjust the camera location
        // by the amount it is out of bounds
        if (_temp.x > _maxX) {
            loc.x -= (_temp.x - _maxX);
        } else if (_temp.x < _minX) {
            loc.x += (_minX - _temp.x);
        }
        if (_temp.y > _maxY) {
            loc.y -= (_temp.y - _maxY);
        } else if (_temp.y < _minY) {
            loc.y += (_minY - _temp.y);
        }
    }

    /** Used to dispatch {@link CameraPath.Observer#pathCompleted}. */
    protected static class CompletedOp implements ObserverList.ObserverOp
    {
        public CompletedOp (CameraPath path) {
            _path = path;
        }
        public boolean apply (Object observer) {
            return ((CameraPath.Observer)observer).pathCompleted(_path);
        }
        protected CameraPath _path;
    }

    protected Camera _camera;
    protected CameraPath _campath;
    protected ObserverList _campathobs =
        new ObserverList(ObserverList.SAFE_IN_ORDER_NOTIFY);

    protected Matrix3f _rotm = new Matrix3f();
    protected Vector3f _temp = new Vector3f();

    protected boolean _boundViaFrustum, _limitsEnabled = true;
    protected float _minX = -Float.MAX_VALUE, _maxX = Float.MAX_VALUE;
    protected float _minY = -Float.MAX_VALUE, _maxY = Float.MAX_VALUE;
    protected float _minZ = -Float.MAX_VALUE, _maxZ = Float.MAX_VALUE;
    protected float _minZoom = 0f, _maxZoom = Float.MAX_VALUE;
    protected float _minTilt = -Float.MAX_VALUE, _maxTilt = Float.MAX_VALUE;

    protected Vector3f _rxdir = new Vector3f(1, 0, 0);
    protected Vector3f _rydir = new Vector3f(0, 1, 0);

    protected static final Vector3f _xdir = new Vector3f(1, 0, 0);
    protected static final Vector3f _ydir = new Vector3f(0, 1, 0);

    protected static final Plane _ground = new Plane(new Vector3f(0, 0, 1), 0);
}
