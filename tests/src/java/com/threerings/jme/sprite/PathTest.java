//
// $Id$

package com.threerings.jme.sprite;

import java.util.Arrays;
import java.util.logging.Level;

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Disk;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.LightState;
import com.jme.util.LoggingSystem;

import com.threerings.jme.JmeApp;
import com.threerings.jme.sprite.LineSegmentPath;

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

        for (int yy = -1; yy <= 1; yy++) {
            for (int xx = -1; xx <= 1; xx++) {
                if (xx != 0 || yy != 0) {
                    setup(xx * dist, yy * dist);
                }
            }
        }
    }

    protected void setup (float x, float y)
    {
        Box target = new Box("target", new Vector3f(-.1f, -.1f, -.1f),
                             new Vector3f(.1f, .1f, .1f));
        target.setLocalTranslation(new Vector3f(x, y, 0));
        _geom.attachChild(target);

        Box box = new Box("box", new Vector3f(-1, -0.5f, -0.25f),
                          new Vector3f(1, 0.5f, 0.25f));
        Disk disk = new Disk("dot", 10, 10, 0.5f);
        disk.setLightCombineMode(LightState.OFF);
        disk.setLocalTranslation(new Vector3f(0.5f, 0f, 0.3f));
        Sprite shot = new Sprite();
        shot.attachChild(box);
        shot.attachChild(disk);
        _geom.attachChild(shot);

        testBallistic(shot, target);
//         testLineSegment(shot);
    }

    protected void testBallistic (Sprite shot, Box target)
    {
        // start with the "cannon" pointed along the x axis
        Vector3f diff = target.getLocalTranslation().subtract(
            shot.getLocalTranslation());
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

        Vector3f start = new Vector3f(0, 0, 0);
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

    protected void testLineSegment (Sprite shot)
    {
        Vector3f[] points = new Vector3f[] {
            new Vector3f(0, 0, 0), new Vector3f(2, 0, 0), new Vector3f(3, 0, 0),
            new Vector3f(3, 2, 0), new Vector3f(5, 2, 0), new Vector3f(5, -2, 0),
            new Vector3f(-4, -2, 0)
        };
        float[] durations = new float[points.length];
        Arrays.fill(durations, 1f);
        Vector3f up = new Vector3f(0, 0, 1);
        Vector3f orient = new Vector3f(1, 0, 0);
        shot.move(new LineSegmentPath(shot, up, orient, points, durations));
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
