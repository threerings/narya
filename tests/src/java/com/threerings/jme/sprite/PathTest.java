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

        // set up the camera
        Vector3f loc = new Vector3f(250, -600, 0);
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
        box2.setLocalTranslation(new Vector3f(560, 0, 0));
        _geom.attachChild(box2);

        Sphere ball = new Sphere("ball", 10, 10, 1);
        Sprite shot = new Sprite();
        shot.attachChild(ball);
        _geom.attachChild(shot);

        Vector3f start = new Vector3f(0, 0, 0);

        // start with the "cannon" pointed along the x axis
        Vector3f vel = new Vector3f(1, 0, 0);
        // rotate it up (around the y axis) the necessary elevation
        Quaternion rot = new Quaternion();
        rot.fromAngleAxis(-FastMath.PI*27.35f/180f, new Vector3f(0, 1, 0));
//         rot.fromAngleAxis(-FastMath.PI*62.65f/180f, new Vector3f(0, 1, 0));
        rot.multLocal(vel);
        // give the cannon a "muzzle velocity"
        vel.multLocal(82f);

        System.out.println("Start: " + start);
        System.out.println("Velocity: " + vel);

        shot.move(new BallisticPath(shot, start, vel, GRAVITY, 7.7f));
//        shot.move(new BallisticPath(shot, start, vel, GRAVITY, 15f));
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
