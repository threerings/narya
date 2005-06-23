//
// $Id$

package com.threerings.jme.sprite;

import java.util.logging.Level;

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.LightState;
import com.jme.util.LoggingSystem;

import com.threerings.jme.JmeApp;

/**
 * Used for testing paths.
 */
public class PathTest extends JmeApp
{
    protected void initRoot ()
    {
        super.initRoot();

        float dist = 10;

        // set up the camera
        Vector3f loc = new Vector3f(0, -dist, 10);
        _camera.setLocation(loc);
        Matrix3f rotm = new Matrix3f();
        rotm.fromAngleAxis(-FastMath.PI/4, _camera.getLeft());
        rotm.multLocal(_camera.getDirection());
        rotm.multLocal(_camera.getUp());
        rotm.multLocal(_camera.getLeft());
        _camera.update();

        for (int yy = -1; yy < 1; yy++) {
            for (int xx = -1; xx < 1; xx++) {
                Quad quad = new Quad("ground", 10, 10);
                quad.setLightCombineMode(LightState.OFF);
                int index = (yy+1)*2 + (xx+1);
                quad.setSolidColor(COLORS[index]);
                quad.setLocalTranslation(
                    new Vector3f(xx * 10 + 5, yy * 10 + 5, -1));
                _geom.attachChild(quad);
            }
        }

        Box box = new Box("box", new Vector3f(-1, -0.5f, -0.25f),
                          new Vector3f(1, 0.5f, 0.25f));
        _geom.attachChild(box);

        Box box2 = new Box("box2", new Vector3f(-.1f, -.1f, -.1f),
                           new Vector3f(.1f, .1f, .1f));
        box2.setLocalTranslation(new Vector3f(dist, dist, 0));
        _geom.attachChild(box2);

        Sprite shot = new Sprite();
        shot.attachChild(box);
        _geom.attachChild(shot);

        Vector3f start = new Vector3f(0, 0, 0);

        // start with the "cannon" pointed along the x axis
        Vector3f diff = box2.getLocalTranslation().subtract(
            box.getLocalTranslation());
        Vector3f vel = diff.normalize();
        float range = diff.length(), angle = -FastMath.PI/4;

        Vector3f axis = UP.cross(vel);
        axis.normalizeLocal();

        // rotate it up (around the y axis) the necessary elevation
        Quaternion rot = new Quaternion();
        rot.fromAngleAxis(angle, axis);
        rot.multLocal(vel);

        // give the cannon its muzzle velocity
        float muzvel = FastMath.sqrt(range * BallisticPath.G /
                                     FastMath.sin(2*angle));
        vel.multLocal(muzvel);

        float time = BallisticPath.computeFlightTime(range, muzvel, angle);
        shot.move(new OrientingBallisticPath(
                      shot, new Vector3f(1, 0, 0), start, vel, GRAVITY, time));

        System.out.println("Range: " + range);
        System.out.println("Muzzle velocity: " + muzvel);
        System.out.println("Rotation axis: " + axis);
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
    protected static final Vector3f UP = new Vector3f(0, 0, 1);
    protected static final ColorRGBA[] COLORS = {
        ColorRGBA.red, ColorRGBA.green, ColorRGBA.blue, ColorRGBA.gray };
}
