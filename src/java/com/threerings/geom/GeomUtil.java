//
// $Id: GeomUtil.java,v 1.3 2002/10/29 06:07:22 mdb Exp $

package com.threerings.geom;

import java.awt.Point;
import java.awt.Rectangle;

import com.samskivert.util.StringUtil;

/**
 * General geometry utilites.
 */
public class GeomUtil
{
    /**
     * Computes and returns the dot product of the two vectors.
     *
     * @param v1s the starting point of the first vector.
     * @param v1e the ending point of the first vector.
     * @param v2s the starting point of the second vector.
     * @param v2e the ending point of the second vector.
     */
    public static int dot (Point v1s, Point v1e, Point v2s, Point v2e)
    {
        return ((v1e.x - v1s.x) * (v2e.x - v2s.x) +
                (v1e.y - v1s.y) * (v2e.y - v2s.y));
    }

    /**
     * Computes and returns the dot product of the two vectors.  See
     * {@link #dot(Point,Point,Point,Point)} for an explanation of the
     * arguments
     */
    public static int dot (int v1sx, int v1sy, int v1ex, int v1ey,
                           int v2sx, int v2sy, int v2ex, int v2ey)
    {
        return ((v1ex - v1sx) * (v2ex - v2sx) + (v1ey - v1sy) * (v2ey - v2sy));
    }

    /**
     * Computes and returns the dot product of the two vectors. The
     * vectors are assumed to start with the same coordinate and end with
     * different coordinates.
     *
     * @param vs the starting point of both vectors.
     * @param v1e the ending point of the first vector.
     * @param v2e the ending point of the second vector.
     */
    public static int dot (Point vs, Point v1e, Point v2e)
    {
        return ((v1e.x - vs.x) * (v2e.x - vs.x) +
                (v1e.y - vs.y) * (v2e.y - vs.y));
    }

    /**
     * Computes and returns the dot product of the two vectors.
     * See {@link #dot(Point,Point,Point)} for an explanation of the
     * arguments
     */
    public static int dot (int vsx, int vsy, int v1ex, int v1ey,
                           int v2ex, int v2ey)
    {
        return ((v1ex - vsx) * (v2ex - vsx) + (v1ey - vsy) * (v2ey - vsy));
    }

    /**
     * Computes the point nearest to the specified point <code>p3</code>
     * on the line defined by the two points <code>p1</code> and
     * <code>p2</code>. The computed point is stored into <code>n</code>.
     * <em>Note:</em> <code>p1</code> and <code>p2</code> must not be
     * coincident.
     *
     * @param p1 one point on the line.
     * @param p2 another point on the line (not equal to <code>p1</code>).
     * @param p3 the point to which we wish to be most near.
     * @param n the point on the line defined by <code>p1</code> and
     * <code>p2</code> that is nearest to <code>p</code>.
     *
     * @return the point object supplied via <code>n</code>.
     */
    public static Point nearestToLine (Point p1, Point p2, Point p3, Point n)
    {
        // see http://astronomy.swin.edu.au/~pbourke/geometry/pointline/
        // for a (not very good) explanation of the math
        int Ax = p2.x - p1.x, Ay = p2.y - p1.y;
        float u = (p3.x - p1.x) * Ax + (p3.y - p1.y) * Ay;
        u /= (Ax * Ax + Ay * Ay);
        n.x = p1.x + Math.round(Ax * u);
        n.y = p1.y + Math.round(Ay * u);
        return n;
    }

    /**
     * Returns less than zero if <code>p2</code> is on the left hand side
     * of the line created by <code>p1</code> and <code>theta</code> and
     * greater than zero if it is on the right hand side. In theory, it
     * will return zero if the point is on the line, but due to rounding
     * errors it almost always decides that it's not exactly on the line.
     *
     * @param p1 the point on the line whose side we're checking.
     * @param theta the (logical) angle defining the line.
     * @param p2 the point that lies on one side or the other of the line.
     */
    public static int whichSide (Point p1, double theta, Point p2)
    {
        // obtain the point defining the right hand normal (N)
        theta += Math.PI/2;
        int x = p1.x + (int)Math.round(1000*Math.cos(theta)),
            y = p1.y + (int)Math.round(1000*Math.sin(theta));

        // now dot the vector from p1->p2 with the vector from p1->N, if
        // it's positive, we're on the right hand side, if it's negative
        // we're on the left hand side and if it's zero, we're on the line
        return dot(p1.x, p1.y, p2.x, p2.y, x, y);
    }

    /**
     * Shifts the position of the <code>tainer</code> rectangle to ensure
     * that it contains the <code>tained</code> rectangle. The
     * <code>tainer</code> rectangle must be larger than or equal to the
     * size of the <code>tained</code> rectangle.
     */
    public static void shiftToContain (Rectangle tainer, Rectangle tained)
    {
        if (tainer.width < tained.width || tainer.height < tained.height) {
            throw new IllegalArgumentException(
                tainer + " cannot contain " + tained);
        }
        if (tained.x < tainer.x) {
            tainer.x = tained.x;
        }
        if (tained.y < tainer.y) {
            tainer.y = tained.y;
        }
        if (tained.x + tained.width > tainer.x + tainer.width) {
            tainer.x = tained.x - (tainer.width - tained.width);
        }
        if (tained.y + tained.height > tainer.y + tainer.height) {
            tainer.y = tained.y - (tainer.height - tained.height);
        }
    }
}
