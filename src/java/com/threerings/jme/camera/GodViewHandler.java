//
// $Id$

package com.threerings.jme.camera;

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.RelativeMouse;
import com.jme.input.action.*;
import com.jme.input.action.InputActionEvent;

import com.threerings.jme.JmeApp;
import com.threerings.jme.Log;

/**
 * Sets up camera controls for moving around from a top-down perspective,
 * suitable for strategy games and their ilk. The "ground" is assumed to
 * be the XY plane.
 */
public class GodViewHandler extends InputHandler
{
    /**
     * Creates the handler.
     *
     * @param cam The camera to move with this handler.
     * @param api The API from which to create a KeyBindingManager.
     */
    public GodViewHandler (Camera cam, String api)
    {
        _camera = cam;
        setKeyBindings(api);
        addActions(cam);
    }

    /**
     * Configures the minimum and maximum z-axis elevation allowed for the
     * camera.
     */
    public void setZoomLimits (float minZ, float maxZ)
    {
        _minZ = minZ;
        _maxZ = maxZ;
    }

    /**
     * Configures limits on the camera roll.
     */
    public void setRollLimits (float minAngle, float maxAngle)
    {
        _minRoll = minAngle;
        _maxRoll = maxAngle;
    }

    /**
     * Configures limits on the distance the camera can be panned.
     */
    public void setPanLimits (float minX, float minY, float maxX, float maxY)
    {
        _minX = minX;
        _minY = minY;
        _maxX = maxX;
        _maxY = maxY;
    }

    /**
     * Sets the camera zoom level to a value between zero (zoomed in
     * maximally) and 1 (zoomed out maximally). Zoom limits must have
     * already been set up via a call to {@link #setZoomLimits}.
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
     * Configures the rotational velocity of the camera, which is used to
     * smoothly slide the camera to its new position following a call to
     * {@link #rotateCamera}.
     */
    public void setRotateVelocity (float radiansPerSecond)
    {
        _rotateVelocity = radiansPerSecond;
    }

    /**
     * Swings the camera around the point on the ground at which it is
     * "looking", by the requested angle (in radians). The camera does not
     * jump by the specified angle but rather smoothly rotates there.
     */
    public void rotateCamera (float deltaAngle)
    {
        _rotateDelta += deltaAngle;
    }

    /**
     * Returns true if the camera is still rotating due to a previous call
     * to {@link #rotateCamera}.
     */
    public boolean isRotating ()
    {
        return _rotateDelta != 0;
    }

    // documentation inherited
    public void update (float time)
    {
        super.update(time);

        float deltaAngle = 0;
        if (_rotateDelta < 0) {
            deltaAngle = Math.min(time * _rotateVelocity, -_rotateDelta);
        } else if (_rotateDelta > 0) {
            deltaAngle = Math.max(-1f * time * _rotateVelocity, -_rotateDelta);
        }
        if (deltaAngle != 0) {
            _rotateDelta += deltaAngle;
            rotateCamera(_groundNormal, deltaAngle);
        }
    }

    protected void setKeyBindings (String api)
    {
        KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();

        // the key bindings for the pan actions
        keyboard.set("forward", KeyInput.KEY_W);
        keyboard.set("backward", KeyInput.KEY_S);
        keyboard.set("left", KeyInput.KEY_A);
        keyboard.set("right", KeyInput.KEY_D);

        // the key bindings for the zoom actions
        keyboard.set("zoomIn", KeyInput.KEY_UP);
        keyboard.set("zoomOut", KeyInput.KEY_DOWN);

        // the key bindings for the orbit actions
        keyboard.set("turnRight", KeyInput.KEY_RIGHT);
        keyboard.set("turnLeft", KeyInput.KEY_LEFT);

        // the key bindings for the roll actions
        keyboard.set("rollForward", KeyInput.KEY_HOME);
        keyboard.set("rollBack", KeyInput.KEY_END);

        keyboard.set("screenshot", KeyInput.KEY_F12);
    }

    protected void addActions (Camera cam)
    {
        addAction(new KeyScreenShotAction(), "screenshot", false);

        addPanActions(cam);
        addZoomActions(cam);
        addOrbitActions(cam);
        addRollActions(cam);
    }

    /**
     * Adds actions for panning the camera around the scene.
     */
    protected void addPanActions (Camera cam)
    {
        CameraAction forward = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                loc.addLocal(_rydir.mult(speed * evt.getTime(), _temp));
                _camera.setLocation(boundPan(loc));
                _camera.update();
            }
        };
        addAction(forward, "forward", true);

        CameraAction backward = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                loc.subtractLocal(_rydir.mult(speed * evt.getTime(), _temp));
                _camera.setLocation(boundPan(loc));
                _camera.update();
            }
        };
        addAction(backward, "backward", true);

        CameraAction left = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                loc.subtractLocal(_rxdir.mult(speed * evt.getTime(), _temp));
                _camera.setLocation(boundPan(loc));
                _camera.update();
            }
        };
        addAction(left, "left", true);

        CameraAction right = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                loc.addLocal(_rxdir.mult(speed * evt.getTime(), _temp));
                _camera.setLocation(boundPan(loc));
                _camera.update();
            }
        };
        addAction(right, "right", true);
    }

    /**
     * Adds actions for zooming the camaera in and out.
     */
    protected void addZoomActions (Camera cam)
    {
        CameraAction zoomIn = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                if (loc.z > _minZ) {
                    _camera.getDirection().mult(speed * evt.getTime(), _temp);
                    loc.addLocal(_temp);
                    _camera.setLocation(boundPan(loc));
                    _camera.update();
                }
            }
        };
        addAction(zoomIn, "zoomIn", true);

        CameraAction zoomOut = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                if (loc.z < _maxZ) {
                    _camera.getDirection().mult(speed * evt.getTime(), _temp);
                    loc.subtractLocal(_temp);
                    _camera.setLocation(boundPan(loc));
                    _camera.update();
                }
            }
        };
        addAction(zoomOut, "zoomOut", true);
    }

    /**
     * Adds actions for orbiting the camera around the viewpoint.
     */
    protected void addOrbitActions (Camera cam)
    {
        addAction(new OrbitAction(-FastMath.PI / 2), "turnRight", true);
        addAction(new OrbitAction(FastMath.PI / 2), "turnLeft", true);
    }

    /**
     * Adds actions for rolling the camera around the yaw axis.
     */
    protected void addRollActions (Camera cam)
    {
        addAction(new RollAction(-FastMath.PI / 2), "rollForward", true);
        addAction(new RollAction(FastMath.PI / 2), "rollBack", true);
    }

    /**
     * Locates the point on the ground at which the camera is "looking"
     * and rotates the camera from that point around the specified vector
     * by the specified angle.
     */
    protected void rotateCamera (Vector3f around, float deltaAngle)
    {
        // locate the point at which the camera is "pointing" on
        // the ground plane
        Vector3f camloc = _camera.getLocation();
        Vector3f center = groundPoint(_camera);

        // get a vector from the camera's position to the point
        // around which we're going to orbit
        Vector3f direction = camloc.subtract(center);

        // create a rotation matrix
        _rotm.fromAxisAngle(around, deltaAngle);

        // rotate the center to camera vector and the camera itself
        _rotm.mult(direction, direction);
        _rotm.mult(_camera.getUp(), _camera.getUp());
        _rotm.mult(_camera.getLeft(), _camera.getLeft());
        _rotm.mult(_camera.getDirection(), _camera.getDirection());
        if (around == _groundNormal) {
            _rotm.mult(_rxdir, _rxdir);
            _rotm.mult(_rydir, _rydir);
        }

        // and move the camera to its new location
        _camera.setLocation(boundPan(center.add(direction)));

        _camera.update();
    }

    protected static Vector3f groundPoint (Camera camera)
    {
        float dist = -1f * _groundNormal.dot(camera.getLocation()) /
            _groundNormal.dot(camera.getDirection());
        return camera.getLocation().add(camera.getDirection().mult(dist));
    }

    protected Vector3f boundPan (Vector3f loc)
    {
        loc.x = Math.max(Math.min(loc.x, _maxX), _minX);
        loc.y = Math.max(Math.min(loc.y, _maxY), _minY);
        return loc;
    }

    protected abstract class CameraAction extends KeyInputAction
    {
        public CameraAction (Camera camera, float speed)
        {
            _camera = camera;
            this.speed = speed;
        }

        /** The camera to manipulate. */
        protected Camera _camera;

        /** A temporary vector. */
        protected Vector3f _temp = new Vector3f();
    }

    protected class OrbitAction extends KeyInputAction
    {
        public OrbitAction (float radPerSec)
        {
            _radPerSec = radPerSec;
        }

        public void performAction (InputActionEvent evt)
        {
            rotateCamera(_groundNormal, _radPerSec * evt.getTime());
        }

        protected float _radPerSec;
    }

    protected class RollAction extends KeyInputAction
    {
        public RollAction (float radPerSec)
        {
            _radPerSec = radPerSec;
        }

        public void performAction (InputActionEvent evt)
        {
            rotateCamera(_camera.getLeft(), _radPerSec * evt.getTime());
        }

        protected float _radPerSec;
    }

    protected Camera _camera;
    protected Matrix3f _rotm = new Matrix3f();

    protected float _rotateVelocity = FastMath.PI, _rotateDelta;

    protected float _minX = Float.MIN_VALUE, _maxX = Float.MAX_VALUE;
    protected float _minY = Float.MIN_VALUE, _maxY = Float.MAX_VALUE;
    protected float _minZ = Float.MIN_VALUE, _maxZ = Float.MAX_VALUE;
    protected float _minRoll = Float.MIN_VALUE, _maxRoll = Float.MAX_VALUE;

    protected Vector3f _rxdir = new Vector3f(1, 0, 0);
    protected Vector3f _rydir = new Vector3f(0, 1, 0);

    protected static final Vector3f _xdir = new Vector3f(1, 0, 0);
    protected static final Vector3f _ydir = new Vector3f(0, 1, 0);

    protected static final Vector3f _groundNormal = new Vector3f(0, 0, 1);
}
