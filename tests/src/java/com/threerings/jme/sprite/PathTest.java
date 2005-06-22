//
// $Id$

package com.threerings.jme.sprite;

import java.util.logging.Level;

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.util.LoggingSystem;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;

import com.threerings.jme.JmeApp;

/**
 * Used for testing paths.
 */
public class PathTest extends JmeApp
{
    protected void initRoot ()
    {
        super.initRoot();

        float range = 56, muzvel = 24;

        // set up the camera
        Vector3f loc = new Vector3f(range/2, -range*2, 0);
        _camera.setLocation(loc);
        Matrix3f rotm = new Matrix3f();
        rotm.fromAngleAxis(-FastMath.PI/2, _camera.getLeft());
        rotm.multLocal(_camera.getDirection());
        rotm.multLocal(_camera.getUp());
        rotm.multLocal(_camera.getLeft());
        _camera.update();

        Box box = new Box("box", new Vector3f(-1, -1, -1),
                          new Vector3f(1, 1, 1));
        _geom.attachChild(box);

        Box box2 = new Box("box2", new Vector3f(-1, -1, -1),
                           new Vector3f(1, 1, 1));
        box2.setLocalTranslation(new Vector3f(range, 0, 0));
        _geom.attachChild(box2);

        Sprite shot = new Sprite();
        shot.attachChild(box);
        _geom.attachChild(shot);

        Vector3f start = new Vector3f(0, 0, 0);

        // start with the "cannon" pointed along the x axis
        Vector3f vel = new Vector3f(1, 0, 0);

        // rotate it up (around the y axis) the necessary elevation
        Quaternion rot = new Quaternion();
        float angle = BallisticPath.computeElevation(range, muzvel, -9.8f);
        rot.fromAngleAxis(angle, new Vector3f(0, 1, 0));
        rot.multLocal(vel);

        // give the cannon its muzzle velocity
        vel.multLocal(muzvel);

        float time = BallisticPath.computeFlightTime(range, muzvel, angle);
        shot.move(new OrientingBallisticPath(
                      shot, new Vector3f(1, 0, 0), start, vel, GRAVITY, time));

        System.out.println("Range: " + range);
        System.out.println("Muzzle velocity: " + muzvel);
        System.out.println("Angle: " + angle + " (" +
                           (angle * 180 / FastMath.PI) + ")");
        System.out.println("Flight time: " + time);
        System.out.println("Velocity: " + vel);
    }

    public static void main (String[] args)
    {
        LoggingSystem.getLogger().setLevel(Level.OFF);
        PathTest test = new PathTest();
        if (!test.init()) {
            System.exit(-1);
        }
        test.run();
    }

    protected static final Vector3f GRAVITY = new Vector3f(0, 0, -9.8f);
}
