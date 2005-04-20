//
// $Id$

package com.threerings.jme.input;

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

import com.jme.input.InputHandler;
import com.jme.input.InputSystem;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.RelativeMouse;
import com.jme.input.action.*;
import com.jme.input.action.InputActionEvent;

import com.threerings.jme.JmeApp;

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
     * @param app The application to be terminated on an "exit" action.
     * @param cam The camera to move with this handler.
     * @param api The API from which to create a KeyBindingManager.
     */
    public GodViewHandler (JmeApp app, Camera cam, String api)
    {
        setKeyBindings(api);
        setMouse(cam);
        setActions(cam, app);
    }

    protected void setKeyBindings (String api)
    {
        KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();
        InputSystem.createInputSystem(api);

        keyboard.setKeyInput(InputSystem.getKeyInput());
        keyboard.set("forward", KeyInput.KEY_W);
        keyboard.set("backward", KeyInput.KEY_S);
        keyboard.set("left", KeyInput.KEY_A);
        keyboard.set("right", KeyInput.KEY_D);
        keyboard.set("zoomIn", KeyInput.KEY_UP);
        keyboard.set("zoomOut", KeyInput.KEY_DOWN);
        keyboard.set("turnRight", KeyInput.KEY_RIGHT);
        keyboard.set("turnLeft", KeyInput.KEY_LEFT);
        keyboard.set("screenshot", KeyInput.KEY_F12);
        keyboard.set("exit", KeyInput.KEY_ESCAPE);

        setKeyBindingManager(keyboard);
    }

    protected void setMouse (Camera cam)
    {
//         RelativeMouse mouse = new RelativeMouse("Mouse Input");
//         mouse.setMouseInput(InputSystem.getMouseInput());
//         setMouse(mouse);

//         MouseLook mouseLook = new MouseLook(mouse, cam, 1.0f);
//         mouseLook.setKey("mouselook");
//         mouseLook.setLockAxis(
//             new Vector3f(cam.getUp().x, cam.getUp().y, cam.getUp().z));
//         addAction(mouseLook);
    }

    protected void setActions (Camera cam, final JmeApp app)
    {
        KeyInputAction exit = new KeyInputAction() {
            public void performAction (InputActionEvent evt) {
                app.stop();
            }
        };
        exit.setKey("exit");
        addAction(exit);

        KeyScreenShotAction screen = new KeyScreenShotAction();
        screen.setKey("screenshot");
        addAction(screen);

        CameraAction forward = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                loc.addLocal(_rydir.mult(speed * evt.getTime(), _temp));
                _camera.setLocation(loc);
                _camera.update();
            }
        };
        forward.setKey("forward");
        addAction(forward);

        CameraAction backward = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                loc.subtractLocal(_rydir.mult(speed * evt.getTime(), _temp));
                _camera.setLocation(loc);
                _camera.update();
            }
        };
        backward.setKey("backward");
        addAction(backward);

        CameraAction left = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                loc.addLocal(_rxdir.mult(speed * evt.getTime(), _temp));
                _camera.setLocation(loc);
                _camera.update();
            }
        };
        left.setKey("left");
        addAction(left);

        CameraAction right = new CameraAction(cam, 0.5f) {
            public void performAction (InputActionEvent evt) {
                Vector3f loc = _camera.getLocation();
                loc.subtractLocal(_rxdir.mult(speed * evt.getTime(), _temp));
                _camera.setLocation(loc);
                _camera.update();
            }
        };
        right.setKey("right");
        addAction(right);

        KeyForwardAction zoomIn = new KeyForwardAction(cam, 0.5f);
        zoomIn.setKey("zoomIn");
        addAction(zoomIn);
        KeyBackwardAction zoomOut = new KeyBackwardAction(cam, 0.5f);
        zoomOut.setKey("zoomOut");
        addAction(zoomOut);

//         CameraAction lookUp = new CameraAction(cam, 0.5f) {
//             public void performAction (InputActionEvent evt) {
//                 _incr.fromAxisAngle(_camera.getLeft(),
//                                     -FastMath.PI * evt.getTime() / 2);
//                 _incr.mult(_camera.getLeft(), _camera.getLeft());
//                 _incr.mult(_camera.getDirection(), _camera.getDirection());
//                 _incr.mult(_camera.getUp(), _camera.getUp());
//                 _camera.update();
//             }
//             private Matrix3f _incr = new Matrix3f();
//         };
//         lookUp.setKey("lookUp");
//         addAction(lookUp);

//         CameraAction lookDown = new CameraAction(cam, 0.5f) {
//             public void performAction (InputActionEvent evt) {
//                 _incr.fromAxisAngle(_camera.getLeft(),
//                                     FastMath.PI * evt.getTime() / 2);
//                 _incr.mult(_camera.getLeft(), _camera.getLeft());
//                 _incr.mult(_camera.getDirection(), _camera.getDirection());
//                 _incr.mult(_camera.getUp(), _camera.getUp());
//                 _camera.update();
//             }
//             private Matrix3f _incr = new Matrix3f();
//         };
//         lookDown.setKey("lookDown");
//         addAction(lookDown);

        CameraAction orbitRight = new OrbitAction(cam, -FastMath.PI / 2);
        orbitRight.setKey("turnRight");
        addAction(orbitRight);

        CameraAction orbitLeft = new OrbitAction(cam, FastMath.PI / 2);
        orbitLeft.setKey("turnLeft");
        addAction(orbitLeft);
    }

    protected static Vector3f groundPoint (Camera camera)
    {
        float dist = -1f * _groundNormal.dot(camera.getLocation()) /
            _groundNormal.dot(camera.getDirection());
        return camera.getLocation().add(camera.getDirection().mult(dist));
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

    protected class OrbitAction extends CameraAction
    {
        public OrbitAction (Camera camera, float radPerSec)
        {
            super(camera, 0f);
            _radPerSec = radPerSec;
        }

        public void performAction (InputActionEvent evt)
        {
            // locate the point at which the camera is "pointing" on
            // the ground plane
            Vector3f camloc = _camera.getLocation();
            Vector3f center = groundPoint(_camera);

            // get a vector from the camera's position to the point
            // around which we're going to orbit
            Vector3f direction = camloc.subtract(center);

            // compute the amount we wish to orbit
            float deltaA = _radPerSec * evt.getTime();
            // create a rotation matrix
            _rotm.fromAxisAngle(_groundNormal, deltaA);

            // rotate the center to camera vector and the camera itself
            _rotm.mult(direction, direction);
            _rotm.mult(_camera.getUp(), _camera.getUp());
            _rotm.mult(_camera.getLeft(), _camera.getLeft());
            _rotm.mult(_camera.getDirection(), _camera.getDirection());
            _rotm.mult(_rxdir, _rxdir);
            _rotm.mult(_rydir, _rydir);

            // and move the camera to its new location
            _camera.setLocation(center.add(direction));

            _camera.update();
        }

        protected float _radPerSec;
        protected Matrix3f _rotm = new Matrix3f();
    }

    protected static final Vector3f _xdir = new Vector3f(1, 0, 0);
    protected static final Vector3f _ydir = new Vector3f(0, 1, 0);

    protected static final Vector3f _rxdir = new Vector3f(1, 0, 0);
    protected static final Vector3f _rydir = new Vector3f(0, 1, 0);

    protected static final Vector3f _groundNormal = new Vector3f(0, 0, 1);
}
